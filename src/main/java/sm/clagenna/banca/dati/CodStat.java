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

public class CodStat implements Comparable<CodStat>, Cloneable {
  private static final Logger s_log = LogManager.getLogger(CodStat.class);

  @Getter @Setter
  private int          idCodStat;
  @Getter @Setter
  private int          cod1;
  @Getter @Setter
  private int          cod2;
  @Getter @Setter
  private int          cod3;
  @Getter @Setter
  private String       codice;
  @Getter @Setter
  private String       descr;
  @Getter @Setter
  private int          livello;
  @Getter @Setter
  private double       totdare;
  @Getter @Setter
  private double       totavere;
  private CodStat      father;
  @Getter @Setter
  private boolean      matched;
  @Getter
  private Set<CodStat> figli;

  public CodStat() {
    livello = 0;
  }

  public CodStat(int idCd, String cods, String desc) {
    assign(CodStat.parse(cods));
    // la parse torna un nuovo CodStat senza idCodStat
    setIdCodStat(idCd);
    setDescr(desc);
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

  public void assign(CodStat p_cds) {
    setIdCodStat(p_cds.idCodStat);
    assign(p_cds.cod1, p_cds.cod2, p_cds.cod3);
    setDescr(p_cds.descr);
    if (null == figli)
      figli = new TreeSet<CodStat>();
    if (null != p_cds.figli)
      figli.addAll(p_cds.figli);
  }

  public boolean isInDB() {
    return Utils.isValue(idCodStat);
  }

  public CodStat getFather() {
    if (null == father)
      System.out.printf("%s father null\n", getCodice());
    return father;
  }

  public void setFather(CodStat p) {
    father = p;
  }

  public void clear() {
    if (null != figli)
      figli.clear();
    figli = null;
    idCodStat = cod1 = cod2 = cod3 = livello = 0;
    descr = null;
    matched = false;
    totavere = 0d;
    totdare = 0d;
  }

  public void clearTotali() {
    totavere = totdare = 0;
    if (null == figli)
      return;
    for (CodStat fig : figli)
      fig.clearTotali();
  }

  public static CodStat parse(String szCod) {
    if (null == szCod)
      return null;
    String[] arr = szCod.split("\\.");
    int nl = arr.length;
    if (nl <= 0)
      return null;
    CodStat cds = new CodStat();
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
    CodStat nodo = find(pCdsCodice);
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

  public CodStat getPadre() {
    CodStat ret = null;
    if (null != father)
      return father;
    switch (livello) {
      case 3:
        ret = new CodStat();
        ret.assign(cod1, cod2, 0);
        break;

      case 2:
        ret = new CodStat();
        ret.assign(cod1, 0, 0);
        break;
      default:
        break;
    }
    return ret;
  }

  public CodStat getCodice(int liv) {
    CodStat ret = null;
    // se cerco codice di stesso livello, allora sono io stesso!
    if (liv == livello)
      return this;
    switch (liv) {
      case 3:
        ret = new CodStat();
        ret.assign(cod1, cod2, cod3);
        break;

      case 2:
        ret = new CodStat();
        ret.assign(cod1, cod2, 0);
        break;

      case 1:
        ret = new CodStat();
        ret.assign(cod1, 0, 0);
        break;
      default:
        break;
    }
    return ret;
  }

  public List<CodStat> getList() {
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
  public List<CodStat> getList(String p_descr) {
    List<CodStat> li = new ArrayList<CodStat>();
    String szDes = null;
    if (Utils.isValue(p_descr))
      szDes = p_descr.toLowerCase();
    li = getList(li, this, szDes);
    return li;
  }

  private List<CodStat> getList(List<CodStat> p_li, CodStat nod, String p_descr) {
    CodStat cdst = null;
    if (Utils.isValue(getDescr()) && Utils.isValue(p_descr)) {
      if (getDescr().toLowerCase().contains(p_descr))
        cdst = this;
    }
    if (null != cdst && Utils.isValue(cdst.getDescr()))
      p_li.add(cdst);
    if (null == figli)
      return p_li;
    for (CodStat cds : figli)
      p_li = cds.getList(p_li, cds, p_descr);
    return p_li;
  }

  /**
   * Verifica se e' cambiato il codice statistico oppure la descrizione del
   * modello fornito (<code>p_ob</code>)
   *
   * @param p_ob
   *          altro {@link CodStat} di riferimento
   * @return
   */
  public boolean hasChanged(CodStat p_ob) {
    if (null == p_ob)
      return false;
    if ((cod1 != p_ob.cod1) || (cod2 != p_ob.cod2) || (cod3 != p_ob.cod3))
      return true;
    return Utils.isChanged(descr, p_ob.descr);
  }

  /**
   * Imposta {@link #matched} in base alla stringa parziale fornita in
   * <code>p_sz</code>. Se la stringa e' contenuta nella nostra descrizione
   * allora {@link #matched} = true altrimenti false.<br/>
   * Serve per evindenziare dinamicamente i nodi del treeView quando si cerca un
   * codice statistico in base ad una parola della sua descrizione
   *
   * @param p_sz
   *          la stringa da cercare
   * @return true se la stringa &quot;match-a&quot; la descrizione
   */
  public boolean matchDescr(String p_sz) {
    setMatched(false);
    if (null == p_sz || p_sz.length() < 2 || null == descr || descr.length() < 2)
      return isMatched();
    setMatched(descr.toLowerCase().contains(p_sz.toLowerCase()));
    return isMatched();
  }

  public CodStat find(String p_cds) {
    CodStat lcds = CodStat.parse(p_cds);
    return find(lcds);
  }

  public CodStat find(CodStat cds) {
    if (null == cds)
      return null;
    if (this.equals(cds))
      return this;
    if (null == figli)
      return null;
    CodStat trov = null;
    for (CodStat lcd : figli) {
      trov = lcd.find(cds);
      if (null != trov)
        return trov;
    }
    return null;
  }

  public int getSize() {
    int size = 1;
    if (null != figli)
      size += figli.size();
    return size;
  }

  public int add(CodStat elem) {
    if (null == figli)
      figli = new TreeSet<CodStat>();
    List<CodStat> found = figli.stream().filter(s -> s.equals(elem)).toList();
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

  public List<CodStat> toList() {
    List<CodStat> li = new ArrayList<>();
    walk(li);
    return li;
  }

  private void walk(List<CodStat> p_li) {
    p_li.add(this);
    if (getSize() > 1) {
      for (CodStat el : figli) {
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
      for (CodStat fi : getFigli())
        fi.printAll(p_sb, nesting + 1);
    }
    return p_sb;
  }

  @Override
  public boolean equals(Object obj) {
    if (null == this.codice || null == obj)
      return false;
    if (obj instanceof CodStat o)
      return this.codice.equals(o.codice);
    return false;
  }

  @Override
  public int compareTo(CodStat o) {
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
      for (CodStat cds : figli) {
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

  @Override
  public Object clone() throws CloneNotSupportedException {
    CodStat newc = new CodStat(idCodStat, codice, descr);
    newc.figli = figli;
    newc.father = father;
    newc.totavere=totavere;
    newc.totdare=totdare;
    return newc;
  }
}
