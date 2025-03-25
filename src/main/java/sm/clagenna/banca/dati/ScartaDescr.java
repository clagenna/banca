package sm.clagenna.banca.dati;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.stdcla.utils.AppProperties;

public class ScartaDescr {
  private static final Logger s_log    = LogManager.getLogger(ScartaDescr.class);
  private static final String KEY_PROP = "descr.scrta.%02d.x";

  private List<Pattern> liPatt;

  public ScartaDescr() {
    // 
  }

  public void readProp(AppProperties p_p) {
    int scarti = 0;
    liPatt = new ArrayList<Pattern>();
    for (int i = 0; i < 100 && scarti < 6; i++) {
      String szKey = String.format(KEY_PROP, i);
      String szRegEx = p_p.getProperty(szKey);
      if (null == szRegEx) {
        scarti++;
        continue;
      }
      try {
        Pattern patt = Pattern.compile(szRegEx, Pattern.CASE_INSENSITIVE);
        liPatt.add(patt);
      } catch (Exception e) {
        s_log.error("Regex error \"{}\"\n\t on \"{}\"", e.getMessage(), szRegEx);
      }
    }
  }

  public String convert(String p_sz) {
    String szRet = p_sz;
    for (Pattern pat : liPatt) {
      Matcher mtc = pat.matcher(p_sz);
      if ( !mtc.find())
        continue;
      szRet = null;
      for (int k = 1; k <= mtc.groupCount(); k++) {
        if (k == 1)
          szRet = mtc.group(k);
        else
          szRet += " " + mtc.group(k);
      }
      break;
    }
    return szRet;
  }

}
