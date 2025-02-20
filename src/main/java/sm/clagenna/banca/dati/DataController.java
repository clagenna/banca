package sm.clagenna.banca.dati;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.javafx.LoadBancaMainApp;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class DataController implements IStartApp, PropertyChangeListener {
  private static final Logger s_log                 = LogManager.getLogger(DataController.class);
  private static final String CSZ_PROP_SCARTA       = "voci.scarta";
  private static final String CSZ_PROP_EXCLUDEDCOLS = "excludedcols";
  public static final String  CSZ_PROP_FILECODSTATS = "filecodstats.path";
  public static final String  CSZ_PROP_DATAFILECDS  = "filecodstats.date";
  private static final String CSZ_FLAG_FILTRI       = "FLAG_FILTRI";
  private static final String CSZ_QTA_THREADS       = "QTA_THREADS";
  private static final String CSZ_PERC_INDOV        = "PERC_INDOV";
  public static final String  CSZ_FILTER_FILES      = "filter_files";

  public static final String EVT_DBCHANGE            = "dbchange";
  public static final String EVT_CODSTAT             = "codstat";
  public static final String EVT_SELCODSTAT          = "selcodstat";
  public static final String EVT_NEW_QUERY_RESULT    = "dtsresult";
  public static final String EVT_FILECODSTATS        = "filecodstats";
  public static final String EVT_TOTCODSTAT          = "totcodstats";
  public static final String EVT_TREECODSTAT_CHANGED = "treeCodstat";
  public static final String EVT_FILTER_CODSTAT      = "filterCodstat";
  public static final String EVT_DATASET_CREATED     = "datasetCreated";
  public static final String EVT_GUESSDATA_CREATED   = "guessdataCreated";

  //  public static final String  FILE_CODSTAT    = "CodStat.properties";
  private static final String QRY_TOT_CODSTAT = //
      "SELECT coalesce(Codstat, '99') as codstat, SUM(dare) as totDare, SUM(avere) as totAvere" + //
          "  FROM listaMovimenti" + //
          "  %s" + //
          "  GROUP BY codstat" + //
          "  ORDER BY codstat";

  private static DataController s_inst;

  private Path                  lastDir;
  @Getter @Setter
  private ObservableList<Path>  selPaths;
  @Getter @Setter
  private int                   filtriQuery;
  @Getter @Setter
  private int                   qtaThreads;
  @Getter @Setter
  private int                   percIndov;
  @Getter @Setter
  private CardidAssoc           associd;
  @Getter @Setter
  private List<IRigaBanca>      excludeCols;
  @Getter
  private boolean               overwrite;
  @Getter
  private CsvFileContainer      contCsv;
  private AppProperties         props;
  private List<String>          scartaVoci;
  @Getter @Setter
  private TreeitemCodStat2      codStatData;
  @Getter @Setter
  private String                qryResulView;
  @SuppressWarnings("unused")
  private String                qryResulViewOld;
  @Getter @Setter
  private PropertyChangeSupport propsChange;

  public DataController() {
    if (null != s_inst) {
      s_log.error("New instance of Singleton DataController");
      throw new UnsupportedOperationException("DataController is Singleton!");
    }
    s_inst = this;
    propsChange = new PropertyChangeSupport(this);
    filtriQuery = ESqlFiltri.AllSets.getFlag();
    codStatData = new TreeitemCodStat2();
    codStatData.readTreeCodStats();
    addPropertyChangeListener(this);
  }

  public Path assegnaLastDir(Path p_ld, boolean bForce) {
    if (p_ld == null)
      return p_ld;
    if ( !bForce)
      if (lastDir != null && lastDir.compareTo(p_ld) == 0)
        return lastDir;
    if ( !Files.exists(p_ld, LinkOption.NOFOLLOW_LINKS)) {
      s_log.error("Il path \"{}\" non esiste !", lastDir.toString());
      return p_ld;
    }
    lastDir = p_ld;
    String szFiin = lastDir.toString();
    props.setLastDir(szFiin);
    return lastDir;
  }

  public static DataController getInst() {
    return s_inst;
  }

  public Path getLastDir() {
    return lastDir;
  }

  public void mettiFiltro(ESqlFiltri pf, boolean bset) {
    if (bset)
      filtriQuery |= pf.getFlag();
    else
      filtriQuery &= ESqlFiltri.AllSets.getFlag() ^ pf.getFlag();
    System.out.printf("DataController.mettiFiltro(%06X)\n", filtriQuery);
  }

  public String getCampiFiltro() {
    StringBuilder szRet = new StringBuilder();
    for (ESqlFiltri fl : ESqlFiltri.values()) {
      if (fl.isSet(filtriQuery) && fl.getFlag() < ESqlFiltri.AllSets.getFlag())
        szRet.append(String.format(" AND %s = ?", fl.name().toLowerCase()));
    }
    return szRet.toString();
  }

  public PreparedStatement applicaFiltri(PreparedStatement p_stmt, int k, DBConn dbconn, RigaBanca p_rig) throws SQLException {
    for (ESqlFiltri fl : ESqlFiltri.values()) {
      if ( !fl.isSet(filtriQuery))
        continue;
      switch (fl) {
        case Id:
          dbconn.setStmtInt(p_stmt, k++, p_rig.getRigaid());
          break;
        case tipo:
          dbconn.setStmtString(p_stmt, k++, p_rig.getTiporec());
          break;
        case Dtmov:
          dbconn.setStmtDate(p_stmt, k++, p_rig.getDtmov());
          break;
        case Dtval:
          dbconn.setStmtDate(p_stmt, k++, p_rig.getDtval());
          break;
        case Dare:
          dbconn.setStmtImporto(p_stmt, k++, p_rig.getDare());
          break;
        case Avere:
          dbconn.setStmtImporto(p_stmt, k++, p_rig.getAvere());
          break;
        case Descr:
          dbconn.setStmtString(p_stmt, k++, p_rig.getDescr());
          break;
        case ABICaus:
          dbconn.setStmtString(p_stmt, k++, p_rig.getAbicaus());
          break;
        case Cardid:
          dbconn.setStmtString(p_stmt, k++, p_rig.getCardid());
          break;
        default:
          break;
      }
    }
    return p_stmt;
  }

  @Override
  public void initApp(AppProperties p_props) {
    props = p_props;
    contCsv = new CsvFileContainer();
    filtriQuery = props.getIntProperty(CSZ_FLAG_FILTRI, ESqlFiltri.AllSets.getFlag());
    qtaThreads = props.getIntProperty(CSZ_QTA_THREADS, 1);
    percIndov = props.getIntProperty(CSZ_PERC_INDOV, 40);
    scartaVoci = new ArrayList<String>();
    String sz = props.getLastDir();
    if (Utils.isValue(sz))
      lastDir = Paths.get(sz);
    sz = props.getProperty(CSZ_PROP_SCARTA);
    if (Utils.isValue(sz)) {
      String sep = ";";
      if ( !sz.contains(sep))
        sep = ",";
      scartaVoci.addAll(Arrays.asList(sz.toLowerCase().split(sep)));
    }
    sz = props.getProperty(CSZ_PROP_EXCLUDEDCOLS);
    if (null != sz && sz.length() > 0) {
      String sep = ";";
      if ( !sz.contains(sep))
        sep = ",";
      excludeCols = new ArrayList<>();
      List<String> li = Arrays.asList(sz.toLowerCase().split(sep));
      for (String coln : li)
        excludeCols.add(IRigaBanca.parse(coln));
    }
    associd = new CardidAssoc();
    associd.load(p_props);
    //    try {
    //      codstats = new AppProperties();
    //      codstats.leggiPropertyFile(FILE_CODSTAT, true, false);
    //    } catch (AppPropsException e) {
    //      e.printStackTrace();
    //      return;
    //    }

  }

  @Override
  public void changeSkin() {
    // nothing to do
  }

  @Override
  public void closeApp(AppProperties prop) {
    for (PropertyChangeListener pl : propsChange.getPropertyChangeListeners())
      propsChange.removePropertyChangeListener(pl);

    prop.setIntProperty(CSZ_FLAG_FILTRI, filtriQuery);
    prop.setIntProperty(CSZ_QTA_THREADS, qtaThreads);
    prop.setIntProperty(CSZ_PERC_INDOV, getPercIndov());
    String sz;
    if (null != scartaVoci) {
      sz = String.join(",", scartaVoci);
      prop.setProperty(CSZ_PROP_SCARTA, sz);
    }
    if (null != excludeCols) {
      sz = excludeCols.stream().map(s -> s.getColNam()).collect(Collectors.joining(","));
      prop.setProperty(CSZ_PROP_EXCLUDEDCOLS, sz);
    }
  }

  public AppProperties getProps() {
    if (null == props)
      props = LoadBancaMainApp.getInst().getProps();
    return props;
  }

  public String getDBType() {
    return getProps().getProperty(AppProperties.CSZ_PROP_DB_Type);
  }

  public void setOverwrite(boolean bv) {
    overwrite = bv;
    System.out.printf("DataController.setOverwrite(%s)\n", Boolean.valueOf(bv).toString());
  }

  public void addExcludeCol(IRigaBanca p_colNam, boolean bv) {
    if (null == excludeCols)
      excludeCols = new ArrayList<>();
    if (bv) {
      System.out.println("Excl:" + p_colNam);
      if ( !excludeCols.contains(p_colNam))
        excludeCols.add(p_colNam);
    } else {
      System.out.println("Incl:" + p_colNam);
      if (excludeCols.contains(p_colNam))
        excludeCols.remove(p_colNam);
    }
  }

  /**
   * Esclude dalla vista le colonne estratte dal dataset della query
   * listMovimentiXX
   *
   * @param p_colNam
   */
  public void addExcludeCol(IRigaBanca p_colNam) {
    if (null == excludeCols)
      excludeCols = new ArrayList<>();
    excludeCols.add(p_colNam);
  }

  public boolean scartaVoce(String descr) {
    if (descr == null)
      return true;
    String sz = descr.trim().toLowerCase();
    return scartaVoci.contains(sz);
  }

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    propsChange.addPropertyChangeListener(pcl);
  }

  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    propsChange.removePropertyChangeListener(pcl);
  }

  public void firePropertyChange(String voice, Object oldv, Object newv) {
    propsChange.firePropertyChange(voice, oldv, newv);
  }

  public void setCodStat(String value) {
    if (null == value || value.equals("00"))
      return;
    firePropertyChange(DataController.EVT_CODSTAT, codStatData.getCodStat(), value);
    codStatData.setCodStat(value);
  }

  public void azzeraTotaliCodStat() {
    codStatData.clearTotali();
  }

  public void aggiornaTotaliCodStat2(String szCodStat, Number dareX, Number avereX) {
    if (null == dareX || null == avereX)
      return;
    var szCdS = szCodStat == null ? "99" : szCodStat;
    codStatData.getRoot().somma(szCdS, dareX.doubleValue(), avereX.doubleValue());
  }

  public void fineTotaliCodstat() {
    firePropertyChange(EVT_TOTCODSTAT, "-1", codStatData.getCodStat());
  }

  public void aggiornaTotaliCodStat() {
    // parse : SELECT * from ListaMovimenti WHERE 1=1  AND movStr like '2024%'  ORDER BY dtMov,dtval
    if (null == qryResulView)
      return;
    qryResulViewOld = qryResulView;
    int n = qryResulView.toLowerCase().indexOf("where");
    String szFiltro = qryResulView.substring(n);
    n = szFiltro.toLowerCase().indexOf("order");
    if (n > 0)
      szFiltro = szFiltro.substring(0, n);
    String szQry2 = String.format(QRY_TOT_CODSTAT, szFiltro);
    s_log.debug("Aggiorno i totali della resultView con filtro:{}", szFiltro);
    DBConn connSQL = LoadBancaMainApp.getInst().getConnSQL();
    Dataset dts = null;
    try (Dataset dtset = new Dataset(connSQL)) {
      if ( !dtset.executeQuery(szQry2))
        s_log.error("Errore open dataset con query {}", szQry2);
      else
        dts = dtset;
    } catch (IOException e) {
      s_log.error("Errore open dataset con query {}, err={}", szQry2, e.getMessage());
    }
    qryResulView = null;
    codStatData.clearTotali();
    if (null == dts || dts.size() == 0)
      return;
    for (DtsRow riga : dts.getRighe()) {
      String szCodice = (String) riga.get("codstat");
      // Double dare = (Double) riga.get("totDare");
      // Double avere = (Double) riga.get("totAvere");
      // causa:
      //   Exception in thread "JavaFX Application Thread" java.lang.ClassCastException: class java.lang.Float cannot be cast to class java.lang.Double (java.lang.Float and java.lang.Double are in module java.base of loader 'bootstrap')
      //      at sm.clagenna.banca.dati.DataController.aggiornaTotaliCodStat(DataController.java:285)
      //      at sm.clagenna.banca.javafx.CodStatView.lambda$propertyChange$9(CodStatView.java:456)
      Number dareX = (Number) riga.get("totDare");
      Number avereX = (Number) riga.get("totAvere");

      codStatData.getRoot().somma(szCodice, dareX.doubleValue(), avereX.doubleValue());
    }
    s_log.debug("Calcolato totali X CodStat con {} risultati", dts.size());
    firePropertyChange(EVT_TOTCODSTAT, "-1", codStatData.getCodStat());
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szEvtId = evt.getPropertyName();
    switch (szEvtId) {
      case EVT_FILECODSTATS:
        String szFil = (String) evt.getNewValue();
        props.setProperty(CSZ_PROP_FILECODSTATS, szFil);
        String sz = ParseData.s_fmtDtDate.format(new Date());
        props.setProperty(CSZ_PROP_DATAFILECDS, sz);
        break;
    }
  }

}
