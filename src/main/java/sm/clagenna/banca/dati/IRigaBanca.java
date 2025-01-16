package sm.clagenna.banca.dati;

import java.util.HashMap;
import java.util.Map;

public enum IRigaBanca {
  ID("id"), //
  IDFILE("idfile"), //
  DTMOV("dtmov"), //
  DTVAL("dtval"), //
  DTMOVSTR("movstr"), //
  DTVALSTR("valstr"), //
  DARE("dare"), //
  AVERE("avere"), //
  CARDID("cardid"), //
  DESCR("descr"), //
  CAUS("abicaus"), //
  DESCRCAUS("descrcaus"), //
  COSTO("costo"), //
  CODSTAT("codstat");

  private String                               colNam;
  private static final Map<String, IRigaBanca> s_map;
  static {
    s_map = new HashMap<String, IRigaBanca>();
    for (IRigaBanca ri : IRigaBanca.values())
      s_map.put(ri.getColNam(), ri);
  }

  private IRigaBanca(String sz) {
    colNam = sz;
  }

  public String getColNam() {
    return colNam;
  }

  public static IRigaBanca parse(String coln) {
    if (null == coln)
      return null;
    return s_map.get(coln);
  }
}
