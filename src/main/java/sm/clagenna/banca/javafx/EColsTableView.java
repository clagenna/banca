package sm.clagenna.banca.javafx;

import java.util.Map;
import java.util.TreeMap;

public enum EColsTableView {
  id(0), // 211
  tipo(1), // "BSI" (id=207)
  idfile(2), // 13  Integer  (id=151)
  dtmov(3), //  "2024-01-02" (id=208)
  dtval(4), //  "2024-01-01" (id=209)
  movstr(5), // "2024.01" (id=210)
  valstr(6), // "2024.01" (id=211)
  dare(7), // Float  (id=212)
  avere(8), //  Float  (id=213)
  cardid(9), // null
  descr(10), //   "CANONE INTERNET BANK" (id=214)
  abicaus(11), // "16" (id=215)
  descrcaus(12), // "Comissioni su pagamenti" (id=216)
  costo(13), // Integer  (id=168)
  codstat(14); //  null

  private int                                 colNo;
  private static final String                 s_elencoCols;
  private static Map<Integer, EColsTableView> s_map;
  static {
    s_map = new TreeMap<Integer, EColsTableView>();
    for (EColsTableView cc : EColsTableView.values())
      s_map.put(cc.getColNo(), cc);
    StringBuilder sb = new StringBuilder();
    for (Integer key : s_map.keySet()) {
      sb.append(sb.length() > 0 ? "," : "");
      sb.append(s_map.get(key));
    }
    s_elencoCols = sb.toString();
  }

  private EColsTableView(int pCol) {
    colNo = pCol;
  }

  public int getColNo() {
    return colNo;
  }

  public static EColsTableView colName(int vv) {
    return s_map.get(vv);
  }

  public static String allColumns() {
    return s_elencoCols;
  }

}
