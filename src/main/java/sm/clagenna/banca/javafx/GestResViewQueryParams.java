package sm.clagenna.banca.javafx;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class GestResViewQueryParams {
  private static final Logger s_log = LogManager.getLogger(GestResViewQueryParams.class);

  /** nel file properties SQry_001=Bsi;importUnion;2024;2024.04... etc */
  private static final String PROP_KEY     = "SQry_%03d";
  private static final String PROP_SEP     = "@";
  @SuppressWarnings("unused")
  private static final int    col_name     = 0;
  private static final int    col_tipo     = 1;
  private static final int    col_query    = 2;
  private static final int    col_annoComp = 3;
  private static final int    col_meseComp = 4;
  private static final int    col_parola   = 5;
  private static final int    col_where    = 6;

  @Getter @Setter
  private String  tipoBanca;
  @Getter @Setter
  private String  query;
  @Getter @Setter
  private Integer anno;
  @Getter @Setter
  private String  mese;
  @Getter @Setter
  private String  descr;
  @Getter @Setter
  private String  whereSql;

  private ResultView          resview;
  private AppProperties       props;
  /** map [qry.name] - [property_key] */
  private Map<String, String> mapName2Id;
  private Map<String, String> mapId2Text;

  interface ChiamaMap<T> {
    void doit(T s);
  }

  public GestResViewQueryParams(ResultView p_view) {
    resview = p_view;
    props = resview.getMainProps();
    mapName2Id = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    mapId2Text = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    readPropQryParams();
  }

  public void saveQuery(String p_name) {
    String szKey = null;
    boolean bNew = false;
    if (mapName2Id.containsKey(p_name))
      szKey = mapName2Id.get(p_name);
    else {
      szKey = String.format(PROP_KEY, getFreeId());
      bNew = true;
    }
    tipoBanca = resview.cbTipoBanca.getSelectionModel().getSelectedItem();
    query = resview.cbQuery.getSelectionModel().getSelectedItem();
    anno = resview.cbAnnoComp.getSelectionModel().getSelectedItem();
    mese = resview.cbMeseComp.getSelectionModel().getSelectedItem();
    descr = resview.txParola.getText();
    whereSql = resview.txWhere.getText();
    StringBuilder sb = new StringBuilder();

    ChiamaMap<String> boh = (s) -> {
      if (Utils.isValue(s))
        sb.append(s);
      sb.append(PROP_SEP);
    };
    ChiamaMap<Integer> bohi = (s) -> {
      if (Utils.isValue(s))
        sb.append(s.toString());
      sb.append(PROP_SEP);
    };
    boh.doit(p_name); // 0
    boh.doit(tipoBanca); // 1
    boh.doit(query); // 2
    bohi.doit(anno); // 3
    boh.doit(mese); // 4
    boh.doit(descr); // 5
    boh.doit(whereSql); // 6
    props.setProperty(szKey, sb.toString());
    String szDix = bNew ? "Nuovo" : "Vecchio";

    if (bNew) {
      Platform.runLater(() -> {
        resview.cbSaveQuery.getItems().add(p_name);
        resview.cbSaveQuery.getSelectionModel().select(p_name);
      });
    }
    mapId2Text.put(szKey, sb.toString());
    mapName2Id.put(p_name, szKey);

    s_log.info("Salvato {} Param query con ID({})", szDix, szKey);
  }

  private int getFreeId() {
    int ret = 1;
    for (int k = 0; k < 999; k++) {
      String szK = String.format(PROP_KEY, k);
      if ( !props.getProperties().containsKey(szK)) {
        ret = k;
        break;
      }
    }
    return ret;
  }

  public void readQuery(String szNam) {
    LoadBancaMainApp main = LoadBancaMainApp.getInst();
    if ( !Utils.isValue(szNam) || !mapName2Id.containsKey(szNam)) {
      //      main.messageDialog(AlertType.WARNING, String.format("Non hai nessun save query con nome {}", szNam));
      return;
    }
    String szK = mapName2Id.get(szNam);
    String szv = mapId2Text.get(szK);
    // per il -1 vedi: https://stackoverflow.com/questions/34040614/split-strings-keeping-all-trailing-empty-elements
    String arr[] = szv.split(PROP_SEP, -1);
    if (arr.length <= col_where) {
      main.messageDialog(AlertType.WARNING, String.format("Pochi parametri nella save query con nome {}", szNam));
      return;
    }
    resview.cbTipoBanca.getSelectionModel().select(arr[col_tipo]);
    resview.cbQuery.getSelectionModel().select(arr[col_query]);
    resview.cbAnnoComp.getSelectionModel().select(Integer.valueOf(arr[col_annoComp]));
    resview.cbMeseComp.getSelectionModel().select(arr[col_meseComp]);
    resview.txParola.setText(arr[col_parola]);
    resview.txWhere.setText(arr[col_where]);
  }

  private void readPropQryParams() {
    int qtaManca = 0;
    for (int k = 0; k < 999 && qtaManca < 10; k++) {
      String szK = String.format(PROP_KEY, k);
      if ( !props.getProperties().containsKey(szK)) {
        qtaManca++;
        continue;
      }
      String szv = props.getProperty(szK);
      // il primo param e' il nome salvato
      int n = szv.indexOf(PROP_SEP);
      if (n <= 0) {
        s_log.error("Salva query co ID({}) errato: {}", szv);
        continue;
      }
      String szNam = szv.substring(0, n);
      mapName2Id.put(szNam, szK);
      mapId2Text.put(szK, szv);
    }
  }

  public void caricaCombo(ComboBox<String> cbSaveQuery) {
    if (null == mapName2Id || mapName2Id.size() == 0)
      return;
    // preso da: https://stackoverflow.com/questions/1018750/how-to-convert-object-array-to-string-array-in-java
    String[] arr = Arrays.stream(mapName2Id.keySet().toArray()) //
        .map(Object::toString) //
        .toArray(String[]::new);
    cbSaveQuery.getItems().clear();
    cbSaveQuery.getItems().addAll(arr);
  }

}
