package sm.clagenna.banca.dati;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.javafx.IStartApp;
import sm.clagenna.banca.javafx.LoadBancaMainApp;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.AppProperties;

public class DataController implements IStartApp {
  private static final Logger   s_log           = LogManager.getLogger(DataController.class);
  private static final String   CSZ_PROP_SCARTA = "voci.scarta";
  private static final String   CSZ_FLAG_FILTRI = "FLAG_FILTRI";
  private static final String   CSZ_QTA_THREADS = "QTA_THREADS";
  private static DataController s_inst;

  @Getter @Setter
  private ObservableList<Path> selPaths;
  @Getter @Setter
  private int                  filtriQuery;
  @Getter @Setter
  private int                  qtaThreads;
  @Getter
  private boolean              overwrite;
  private AppProperties        props;
  private List<String>         scartaVoci;

  public DataController() {
    if (null != s_inst) {
      s_log.error("New instance of Singleton DataController");
      throw new UnsupportedOperationException("DataController is Singleton!");
    }
    s_inst = this;
    filtriQuery = ESqlFiltri.AllSets.getFlag();
  }

  public static DataController getInst() {
    return s_inst;
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
          dbconn.setStmtString(p_stmt, k++, p_rig.getCaus());
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
    AppProperties prop = LoadBancaMainApp.getInst().getProps();
    filtriQuery = prop.getIntProperty(CSZ_FLAG_FILTRI, ESqlFiltri.AllSets.getFlag());
    qtaThreads = prop.getIntProperty(CSZ_QTA_THREADS, 1);
    scartaVoci = new ArrayList<String>();
    String sz = prop.getProperty(CSZ_PROP_SCARTA);
    if (null != sz && sz.length() > 0) {
      String sep = ";";
      if ( !sz.contains(sep))
        sep = ",";
      scartaVoci.addAll(Arrays.asList(sz.toLowerCase().split(sep)));
    }
  }

  @Override
  public void changeSkin() {
    // nothing to do
  }

  @Override
  public void closeApp(AppProperties prop) {
    prop.setIntProperty(CSZ_FLAG_FILTRI, filtriQuery);
    prop.setIntProperty(CSZ_QTA_THREADS, qtaThreads);
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

  public boolean scartaVoce(String descr) {
    if (descr == null)
      return true;
    String sz = descr.trim().toLowerCase();
    return scartaVoci.contains(sz);
  }

}
