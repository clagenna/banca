package sm.clagenna.banca.dati;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.utils.Utils;

public class CodStat2 implements Comparable<CodStat2> {
  private static final Logger s_log = LogManager.getLogger(CodStat2.class);

  @Getter @Setter
  private int    cod1;
  @Getter @Setter
  private int    cod2;
  @Getter @Setter
  private int    cod3;
  @Getter @Setter
  private String codice;
  @Getter @Setter
  private String descr;
  @Getter @Setter
  private int    livello;
  @Getter @Setter
  private double totdare;
  @Getter @Setter
  private double totavere;

  private CodStat2      father;
  @Getter @Setter
  private boolean       matched;
  @Getter
  private Set<CodStat2> figli;

  public CodStat2() {
    livello = 0;
  }

  public void assign(int cd1, int cd2, int cd3) {
    setCod1(cd1);
    setCod2(cd2);
    setCod3(cd3);
    if (0 != cd3) {
      setCodice(String.format("%02d.%02d.%02d", cd1, cd2, cd3));
      livello = 3;
    } else if (0 != cd2) {
      setCodice(String.format("%02d.%02d", cd1, cd2));
      livello = 2;
    } else if (0 != cd1) {
      setCodice(String.format("%02d", cd1));
      livello = 1;
    }
  }

  public void assign(CodStat2 p_cds) {
    assign(p_cds.cod1, p_cds.cod2, p_cds.cod3);
    setDescr(p_cds.descr);
    if (null == figli)
      figli = new TreeSet<CodStat2>();
    if (null != p_cds.figli)
      figli.addAll(p_cds.figli);
  }

  public CodStat2 getFather() {
    if (null == father)
      System.out.printf("%s father null\n", getCodice());
    return father;
  }

  public void setFather(CodStat2 p) {
    father = p;
  }

  public void clear() {
    if (null != figli)
      figli.clear();
    figli = null;
    cod1 = cod2 = cod3 = livello = 0;
    descr = null;
    matched = false;
    totavere = 0d;
    totdare = 0d;
  }

  public void clearTotali() {
    totavere = totdare = 0;
    if (null == figli)
      return;
    for (CodStat2 fig : figli)
      fig.clearTotali();
  }

  public static CodStat2 parse(String szCod) {
    if (null == szCod)
      return null;
    String[] arr = szCod.split("\\.");
    int nl = arr.length;
    if (nl <= 0)
      return null;
    CodStat2 cds = new CodStat2();
    int cd1 = 0, cd2 = 0, cd3 = 0;
    if (nl >= 1) {
      cd1 = Integer.parseInt(arr[0]);
      if (cd1 <= 0)
        throw new UnsupportedOperationException("Codice Stat non valido " + szCod);
    }
    if (nl >= 2) {
      cd2 = Integer.parseInt(arr[1]);
      if (cd2 <= 0)
        throw new UnsupportedOperationException("Codice Stat non valido " + szCod);
    }
    if (nl >= 3) {
      cd3 = Integer.parseInt(arr[2]);
      if (cd3 <= 0)
        throw new UnsupportedOperationException("Codice Stat non valido " + szCod);
    }
    cds.assign(cd1, cd2, cd3);
    return cds;
  }

  public void somma(double p_dare, double p_avere) {
    totavere += p_avere;
    totdare += p_dare;
    if (null != father)
      father.somma(p_dare, p_avere);
    //System.out.printf("CodStat2.somma(%s)\n", toString());
  }

  public void somma(String pCdsCodice, Double dare, Double avere) {
    CodStat2 nodo = find(pCdsCodice);
    if (Utils.isValue(nodo))
      nodo.somma(dare, avere);
    else
      s_log.error("Non trovo cod. stat = {}", pCdsCodice);
  }

  public boolean isValid() {
    if ( !Utils.isValue(cod1) || !Utils.isValue(descr))
      return false;
    return true;
  }

  public CodStat2 getPadre() {
    CodStat2 ret = null;
    switch (livello) {
      case 3:
        ret = new CodStat2();
        ret.assign(cod1, cod2, 0);
        break;

      case 2:
        ret = new CodStat2();
        ret.assign(cod1, 0, 0);
        break;
      default:
        break;
    }
    return ret;
  }

  public CodStat2 getCodice(int liv) {
    CodStat2 ret = null;
    // se cerco codice di stesso livello, allora sono io stesso!
    if (liv == livello)
      return this;
    switch (liv) {
      case 3:
        ret = new CodStat2();
        ret.assign(cod1, cod2, cod3);
        break;

      case 2:
        ret = new CodStat2();
        ret.assign(cod1, cod2, 0);
        break;

      case 1:
        ret = new CodStat2();
        ret.assign(cod1, 0, 0);
        break;
      default:
        break;
    }
    return ret;
  }

  public List<CodStat2> getList() {
    return getList(null);
  }

  /**
   * torna tutti i nodi sottostanti sotto forma di un {@link List}. Se il
   * parametro <code>p_descr</code> e' diverso da null esegue un filtro dei soli
   * elementi nella cui descrizione sia inclusa la stringa <code>p_descr</code>
   *
   * @param p_descr
   * @return
   */
  public List<CodStat2> getList(String p_descr) {
    List<CodStat2> li = new ArrayList<CodStat2>();
    String szDes = null;
    if (Utils.isValue(p_descr))
      szDes = p_descr.toLowerCase();
    li = getList(li, this, szDes);
    return li;
  }

  private List<CodStat2> getList(List<CodStat2> p_li, CodStat2 nod, String p_descr) {
    CodStat2 cdst = null;
    if (Utils.isValue(getDescr()) && Utils.isValue(p_descr)) {
      if (getDescr().toLowerCase().contains(p_descr))
        cdst = this;
    }
    if (null != cdst && Utils.isValue(cdst.getDescr()))
      p_li.add(cdst);
    if (null == figli)
      return p_li;
    for (CodStat2 cds : figli)
      p_li = cds.getList(p_li, cds, p_descr);
    return p_li;
  }

  public boolean matchDescr(String p_sz) {
    setMatched(false);
    if (null == p_sz || p_sz.length() < 2 || null == descr || descr.length() < 2)
      return isMatched();
    setMatched(descr.toLowerCase().contains(p_sz.toLowerCase()));
    return isMatched();
  }

  public CodStat2 find(String p_cds) {
    CodStat2 lcds = CodStat2.parse(p_cds);
    return find(lcds);
  }

  public CodStat2 find(CodStat2 cds) {
    if (null == cds)
      return null;
    if (this.equals(cds))
      return this;
    if (null == figli)
      return null;
    CodStat2 trov = null;
    for (CodStat2 lcd : figli) {
      trov = lcd.find(cds);
      if (null != trov)
        return trov;
      //      if (lcd.equals(cds))
      //        return lcd;
    }
    return null;
  }

  public int getSize() {
    int size = 1;
    if (null != figli)
      size += figli.size();
    return size;
  }

  public int add(CodStat2 elem) {
    if (null == figli)
      figli = new TreeSet<CodStat2>();
    List<CodStat2> found = figli.stream().filter(s -> s.equals(elem)).toList();
    int indx = found.size();
    if (indx <= 0) {
      figli.add(elem);
      elem.setFather(this);
    } else {
      var pad = found.get(0);
      pad.assign(elem);
      pad.setFather(this);
    }
    return indx;
  }

  public List<CodStat2> toList() {
    List<CodStat2> li = new ArrayList<>();
    walk(li);
    return li;
  }

  private void walk(List<CodStat2> p_li) {
    p_li.add(this);
    if (getSize() > 1) {
      for (CodStat2 el : figli) {
        el.walk(p_li);
      }
    }
  }

  public StringBuilder printAll(StringBuilder p_sb) {
    return printAll(p_sb, 0);
  }

  public StringBuilder printAll(StringBuilder p_sb, int nesting) {
    String ident = "  ".repeat(nesting);
    p_sb.append(String.format("%-20s %16s %16s %s" //
        , ident + getCodice() //
        , Utils.formatDouble(totdare), Utils.formatDouble(totavere), getDescr()));
    p_sb.append("\n");
    if (null != figli) {
      for (CodStat2 fi : getFigli())
        fi.printAll(p_sb, nesting + 1);
    }
    return p_sb;
  }

  @Override
  public boolean equals(Object obj) {
    if (null == this.codice || null == obj)
      return false;
    if (obj instanceof CodStat2 o)
      return this.codice.equals(o.codice);
    return false;
  }

  @Override
  public int compareTo(CodStat2 o) {
    if (null == o || this.cod1 < o.cod1)
      return -1;
    // livello 1
    if (this.cod1 > o.cod1)
      return 1;
    // cod1=o.cod1;   livello 2
    if (this.cod2 < o.cod2)
      return -1;
    if (this.cod2 > o.cod2)
      return 1;
    // cod2=o.cod2;   livello 3
    if (this.cod3 < o.cod3)
      return -1;
    if (this.cod3 > o.cod3)
      return 1;
    return 0;
  }

  public String toStringEx() {
    return String.format("%d.%d.%d %s" //
        , cod1, cod2, cod3 //
        //        , Utils.formatDouble(totdare) //
        //        , Utils.formatDouble(totavere) //
        , descr);
  }

  public String toExpanded() {
    StringBuilder sb = new StringBuilder();
    toExpanded(livello, sb);
    return sb.toString();
  }

  private String toExpanded(int liv, StringBuilder sb) {
    String szTab = "  ".repeat(liv);
    sb.append(szTab);
    sb.append(toStringEx()).append("\n");
    if (null != figli) {
      for (CodStat2 cds : figli) {
        sb.append(cds.toExpanded(liv + 1, sb));
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return String.format("%-12s  %-50s %s %s" //
        , getCodice() //
        , null == descr ? "*null*" : descr //
        , Utils.formatDouble(totdare) //
        , Utils.formatDouble(totavere));
  }

}
