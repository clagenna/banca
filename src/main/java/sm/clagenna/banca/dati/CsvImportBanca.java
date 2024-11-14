package sm.clagenna.banca.dati;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.concurrent.Task;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class CsvImportBanca
    extends Task<String> /* implements Thread.UncaughtExceptionHandler */ {
  private static final Logger s_log = LogManager.getLogger(CsvImportBanca.class);

  private Path    csvFile;
  @Getter @Setter
  private String  sqlTableName;
  @Getter @Setter
  private String  cardIdent;
  @Getter @Setter
  private boolean bancaWise;
  private Dataset dtsCsv;

  private Map<String, List<String>> nomiCols;
  @Getter
  private List<RigaBanca>           righeBanca;
  private DBConn                    dbconn;

  public CsvImportBanca() {
    init();
  }

  public CsvImportBanca(Path p_fiCsv) {
    setCsvFile(p_fiCsv);
    init();
  }

  private void init() {
    // i CSV degli export Welly/BSI sono in Locale.US
    Utils.setLocale(Locale.ITALY);
    nomiCols = new HashMap<>();
    nomiCols.put("dtmov", Arrays.asList(new String[] { "data", "Data transazione", "Created on" }));
    nomiCols.put("dtval", Arrays.asList(new String[] { "valuta", "Data contabile", "Finished on" }));
    nomiCols.put("dare", Arrays.asList(new String[] { "dare", "importo", "Source amount (after fees)" }));
    nomiCols.put("avere", Arrays.asList(new String[] { "avere", "*no*", "*no*" }));
    nomiCols.put("descr", Arrays.asList(new String[] { "causale", "descrizione", "Target name" }));
    nomiCols.put("caus", Arrays.asList(new String[] { "causale abi", "categoria", "ID" }));

    // Thread.setDefaultUncaughtExceptionHandler(this);
  }

  @Override
  protected String call() throws Exception {
    // System.out.println("GestPDFFatt Runner .call()");
    s_log.debug("Start background import of {}", getCsvFile().toString());
    try {
      importCSV();
      analizzaBanca();
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
    try (Dataset dts = new Dataset()) {
      dts.setIntToDouble(true);

      if (sqlTableName.equals("wise")) {
        dts.setCsvdelim(",");
        Utils.setLocale(Locale.US);
        setBancaWise(true);
      }
      dtsCsv = dts.readcsv(getCsvFile());
      s_log.debug("Readed {} recs from {}", dtsCsv.size(), getCsvFile().toString());
    } catch (Exception e) {
      s_log.error("Errore read csv, err={}", e.getMessage(), e);
    }
  }

  private void saveSuDB() {
    DataController dtc = DataController.getInst();
    String szDbType = dtc.getDBType();
    ISQLGest sqlg = SqlGestFactory.get(szDbType);
    s_log.debug("Save CSV file {} su DB({}) over={}", getCsvFile().toString(), szDbType, dtc.isOverwrite());
    sqlg.setDbconn(dbconn);
    sqlg.setTableName(getSqlTableName());
    sqlg.setDbconn(dbconn);
    sqlg.setOverwrite(dtc.isOverwrite());
    for (RigaBanca ri : getRigheBanca())
      sqlg.write(ri);
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
    if (mat.find())
      cardIdent = mat.group(1);
    s_log.debug("cardIdent:{} for file: {}", cardIdent, p_sz);
  }

  private void discerniSqlTable(String p_sz) {
    sqlTableName = null;
    Pattern pat = Pattern.compile(".*conto_([a-z_]+)[_\\- ][0-9\\-]+.+", Pattern.CASE_INSENSITIVE);
    Matcher mat = pat.matcher(p_sz);
    if (mat.find()) {
      String sz = mat.group(1).toLowerCase();
      if (sz.contains("bsi"))
        sqlTableName = "bsi";
      if (sz.contains("cari"))
        sqlTableName = "carisp";
      if (sz.contains("cred"))
        sqlTableName += "Credit";
      if (sz.contains("tpay"))
        sqlTableName = "carispCredit";
      if (sz.contains("wise"))
        sqlTableName = "wise";
    }
  }

  public Path getCsvFile() {
    return csvFile;
  }

  public void setCsvFile(Path p_csvFile) {
    csvFile = p_csvFile;
    discerniCardIdent(csvFile.toString());
    discerniSqlTable(csvFile.toString());
  }

  public void setConnSql(DBConn p_conn) {
    dbconn = p_conn;
  }

  public List<RigaBanca> analizzaBanca() {
    if (null == dtsCsv || dtsCsv.getQtaCols() == 0)
      throw new UnsupportedOperationException("CSV dataset not opened !");
    righeBanca = new ArrayList<RigaBanca>();
    for (DtsRow row : dtsCsv.getRighe()) {
      if (isBancaWise())
        studiaRigaWise(row);
      else
        studiaRiga(row);
    }
    return righeBanca;
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
      s_log.debug("Scarto riga Wise: {}", row.toString());
      return;
    }
    dtval = ParseData.parseData(val.toString());

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
      cardid = source.substring(0, 3).toLowerCase();
    if (null == descr) {
      val = getRowVal("descr", row);
      if (null == val) {
        s_log.debug("Scarto riga : {}", row.toString());
        return;
      }
      descr = val.toString().replaceAll("\"", "");
    }

    RigaBanca rigb = new RigaBanca(dtmov, dtval, dare, avere, descr, caus, cardid);
    if (null != cardIdent)
      rigb.setCardid(cardIdent);
    righeBanca.add(rigb);

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

  //  @Override
  //  public void uncaughtException(Thread t, Throwable e) {
  //    s_log.error("Exception on thread, {}", e.getMessage(), e);
  //  }

}
