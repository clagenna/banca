package sm.clagenna.banca.dati;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.concurrent.Task;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class CsvImportBanca extends Task<String> implements Closeable {

  private static final Logger s_log = LogManager.getLogger(CsvImportBanca.class);

  private Path    csvFile;
  @Getter @Setter
  private String  sqlTableName;
  @Getter @Setter
  private String  cardIdent;
  @Getter @Setter
  private String  tipoFile;
  @Getter @Setter
  private boolean skipSaveDB;
  private Dataset dtsCsv;

  private PropertyChangeSupport prchsupp;
  // private Map<EColsTableView, List<String>> nomiCols;
  @Getter
  private List<RigaBanca> righeBanca;
  private DBConn          dbconn;
  @Getter
  private DataModel       cntrl;
  private double          dblQtaRows;

  private ConvertCsv2RigaBanca cnvRb;

  public CsvImportBanca() {
    // WARNING(?!?): [this-escape] possible 'this' escape before subclass is fully initialized
    this.init();
  }

  public CsvImportBanca(Path p_fiCsv) {
    // WARNING(?!?): [this-escape] possible 'this' escape before subclass is fully initialized
    this.setCsvFile(p_fiCsv);
    this.init();
  }

  private void init() {
    // i CSV degli export Welly/BSI sono in Locale.US
    skipSaveDB = false;
    // WARNING(?!?) : [this-escape] previous possible 'this' escape happens here via invocation
    prchsupp = new PropertyChangeSupport(this);
    Utils.setLocale(Locale.ITALY);
    cntrl = DataModel.getInst();
    // Thread.setDefaultUncaughtExceptionHandler(this);
  }

  @Override
  protected String call() throws Exception {
    s_log.debug("Start background import of {}", getCsvFile().toString());
    try {
      importCSV();
      analizzaBanca();
      analizzaRighe();
      saveSuDB();
    } catch (Exception e) {
      s_log.error("Errore background Job:{}", e.getMessage(), e);
    }
    // System.out.println("RunTask() ... Sleep!");
    // Thread.sleep(500);
    return "...done!";
  }

  public void importCSV(Path p_fiCsv) {
    setCsvFile(p_fiCsv);
    importCSV();
  }

  public void importCSV() {
    s_log.debug("Import CSV file {}", getCsvFile().toString());
    setTipoFile("csv");
    firePropertyChange(Consts.EVT_PARSECSV, 0.);
    try (Dataset dts = new Dataset()) {
      dts.setIntToDouble(true);
      String szExt = Utils.getFileExtention(csvFile);
      switch (sqlTableName) {
        case Consts.BANCA_AMAZON:
        case Consts.BANCA_AMAZONL:
        case Consts.BANCA_PAYPAL:
        case Consts.BANCA_REVOLUT:
        case Consts.BANCA_WISE:
          dts.setCsvdelim(",");
          Utils.setLocale(Locale.US);
          break;
      }

      switch (szExt) {
        case ".csv":
          dtsCsv = dts.readcsv(getCsvFile());
          break;
        case ".xls":
        case ".xlsx":
          setTipoFile(szExt.toLowerCase().replace(".", ""));
          dtsCsv = dts.readexcel(csvFile);
      }
      dblQtaRows = dts.size();
      firePropertyChange(Consts.EVT_SIZEDTS, dblQtaRows);
      s_log.debug("Readed {} recs from {}", dtsCsv.size(), getCsvFile().toString());
    } catch (Exception e) {
      s_log.error("Errore read csv, err={}", e.getMessage(), e);
    }
  }

  public List<RigaBanca> analizzaBanca() {
    if (null == dtsCsv || dtsCsv.getQtaCols() == 0)
      throw new UnsupportedOperationException("CSV dataset not opened !");
    if (sqlTableName.equals(Consts.BANCA_AMAZON) || //
        sqlTableName.equals(Consts.BANCA_AMAZONL)) {
      cnvRb = new ConvertCsv2RigaBanca(Consts.BANCA_AMAZON);
      String propCols = String.format(ConvertCsv2RigaBanca.CSZ_FILE_COLS, Consts.BANCA_AMAZON);
      Path pthCols = Paths.get(propCols);
      cnvRb.readConvProperties(pthCols);
    }
    righeBanca = new ArrayList<RigaBanca>();
    Locale prevloc = Utils.getLocale();
    // firePropertyChange(Consts.EVT_FUNCTYPE, 0.);
    int nRow = 0;
    try {
      for (DtsRow row : dtsCsv.getRighe()) {
        firePropertyChange(Consts.EVT_DTSROW, (double) nRow++);
        switch (sqlTableName) {
          case Consts.BANCA_WISE:
            studiaRigaWise(row);
            break;
          case Consts.BANCA_REVOLUT:
            studiaRigaRevolut(row);
            break;
          case Consts.BANCA_SMAC:
            studiaRigaSmac(row);
            break;
          case Consts.BANCA_CONTANTI:
            studiaRigaContanti(row);
            break;
          case Consts.BANCA_PAYPAL:
            // i decimali da PayPall hanno le 'virgole'?!?
            Utils.setLocale(Locale.ITALY);
            studiaRigaPayPal(row);
            break;
          case Consts.BANCA_AMAZON:
          case Consts.BANCA_AMAZONL:
            studiaRigaAmazon(row);
            break;

          default:
            studiaRiga(row);
            break;
        }
      }
    } catch (Exception e) {
      s_log.error("Errore studia riga, err={}", e.getMessage(), e);
    } finally {
      Utils.setLocale(prevloc);
      firePropertyChange(Consts.EVT_ENDDTSROW, (double) dtsCsv.size());
      if ( !DataModel.isJUnit())
        updateProgress(nRow, nRow);
      System.out.println("CsvImportBanca.analizzaBanca - " + Consts.EVT_ENDDTSROW);
    }
    return righeBanca;
  }

  /**
   * Routine che verifica che se due righe sono uguali per <code>idSet</code>
   * (<code>dtmov+dare+avere</code>) allora siano almeno differenti nel orario
   * sommando 5sec <code>dtmov</code>
   */
  private void analizzaRighe() {
    Set<String> myset = new HashSet<String>();
    for (RigaBanca rb : righeBanca) {
      while (myset.contains(rb.getIdSet())) {
        rb.suply(5);
      }
      myset.add(rb.getIdSet());
    }
  }

  private void saveSuDB() {
    if (skipSaveDB)
      return;

    DataModel dtc = DataModel.getInst();
    String szDbType = dtc.getDBType();
    ISQLGest sqlg = SqlGestFactory.get(szDbType);
    CsvFileContainer contcsv = cntrl.getContCsv();
    ImpFile impf = contcsv.getFromPath(csvFile);
    firePropertyChange(Consts.EVT_SAVEDB, dblQtaRows);
    if (null == impf)
      impf = contcsv.addFile(csvFile);
    impf.completaInfo(getRigheBanca());
    contcsv.saveDb(impf);

    s_log.info("Scrivo file {} di {} recs su DB({}) over={}", getCsvFile().toString(), getRigheBanca().size(), szDbType,
        dtc.isOverwrite());
    int qryFiltrBefore = dtc.getFiltriQuery();
    int qryFiltrNow = qryFiltrBefore;
    int nRow = 0;
    try {
      sqlg.setDbconn(dbconn);
      sqlg.setOverwrite(dtc.isOverwrite());
      switch (getSqlTableName()) {
        case "wise":
          // per WISE limito il filtro di exist su soli questi campi
          qryFiltrNow = ESqlFiltri.Dtmov.getFlag() //
              | ESqlFiltri.Dare.getFlag() //
              | ESqlFiltri.Avere.getFlag();
          break;
      }
      dtc.setFiltriQuery(qryFiltrNow);
      int nQtaTran = 0;
      sqlg.beginTrans();
      for (RigaBanca ri : getRigheBanca()) {
        ri.setIdfile(impf.getId());
        sqlg.writeMovimento(ri);
        firePropertyChange(Consts.EVT_SAVEDBROW, (double) nRow++);
        if (nQtaTran++ > 100) {
          sqlg.commitTrans();
          sqlg.beginTrans();
          nQtaTran = 0;
        }
      }
      sqlg.commitTrans();
    } catch (Exception e) {
      s_log.error("Error save DB : {}", e.getMessage());
    } finally {
      dtc.setFiltriQuery(qryFiltrBefore);
      firePropertyChange(Consts.EVT_ENDSAVEDB, dblQtaRows * 2.);
      s_log.debug("CsvImportBanca.saveSuDB() - " + Consts.EVT_ENDSAVEDB);
    }
  }

  /**
   * Estrapola dal nome file CSV <b>l'eventuale</b> proprietario del estratto.
   * Es: nel file<br/>
   * <code>estrattoconto_BSI_Credit 2024-11_cla.csv</code> <br/>
   * il card Ident e' <b>&quot;cla&quot;</b>
   *
   * @param p_sz
   */
  private void discerniCardIdent(String p_sz) {
    cardIdent = null;
    int n = p_sz.lastIndexOf("_");
    if (n < 0)
      return;
    String sz2 = p_sz.substring(n - 2);
    Pattern pat = Pattern.compile(".*_([a-z]+)\\.[a-z]+", Pattern.CASE_INSENSITIVE);
    Matcher mat = pat.matcher(sz2);
    if (mat.find()) {
      cardIdent = mat.group(1);
      s_log.debug("cardIdent: \"{}\" for file: {}", cardIdent, p_sz);
    }
  }

  private void discerniSqlTable(String p_sz) {
    sqlTableName = null;
    String szFil = p_sz.toLowerCase().replace("conto_", "_");
    Pattern pat = Pattern.compile(".*_([a-z]+)[_\\- ][0-9\\-]+.+", Pattern.CASE_INSENSITIVE);
    Matcher mat = pat.matcher(szFil);
    if ( !mat.find()) {
      pat = Pattern.compile(".*_([a-z]+)_.+", Pattern.CASE_INSENSITIVE);
      mat = pat.matcher(szFil);
      if ( !mat.find())
        throw new UnsupportedOperationException("Non trovo il nome Tabella; Il nome file mal formato?");
    }

    String sz = mat.group(1).toLowerCase();
    if (sz.contains(Consts.BANCA_BSICREDIT) || //
        sz.contains(Consts.BANCA_BSICREDIT_UND))
      sqlTableName = Consts.BANCA_BSICREDIT;
    else if (sz.contains(Consts.BANCA_REVOLUT))
      sqlTableName = Consts.BANCA_REVOLUT;
    else if (sz.contains(Consts.BANCA_BSI))
      sqlTableName = Consts.BANCA_BSI;
    else if (sz.contains(Consts.BANCA_CARCREDIT) || sz.contains(Consts.BANCA_CARCREDIT_UND))
      sqlTableName = Consts.BANCA_CARCREDIT;
    else if (sz.contains("tpay") || sz.contains("bkn3"))
      sqlTableName = Consts.BANCA_CARCREDIT;
    else if (sz.contains("cari"))
      sqlTableName = Consts.BANCA_CARISP;
    else if (sz.contains("contant"))
      sqlTableName = Consts.BANCA_CONTANTI;
    else if (sz.contains(Consts.BANCA_PAYPAL))
      sqlTableName = Consts.BANCA_PAYPAL;
    else if (sz.contains(Consts.BANCA_WISE))
      sqlTableName = Consts.BANCA_WISE;
    else if (sz.contains(Consts.BANCA_SMAC))
      sqlTableName = Consts.BANCA_SMAC;
    else if (sz.contains(Consts.BANCA_AMAZON))
      sqlTableName = Consts.BANCA_AMAZON;
    else if (sz.contains(Consts.BANCA_AMAZONL))
      sqlTableName = Consts.BANCA_AMAZON;

    if (null == sqlTableName)
      throw new UnsupportedOperationException("Non trovo il nome Banca; Il nome file mal formato?");
  }

  public Path getCsvFile() {
    return csvFile;
  }

  public void setCsvFile(Path p_csvFile) {
    csvFile = p_csvFile;
    if ( !Files.exists(p_csvFile, LinkOption.NOFOLLOW_LINKS))
      throw new UnsupportedOperationException("File non trovato" + p_csvFile.toString());
    discerniCardIdent(csvFile.toString());
    discerniSqlTable(csvFile.toString());
  }

  public void setConnSql(DBConn p_conn) {
    dbconn = p_conn;
  }

  private void studiaRigaWise(DtsRow row) {
    LocalDateTime dtmov;
    LocalDateTime dtval;
    Double dare = null;
    Double avere = null;
    String descr = null;
    String caus = null;
    String cardid = null;

    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      s_log.debug("Scarto riga Wise: {}", row.toString());
      return;
    }
    dtmov = ParseData.parseData(val.toString());

    val = getRowVal(EColsTableView.dtval, row);
    if (null == val) {
      dtval = dtmov;
    } else
      dtval = ParseData.parseData(val.toString());
    /* source = Claudio Gennari/ TransferWise / "" */
    String source = (String) row.get("Source name");
    if (null == source)
      source = "";
    else
      source = source.toLowerCase().replace("\"", "");

    val = getRowVal(EColsTableView.dare, row);
    if (null == val || val.toString().length() == 0)
      dare = 0.;
    else if (val instanceof Double dbl)
      dare = dbl;
    else
      dare = Utils.parseDouble(val.toString());
    caus = Consts.ABICAUS_POS;
    String idTran = (String) row.get("ID");
    if (null == idTran)
      idTran = "*";
    avere = 0.;
    if (source.toLowerCase().contains("wise") || idTran.toLowerCase().startsWith("transf")) {
      // Balance cash back or Transfer
      avere = dare;
      dare = 0.;
      caus = Consts.ABICAUS_TRANSF;
      if (null != idTran && idTran.toLowerCase().contains("cashback")) {
        caus = Consts.ABICAUS_CASH;
        if (null == descr || descr.trim().length() == 0)
          descr = "cash back";
      }
    } else
      cardid = source.length() > 3 ? source.substring(0, 3).toLowerCase() : null;
    if (dare < 0) {
      dare = -dare;
    } else if (source.length() == 0) {
      // solo se source è valorizzato posso invertire
      avere = dare;
      dare = 0.;
    }

    if (null == descr) {
      val = getRowVal(EColsTableView.descr, row);
      if (null == val) {
        s_log.debug("Scarto riga : {}", row.toString());
        return;
      }
      descr = val.toString().replace("\"", "");
    }

    RigaBanca rigb = new RigaBanca(sqlTableName, dtmov, dtval, dare, avere, descr, caus, cardid, null);
    if (null != cardIdent)
      rigb.setCardid(cardIdent);
    righeBanca.add(rigb);
  }

  /**
   * Analizza il CSV della Revolut
   * <pre>
   * Tipo,Prodotto,Data di inizio,Data di completamento,Descrizione,Importo,Costo,Valuta,State,Saldo
   * Pagamento,Attuale,2026-02-09 18:42:51,2026-02-09 18:42:51,Balance migration to another region or legal entity,-1.30,0.00,EUR,COMPLETATO,0.00
   * Ricarica,Attuale,2026-02-26 19:52:01,2026-02-26 19:52:01,Pagamento da GENNARI ALESSANDRO,300.00,0.00,EUR,COMPLETATO,306.30
   * Pagamento con carta,Attuale,2026-02-28 20:35:53,2026-03-01 11:30:38,Necessaire,-8.00,0.00,EUR,COMPLETATO,298.30
   * Pagamento con carta,Attuale,2026-04-01 14:58:37,2026-04-02 17:08:32,Starbucks,-3.20,0.00,EUR,COMPLETATO,295.10
   * </pre>
   * @param row
   */
  private void studiaRigaRevolut(DtsRow row) {
    LocalDateTime dtmov; // (1)
    LocalDateTime dtval; // (2)
    Double dare = null; // (3)
    Double avere = null; // (4)
    String descr = null; // (5)
    String caus = null; // (6)
    String cardid = null; // (7)

    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      s_log.debug("Scarto riga Revolut: {}", row.toString());
      return;
    }
    // --> (1)
    dtmov = ParseData.parseData(val.toString());
    // --> (2)
    val = getRowVal(EColsTableView.dtval, row);
    if (null == val) {
      dtval = dtmov;
    } else
      dtval = ParseData.parseData(val.toString());

    // --> (3)
    val = getRowVal(EColsTableView.dare, row);
    if (null == val || val.toString().length() == 0)
      dare = 0.;
    else if (val instanceof Double dbl)
      dare = dbl;
    else
      dare = Utils.parseDouble(val.toString());
    // -> (4)
    if (dare >= 0) {
      avere = dare;
      dare = 0.;
    } else {
      dare = -dare;
      avere = 0.;
    }

    // -> (5)
    val = getRowVal(EColsTableView.descr, row);
    if (null == val) {
      s_log.debug("Scarto riga : {}", row.toString());
      return;
    }
    descr = val.toString().replace("\"", "");

    // --> (6)
    /* causale = Pagamento/Ricarica/Pagamento con Carta, etc... */
    caus = Consts.ABICAUS_PAGARE;
    String source = (String) row.get("Tipo");
    if (null != source) {
      source = source.toLowerCase().replace("\"", "");
      if (source.toLowerCase().contains("pagamento con carta"))
        caus = Consts.ABICAUS_POS;
      else if (source.toLowerCase().contains("ricarica"))
        caus = Consts.ABICAUS_TRANSF;
      else if (source.toLowerCase().contains("rimborso su carta"))
        caus = Consts.ABICAUS_STORNO;
    }
    RigaBanca rigb = new RigaBanca(sqlTableName, dtmov, dtval, dare, avere, descr, caus, cardid, null);
    if (null != cardIdent)
      rigb.setCardid(cardIdent);
    righeBanca.add(rigb);
  }

  private void studiaRigaSmac(DtsRow row) {
    LocalDateTime dtmov;
    LocalDateTime dtval;
    Double dare = null;
    Double avere = null;
    String descr = null;
    String caus = null;
    String cardid = null;

    final String OPER_Borsellino = "borsellino"; // S5
    final String OPER_Fiscale = "fiscale"; // S3
    final String OPER_Ricarica = "ricarica"; // S4
    final String OPER_Spesa = "spesa"; // s2
    final String OPER_Spesa_fiscale = "spesa fiscale"; // S1

    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      s_log.debug("Scarto riga SMAC: {}", row.toString());
      return;
    }
    dtmov = ParseData.parseData(val.toString());
    dtval = dtmov;

    val = row.get("Importo");
    dare = 0.;
    if (val instanceof Double dbl)
      dare = dbl;

    val = row.get("Sconto");
    if (null == val || val.toString().length() == 0)
      avere = 0.;
    else if (val instanceof Double dbl)
      avere = dbl;
    else
      avere = Utils.parseDouble(val.toString());

    val = row.get("Operazione");
    String op = "*";
    if (null != val)
      op = val.toString();
    switch (op.toLowerCase()) {
      case OPER_Borsellino:
        descr = "Ricarica";
        caus = "S5"; // !
        dare = 0.;
        break;
      case OPER_Fiscale:
        caus = "S3";
        break;
      case OPER_Ricarica:
        caus = "S4";
        break;
      case OPER_Spesa:
        caus = "S2";
        break;
      case OPER_Spesa_fiscale:
        caus = "S1";
        break;
      default:
        caus = "S6";
        break;
    }
    cardid = this.cardIdent;
    val = row.get("Esercente");
    if (null != val)
      descr = val.toString().replace("\"", "");
    if (null == descr) {
      s_log.debug("Scarto riga SMAC: {}", row.toString());
      return;
    }
    // 25/03/2026 Aggiungo indirizzo nella descrizione (ovviare omonimi)
    val = row.get("Indirizzo");
    if (null != val)
      descr += ", " + val.toString().trim();

    RigaBanca rigb = new RigaBanca(sqlTableName, dtmov, dtval, dare, avere, descr, caus, cardid, null);
    if (null != cardIdent)
      rigb.setCardid(cardIdent);
    righeBanca.add(rigb);
  }

  private void studiaRigaContanti(DtsRow row) {
    RigaBanca rb = new RigaBanca();
    rb.setTiporec(Consts.BANCA_CONTANTI);
    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      s_log.warn("Scarto riga contante: {}", row.toString());
      return;
    }
    if (val instanceof Double dbl) {
      Date dt = new Date();
      dt.setTime(dbl.longValue());
      System.out.println("CsvImportBanca.studiaRigaContanti():" + dt.toString());
    }
    rb.setDtmov(ParseData.parseData(val.toString()));
    rb.setDtval(rb.getDtmov());

    val = getRowVal(EColsTableView.dare, row);
    double dbl = 0.;
    if (null == val || val.toString().length() == 0)
      dbl = 0.;
    else if (val instanceof Double dou)
      dbl = dou;
    else
      dbl = Utils.parseDouble(val.toString());
    rb.setDare(dbl);

    dbl = 0.;
    val = getRowVal(EColsTableView.avere, row);
    if (null == val || val.toString().length() == 0) {
      dbl = 0.;
    } else if (val instanceof Double dou)
      dbl = dou;
    else
      dbl = Utils.parseDouble(val.toString());
    rb.setAvere(dbl);

    val = getRowVal(EColsTableView.descr, row);
    if (null == val) {
      s_log.warn("Scarto riga contante: {}", row.toString());
      return;
    }
    rb.setDescr(val.toString());
    rb.setAbicaus("CO");

    if (null != cardIdent)
      rb.setCardid(cardIdent);
    righeBanca.add(rb);
  }

  private void studiaRigaPayPal(DtsRow row) {
    RigaBanca rb = new RigaBanca();
    rb.setTiporec(sqlTableName);
    String sz = null;
    Object dt = row.get("Date");
    Object val = row.get("Time");
    if (null == dt || null == val) {
      s_log.warn("Scarto riga contante: {}", row.toString());
      return;
    }
    LocalDateTime locd = null;
    if (dt instanceof LocalDateTime ldt) {
      sz = String.format("%s %s", ParseData.s_fmtY4MD.format(ldt), val.toString());
      locd = ParseData.guessData(sz);
    } else if (dt instanceof String lsz) {
      sz = String.format("%s %s", lsz.substring(0, 10), val.toString());
      locd = ParseData.parseData(sz);
    }
    rb.setDtmov(locd);
    rb.setDtval(locd);

    val = row.get("name");
    if (null == val || val.toString().trim().length() < 2) {
      s_log.debug("Scarto riga PayPal: {}", row.toString());
      return;
    }
    rb.setDescr(val.toString().trim());

    val = row.get("Amount");
    rb.setAvere(0.);
    rb.setDare(0.);
    double dbl = 0.;
    if (null == val || val.toString().length() == 0)
      dbl = 0.;
    else if (val instanceof Double dou)
      dbl = dou;
    else
      dbl = Utils.parseDouble(val.toString());
    if (dbl > 0)
      rb.setAvere(dbl);
    else
      rb.setDare( -dbl);
    rb.setAbicaus("PP");

    if (null != cardIdent)
      rb.setCardid(cardIdent);
    righeBanca.add(rb);
  }

  private void studiaRigaAmazon(DtsRow row) {
    RigaBanca rb = new RigaBanca();
    rb.setRigaid(1);
    rb.setCardid(cardIdent);
    cnvRb.assign(rb, row);
    if (rb.isValido())
      righeBanca.add(rb);
    else
      s_log.warn("Scarto riga: {}", rb.toString());
  }

  private void studiaRiga(DtsRow row) {
    LocalDateTime dtmov;
    LocalDateTime dtval;
    Double dare = null;
    Double avere = null;
    String descr;
    String caus = null;
    String cardid = null;
    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      s_log.debug("Scarto riga : {}", row.toString());
      return;
    }
    dtmov = ParseData.parseData(val.toString());

    val = getRowVal(EColsTableView.dtval, row);
    if (null == val) {
      s_log.debug("Scarto riga : {}", row.toString());
      return;
    }
    dtval = ParseData.parseData(val.toString());

    val = getRowVal(EColsTableView.dare, row);
    if (null == val || val.toString().length() == 0)
      dare = 0.;
    else if (val instanceof Double dbl)
      dare = dbl;
    else
      dare = Utils.parseDouble(val.toString());

    val = getRowVal(EColsTableView.avere, row);
    if (null == val || val.toString().length() == 0) {
      if (dare < 0) {
        avere = -dare;
        dare = 0.;
      } else
        avere = 0.;
    } else if (val instanceof Double dbl)
      avere = dbl;
    else
      avere = Utils.parseDouble(val.toString());

    val = getRowVal(EColsTableView.descr, row);
    if (null == val) {
      s_log.debug("Scarto riga : {}", row.toString());
      return;
    }
    descr = val.toString();
    if (cntrl.scartaVoce(descr)) {
      s_log.debug("Scarto voce riga : {}", row.toString());
      return;
    }

    val = getRowVal(EColsTableView.abicaus, row);
    if (null != val)
      caus = val.toString();
    RigaBanca rigb = new RigaBanca(sqlTableName, dtmov, dtval, dare, avere, descr, caus, cardid, null);
    if (null != cardIdent)
      rigb.setCardid(cardIdent);
    righeBanca.add(rigb);
  }

  private Object getRowVal(EColsTableView p_nome, DtsRow p_row) {
    List<String> colsn = Consts.getNomiCols().get(p_nome);
    if (null == colsn)
      throw new UnsupportedOperationException("Colname " + p_nome + " not recognized");
    Object val = null;
    for (String cn : colsn) {
      val = p_row.get(cn);
      if (null != val)
        break;
    }
    if (null == val)
      return val;
    String szClsNam = val.getClass().getSimpleName();
    switch (szClsNam) {
      case "String":
        val = val.toString().trim().replaceAll(" +", " ");
        break;
      case "Double":
        break;
      case "Integer":
        break;
      case "BigDecimal":
        break;
      case "Object":
        break;
      case "Date":
        break;
      case "LocalDate":
        break;
      case "LocalDateTime":
        break;
      default:
        s_log.error("Non tratto il tipo {} sulla riga {}", szClsNam, p_row.toString());
        break;
    }
    return val;
  }

  @Override
  public String toString() {
    if (null == dtsCsv)
      return "*null*";
    return dtsCsv.toString();
  }

  private void firePropertyChange(String szEvt, Double dbl) {
    prchsupp.firePropertyChange(szEvt, -1., dbl);
    if ( !DataModel.isJUnit())
      updateProgress(dbl, dblQtaRows);
  }

  public void addPropertyChangeListener(PropertyChangeListener loadBancaController) {
    // System.out.println("CsvImportBanca.addPropertyChangeListener()");
    prchsupp.addPropertyChangeListener(loadBancaController);
  }

  public void removePropertyChangeListener(PropertyChangeListener loadBancaController) {
    // System.out.println("CsvImportBanca.removePropertyChangeListener()");
    prchsupp.removePropertyChangeListener(loadBancaController);
  }

  @Override
  public void close() throws IOException {
    // System.out.println("CsvImportBanca.close()");
    if (null != prchsupp) {
      List<PropertyChangeListener> li = Arrays.asList(prchsupp.getPropertyChangeListeners());
      for (PropertyChangeListener el : li)
        prchsupp.removePropertyChangeListener(el);
    }
    prchsupp = null;
  }

}
