package sm.clagenna.banca.dati;

import lombok.Getter;

public class CodeAteco implements Comparable<CodeAteco> {
  @Getter
  private String chiave;
  @Getter
  private String descr;

  public CodeAteco() {

  }

  public CodeAteco(String p_key, String p_val) {
    chiave = p_key;
    descr = p_val;
  }

  public boolean contains(String psz) {
    if (null == psz || null == descr)
      return false;
    // boolean bRet = descr.regionMatches(true, 0, psz, 0, psz.length());
    // vedi https://stackoverflow.com/questions/86780/how-to-check-if-a-string-contains-another-string-in-a-case-insensitive-manner-in/25379180#25379180
    boolean bRet2 = descr.toLowerCase().contains(psz.toLowerCase());
    //    if (bRet ^ bRet2)
    //      System.out.println("CodeAteco.contains()");
    return bRet2;
    // vedi https://stackoverflow.com/questions/86780/how-to-check-if-a-string-contains-another-string-in-a-case-insensitive-manner-in
    // return Pattern.compile(Pattern.quote(psz), Pattern.CASE_INSENSITIVE).matcher(descr).find();
  }

  @Override
  public String toString() {
    return String.format("(%s) %s", chiave, descr);
  }

  @Override
  public int compareTo(CodeAteco o) {
    if (null == o || null == chiave || null == o.chiave)
      return -1;
    return chiave.compareTo(chiave);
  }

}
