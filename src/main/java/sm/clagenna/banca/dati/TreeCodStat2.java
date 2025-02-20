package sm.clagenna.banca.dati;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;

public class TreeCodStat2 {
  private static final Logger s_log        = LogManager.getLogger(TreeCodStat2.class);
  public static final String  FILE_CODSTAT = "CodStat.properties";

  @Getter @Setter
  private Path     fileCodStats;
  @Getter @Setter
  private CodStat2 root;
  @Getter @Setter
  private String   codStat;

  private DataController        datac;
  private AppProperties         props;
  private AppProperties         codstats;
  @Getter
  private Map<String, CodStat2> mapCodStat;

  public TreeCodStat2() {
    init();
  }

  public TreeCodStat2(CodStat2 p_no) {
    init();
    setRoot(p_no);
  }

  private void init() {
    setRoot(new CodStat2());
    datac = DataController.getInst();
    String szficds = null;
    if (null != datac) {
      props = datac.getProps();
      szficds = props.getProperty(DataController.CSZ_PROP_FILECODSTATS);
    }
    if (null == szficds)
      szficds = FILE_CODSTAT;
    setFileCodStats(Paths.get(szficds));
    mapCodStat = new TreeMap<String, CodStat2>(String.CASE_INSENSITIVE_ORDER);
  }

  public CodStat2 readTreeCodStats() {
    if (null != root)
      root.clear();
    root = new CodStat2();
    try {
      if (null == fileCodStats)
        setFileCodStats(Paths.get(FILE_CODSTAT));
      codstats = new AppProperties();
      codstats.leggiPropertyFile(fileCodStats.toFile(), true, false);
    } catch (AppPropsException e) {
      s_log.error("Errore lettura File dei codici statistici \"{}\", err={}", fileCodStats.toString(), e.getMessage(), e);
      return root;
    }

    for (Object szKey : codstats.getProperties().keySet()) {
      String szVal = codstats.getProperty(szKey.toString());
      // System.out.println("Add:" + szKey);
      CodStat2 nuovo = CodStat2.parse(szKey.toString());
      nuovo.setDescr(szVal);
      add(nuovo);
    }
    return root;
  }

  public void add(CodStat2 p_cds) {
    // System.out.printf("\n---------- %s -----------\n", p_cds.getCodice());
    CodStat2 start = root;
    for (int liv = 1; liv <= p_cds.getLivello() || liv <= 3; liv++) {
      CodStat2 trova = p_cds.getCodice(liv);
      CodStat2 trovato = start.find(trova);
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

  public CodStat2 find(String string) {
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

  public List<CodStat2> getList(String p_sz) {
    List<CodStat2> li = null;
    if (null == root)
      return li;
    li = root.getList(p_sz);
    return li;
  }

  public void updateCodStat(CodStat2 cdsCurr) {
    if (null == cdsCurr)
      return;
    codstats.setProperty(cdsCurr.getCodice(), cdsCurr.getDescr());
  }

  public void saveAll() {
    codstats.salvaSuProperties();
    if (null != datac)
      datac.firePropertyChange(DataController.EVT_FILECODSTATS, "*null*", fileCodStats.toString());
    s_log.info("Salvato il file Codici Statistici {}", codstats.getPropertyFile().toString());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (null == root)
      return "*null tree*";
    for (CodStat2 cds : root.toList()) {
      String tab = "   ".repeat(cds.getLivello());
      sb.append(tab).append(cds.toString()).append("\n");
    }
    return sb.toString();
  }

}
