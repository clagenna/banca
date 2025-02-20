package prova.stat2;

import org.junit.Test;

import sm.clagenna.banca.dati.CodStat2;
import sm.clagenna.banca.dati.TreeCodStat2;
import sm.clagenna.stdcla.utils.AppProperties;

public class ProvaCodStat2 {

  private TreeCodStat2 tree;

  @Test
  public void doTheJob() {
    AppProperties.setSingleton(false);
    tree = new TreeCodStat2();
    aggiungi(2, 4, 6);
    aggiungi(1, 1, 7);
    aggiungi(1, 2, 0);
    aggiungi(1, 0, 0, "primo Liv=1");
    aggiungi(2, 0, 0, "Secondo liv=1");
    //    try {
    //      aggiungi(2, 0, 22, "Fake  liv=3");
    //    } catch (Exception e) {
    //      e.printStackTrace();
    //    }

    tree.readTreeCodStats();
    System.out.println(tree);
    String szFind = "20.02.02";
    CodStat2 cds = tree.find(szFind);
    cds.somma(234.0, 0.);
    System.out.printf("Cerco \"%s\" trovo %s\n", szFind, cds);
  }

  private void aggiungi(int i, int j, int k) {
    aggiungi(i, j, k, null);
  }

  private void aggiungi(int i, int j, int k, String descr) {
    String szCod = null;
    if (k > 0)
      szCod = String.format("%02d.%02d.%02d", i, j, k);
    else if (j > 0)
      szCod = String.format("%02d.%02d", i, j);
    else if (i > 0)
      szCod = String.format("%02d", i);
    CodStat2 cds = CodStat2.parse(szCod);
    if (null == descr)
      cds.setDescr("Con cod " + szCod);
    else
      cds.setDescr(descr);
    tree.add(cds);
    System.out.println(tree);
  }

}
