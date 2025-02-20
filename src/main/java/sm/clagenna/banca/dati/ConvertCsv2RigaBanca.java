package sm.clagenna.banca.dati;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;

public class ConvertCsv2RigaBanca {
  static final Logger s_log = LogManager.getLogger(ConvertCsv2RigaBanca.class);

  public static final String CSZ_FILE_COLS = "%s_cols.properties";
  
  @Getter @Setter
  private String        tipo;
  @Getter @Setter
  private String        csvDelim;
  @Getter @Setter
  private RigaBanca     rigb;
  @Getter @Setter
  private boolean       blankOnZero;
  private AppProperties convProps;
  @Getter
  Map<String, String>   env;
  private List<Convert2CsvCol> convCols;

  public ConvertCsv2RigaBanca(String pTipo) {
    //
  }

  public void readConvProperties(Path p_pth) {
    try {
      convProps = new AppProperties();
      final boolean FROM_JAR = true;
      convProps.leggiPropertyFile(p_pth.toFile(), true, FROM_JAR);
      parseProps();
    } catch (AppPropsException e) {
      // e.printStackTrace();
      s_log.error("Errore parse properties colonne, err={}", e.getMessage(),e);
    }
  }

  private void parseProps() {
    String tp = convProps.getProperty("tipo");
    setTipo(tp);
    String key = String.format("%s.csvDelim", tipo);
    setCsvDelim(convProps.getProperty(key, ";"));
    key = String.format("%s.blankOnZero", tipo);
    setBlankOnZero(convProps.getBooleanProperty(key, false));
    parseEnv();
    parseCols();
  }

  private void parseEnv() {
    final String fmt_env = "%s.env.";
    env = new HashMap<>();
    String prefix = String.format(fmt_env, getTipo());
    for (Object okey : convProps.getProperties().keySet()) {
      if (okey instanceof String key) {
        if (key.startsWith(prefix)) {
          String szVal = convProps.getProperty(key);
          env.put(key.substring(prefix.length()), szVal);
        }
      }
    }
  }

  private void parseCols() {
    final String fmt_col = "%s.col_%02d.";
    convCols = new ArrayList<>();
    int fails = 0;
    for (int k = 0; k < 100 && fails < 5; k++) {
      Convert2CsvCol ccol = new Convert2CsvCol(this);
      String szKey = String.format(fmt_col, getTipo(), k);
      if ( !ccol.parseProps(convProps, szKey)) {
        fails++;
        continue;
      }
      convCols.add(ccol);
    }
  }

  public RigaBanca assign(RigaBanca p_rb, DtsRow P_row) {
    for (Convert2CsvCol cc : convCols) {
      cc.assign(p_rb, P_row);
    }
    return p_rb;
  }
}
