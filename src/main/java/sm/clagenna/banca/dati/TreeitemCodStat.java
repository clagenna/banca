package sm.clagenna.banca.dati;

import java.util.Set;

import javafx.scene.control.TreeItem;
import lombok.Getter;

public class TreeitemCodStat extends TreeCodStat {

  @Getter
  private TreeItem<CodStat> treeItemRoot;

  public TreeitemCodStat() {
    super();
  }

  @Override
  public CodStat readTreeCodStats() {
    CodStat rad = super.readTreeCodStats();
    refreshTreeItems(); // ??
    return rad;
  }

  public void refreshTreeItems() {
    //    Map<String, CodStat2> map = getMapCodStat();
    //    for ( CodStat2 cds : map.values()) {
    //      if (null == cds.getFather())
    //        System.out.printf("%s father null\n", cds.getCodice());
    //    }
    refreshTreeItems(getRoot()); // ??
  }

  public void refreshTreeItems(CodStat cdsCurr) { // ??
    treeItemRoot = buildTree(getRoot()); // ??
    expandNodes(treeItemRoot, cdsCurr);
  }

  public Object expandNode(CodStat cds) {
    TreeItem<CodStat> exp = treeFind(treeItemRoot, cds);
    if ( null != exp)
      exp.getValue().setMatched(true);
    while(  null != exp ) {
      exp.setExpanded(true);
      exp = exp.getParent();
    }
    return exp;
  }

  private TreeItem<CodStat> treeFind(TreeItem<CodStat> treeI, CodStat cds) {
    CodStat no = treeI.getValue();
    if (no.equals(cds))
      return treeI;
    TreeItem<CodStat> ret = null;
    for (TreeItem<CodStat> lno : treeI.getChildren()) {
      ret = treeFind(lno, cds);
      if (null != ret)
        break;
    }
    return ret;
  }

  private boolean expandNodes(TreeItem<CodStat> treeItem, CodStat cdsCurr) {
    if (null == cdsCurr)
      return false;
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

  public TreeItem<CodStat> buildTree(CodStat p_cds) {
    TreeItem<CodStat> treeI = new TreeItem<>(p_cds);
    treeI.setExpanded(p_cds.getLivello() <= 0 || p_cds.isMatched());
    addTreeItems(treeI, p_cds);
    return treeI;
  }

  private TreeItem<CodStat> addTreeItems(TreeItem<CodStat> trit, CodStat p_cds) {
    Set<CodStat> fig = p_cds.getFigli();
    if (null == fig || fig.size() == 0)
      return trit;
    for (CodStat no : fig) {
      TreeItem<CodStat> it = buildTree(no);
      trit.getChildren().add(it);
    }
    return trit;
  }

}
