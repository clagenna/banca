package sm.clagenna.banca.dati.csv;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import javafx.concurrent.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.ETipoBanca;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.sql.EServerId;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

/**
 * Classe che importa un file CSV ({@link CsvImpFile}) di una banca,
 * eventualmente in un background runnable Task (tramite la {@link #call()} )
 * <ol>
 * <li>legge il file CSV con la chiamata {@link #importCSV(Path)} lo converte in
 * un {@link Dataset}</li>
 * <li>e lo converte in una <b>lista</b> (vedi {@link #righeBanca}) di
 * {@link RigaBanca} con la {@link #analizzaRigheCsvBanca()}</li>
 * <li>poi salvarlo su DB con {@link #saveSuDB}
 * </ol>
 * la sequenza di chiamate puo' essere anche fatta in modo separato, e.g.:
 * <ol>
 * <li>{@link #importCSV(Path)}</li>
 * <li>{@link #analizzaRigheCsvBanca()}</li>
 * <li>{@link #analizzaRighe()};</li>
 * <li>{@link #saveSuDB();}</li>
 * </ol>
 *
 * @author clagenna
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class CsvImportBanca extends Task<String> implements Closeable {

  private CsvImpFile            csvImpFile;
  private double                dblQtaRows;
  private ETipoBanca            tipoBanca;
  private String                cardIdent;
  private String                tipoFile;
  private boolean               skipSaveDB;
  private Dataset               dtsCsv;
  private ConvertCsv2RigaBanca  cnvRb;
  private List<RigaBanca>       righeBanca;
  private DBConn                dbconn;
  private DataModel             model;
  private PropertyChangeSupport prchsupp;

  public void setCsvImpFile(CsvImpFile p_csvImpFile) {
    csvImpFile = p_csvImpFile;
    setTipoBanca(p_csvImpFile.getTipoBanca());
    setCardIdent(p_csvImpFile.getCardHold());
  }

  public CsvImportBanca() {
    // WARNING(?!?): [this-escape] possible 'this' escape before subclass is fully initialized
    init();
  }

  public CsvImportBanca(Path p_fiCsv) {
    // WARNING(?!?): [this-escape] possible 'this' escape before subclass is fully initialized
    Path basedir = model.getLastDir();
    csvImpFile = new CsvImpFile();
    csvImpFile.assignPath(basedir, p_fiCsv);
    cardIdent = csvImpFile.getCardHold();
    init();
  }

  protected void init() {
    // i CSV degli export Welly/BSI sono in Locale.US
    skipSaveDB = false;
    // WARNING(?!?) : [this-escape] previous possible 'this' escape happens here via invocation
    prchsupp = new PropertyChangeSupport(this);
    Utils.setLocale(Locale.ITALY);
    model = DataModel.getInst();
    // Thread.setDefaultUncaughtExceptionHandler(this);
  }

  public abstract Logger getLogger();

  public abstract Dataset importCSV();

  public abstract List<RigaBanca> analizzaRigheCsvBanca();

  @Override
  protected String call() throws Exception {
    getLogger().debug("Start background import of {}", getCsvImpFile().toString());
    try {
      importCSV();
      analizzaRigheCsvBanca();
      analizzaRighe();
      saveSuDB();
    } catch (Exception e) {
      getLogger().error("Errore background Job:{}", e.getMessage(), e);
    }
    // System.out.println("RunTask() ... Sleep!");
    // Thread.sleep(500);
    return "...done!";
  }

  public void importCSV(Path p_fiCsv) {
    Path basedir = model.getLastDir();
    csvImpFile = new CsvImpFile();
    csvImpFile.assignPath(basedir, p_fiCsv);
    importCSV();
  }

  /**
   * Aggiunge una riga di banca alla lista {@link #righeBanca} con i parametri
   * passati
   *
   * @param dtmov
   * @param dtval
   * @param dare
   * @param avere
   * @param descr
   * @param caus
   * @return la riga appena aggiunta
   */
  protected RigaBanca addRigaBanca(LocalDateTime dtmov, LocalDateTime dtval, double dare, double avere, String descr, String caus) {
    RigaBanca rb = new RigaBanca(tipoBanca.getAppellativo(), dtmov, dtval, dare, avere, descr, caus, cardIdent, null);
    if (DataModel.isJunit())
      getLogger().trace("addRigaBanca: {} ", rb.toString());
    righeBanca.add(rb);
    return rb;
  }

  /**
   * Aggiunge una riga di banca alla lista {@link #righeBanca} con i parametri
   * passati
   *
   * @param dtmov
   * @param dtval
   * @param dare
   * @param avere
   * @param descr
   * @param caus
   * @param p_cardIdent
   *          card holder (e.g. "cla","ale","and" ...)
   * @return la riga appena aggiunta
   */
  protected RigaBanca addRigaBanca(LocalDateTime dtmov, LocalDateTime dtval, double dare, double avere, String descr, String caus,
      String p_cardIdent) {
    RigaBanca rb = new RigaBanca(tipoBanca.getAppellativo(), dtmov, dtval, dare, avere, descr, caus, p_cardIdent, null);
    if (DataModel.isJunit())
      System.out.println("addRigaBanca: " + rb.toString());
    righeBanca.add(rb);
    return rb;
  }

  /**
   * Analizza il {@link Dataset} del CSV e lo converte in una lista di
   * {@link RigaBanca} con la chiamata {@link #studiaRiga(DtsRow)} a seconda del
   * tipo di banca (vedi {@link #tipoBanca})
   *
   * @return
   */
  //  public List<RigaBanca> analizzaRigheCsvBanca_OLD() {
  //    if (null == dtsCsv || dtsCsv.getQtaCols() == 0)
  //      throw new UnsupportedOperationException("CSV dataset not opened !");
  //    if (tipoBanca.equals(ETipoBanca.Amazon)) {
  //      cnvRb = new ConvertCsv2RigaBanca(Consts.BANCA_AMAZON);
  //      // leggo la descrizione completa delle colonne del CSV di Amazon
  //      String propCols = String.format(Consts.CSZ_FILE_PROPERTY_COLS, Consts.BANCA_AMAZON);
  //      Path pthCols = Paths.get(propCols);
  //      cnvRb.readConvProperties(pthCols);
  //    }
  //    righeBanca = new ArrayList<RigaBanca>();
  //    Locale prevloc = Utils.getLocale();
  //    // firePropertyChange(Consts.EVT_FUNCTYPE, 0.);
  //    int nRow = 0;
  //    try {
  //      for (DtsRow row : dtsCsv.getRighe()) {
  //        firePropertyChange(Consts.EVT_DTSROW, (double) nRow++);
  //        switch (tipoBanca) {
  //          case Wise:
  //            // studiaRigaWise(row);
  //            break;
  //          case BsiCredit:
  //            studiaRigaBSICredit(row);
  //            break;
  //          case Revolut:
  //            // studiaRigaRevolut(row);
  //            break;
  //          case Smac:
  //            // studiaRigaSmac(row);
  //            break;
  //          case Contanti:
  //            // studiaRigaContanti(row);
  //            break;
  //          case PayPal:
  //            // i decimali da PayPall hanno le 'virgole'?!?
  //            Utils.setLocale(Locale.ITALY);
  //            studiaRigaPayPal(row);
  //            break;
  //          case Amazon:
  //            // studiaRigaAmazon(row);
  //            break;
  //
  //          default:
  //            studiaRiga(row);
  //            break;
  //        }
  //      }
  //    } catch (Exception e) {
  //      getLogger().error("Errore studia riga, err={}", e.getMessage(), e);
  //    } finally {
  //      Utils.setLocale(prevloc);
  //      firePropertyChange(Consts.EVT_ENDDTSROW, (double) dtsCsv.size());
  //      if ( !DataModel.isJunit())
  //        updateProgress(nRow, nRow);
  //      System.out.println("CsvImportBanca.analizzaBanca - " + Consts.EVT_ENDDTSROW);
  //    }
  //    return righeBanca;
  //  }

  /**
   * Routine che verifica che se due righe sono uguali per <code>idSet</code>
   * (<code>dtmov+dare+avere</code>) allora siano almeno differenti nel orario
   * sommando 5sec al <code>dtmov</code>
   */
  protected void analizzaRighe() {
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

    // String szDbType = model.getDBType();
    ISQLGest sqlg = model.getSqlgest();
    EServerId idServer = sqlg.getDbconn().getServerId();
    CsvFileContainer contcsv = model.getContCsv();
    CsvImpFile impf = contcsv.getFromPath(getCsvImpFile().getPathName());
    firePropertyChange(Consts.EVT_SAVEDB, dblQtaRows);
    if (null == impf)
      impf = contcsv.addFile(getCsvImpFile().getPathName());
    impf.completaInfo(getRigheBanca());
    contcsv.saveDb(impf);

    getLogger().info("Scrivo file {} di {} recs su DB({}) over={}", getCsvImpFile().getFileName(), getRigheBanca().size(),
        idServer.name(), model.isOverwrite());
    int qryFiltrBefore = model.getFiltriQuery();
    int qryFiltrNow = qryFiltrBefore;
    int nRow = 0;
    try {
      sqlg.setDbconn(dbconn);
      sqlg.setOverwrite(model.isOverwrite());
      switch (getTipoBanca()) {
        case Wise:
          // per WISE limito il filtro di exist su soli questi campi
          qryFiltrNow = ESqlFiltri.Dtmov.getFlag() //
              | ESqlFiltri.Dare.getFlag() //
              | ESqlFiltri.Avere.getFlag();
          break;
        default:
          break;
      }
      model.setFiltriQuery(qryFiltrNow);
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
      getLogger().error("Error save DB : {}", e.getMessage());
    } finally {
      model.setFiltriQuery(qryFiltrBefore);
      firePropertyChange(Consts.EVT_ENDSAVEDB, dblQtaRows * 2.);
      getLogger().debug("CsvImportBanca.saveSuDB() - " + Consts.EVT_ENDSAVEDB);
    }
  }

  public Path getCsvFile() {
    if (null == csvImpFile)
      return null;
    return csvImpFile.fullPath();
  }

  public void setConnSql(DBConn p_conn) {
    dbconn = p_conn;
  }

  @SuppressWarnings("unused")
  private void studiaRigaPayPal(DtsRow row) {
    RigaBanca rb = new RigaBanca();
    rb.setTiporec(tipoBanca.getAppellativo());
    String sz = null;
    Object dt = row.get("Date");
    Object val = row.get("Time");
    if (null == dt || null == val) {
      getLogger().warn("Scarto riga contante: {}", row.toString());
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
      getLogger().debug("Scarto riga PayPal: {}", row.toString());
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

  @SuppressWarnings("unused")
  private void studiaRigaBSICredit(DtsRow row) {
    RigaBanca rb = new RigaBanca();
    rb.setTiporec(tipoBanca.getAppellativo());
    Object dt = row.get("Data");
    Object val = row.get("Valuta");
    if (null == dt || null == val) {
      getLogger().warn("Scarto riga BSI Credit (no date): {}", row.toString());
      return;
    }
    LocalDateTime dtTrans = ParseData.parseData(dt.toString());
    LocalDateTime dtValuta = ParseData.parseData(val.toString());
    rb.setDtmov(dtTrans);
    rb.setDtval(dtValuta);

    val = row.get("Descrizione");
    if (null == val || val.toString().trim().length() < 2) {
      getLogger().warn("Scarto riga BSI Credit (no descr): {}", row.toString());
      return;
    }
    rb.setDescr(val.toString().trim());

    rb.setAvere(0.);
    rb.setDare(0.);
    val = row.get("Addebiti");
    if (val instanceof Double dbl)
      rb.setDare(Math.abs(dbl));
    else if (null != val && val.toString().length() > 0)
      rb.setDare(Math.abs(Utils.parseDouble(val.toString())));
    val = row.get("Accrediti");
    if (val instanceof Double dbl)
      rb.setAvere(dbl);
    else if (null != val && val.toString().length() > 0)
      rb.setAvere(Math.abs(Utils.parseDouble(val.toString())));
    if (rb.getDare() == 0 && rb.getAvere() == 0) {
      getLogger().warn("Scarto riga BSI Credit (no dare/avere): {}", row.toString());
      return;
    }

    rb.setAbicaus("BSICC");

    if (null != cardIdent)
      rb.setCardid(cardIdent);
    righeBanca.add(rb);
  }

  /**
   * Analizza una riga <b>generica</b> del CSV su un DtsRow e la converte in un
   * oggetto {@link RigaBanca} che viene aggiunto alla lista {@link #righeBanca}
   *
   * @param row
   */
  protected void studiaRiga(DtsRow row) {
    LocalDateTime dtmov;
    LocalDateTime dtval;
    Double dare = null;
    Double avere = null;
    String descr;
    String caus = null;

    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      getLogger().debug("Scarto riga (no dtmov): {}", row.toString());
      return;
    }
    dtmov = ParseData.parseData(val.toString());

    val = getRowVal(EColsTableView.dtval, row);
    if (null == val) {
      getLogger().debug("Scarto riga (no dtVal): {}", row.toString());
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
    //    if ( dare == 0 && avere == 0) {
    //      getLogger().debug("Scarto perche dare == 0 avere == 0, riga : {}", row.toString());
    //      return;
    //    }

    val = getRowVal(EColsTableView.descr, row);
    if (null == val) {
      getLogger().debug("Scarto riga (no descr): {}", row.toString());
      return;
    }
    descr = val.toString();
    if (model.scartaVoce(descr)) {
      getLogger().debug("Scarto voce riga : {}", row.toString());
      return;
    }

    val = getRowVal(EColsTableView.abicaus, row);
    if (null != val)
      caus = val.toString();
    addRigaBanca(dtmov, dtval, dare, avere, descr, caus);
  }

  /**
   * Nelle Consts sono definiti tutte le eventuali nomi di colonne che possono
   * essere presenti nel CSV. Per ogni colonna e' possibile definire piu' nomi
   * alternativi.<br/>
   * es:
   *
   * <pre>
   * dtmov = ["Data", "Date", "Data di inizio", "Data di completamento"]
   * </pre>
   *
   * Per cui tenta di estrapolare da un DtsRow il valore della colonna indicata
   * da uno dei nomi alternativi definiti in {@link Consts#getNomiCols()} per
   * p_nome. Se la colonna non esiste o e' vuota ritorna null.
   *
   * @param p_nome
   *          nome della colonna da estrarre
   * @param p_row
   *          riga del CSV
   * @return valore della colonna o null se non esiste o e' vuota
   */
  protected Object getRowVal(EColsTableView p_nome, DtsRow p_row) {
    List<String> colsn = Consts.getNomiCols().get(p_nome);
    if (null == colsn)
      throw new UnsupportedOperationException("Colname " + p_nome + " not recognized");
    getLogger().trace("Cerco {} con questi {}", p_nome.name(), colsn.toString());
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
        getLogger().error("Non tratto il tipo {} sulla riga {}", szClsNam, p_row.toString());
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

  protected void firePropertyChange(String szEvt, Double dbl) {
    prchsupp.firePropertyChange(szEvt, -1., dbl);
    if ( !DataModel.isJunit())
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
