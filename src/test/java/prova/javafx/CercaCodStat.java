package prova.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.TreeCodStat;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class CercaCodStat implements Initializable, IStartApp, PropertyChangeListener {

  private static final String          KEY_POS = "cercacdst";
  @FXML
  private TextField                    txParola;
  @FXML
  private TableView<CodStat>           tblCodstat;
  @FXML
  private TableColumn<CodStat, String> colCode;
  @FXML
  private TableColumn<CodStat, String> colDescr;

  private AppProperties props;
  private DataModel     model;
  private TreeCodStat   treeData;
  private Stage         primStage;

  public CercaCodStat() {
    //
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("CercaCodStat.initialize()");
    model = DataModel.getInst();
    treeData = model.getCodStatData();

    if (null == primStage) {
      Scene scene = txParola.getScene();
      if (null != scene)
        setStage((Stage) scene.getWindow());
    }

  }

  @Override
  public void initApp(AppProperties p_props) {
    // System.out.println("CercaCodStat.initApp()");
    model = DataModel.getInst();
    model.addPropertyChangeListener(this);
    props = model.getProps();
    if (null == primStage) {
      Scene scene = txParola.getScene();
      if (null != scene)
        setStage((Stage) scene.getWindow());
    }
    txParola.textProperty().addListener((obj, old, nv) -> txParolaSel(obj, old, nv));
    tblCodstat.getColumns().clear();
    colCode = new TableColumn<CodStat, String>("Cod. Stat.");
    colCode.setCellValueFactory(param -> {
      String sz = param.getValue().getCodice();
      var cel = new SimpleStringProperty();
      cel.set(sz);
      return cel;
    });
    tblCodstat.getColumns().add(colCode);

    colDescr = new TableColumn<CodStat, String>("Descrizione");
    colDescr.setCellValueFactory(param -> {
      String sz = param.getValue().getDescr();
      var cel = new SimpleStringProperty();
      cel.set(sz);
      return cel;
    });
    tblCodstat.getColumns().add(colDescr);

    tblCodstat.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> rowSelecion(ob, ov, nv));

    JFXUtils.readPosStage(primStage, p_props, KEY_POS);
  }

  private Object rowSelecion(ObservableValue<? extends CodStat> ob, CodStat ov, CodStat nv) {
    model.firePropertyChange(Consts.EVT_SELCODSTAT, ov, nv);
    return null;
  }

  private Object txParolaSel(ObservableValue<? extends String> obj, String old, String nv) {
    String szDesc = txParola.getText();
    if (Utils.isValue(szDesc) && szDesc.length() >= 2) {
      Platform.runLater(() -> {
        List<CodStat> li = treeData.getList(szDesc);
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
        JFXUtils.savePosStage(primStage, props, KEY_POS);
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
    model.removePropertyChangeListener(this);

  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szEvt = evt.getPropertyName();
    switch (szEvt) {
      case Consts.EVT_CLOSE_WIN:
        forceClose();
        break;
    }

  }

  public void setStage(Stage st) {
    primStage = st;
  }

  public Stage getStage() {
    return primStage;
  }

  @Override
  public void forceClose() {
    Stage stage = getStage();
    if (null == stage)
      return;
    if (Platform.isFxApplicationThread()) {
      stage.close();
    } else {
      Platform.runLater(stage::close);
    }
  }

}
