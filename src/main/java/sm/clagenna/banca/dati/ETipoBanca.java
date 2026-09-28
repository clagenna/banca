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
      for (String s : t.appellativi) {
        s_map.put(s, t);
      }
    }
  }

  private String[] appellativi;

  private ETipoBanca(String... p_apellativi) {
    this.appellativi = p_apellativi;
  }

  public String[] getAppellativi() {
    return appellativi;
  }

  public String getAppellativo() {
    return appellativi[0];
  }

  public static ETipoBanca parse(Path p) {
    if (null == p || !Files.exists(p, LinkOption.NOFOLLOW_LINKS))
      return null;
    return parse(p.getFileName().toString());
  }

  /**
   * Ritorna il tipo di banca in base al nome del file o alla stringa passata
   * come parametro. Viene cercato l'<b>appellativo</b> nella stringa passata.
   * Se non trova corrispondenza ritorna null.
   *
   * @param s
   *          Stringa da analizzare
   * @return ETipoBanca corrispondente, null se non trova corrispondenza
   */
  public static ETipoBanca parse(String s) {
    ETipoBanca ret = null;
    if (null == s || s.isEmpty() || s.length() < 2)
      return ret;
    String s1 = s.toLowerCase();
    for (String chiave : s_map.keySet()) {
      if ( s1.equals(chiave)) {
        ret = s_map.get(chiave);
        break;
      }
      // cerco la chiave con "_" davanti e dietro, forse, '_','-',' ' 
      // per evitare di prendere "carisp" in "carispcredit"
      String k2 = ".*_" + chiave + "[_\\- ]*.*";
      if (s1.matches(k2)) {
        ret = s_map.get(chiave);
        break;
      }
    }
    return ret;
  }
}
