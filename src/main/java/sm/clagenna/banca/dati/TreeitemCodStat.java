package sm.clagenna.banca.dati;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import sm.clagenna.stdcla.javafx.MessageDialog;
import sm.clagenna.stdcla.utils.Utils;

/**
 * Un singolo nodo che fornisce una gerarchia di valori a un controllo, come ad
 * esempio un TreeView. Il modello può essere implementato in modo tale che i
 * valori vengano caricati in memoria solo quando necessario.
 * 
 */
public class TreeitemCodStat extends TreeCodStat {
  private static final Logger s_log = LogManager.getLogger(TreeitemCodStat.class);

  @Getter
  private TreeItem<CodStat> treeItemRoot;

  public TreeitemCodStat() {
    super();
  }

  @Override
  public CodStat readTreeCodStats() {
    CodStat rad = super.readTreeCodStats();
    refreshTreeItems();
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

  /**
   * Cerca il CodStat in questione sul treeItemRoot con:
   * <ol>
   * <li>con il codice statistico</li>
   * <li>(se non lo trova) con l' id</li>
   * </ol>
   * e lo aggiorna con i dati del CodStat passato
   *
   * @param cdsCurr
   *          CodStat da aggiornare
   */
  public void updateTreeItem(CodStat cdsCurr) {
    CodStat cds = find(cdsCurr.getCodice());
    // se non lo trovo puo' essere cambiato il codice stat
    // allora (se in modifica di un esistente) lo cerco col suo id
    if (null == cds && Utils.isValue(cdsCurr.getIdCodStat()))
      cds = find(cdsCurr.getIdCodStat());
    if (null != cds)
      cds.assign(cdsCurr);
    else {
      String szMsg = String.format("Non trovo il CodStat = %s nel albero", cdsCurr.toStringEx());
      s_log.warn(szMsg);
      MessageDialog.messageDialog(AlertType.WARNING, szMsg);
    }
  }

  public void refreshTreeItems(CodStat cdsCurr) { // ??
    treeItemRoot = buildTree(getRoot()); // ??
    expandNodes(treeItemRoot, cdsCurr);
  }

  /**
   * (Ri-)costruisce un {@link TreeitemCodStat} (deriv. TreeItem<CodStat>) a
   * partire da un CodStat aggiungendo tutti i suoi figli <b>ricorsivamente</b>
   * e impostando l'espansione dei nodi in base al livello o se il nodo è
   * <b>matched</b>
   *
   * @param p_cds
   *          CodStat da cui partire per costruire il TreeItem
   * @return TreeItem<CodStat> costruito
   */
  public TreeItem<CodStat> buildTree(CodStat p_cds) {
    TreeItem<CodStat> treeI = new TreeItem<>(p_cds);
    treeI.setExpanded(p_cds.getLivello() <= 0 || p_cds.isMatched());
    addTreeItems(treeI, p_cds); // aggiunge i figli di CodStat 
    return treeI;
  }

  /**
   * Aggiunge i figli di un CodStat al TreeItem<CodStat> passato come parametro
   * <b>ricorsivamente</b>
   *
   * @param trit
   *          TreeItem<CodStat> a cui aggiungere i figli
   * @param p_cds
   *          CodStat da cui prendere i figli
   * @return TreeItem<CodStat> aggiornato con i figli
   */
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

  public Object expandNode(CodStat cds) {
    TreeItem<CodStat> exp = treeFind(treeItemRoot, cds);
    if (null != exp)
      exp.getValue().setMatched(true);
    while (null != exp) {
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
    if (null == cdsCurr || !cdsCurr.isValid())
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

}
