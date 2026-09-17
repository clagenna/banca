package sm.clagenna.banca.dati;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public enum ETipoBanca {
  Amazon("amazon", "amzn"), //
  BsiCredit("bsicredit", "bsi_credit"), //
  Bsi("bsi"), //
  CarispCredit("carispcredit", "carisp_credit", "tpay", "klirway"), //
  Carisp("carisp"), //
  Contanti("contanti"), //
  PayPal("paypal"), //
  Revolut("revolut"), //
  Smac("smac"), //
  Wise("wise");

  private static final Map<String, ETipoBanca> s_map;
  static {
    s_map = new TreeMap<>(new ETipoBancaComparator());
    for (ETipoBanca t : ETipoBanca.values()) {
      for (String s : t.appellativo) {
        s_map.put(s, t);
      }
    }
  }

  private String[] appellativo;

  private ETipoBanca(String... apellativo) {
    this.appellativo = apellativo;
  }

  public String[] getAppellativo() {
    return appellativo;
  }

  public static ETipoBanca parse(Path p) {
    if (null == p || !Files.exists(p, LinkOption.NOFOLLOW_LINKS))
      return null;
    return parse(p.getFileName().toString());
  }

  public static ETipoBanca parse(String s) {
    ETipoBanca ret = null;
    if (null == s || s.isEmpty() || s.length() < 2)
      return ret;
    String s1 = s.toLowerCase();
    for (String chiave : s_map.keySet()) {
      if (s1.contains(chiave)) {
        ret = s_map.get(chiave);
        break;
      }
    }
    return ret;
  }
}
