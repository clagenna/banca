package sm.clagenna.banca.dati;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.sys.ex.AppPropsException;

/**
 * Convertitore di riga CSV a {@link RigaBanca}<br/>
 * Dato un {@link Dataset} contenente le colonne del CSV questa classe, con la
 * {@link #assign(RigaBanca, DtsRow)}
 * <ol>
 * <li>pesca il valore dalla colonna del {@link Dataset}</li>
 * <li>decide di quale colonna del {@link RigaBanca} si tratta</li>
 * <li>converte il valore prelevato e lo deposita nel attributo corrispondente
 * di {@link RigaBanca}<br/>
 * e.g: <code>dtMov, dare, avere, descr</code> etc ...</li>
 * </ol>
 */
public class ConvertCsv2RigaBanca {
  static final Logger s_log = LogManager.getLogger(ConvertCsv2RigaBanca.class);

  public static final String CSZ_FILE_COLS = "%s_cols.properties";

  @Getter @Setter
  private String               tipo;
  @Getter @Setter
  private String               csvDelim;
  @Getter @Setter
  private RigaBanca            rigb;
  @Getter @Setter
  private boolean              blankOnZero;
  private AppProperties        convProps;
  @Getter
  Map<String, String>          env;
  private List<Convert2CsvCol> convCols;

  public ConvertCsv2RigaBanca(String pTipo) {
    //
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Tipo=").append(tipo);
    sb.append("\n\t").append("scvd=").append(csvDelim);
    sb.append("\n\t").append("Blk0=").append(blankOnZero ? "blank" : "zero");
    sb.append("\n\t").append("RBanca=").append(null == rigb ? "_" : rigb.toString());
    if (null == env || env.size() == 0)
      return sb.toString();
    sb.append("\n\t---- env vars ---\n\t\t");
    sb.append(env.entrySet() //
        .stream() //
        .map(e -> String.format("%-20s= %s", e.getKey(), e.getValue())) //
        .collect(Collectors.joining("\n\t\t")));
    return sb.toString();
  }

  public void readConvProperties(Path p_pth) {
    try {
      convProps = new AppProperties();
      boolean FROM_JAR = false;
      Path locProp = Paths.get("src/main/resources", p_pth.toString());
      convProps.leggiPropertyFile(locProp.toFile(), false, FROM_JAR);
      if (convProps.size() == 0) {
        FROM_JAR = true;
        convProps.leggiPropertyFile(p_pth.toFile(), true, FROM_JAR);
      }
      parseProps();
    } catch (AppPropsException e) {
      // e.printStackTrace();
      s_log.error("Errore parse properties colonne, err={}", e.getMessage(), e);
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

  /**
   * Popola un {@link RigaBanca} con le colonne passate con P_row sotto la guida
   * delle {@link #convCols} che hanno una tipologia, defaut value, SqlType etc
   * ...
   * 
   * @param p_rb
   *          il {@link RigaBanca} da popolare
   * @param P_row
   *          la riga di {@link Dataset} da cui prelevare i valori
   * @return il {@link RigaBanca} popolato
   */
  public RigaBanca assign(RigaBanca p_rb, DtsRow P_row) {
    for (Convert2CsvCol cc : convCols)
      cc.assign(p_rb, P_row);
    return p_rb;
  }
}
