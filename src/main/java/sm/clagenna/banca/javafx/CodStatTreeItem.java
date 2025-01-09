package sm.clagenna.banca.javafx;

import java.util.List;

import javafx.scene.control.TreeItem;
import lombok.Getter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.stdcla.utils.AppProperties;

public class CodStatTreeItem {
  private TreeItem<CodStat> trit;
  @Getter
  private CodStat           root;

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

}
