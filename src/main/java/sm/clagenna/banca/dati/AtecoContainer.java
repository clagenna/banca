package sm.clagenna.banca.dati;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

public class AtecoContainer {
  private static final Logger s_log          = LogManager.getLogger(AtecoContainer.class);
  private static final String CSZ_ATECO_FILE = "CAteco-2024.txt";
  private static Pattern      s_pat;

  static {
    s_pat = Pattern.compile("^([0-9]+[\\.0-9]+)[ \t]+(.*)");
  }

  @Getter
  // private Map<String, String> mapAteco;
  List<CodeAteco> liCode;

  public AtecoContainer() {
    readFile();
  }

  private void readFile() {
    InputStream res = getClass().getResourceAsStream(CSZ_ATECO_FILE);
    if (null == res)
      res = getClass().getClassLoader().getResourceAsStream(CSZ_ATECO_FILE);
    liCode = new ArrayList<CodeAteco>();
    // Map<String, CodeAteco> mapk = new HashMap<String, CodeAteco>();
    String riga = null;
    try (BufferedReader buf = new BufferedReader(new InputStreamReader(res))) {
      while ( (riga = buf.readLine()) != null) {
        Matcher mat = s_pat.matcher(riga);
        if (mat.find()) {
          CodeAteco ate = new CodeAteco(mat.group(1), mat.group(2));
          List<CodeAteco> ll = contains(ate.getDescr());
          if (null != ll && ll.size() > 0)
            liCode.removeAll(ll);
          liCode.add(ate);
        } else if (null != riga && riga.length() > 2)
          s_log.debug("Scarto: {}", riga);
      }
    } catch (IOException e) {
      s_log.error("Errore lettura {}, err={}", CSZ_ATECO_FILE, e.getMessage());
    }
  }

  public List<CodeAteco> contains(String pv) {
    List<CodeAteco> li = null;
    if (null == pv || null == liCode)
      return li;
    li = liCode.stream().filter(s -> s.contains(pv)).toList();
    return li;
  }

}
