package sm.clagenna.banca.dati;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.sql.SqlGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.DBConn;

public class TreeCodStat {
  private static final Logger s_log = LogManager.getLogger(TreeCodStat.class);
  // public static final String  FILE_CODSTAT = "CodStat.properties";

  //  @Getter @Setter
  //  private Path    fileCodStats;
  @Getter @Setter
  private CodStat   root;
  @Getter @Setter
  private String    codStat;
  private DataModel model;
  // private AppProperties        codstats;
  @Getter
  private Map<String, CodStat> mapCodStat;
  private DBConn               dbConn;

  private SqlGest sqlGest;

  public TreeCodStat() {
    init();
  }

  public TreeCodStat(CodStat p_no) {
    init();
    setRoot(p_no);
  }

  private void init() {
    setRoot(new CodStat());
    model = DataModel.getInst();
    // String szficds = null;
    //    if (null != contrlr) {
    //      props = contrlr.getProps();
    //      szficds = props.getProperty(DataController.CSZ_PROP_FILECODSTATS);
    //    }
    //    if (null == szficds)
    //      szficds = FILE_CODSTAT;
    //    setFileCodStats(Paths.get(szficds));
    // mapCodStat = new TreeMap<String, CodStat>(String.CASE_INSENSITIVE_ORDER);
    root = new CodStat();
    dbConn = model.getDbConn();
    sqlGest = (SqlGest) SqlGestFactory.get(dbConn.getServerId());
    sqlGest.setDbconn(dbConn);
  }

  //  public CodStat readTreeCodStats() {
  //    if (null != root)
  //      root.clear();
  //    root = new CodStat();
  //    try {
  //      if (null == fileCodStats)
  //        setFileCodStats(Paths.get(FILE_CODSTAT));
  //      codstats = new AppProperties();
  //      codstats.leggiPropertyFile(fileCodStats.toFile(), true, false);
  //    } catch (AppPropsException e) {
  //      s_log.error("Errore lettura File dei codici statistici \"{}\", err={}", fileCodStats.toString(), e.getMessage(), e);
  //      return root;
  //    }
  //
  //    for (Object szKey : codstats.getProperties().keySet()) {
  //      String szVal = codstats.getProperty(szKey.toString());
  //      // System.out.println("Add:" + szKey);
  //      CodStat nuovo = CodStat.parse(szKey.toString());
  //      nuovo.setDescr(szVal);
  //      add(nuovo);
  //    }
  //    return root;
  //  }

  public CodStat readTreeCodStats() {
    if (null != root)
      root.clear();
    mapCodStat = new TreeMap<String, CodStat>(String.CASE_INSENSITIVE_ORDER);
    List<CodStat> liCodStats = sqlGest.getListCodStat();
    for (CodStat cdst : liCodStats) {
      add(cdst);
    }
    s_log.debug("TreeCodStat: added {} nodes", liCodStats.size());
    return root;
  }

  public void add(CodStat p_cds) {
    // System.out.printf("\n---------- %s -----------\n", p_cds.getCodice());
    CodStat start = root;
    for (int liv = 1; liv <= p_cds.getLivello() || liv <= 3; liv++) {
      CodStat trova = p_cds.getCodice(liv);
      CodStat trovato = start.find(trova);
      if (null == trovato) {
        start.add(trova);
        mapCodStat.put(trova.getCodice(), trova);
        if (liv == p_cds.getLivello())
          break;
      } else if (trovato.equals(p_cds)) {
        trovato.assign(p_cds);
        mapCodStat.put(trovato.getCodice(), trovato);
        trova = trovato;
        break;
      } else
        trova = trovato;
      start = trova;
    }
  }

  public CodStat find(String string) {
    if (null == root || null == mapCodStat)
      return null;
    var cds = mapCodStat.get(string);
    return cds;
  }

  public void clearTotali() {
    if (null == root)
      return;
    root.clearTotali();
  }

  public List<CodStat> getList(String p_sz) {
    List<CodStat> li = null;
    if (null == root)
      return li;
    li = root.getList(p_sz);
    return li;
  }

  public void updateCodStat(CodStat cdsCurr, boolean bUpdDB) {
    if (null == cdsCurr)
      return;
    // codstats.setProperty(cdsCurr.getCodice(), cdsCurr.getDescr());
    if (sqlGest.existCodStat(cdsCurr))
      sqlGest.updadetCodStat(cdsCurr);
    else
      sqlGest.insertCodStat(cdsCurr);
  }

  @Deprecated
  public void saveAll() {
    //    codstats.salvaSuProperties();
    //    if (null != datac)
    //      datac.firePropertyChange(DataController.EVT_FILECODSTATS, "*null*", fileCodStats.toString());
    // s_log.info("Salvato il file Codici Statistici {}", codstats.getPropertyFile().toString());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (null == root)
      return "*null tree*";
    for (CodStat cds : root.toList()) {
      String tab = "   ".repeat(cds.getLivello());
      sb.append(tab).append(cds.toString()).append("\n");
    }
    return sb.toString();
  }

}
