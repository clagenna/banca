package sm.clagenna.banca.dati;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.scene.Scene;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.csv.CsvFileContainer;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.banca.javafx.LoadBancaMainApp;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class DataModel implements IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(DataModel.class);

  private static DataModel inst;
  @Getter @Setter
  private static boolean   junit = false;

  private PropertyChangeSupport propsChange;
  private String                propsFile;
  private AppProperties         props;
  private DBConn                dbConn;
  private ISQLGest              sqlgest;
  // private Stage                 primaryStage;
  private String skin;
  private URL    mainCSS;
  private Scene  padreCercaCodstat;

  private TreeitemCodStat  codStatData;
  private CsvFileContainer contCsv;
  private Path             lastDir;
  private CardidAssoc      associd;

  // questa lo fa la dbconn.setShowStatement(true)
  //  private boolean              debugQryFilter;
  //  /** Stringa della query con i filtri applicati */
  //  private String               queryFilter;
  private boolean              overwrite;
  /** Flag dei filtri da applicare alla query */
  private int                  filtriQueryMovimenti;
  private String               codStat;
  private String               comboQuery;
  private int                  annoComp;
  private int                  qtaThreads;
  private int                  percIndov;
  private ArrayList<String>    scartaVoci;
  private boolean              doScartaDescr;
  private ScartaDescr          scartaDescr;
  private List<EColsTableView> excludeCols;

  public DataModel() {
    if (null != inst) {
      s_log.error("New instance of Singleton DataController");
      throw new UnsupportedOperationException("DataController is Singleton!");
    }
    inst = this;
    // WARNING(?!?): [this-escape] possible 'this' escape before subclass is fully initialized
    propsChange = new PropertyChangeSupport(this);
    setFiltriQueryMovimenti(ESqlFiltri.AllSets.getFlag());
    addPropertyChangeListener(this);
  }

  public void setFiltriQueryMovimenti(int p_flags) {
    if (filtriQueryMovimenti == p_flags)
      return;
    // voglio vedere chi passa da qui !!!
    // per vedere la query effettiva: dbconn.toString(stmtSelMov)
    int oldv = filtriQueryMovimenti;
    filtriQueryMovimenti = p_flags;
    if (null != sqlgest)
      ((SqlGest) sqlgest).resetStmtExistMovimento();
    firePropertyChange(Consts.EVT_OPTZ_FILTR_CHANGE, oldv, filtriQueryMovimenti);
  }

  public static DataModel getInst() {
    return inst;
  }

  public static void resetInst() {
    if (null != inst)
      inst.closeApp(null);
    inst = null;
  }

  @Override
  public void initApp(AppProperties p_props) {

    openProperties();
    openDb();

    contCsv = new CsvFileContainer();
    setFiltriQueryMovimenti(props.getIntProperty(Consts.PROP_FLAG_FILTRI, ESqlFiltri.AllSets.getFlag()));
    qtaThreads = props.getIntProperty(Consts.PROP_QTA_THREADS, 1);
    percIndov = props.getIntProperty(Consts.PROP_PERC_INDOV, 40);
    skin = props.getProperty(AppProperties.CSZ_PROP_SKIN);
    scartaVoci = new ArrayList<String>();
    doScartaDescr = props.getBooleanProperty(Consts.PROP_SCARTA_DESCR, false);
    String sz = props.getLastDir();
    if (Utils.isValue(sz))
      lastDir = Paths.get(sz);
    sz = props.getProperty(Consts.PROP_PROP_SCARTA);
    if (Utils.isValue(sz)) {
      String sep = ";";
      if ( !sz.contains(sep))
        sep = ",";
      scartaVoci.addAll(Arrays.asList(sz.toLowerCase().split(sep)));
    }
    scartaDescr = new ScartaDescr();
    scartaDescr.readProp(props);
    sz = props.getProperty(Consts.PROP_EXCLUDEDCOLS);
    if (null != sz && sz.length() > 0) {
      String sep = ";";
      if ( !sz.contains(sep))
        sep = ",";
      excludeCols = new ArrayList<>();
      List<String> li = Arrays.asList(sz.toLowerCase().split(sep));
      for (String coln : li)
        excludeCols.add(EColsTableView.parse(coln));
    }
    associd = new CardidAssoc();
    associd.load(props);
    refreshCodstatData();

  }

  private void openProperties() {
    AppProperties.setSingleton(false);
    DBConnFactory.setSingleton(false);
    try {
      if (null == propsFile)
        propsFile = Consts.CSZ_MAIN_PROPS;
      if (props == null) {
        props = new AppProperties();
        props.leggiPropertyFile(new File(propsFile), false, false);
      }
    } catch (Exception e) {
      s_log.error("Errore in  initApp.openProperties(): {}", e.getMessage(), e);
      Platform.exit();
    }
  }

  private void openDb() {
    String szDbType;
    try {
      szDbType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
      // connSQL = new DBConnSQL();
      DBConnFactory conFact = new DBConnFactory();
      dbConn = conFact.get(szDbType);
      dbConn.readProperties(props);
      dbConn.doConn();
      dbConn.setShowStatement(props.getBooleanProperty(Consts.PROP_DEBUG_QRY, false));
    } catch (Exception e) {
      s_log.error("Errore apertura DB, error={}", e.getMessage(), e);
      Platform.exit();
      System.exit(1957);
    }
    sqlgest = SqlGestFactory.get(dbConn.getServerId());
    sqlgest.setDbconn(dbConn);
  }

  public void addExcludeCol(EColsTableView p_colNam, boolean bv) {
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
  public void addExcludeCol(EColsTableView p_colNam) {
    if (null == excludeCols)
      excludeCols = new ArrayList<>();
    excludeCols.add(p_colNam);
  }

  public void mettiFiltro(ESqlFiltri tipo, Boolean bset) {
    if (bset)
      setFiltriQueryMovimenti(filtriQueryMovimenti | tipo.getFlag());
    else
      setFiltriQueryMovimenti(filtriQueryMovimenti & (ESqlFiltri.AllSets.getFlag() ^ tipo.getFlag()));
    firePropertyChange(Consts.EVT_OPTZ_FILTR_CHANGE, null, tipo);
    s_log.debug("DataController metti(cambia) Filtro(%06X)", filtriQueryMovimenti);
  }

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    if (propsChange.getPropertyChangeListeners() != null && //
        Arrays.asList(propsChange.getPropertyChangeListeners()).contains(pcl))
      return;
    propsChange.addPropertyChangeListener(pcl);
  }

  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    propsChange.removePropertyChangeListener(pcl);
  }

  public void firePropertyChange(String voice, Object oldv, Object newv) {
    propsChange.firePropertyChange(voice, oldv, newv);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szEvtId = evt.getPropertyName();
    switch (szEvtId) {

      case Consts.EVT_DBCHANGE:
        s_log.warn("Cambio di DB, ora sono su {}", evt.getNewValue());
        openDb();
        break;

      //      case EVT_FILECODSTATS:
      //        String szFil = (String) evt.getNewValue();
      //        props.setProperty(CSZ_PROP_FILECODSTATS, szFil);
      //        String sz = ParseData.s_fmtDtDate.format(new Date());
      //        props.setProperty(CSZ_PROP_DATAFILECDS, sz);
      //        break;

      case Consts.EVT_DBCODSTAT_CHANGED:
        CodStat cdsLavoro = (CodStat) evt.getNewValue();
        refreshCodstatData();
        codStatData.setCodStat(cdsLavoro.getCodice());
        break;
    }

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

  /**
   * Rileggo da capo tutti i CodStat e rinfresco {@link #codStatData}
   *
   * @return
   */
  public TreeitemCodStat refreshCodstatData() {
    codStatData = new TreeitemCodStat();
    codStatData.readTreeCodStats();
    return codStatData;
  }

  public void azzeraRifACodStat() {
    sqlgest.azzeraIdCodStats();
    refreshCodstatData();
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
    firePropertyChange(Consts.EVT_TOTCODSTAT, "-1", codStatData.getCodStat());
  }

  public boolean isPadreCercaCodstat(Scene sc) {
    return padreCercaCodstat != null && padreCercaCodstat.equals(sc);
  }

  public String getCampiFiltro() {
    StringBuilder szRet = new StringBuilder();
    for (ESqlFiltri fl : ESqlFiltri.values()) {
      if (fl.isSet(filtriQueryMovimenti) && fl.getFlag() < ESqlFiltri.AllSets.getFlag())
        szRet.append(String.format(" AND %s = ?", fl.name().toLowerCase()));
    }
    return szRet.toString();
  }


  public boolean scartaVoce(String descr) {
    if (descr == null)
      return true;
    String sz = descr.trim().toLowerCase();
    return scartaVoci.contains(sz);
  }

  public void setSkin(String pSk) {
    if ( !Utils.isChanged(skin, pSk))
      return;
    skin = pSk;
    mainCSS = null;
    props.setProperty(AppProperties.CSZ_PROP_SKIN, skin);
    /* URL url = */ getUrlSkin();
    firePropertyChange(Consts.EVT_CHANGESKIN, null, skin);
  }

  public URL getUrlSkin() {
    if (null != mainCSS)
      return mainCSS;
    if (null == skin)
      skin = Consts.CSZ_MAIN_APP_CSS;
    String skinCss = String.format("%s.css", skin);
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    mainCSS = mainApp.getClass().getResource(skinCss);
    if (null == mainCSS)
      mainCSS = mainApp.getClass().getClassLoader().getResource(skinCss);
    return mainCSS;
  }

  @Override
  public void changeSkin() {
    // nothing to do !
  }

  @Override
  public void closeApp(AppProperties p_props) {
    for (PropertyChangeListener pl : propsChange.getPropertyChangeListeners())
      propsChange.removePropertyChangeListener(pl);

    p_props.setIntProperty(Consts.PROP_FLAG_FILTRI, filtriQueryMovimenti);
    p_props.setIntProperty(Consts.PROP_QTA_THREADS, qtaThreads);
    p_props.setIntProperty(Consts.PROP_PERC_INDOV, getPercIndov());
    String sz;
    if (null != scartaVoci) {
      sz = String.join(",", scartaVoci);
      p_props.setProperty(Consts.PROP_PROP_SCARTA, sz);
    }
    if (null != excludeCols) {
      sz = excludeCols.stream().map(s -> s.toString()).collect(Collectors.joining(","));
      p_props.setProperty(Consts.PROP_EXCLUDEDCOLS, sz);
    }

  }

}
