package sm.clagenna.banca.javafx;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.stdcla.utils.AppProperties;

public class ConfOpzioniController implements Initializable, IStartApp {
  private static final Logger s_log = LogManager.getLogger(ConfOpzioniController.class);

  public static final String  CSZ_FXMLNAME       = "ConfOpzioni.fxml";
  private static final String CSZ_PROP_POSVIEW_X = "viewopts.x";
  private static final String CSZ_PROP_POSVIEW_Y = "viewopts.y";
  private static final String CSZ_PROP_DIMVIEW_X = "viewopts.lx";
  private static final String CSZ_PROP_DIMVIEW_Y = "viewopts.ly";

  @FXML
  private CheckBox         ckoverwrite;
  @FXML
  private Spinner<Integer> spinQtaThread;

  @FXML
  private Button   btTutti;
  @FXML
  private Button   btNessuno;
  @FXML
  private CheckBox ckDtmov;
  @FXML
  private CheckBox ckDtval;
  @FXML
  private CheckBox ckImpdare;
  @FXML
  private CheckBox ckImpavere;
  @FXML
  private CheckBox ckDescr;
  @FXML
  private CheckBox ckCausABI;
  @FXML
  private CheckBox ckcredhold;

  private AppProperties    m_mainProps;
  private Stage            lstage;
  private LoadBancaMainApp m_appmain;
  @Getter @Setter
  private Scene            myScene;
  private DataController   dataCntr;
  private boolean          bSema;

  public ConfOpzioniController() {
    //
  }

  @Override
  public void initApp(AppProperties p_props) {
    dataCntr = DataController.getInst();
    m_appmain = LoadBancaMainApp.getInst();
    m_mainProps = m_appmain.getProps();
    bSema = false;

    int filtr = dataCntr.getFiltriQuery();
    ckDtmov.setSelected(ESqlFiltri.Dtmov.isSet(filtr));
    ckDtval.setSelected(ESqlFiltri.Dtval.isSet(filtr));
    ckImpdare.setSelected(ESqlFiltri.Dare.isSet(filtr));
    ckImpavere.setSelected(ESqlFiltri.Avere.isSet(filtr));
    ckDescr.setSelected(ESqlFiltri.Descr.isSet(filtr));
    ckCausABI.setSelected(ESqlFiltri.ABICaus.isSet(filtr));
    ckcredhold.setSelected(ESqlFiltri.Cardid.isSet(filtr));
    bSema = true;

    impostaForma(m_mainProps);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("ConfOpzioniController.initialize()");
    //
  }

  @Override
  public void closeApp(AppProperties p_props) {
    double px = myScene.getWindow().getX();
    double py = myScene.getWindow().getY();
    double dx = myScene.getWindow().getWidth();
    double dy = myScene.getWindow().getHeight();

    p_props.setProperty(CSZ_PROP_POSVIEW_X, (int) px);
    p_props.setProperty(CSZ_PROP_POSVIEW_Y, (int) py);
    p_props.setProperty(CSZ_PROP_DIMVIEW_X, (int) dx);
    p_props.setProperty(CSZ_PROP_DIMVIEW_Y, (int) dy);

  }

  private void impostaForma(AppProperties p_props) {
    lstage = null;
    if (myScene == null)
      myScene = btTutti.getScene();
    if (lstage == null && myScene != null)
      lstage = (Stage) myScene.getWindow();
    if (lstage == null) {
      s_log.error("Non trovo lo stage per ConfOpzioniView");
      return;
    }

    ckoverwrite.selectedProperty().addListener((obs, o, n) -> {
      if (bSema)
        dataCntr.setOverwrite(n);
    });
    int qtaTh = dataCntr.getQtaThreads();
    spinQtaThread.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, qtaTh, 1));
    spinQtaThread.valueProperty().addListener((obj, ov, nv) -> changeQtaThreads(nv));

    ckDtmov.selectedProperty().addListener((obs, o, n) -> {
      if (bSema)
        dataCntr.mettiFiltro(ESqlFiltri.Dtmov, n);
    });
    ckDtval.selectedProperty().addListener((obs, o, n) -> {
      if (bSema)
        dataCntr.mettiFiltro(ESqlFiltri.Dtval, n);
    });
    ckImpdare.selectedProperty().addListener((obs, o, n) -> {
      if (bSema)
        dataCntr.mettiFiltro(ESqlFiltri.Dare, n);
    });
    ckImpavere.selectedProperty().addListener((obs, o, n) -> {
      if (bSema)
        dataCntr.mettiFiltro(ESqlFiltri.Avere, n);
    });
    ckDescr.selectedProperty().addListener((obs, o, n) -> {
      if (bSema)
        dataCntr.mettiFiltro(ESqlFiltri.Descr, n);
    });
    ckCausABI.selectedProperty().addListener((obs, o, n) -> {
      if (bSema)
        dataCntr.mettiFiltro(ESqlFiltri.ABICaus, n);
    });
    ckcredhold.selectedProperty().addListener((obs, o, n) -> {
      if (bSema)
        dataCntr.mettiFiltro(ESqlFiltri.Cardid, n);
    });
    int px = p_props.getIntProperty(CSZ_PROP_POSVIEW_X, 10);
    int py = p_props.getIntProperty(CSZ_PROP_POSVIEW_Y, 10);
    int dx = p_props.getIntProperty(CSZ_PROP_DIMVIEW_X, 300);
    int dy = p_props.getIntProperty(CSZ_PROP_DIMVIEW_Y, 240);

    if (px * py != 0) {
      lstage.setX(px);
      lstage.setY(py);
      lstage.setWidth(dx);
      lstage.setHeight(dy);
    }
    lstage.setOnHiding(ev -> {
      closeApp(m_mainProps);
    });
  }

  private Object changeQtaThreads(Integer nv) {
    System.out.printf("ConfOpzioniController.changeQtaThreads(%d)\n", nv);
    dataCntr.setQtaThreads(nv);
    return null;
  }

  @FXML
  public void btTuttiClick(ActionEvent event) {
    //System.out.println("ConfOpzioniController.btTuttiClick()");
    setCheckBoxes(true);
  }

  private void setCheckBoxes(boolean bv) {
    ckDtmov.setSelected(bv);
    ckDtval.setSelected(bv);
    ckImpavere.setSelected(bv);
    ckImpdare.setSelected(bv);
    ckDescr.setSelected(bv);
    ckCausABI.setSelected(bv);
    ckcredhold.setSelected(bv);
  }

  @FXML
  public void btNessunoClick(ActionEvent event) {
    // System.out.println("ConfOpzioniController.btNessunoClick()");
    setCheckBoxes(false);
  }

}
