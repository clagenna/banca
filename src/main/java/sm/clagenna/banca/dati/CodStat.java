package sm.clagenna.banca.dati;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class CodStat implements Comparable<CodStat> {
  private static final Logger s_log = LogManager.getLogger(CodStat.class);

  private int    cod1;
  private int    cod2;
  private int    cod3;
  private String codice;
  private String descr;
  private Double totdare;
  private Double totavere;

  private CodStat       padre;
  private List<CodStat> figli;
  private boolean       bSorted;
  private int           livello;
  private boolean       matched;

  public CodStat() {
    assign(0, 0, 0);
  }

  public void assign(CodStat cds) {
    assign(cds.cod1, cds.cod2, cds.cod3);
    setDescr(cds.descr);
  }

  public void assign(int cd1, int cd2, int cd3) {
    setDescr(null);
    setPadre(null);
    setCod1(cd1);
    setCod2(cd2);
    setCod3(cd3);
    calcLivello();
    setMatched(false);
    totdare = 0d;
    totavere = 0d;
  }

  private void calcLivello() {
    livello = 0;
    livello += cod1 != 0 ? 1 : 0;
    livello += cod2 != 0 ? 1 : 0;
    livello += cod3 != 0 ? 1 : 0;
    calcKey();
  }

  public static CodStat parse(String p_sz) {
    CodStat cds = null;
    if ( !Utils.isValue(p_sz))
      return cds;
    cds = new CodStat();
    if ( !p_sz.contains(".") && Utils.isNumeric(p_sz)) {
      cds.setCod1(Integer.parseInt(p_sz));
      cds.calcLivello();
      return cds;
    }
    String arr[] = p_sz.split("\\.");
    if (arr.length > 3)
      throw new UnsupportedOperationException("Troppi campi : " + p_sz);
    try {
      if (arr.length >= 1)
        cds.setCod1(Integer.parseInt(arr[0]));
      if (arr.length >= 2)
        cds.setCod2(Integer.parseInt(arr[1]));
      if (arr.length >= 3)
        cds.setCod3(Integer.parseInt(arr[2]));
      cds.calcLivello();
    } catch (NumberFormatException e) {
      s_log.error("Cod. stat. \"{}\" non e' interpretabile", p_sz);
      cds = null;
    }
    return cds;
  }

  public void clear() {
    setTotavere(0.);
    setTotdare(0.);
    if (null == figli || figli.size() == 0)
      return;
    for (CodStat no : figli)
      no.clear();
  }

  private void calcKey() {
    if (cod1 == 0) {
      codice = "Codici stat.";
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%02d", cod1));
    if (cod2 > 0) {
      sb.append(String.format(".%02d", cod2));
      if (cod3 > 0)
        sb.append(String.format(".%02d", cod3));
    }
    codice = sb.toString();
  }

  public CodStat getCodiceUpLevel() {
    int upLiv = livello - 1;
    if (upLiv <= 0)
      return null;
    StringBuilder sb = new StringBuilder();
    switch (upLiv) {
      case 3:
        sb.append(String.format(".%02d", cod3));
      case 2:
        sb.insert(0, String.format(".%02d", cod2));
      case 1:
        sb.insert(0, String.format("%02d", cod1));
        break;
    }
    return CodStat.parse(sb.toString());
  }

  public boolean isValid() {
    if ( !Utils.isValue(cod1) || !Utils.isValue(descr))
      return false;
    return true;
  }

  public CodStat add(CodStat cds) {
    if (cds.livello <= livello) {
      s_log.error("Add su elemento paritario, this={}, add={}", getCodice(), cds.getCodice());
      return this;
    }
    if (null == figli)
      figli = new ArrayList<CodStat>();
    bSorted = false;
    int diff = cds.getLivello() - getLivello();
    // sono sul Padre, aggiungo qui
    if (diff == 1) {
      // allora figlio diretto
      cds.setPadre(this);
      if ( !figli.contains(cds))
        figli.add(cds);
      else
        figli.get(figli.indexOf(cds)).update(cds);
      return this;
    }
    // sono sugli ancestrali
    int livdiscend = livello + 1;
    String arr[] = cds.getCodice().split("\\.");
    StringBuilder sb = new StringBuilder();
    for (String szcod : arr) {
      livdiscend--;
      if (sb.length() > 0)
        sb.append(".");
      sb.append(szcod);
      if (livdiscend == 0)
        break;
    }
    CodStat discend = CodStat.parse(sb.toString());
    int ndiscend = figli.indexOf(discend);
    if (ndiscend < 0) {
      discend.setPadre(this);
      figli.add(discend);
    } else
      discend = figli.get(ndiscend);
    discend.add(cds);
    return this;
  }

  private void update(CodStat cds) {
    setDescr(cds.getDescr());
  }

  public void somma(String pCdsCodice, Double dare, Double avere) {
    CodStat nodo = find(pCdsCodice);
    if (Utils.isValue(nodo))
      nodo.somma(dare, avere);
    else
      s_log.error("Non trovo cod. stat = {}", pCdsCodice);
  }

  public void somma(Double dare, Double avere) {
    setTotavere(getTotavere() + avere);
    setTotdare(getTotdare() + dare);
    CodStat father = getPadre();
    if (null != father)
      father.somma(dare, avere);
  }

  public CodStat find(String pCdsCodice) {
    CodStat ret = null;
    if (null == pCdsCodice || null == getCodice())
      return ret;
    if (getCodice().equals(pCdsCodice))
      return this;
    if (null == figli)
      return ret;
    for (CodStat no : figli) {
      ret = no.find(pCdsCodice);
      if (null != ret)
        break;
    }
    return ret;
  }

  public boolean matchDescr(String p_sz) {
    setMatched(false);
    if (null == p_sz || p_sz.length() < 2 || null == descr || descr.length() < 2)
      return isMatched();
    setMatched(descr.toLowerCase().contains(p_sz.toLowerCase()));
    return isMatched();
  }

  public List<CodStat> getFigli() {
    if (bSorted || null == figli)
      return figli;
    Collections.sort(figli);
    bSorted = true;
    return figli;
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
    return String.format("%-12s %12s %12s %s" //
        , getCodice() //
        , Utils.formatDouble(totdare) //
        , Utils.formatDouble(totavere) //
        , descr);
  }

  @Override
  public int compareTo(CodStat o) {
    if (null == o || this.cod1 < o.cod1)
      return -1;
    if (this.cod1 > o.cod1)
      return 1;
    // cod 2
    if (this.cod2 < o.cod2)
      return -1;
    if (this.cod2 > o.cod2)
      return 1;
    // cod 3
    if (this.cod3 < o.cod3)
      return -1;
    if (this.cod3 > o.cod3)
      return 1;
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    boolean bRet = false;
    if (obj instanceof CodStat other) {
      bRet = getCodice().equals(other.getCodice());

    }
    return bRet;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
