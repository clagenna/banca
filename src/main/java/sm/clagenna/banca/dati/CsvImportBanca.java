package sm.clagenna.banca.dati;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.concurrent.Task;
import lombok.Getter;
import lombok.Setter;
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

  public static final String EVT_PARSECSV  = "parsecsv";
  public static final String EVT_SIZEDTS   = "sizedts";
  public static final String EVT_FUNCTYPE  = "functype";
  public static final String EVT_DTSROW    = "dtsrow";
  public static final String EVT_ENDDTSROW = "Endrow";
  public static final String EVT_SAVEDB    = "savedb";
  public static final String EVT_SAVEDBROW = "savedbrow";
  public static final String EVT_ENDSAVEDB = "endsavedb";

  public static final String BANCA_BKN301   = "carispcredit";
  public static final String BANCA_BSI      = "bsi";
  public static final String BANCA_CARISP   = "carisp";
  public static final String BANCA_CONTANTI = "contanti";
  public static final String BANCA_PAYPAL   = "paypal";
  public static final String BANCA_SMAC     = "smac";
  public static final String BANCA_WISE     = "wise";

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

  private PropertyChangeSupport     prchsupp;
  private Map<String, List<String>> nomiCols;
  @Getter
  private List<RigaBanca>           righeBanca;
  private DBConn                    dbconn;
  @Getter
  private DataController            cntrl;
  private double                    dblQtaRows;

  public CsvImportBanca() {
    init();
  }

  public CsvImportBanca(Path p_fiCsv) {
    setCsvFile(p_fiCsv);
    init();
  }

  private void init() {
    // i CSV degli export Welly/BSI sono in Locale.US
    skipSaveDB = false;
    prchsupp = new PropertyChangeSupport(this);
    Utils.setLocale(Locale.ITALY);
    nomiCols = new HashMap<>();
    nomiCols.put("dtmov", Arrays.asList(new String[] { "dtmov", "data", "Date", "Data transazione", "Created on", "" }));
    nomiCols.put("dtval", Arrays.asList(new String[] { "dtval", "valuta", "Data contabile", "Finished on" }));
    nomiCols.put("dare", Arrays.asList(new String[] { "dare", "importo", "Amount", "Source amount (after fees)" }));
    nomiCols.put("avere", Arrays.asList(new String[] { "avere", "*no*", "*no*" }));
    nomiCols.put("descr",
        Arrays.asList(new String[] { "descr", "causale", "descrizione", "Target name", "Esercente", "Merchant" }));
    nomiCols.put("caus", Arrays.asList(new String[] { "causabi", "causale abi", "categoria", "ID" }));

    cntrl = DataController.getInst();
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
    firePropertyChange(EVT_PARSECSV, 0.);
    try (Dataset dts = new Dataset()) {
      dts.setIntToDouble(true);
      String szExt = Utils.getFileExtention(csvFile);
      if (sqlTableName.equals(BANCA_WISE) || sqlTableName.equals(BANCA_PAYPAL)) {
        dts.setCsvdelim(",");
        Utils.setLocale(Locale.US);
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
      firePropertyChange(EVT_SIZEDTS, dblQtaRows);
      s_log.debug("Readed {} recs from {}", dtsCsv.size(), getCsvFile().toString());
    } catch (Exception e) {
      s_log.error("Errore read csv, err={}", e.getMessage(), e);
    }
  }

  public List<RigaBanca> analizzaBanca() {
    if (null == dtsCsv || dtsCsv.getQtaCols() == 0)
      throw new UnsupportedOperationException("CSV dataset not opened !");
    righeBanca = new ArrayList<RigaBanca>();
    Locale prevloc = Utils.getLocale();
    firePropertyChange(EVT_FUNCTYPE, 0.);
    int nRow = 0;
    try {
      for (DtsRow row : dtsCsv.getRighe()) {
        firePropertyChange(EVT_DTSROW, (double) nRow++);
        switch (sqlTableName) {
          case BANCA_WISE:
            studiaRigaWise(row);
            break;
          case BANCA_SMAC:
            studiaRigaSmac(row);
            break;
          case BANCA_CONTANTI:
            studiaRigaContanti(row);
            break;
          case BANCA_PAYPAL:
            // i decimali da PayPall hanno le 'virgole'?!?
            Utils.setLocale(Locale.ITALY);
            studiaRigaPayPal(row);
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
      firePropertyChange(EVT_ENDDTSROW, (double) dtsCsv.size());
      updateProgress(nRow, nRow);
      System.out.println("CsvImportBanca.analizzaBanca - " + EVT_ENDDTSROW);
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

    DataController dtc = DataController.getInst();
    String szDbType = dtc.getDBType();
    ISQLGest sqlg = SqlGestFactory.get(szDbType);
    CsvFileContainer contcsv = cntrl.getContCsv();
    ImpFile impf = contcsv.getFromPath(csvFile);
    firePropertyChange(EVT_SAVEDB, dblQtaRows);
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
      sqlg.setTableName(getSqlTableName());
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
      for (RigaBanca ri : getRigheBanca()) {
        ri.setIdfile(impf.getId());
        sqlg.write(ri);
        firePropertyChange(EVT_SAVEDBROW, (double) nRow++);
      }
    } finally {
      dtc.setFiltriQuery(qryFiltrBefore);
      firePropertyChange(EVT_ENDSAVEDB, dblQtaRows * 2.);
      s_log.debug("CsvImportBanca.saveSuDB() - " + EVT_ENDSAVEDB);
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
      s_log.debug("cardIdent:{} for file: {}", cardIdent, p_sz);
    }
  }

  private void discerniSqlTable(String p_sz) {
    sqlTableName = null;
    Pattern pat = Pattern.compile(".*conto_([a-z_]+)[_\\- ][0-9\\-]+.+", Pattern.CASE_INSENSITIVE);
    Matcher mat = pat.matcher(p_sz);
    if ( !mat.find()) {
      pat = Pattern.compile(".*conto_([a-z_]+).+", Pattern.CASE_INSENSITIVE);
      mat = pat.matcher(p_sz);
      if ( !mat.find())
        throw new UnsupportedOperationException("Non trovo il nome Tabella; Il nome file mal formato?");
    }

    String sz = mat.group(1).toLowerCase();
    if (sz.contains(BANCA_BSI))
      sqlTableName = BANCA_BSI;
    if (sz.contains("cari"))
      sqlTableName = BANCA_CARISP;
    if (sz.contains("contant"))
      sqlTableName = BANCA_CONTANTI;
    //    if (sz.contains("cred"))
    //      sqlTableName += "Credit";
    if (sz.contains("tpay") || sz.contains("bkn3"))
      sqlTableName = BANCA_BKN301;
    if (sz.contains(BANCA_PAYPAL))
      sqlTableName = BANCA_PAYPAL;
    if (sz.contains(BANCA_WISE))
      sqlTableName = BANCA_WISE;
    if (sz.contains(BANCA_SMAC))
      sqlTableName = BANCA_SMAC;

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
    final String CAUS_POS = "43";
    final String CAUS_TRANSF = "Z7";
    final String CAUS_CASH = "18";

    Object val = getRowVal("dtmov", row);
    if (null == val) {
      s_log.debug("Scarto riga Wise: {}", row.toString());
      return;
    }
    dtmov = ParseData.parseData(val.toString());

    val = getRowVal("dtval", row);
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

    val = getRowVal("dare", row);
    if (null == val || val.toString().length() == 0)
      dare = 0.;
    else if (val instanceof Double dbl)
      dare = dbl;
    else
      dare = Utils.parseDouble(val.toString());
    caus = CAUS_POS;
    String idTran = (String) row.get("ID");
    if (null == idTran)
      idTran = "*";
    avere = 0.;
    if (source.toLowerCase().contains("wise") || idTran.toLowerCase().startsWith("transf")) {
      // Balance cash back or Transfer
      avere = dare;
      dare = 0.;
      caus = CAUS_TRANSF;
      if (null != idTran && idTran.toLowerCase().contains("cashback")) {
        caus = CAUS_CASH;
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
      val = getRowVal("descr", row);
      if (null == val) {
        s_log.debug("Scarto riga : {}", row.toString());
        return;
      }
      descr = val.toString().replace("\"", "");
    }

    RigaBanca rigb = new RigaBanca(dtmov, dtval, dare, avere, descr, caus, cardid);
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

    Object val = getRowVal("dtmov", row);
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

    RigaBanca rigb = new RigaBanca(dtmov, dtval, dare, avere, descr, caus, cardid);
    if (null != cardIdent)
      rigb.setCardid(cardIdent);
    righeBanca.add(rigb);
  }

  private void studiaRigaContanti(DtsRow row) {
    RigaBanca rb = new RigaBanca();
    Object val = getRowVal("dtmov", row);
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

    val = getRowVal("dare", row);
    double dbl = 0.;
    if (null == val || val.toString().length() == 0)
      dbl = 0.;
    else if (val instanceof Double dou)
      dbl = dou;
    else
      dbl = Utils.parseDouble(val.toString());
    rb.setDare(dbl);

    dbl = 0.;
    val = getRowVal("avere", row);
    if (null == val || val.toString().length() == 0) {
      dbl = 0.;
    } else if (val instanceof Double dou)
      dbl = dou;
    else
      dbl = Utils.parseDouble(val.toString());
    rb.setAvere(dbl);

    val = getRowVal("descr", row);
    if (null == val) {
      s_log.warn("Scarto riga contante: {}", row.toString());
      return;
    }
    rb.setDescr(val.toString());
    rb.setCaus("CO");

    if (null != cardIdent)
      rb.setCardid(cardIdent);
    righeBanca.add(rb);
  }

  private void studiaRigaPayPal(DtsRow row) {
    RigaBanca rb = new RigaBanca();
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
    rb.setCaus("PP");

    if (null != cardIdent)
      rb.setCardid(cardIdent);
    righeBanca.add(rb);
  }

  private void studiaRiga(DtsRow row) {
    LocalDateTime dtmov;
    LocalDateTime dtval;
    Double dare = null;
    Double avere = null;
    String descr;
    String caus = null;
    String cardid = null;
    Object val = getRowVal("dtmov", row);
    if (null == val) {
      s_log.debug("Scarto riga : {}", row.toString());
      return;
    }
    dtmov = ParseData.parseData(val.toString());

    val = getRowVal("dtval", row);
    if (null == val) {
      s_log.debug("Scarto riga : {}", row.toString());
      return;
    }
    dtval = ParseData.parseData(val.toString());

    val = getRowVal("dare", row);
    if (null == val || val.toString().length() == 0)
      dare = 0.;
    else if (val instanceof Double dbl)
      dare = dbl;
    else
      dare = Utils.parseDouble(val.toString());

    val = getRowVal("avere", row);
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

    val = getRowVal("descr", row);
    if (null == val) {
      s_log.debug("Scarto riga : {}", row.toString());
      return;
    }
    descr = val.toString();
    if (cntrl.scartaVoce(descr)) {
      s_log.debug("Scarto voce riga : {}", row.toString());
      return;
    }

    val = getRowVal("caus", row);
    if (null != val)
      caus = val.toString();
    RigaBanca rigb = new RigaBanca(dtmov, dtval, dare, avere, descr, caus, cardid);
    if (null != cardIdent)
      rigb.setCardid(cardIdent);
    righeBanca.add(rigb);
  }

  private Object getRowVal(String p_nome, DtsRow p_row) {
    List<String> colsn = nomiCols.get(p_nome);
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
    updateProgress(dbl, dblQtaRows);
  }

  public void addPropertyChangeListener(PropertyChangeListener loadBancaController) {
    System.out.println("CsvImportBanca.addPropertyChangeListener()");
    prchsupp.addPropertyChangeListener(loadBancaController);
  }

  public void removePropertyChangeListener(PropertyChangeListener loadBancaController) {
    System.out.println("CsvImportBanca.removePropertyChangeListener()");
    prchsupp.removePropertyChangeListener(loadBancaController);
  }

  @Override
  public void close() throws IOException {
    System.out.println("CsvImportBanca.close()");
    if (null != prchsupp) {
      List<PropertyChangeListener> li = Arrays.asList(prchsupp.getPropertyChangeListeners());
      for (PropertyChangeListener el : li)
        prchsupp.removePropertyChangeListener(el);
    }
    prchsupp = null;
  }

}
