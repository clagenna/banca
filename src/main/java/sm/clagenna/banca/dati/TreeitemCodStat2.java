package sm.clagenna.banca.dati;

import java.util.Set;

import javafx.scene.control.TreeItem;
import lombok.Getter;

public class TreeitemCodStat2 extends TreeCodStat2 {

  @Getter
  private TreeItem<CodStat2> treeItemRoot;

  public TreeitemCodStat2() {
    super();
  }

  @Override
  public CodStat2 readTreeCodStats() {
    CodStat2 rad = super.readTreeCodStats();
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

  public void refreshTreeItems(CodStat2 cdsCurr) { // ??
    treeItemRoot = buildTree(getRoot()); // ??
    expandNodes(treeItemRoot, cdsCurr);
  }

  public Object expandNode(CodStat2 cds) {
    TreeItem<CodStat2> exp = treeFind(treeItemRoot, cds);
    if ( null != exp)
      exp.getValue().setMatched(true);
    while(  null != exp ) {
      exp.setExpanded(true);
      exp = exp.getParent();
    }
    return exp;
  }

  private TreeItem<CodStat2> treeFind(TreeItem<CodStat2> treeI, CodStat2 cds) {
    CodStat2 no = treeI.getValue();
    if (no.equals(cds))
      return treeI;
    TreeItem<CodStat2> ret = null;
    for (TreeItem<CodStat2> lno : treeI.getChildren()) {
      ret = treeFind(lno, cds);
      if (null != ret)
        break;
    }
    return ret;
  }

  private boolean expandNodes(TreeItem<CodStat2> treeItem, CodStat2 cdsCurr) {
    if (null == cdsCurr)
      return false;
    CodStat2 lCds = treeItem.getValue();
    if (lCds.equals(cdsCurr)) {
      if ( !treeItem.isLeaf())
        treeItem.setExpanded(true);
      return true;
    }
    if (treeItem.isLeaf())
      return false;
    for (TreeItem<CodStat2> treecds : treeItem.getChildren()) {
      boolean bRet = expandNodes(treecds, cdsCurr);
      if (bRet) {
        treecds.setExpanded(true);
        return bRet;
      }
    }
    return false;
  }

  public TreeItem<CodStat2> buildTree(CodStat2 p_cds) {
    TreeItem<CodStat2> treeI = new TreeItem<>(p_cds);
    treeI.setExpanded(p_cds.getLivello() <= 0 || p_cds.isMatched());
    addTreeItems(treeI, p_cds);
    return treeI;
  }

  private TreeItem<CodStat2> addTreeItems(TreeItem<CodStat2> trit, CodStat2 p_cds) {
    Set<CodStat2> fig = p_cds.getFigli();
    if (null == fig || fig.size() == 0)
      return trit;
    for (CodStat2 no : fig) {
      TreeItem<CodStat2> it = buildTree(no);
      trit.getChildren().add(it);
    }
    return trit;
  }

}
