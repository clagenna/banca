package sm.clagenna.banca.javafx;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;

public class CodStatTreeData {
  private static final Logger s_log        = LogManager.getLogger(CodStatTreeData.class);
  public static final String  FILE_CODSTAT = "CodStat.properties";

  /** il file properties con tutti i Codstat gestiti */
  @Getter @Setter
  private Path              fileCodStats;
  @Getter
  private AppProperties     codstats;
  @Getter @Setter
  private String            codStat;
  @Getter @Setter
  private CodStat           root;
  @Getter
  private TreeItem<CodStat> treeItemRoot;

  public CodStatTreeData() {
    //
  }

  public CodStat readTree() {
    //    DataController cntrlr = DataController.getInst();
    //    AppProperties codprops = cntrlr.getCodstats();
    if (null != root)
      root.clear();
    root = null;
    try {
      if (null == fileCodStats)
        fileCodStats = Paths.get(FILE_CODSTAT);
      codstats = new AppProperties();
      codstats.leggiPropertyFile(fileCodStats.toFile(), true, false);
    } catch (AppPropsException e) {
      e.printStackTrace();
      return root;
    }

    root = new CodStat();
    for (Object szKey : codstats.getProperties().keySet()) {
      String szVal = codstats.getProperty(szKey.toString());
      // System.out.println("Add:" + szKey);
      CodStat nuovo = CodStat.parse(szKey.toString());
      nuovo.setDescr(szVal);
      root.add(nuovo);
    }
    refreshTreeItems();
    return root;
  }

  public void addNode(CodStat cds) {
    if (null == root)
      root = new CodStat();
    root.add(cds);
  }

  public void refreshTreeItems() {
    refreshTreeItems(null);
  }

  public void refreshTreeItems(CodStat cdsCurr) {
    treeItemRoot = getTree(root);
    if (null == cdsCurr)
      return;
    expandNodes(treeItemRoot, cdsCurr);
  }

  private boolean expandNodes(TreeItem<CodStat> treeItem, CodStat cdsCurr) {
    CodStat lCds = treeItem.getValue();
    if (lCds.equals(cdsCurr)) {
      if ( !treeItem.isLeaf())
        treeItem.setExpanded(true);
      return true;
    }
    if (treeItem.isLeaf())
      return false;
    for (TreeItem<CodStat> treecds : treeItem.getChildren()) {
      boolean bRet = expandNodes(treecds, cdsCurr);
      if (bRet) {
        treecds.setExpanded(true);
        return bRet;
      }
    }
    return false;
  }

  public TreeItem<CodStat> getTree(CodStat p_cds) {
    treeItemRoot = new TreeItem<CodStat>(p_cds);
    treeItemRoot.setExpanded(p_cds.getLivello() <= 0 || p_cds.isMatched());
    addTreeItems(treeItemRoot, p_cds);
    return treeItemRoot;
  }

  private TreeItem<CodStat> addTreeItems(TreeItem<CodStat> trit, CodStat p_cds) {
    List<CodStat> fig = p_cds.getFigli();
    if (null == fig || fig.size() == 0)
      return trit;
    for (CodStat no : fig) {
      CodStatTreeData ltrit = new CodStatTreeData();
      TreeItem<CodStat> it = ltrit.getTree(no);
      trit.getChildren().add(it);
    }
    return trit;
  }

  public void clear() {
    if (null == treeItemRoot)
      return;
    treeItemRoot.getValue().clear();
  }

  public void addTot(String pCdsCodice, Double dare, Double avere) {
    if (null == root) {
      s_log.error("Nessun CodStat Tree per settare il totali a {}", pCdsCodice);
      return;
    }
    CodStat no = root.find(pCdsCodice);
    if (null == no) {
      s_log.error("Nessun CodStat trovato con codice {}", pCdsCodice);
      return;
    }
    no.somma(dare, avere);
  }

  public void updateCodStat(CodStat cdsCurr) {
    if (null == cdsCurr)
      return;
    codstats.setProperty(cdsCurr.getCodice(), cdsCurr.getDescr());
  }

  public void saveAll() {
    codstats.salvaSuProperties();
    s_log.info("Salvato il file Codici Statistici {}", codstats.getPropertyFile().toString());
  }

}
