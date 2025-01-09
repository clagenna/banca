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

  private List<CodStat> figli;
  private boolean       bSorted;
  private int           livello;
  private boolean       matched;

  public CodStat() {
    setCod1(0);
    setCod2(0);
    setCod3(0);
    calcLivello();
    setMatched(false);
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
    if (arr.length >= 1)
      cds.setCod1(Integer.parseInt(arr[0]));
    if (arr.length >= 2)
      cds.setCod2(Integer.parseInt(arr[1]));
    if (arr.length >= 3)
      cds.setCod3(Integer.parseInt(arr[2]));
    cds.calcLivello();
    return cds;
  }

  private void calcKey() {
    if ( cod1 == 0) {
      codice="Codici stat.";
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
        sb.insert(0, String.format(".%02d", cod1));
        break;
    }
    return CodStat.parse(sb.toString());
  }

  public void add(CodStat cds) {
    if (cds.livello <= livello) {
      s_log.error("Add su elemento paritario, this={}, add={}", getCodice(), cds.getCodice());
      return;
    }
    if (null == figli)
      figli = new ArrayList<CodStat>();
    bSorted = false;
    int diff = cds.getLivello() - getLivello();
    if (diff == 1) {
      if ( !figli.contains(cds))
        figli.add(cds);
      else
        figli.get(figli.indexOf(cds)).update(cds);
      return;
    }
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
    if (ndiscend < 0)
      figli.add(discend);
    else
      discend = figli.get(ndiscend);
    discend.add(cds);
  }

  private void update(CodStat cds) {
    setDescr(cds.getDescr());
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
    p_sb.append(String.format("%-20s%s", ident + getCodice(), getDescr()));
    p_sb.append("\n");
    if (null != figli) {
      for (CodStat fi : getFigli())
        fi.printAll(p_sb, nesting + 1);
    }
    return p_sb;
  }

  @Override
  public String toString() {
    return String.format("%s : %s", getCodice(), descr);
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
