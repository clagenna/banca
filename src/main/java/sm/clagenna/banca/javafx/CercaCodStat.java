package sm.clagenna.banca.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import sm.clagenna.banca.dati.CodStat2;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.TreeCodStat2;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class CercaCodStat implements Initializable, IStartApp, PropertyChangeListener {

  @SuppressWarnings("unused")
  private static final Logger s_log = LogManager.getLogger(CercaCodStat.class);

  public static final String            CSZ_FXMLNAME = "CercaCodStat.fxml";
  private static final String           KEY_POS      = "cercacdst";
  private static final String           KEY_COL      = "cercacdst.col%s";
  @FXML
  private TextField                     txParola;
  @FXML
  private TableView<CodStat2>           tblCodstat;
  @FXML
  private TableColumn<CodStat2, String> colCode;
  @FXML
  private TableColumn<CodStat2, String> colDescr;

  private AppProperties  props;
  private DataController dataCntrl;
  private TreeCodStat2   treeData;
  private Stage          primStage;

  public CercaCodStat() {
    //
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // System.out.println("CercaCodStat.initialize()");
    dataCntrl = DataController.getInst();
    treeData = dataCntrl.getCodStatData();

    if (null == primStage) {
      Scene scene = txParola.getScene();
      if (null != scene)
        setStage((Stage) scene.getWindow());
    }

  }

  @Override
  public void initApp(AppProperties p_props) {
    // System.out.println("CercaCodStat.initApp()");
    props = p_props;
    if (null == primStage) {
      Scene scene = txParola.getScene();
      if (null != scene)
        setStage((Stage) scene.getWindow());
    }
    txParola.textProperty().addListener((obj, old, nv) -> txParolaSel(obj, old, nv));
    tblCodstat.getColumns().clear();
    colCode = new TableColumn<CodStat2, String>("Cod. Stat.");
    colCode.setCellValueFactory(param -> {
      String sz = param.getValue().getCodice();
      var cel = new SimpleStringProperty();
      cel.set(sz);
      return cel;
    });
    tblCodstat.getColumns().add(colCode);

    colDescr = new TableColumn<CodStat2, String>("Descrizione");
    colDescr.setCellValueFactory(param -> {
      String sz = param.getValue().getDescr();
      var cel = new SimpleStringProperty();
      cel.set(sz);
      return cel;
    });
    tblCodstat.getColumns().add(colDescr);

    tblCodstat.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> rowSelecion(ob, ov, nv));

    JFXUtils.readPosStage(primStage, p_props, KEY_POS);

    String szKey = String.format(KEY_COL, "CodStat2");
    double vv = p_props.getDoubleProperty(szKey, -1);
    if (vv > 0)
      colCode.setPrefWidth(vv);

    szKey = String.format(KEY_COL, "descr");
    vv = p_props.getDoubleProperty(szKey, -1);
    if (vv > 0)
      colDescr.setPrefWidth(vv);

    getStage().setOnHiding(e -> closeApp(p_props));
  }

  private Object rowSelecion(ObservableValue<? extends CodStat2> ob, CodStat2 ov, CodStat2 nv) {
    dataCntrl.firePropertyChange(DataController.EVT_SELCODSTAT, ov, nv);
    return null;
  }

  private Object txParolaSel(ObservableValue<? extends String> obj, String old, String nv) {
    String szDesc = txParola.getText();
    if (Utils.isValue(szDesc) && szDesc.length() >= 2) {
      Platform.runLater(() -> {
        List<CodStat2> li = treeData.getList(szDesc);
        tblCodstat.getItems().clear();
        tblCodstat.getItems().addAll(li);
      });
    }
    return null;
  }

  @FXML
  private Object keyPressEvent(KeyEvent e) {
    // System.out.printf("CercaCodStat.keyPressEvent(%s)\n", e);

    switch (e.getCode()) {
      case KeyCode.ESCAPE:
        e.consume();
        // JFXUtils.savePosStage(primStage, props, KEY_POS);
        closeApp(props);
        primStage.close();
        return null;

      default:
        break;
    }
    return null;
  }

  @Override
  public void changeSkin() {
    //

  }

  @Override
  public void closeApp(AppProperties p_props) {
    // System.out.println("CercaCodStat.closeApp()");
    JFXUtils.savePosStage(primStage, props, KEY_POS);
    String szKey = String.format(KEY_COL, "CodStat2");
    p_props.setProperty(szKey, Double.valueOf(colCode.getWidth()).intValue());
    szKey = String.format(KEY_COL, "descr");
    p_props.setProperty(szKey, Double.valueOf(colDescr.getWidth()).intValue());
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    //

  }

  public void setStage(Stage st) {
    primStage = st;
  }

  public Stage getStage() {
    return primStage;
  }

}
