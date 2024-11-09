package sm.clagenna.banca.dati;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
  private static final String   CSZ_FLAG_FILTRI = "FLAG_FILTRI";
  private static DataController s_inst;

  @Getter @Setter
  private ObservableList<Path> selPaths;
  @Getter @Setter
  private int                  filtriQuery;
  @Getter @Setter
  private boolean              overwrite;
  private AppProperties        props;

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
  }

  @Override
  public void closeApp(AppProperties prop) {
    prop.setIntProperty(CSZ_FLAG_FILTRI, filtriQuery);
  }

  public AppProperties getProps() {
    if (null == props)
      props = LoadBancaMainApp.getInst().getProps();
    return props;
  }

  public String getDBType() {
    return getProps().getProperty(AppProperties.CSZ_PROP_DB_Type);
  }

}
