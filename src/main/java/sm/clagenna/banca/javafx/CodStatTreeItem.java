package sm.clagenna.banca.javafx;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.TreeItem;
import lombok.Getter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.stdcla.utils.AppProperties;

public class CodStatTreeItem {
  private static final Logger s_log = LogManager.getLogger(CodStatTreeItem.class);
  private TreeItem<CodStat>   trit;
  @Getter
  private CodStat             root;

  public CodStatTreeItem() {
    //
  }

  public CodStat readTree() {
    DataController cntrlr = DataController.getInst();
    AppProperties codprops = cntrlr.getCodstats();
    root = new CodStat();
    for (Object szKey : codprops.getProperties().keySet()) {
      String szVal = codprops.getProperty(szKey.toString());
      // System.out.println("Add:" + szKey);
      CodStat nuovo = CodStat.parse(szKey.toString());
      nuovo.setDescr(szVal);
      root.add(nuovo);
      cntrlr.setCodStatTree(root);
    }
    return root;
  }

  public TreeItem<CodStat> getTree(CodStat p_cds) {
    trit = new TreeItem<CodStat>(p_cds);
    trit.setExpanded(p_cds.getLivello() <= 0 || p_cds.isMatched());
    addTreeItems(trit, p_cds);
    return trit;
  }

  private TreeItem<CodStat> addTreeItems(TreeItem<CodStat> trit, CodStat p_cds) {
    List<CodStat> fig = p_cds.getFigli();
    if (null == fig || fig.size() == 0)
      return trit;
    for (CodStat no : fig) {
      CodStatTreeItem ltrit = new CodStatTreeItem();
      TreeItem<CodStat> it = ltrit.getTree(no);
      trit.getChildren().add(it);
    }
    return trit;
  }

  public void clear() {
    if (null == trit)
      return;
    trit.getValue().clear();
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

}
