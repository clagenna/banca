package sm.clagenna.banca.dati;

import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class CardidAssoc {
  private static final Logger s_log    = LogManager.getLogger(CardidAssoc.class);
  private static final String PROP_TAG = "cardid.assoc.%03d";

  private Map<String, String> map;

  public CardidAssoc() {
    //
  }

  public void load(AppProperties props) {
    map = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    int noMatch = 0;
    for (int i = 1; i <= 990 && noMatch < 6; i++) {
      String szKy = String.format(PROP_TAG, i);
      String szVal = props.getProperty(szKy);
      if ( !Utils.isValue(szVal)) {
        noMatch++;
        continue;
      }
      String arr[] = szVal.toLowerCase().split(";");
      if (null == arr || arr.length != 2) {
        s_log.error("La property {} contiene ({}), valore errato!", szKy, szVal);
        noMatch++;
        continue;
      }
      map.put(arr[0], arr[1]);
    }
  }

  public String findAssoc(String psz) {
    String szRet = null;
    if (null == map || null == psz || psz.trim().length() < 2)
      return szRet;
    for (String ky : map.keySet()) {
      if (psz.contains(ky)) {
        szRet = map.get(ky);
        break;
      }
    }
    return szRet;
  }
}
