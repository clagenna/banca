package sm.clagenna.banca.javafx;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.stdcla.enums.EServerId;
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
  private TextField        txFilesFiltro;

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

  @FXML
  private ComboBox<EServerId> cbServerId;
  @FXML
  private TextField           txDBname;
  @FXML
  private Button              btCerca;
  @FXML
  private Label               lbHost;
  @FXML
  private TextField           txHost;
  @FXML
  private Label               lbService;
  @FXML
  private TextField           txService;
  @FXML
  private Label               lbUser;
  @FXML
  private TextField           txUser;
  @FXML
  private Label               lbPswd;
  @FXML
  private TextField           txPswd;
  @FXML
  private Button              btSalva;

  private AppProperties    m_mainProps;
  private Stage            lstage;
  private LoadBancaMainApp m_appmain;
  @Getter @Setter
  private Scene            myScene;
  private DataController   dataCntr;
  private boolean          bSema;

  @Getter @Setter
  private EServerId serverId;
  @Getter @Setter
  private String    nomeDB;
  @Getter @Setter
  private String    nomeHost;
  @Getter @Setter
  private Integer   service;
  @Getter @Setter
  private String    userName;
  @Getter @Setter
  private String    password;

  public ConfOpzioniController() {
    //
  }

  @Override
  public void initApp(AppProperties p_props) {
    dataCntr = DataController.getInst();
    m_appmain = LoadBancaMainApp.getInst();
    m_mainProps = m_appmain.getProps();

    prepareFiltro();
    prepareDbVal(p_props);

    impostaForma(m_mainProps);
  }

  private void prepareFiltro() {
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
  }

  private void prepareDbVal(AppProperties p_props) {
    String szServerId = p_props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    serverId = EServerId.valueOf(szServerId);
    cbServerIdClick(serverId);
    cbServerId.getItems().clear();
    cbServerId.getItems().add((EServerId) null);
    cbServerId.getItems().addAll(Arrays.asList(EServerId.values()));
    if (null != serverId)
      cbServerId.getSelectionModel().select(serverId);
    cbServerId.valueProperty().addListener((old, ov, nv) -> cbServerIdClick(nv));

    nomeDB = p_props.getProperty(AppProperties.CSZ_PROP_DB_name);
    txDBname.setText(nomeDB);
    txDBname.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> p_observable, String p_oldValue, String p_newValue) {
        if (null != p_newValue && p_newValue.length() > 2)
          settaFileIn(Paths.get(p_newValue), false, true);
      }
    });

    nomeHost = p_props.getProperty(AppProperties.CSZ_PROP_DB_Host);
    txHost.setText(nomeHost);
    txHost.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> p_observable, String p_oldValue, String p_newValue) {
        nomeHost = p_newValue;
      }
    });

    service = p_props.getIntProperty(AppProperties.CSZ_PROP_DB_service);
    if (null != service)
      txService.setText(service.toString());
    txService.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> p_observable, String p_oldValue, String p_newValue) {
        if (null != p_newValue && !p_newValue.matches("\\d*"))
          txService.setText(p_newValue.replaceAll("[^\\d]", ""));
      }
    });

    userName = p_props.getProperty(AppProperties.CSZ_PROP_DB_user);
    txUser.setText(userName);
    txUser.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> p_observable, String p_oldValue, String p_newValue) {
        userName = p_newValue;
      }
    });

    password = p_props.getProperty(AppProperties.CSZ_PROP_DB_passwd);
    txPswd.setText(password);
    txPswd.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> p_observable, String p_oldValue, String p_newValue) {
        password = p_newValue;
      }
    });

  }

  @FXML
  void btCercaClick(ActionEvent event) {
    String szMsg = null;
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();

    FileChooser filChoose = new FileChooser();
    // imposto la dir precedente (se c'e')
    AppProperties props = mainApp.getProps();
    String szLastDir = props.getLastDir();
    if (szLastDir != null) {
      File fi = new File(szLastDir);
      if (fi.exists())
        filChoose.setInitialDirectory(fi);
    }

    File dirScelto = filChoose.showOpenDialog(lstage);
    if (dirScelto != null) {
      settaFileIn(dirScelto.toPath(), true, false);
    } else {
      szMsg = "Non hai scelto nessun file !!";
      s_log.warn(szMsg);
      // messageDialog(AlertType.WARNING, szMsg);
    }
  }

  @FXML
  void btSalvaClick(ActionEvent event) {
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_Type, serverId.toString());
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_name, nomeDB);
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_Host, nomeHost);
    if (null != service)
      m_mainProps.setIntProperty(AppProperties.CSZ_PROP_DB_service, service);
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_user, userName);
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_passwd, password);
    s_log.info("Salvato le properties per il Data Base");
  }

  private Path settaFileIn(Path p_fi, boolean p_setTx, boolean bForce) {
    if (p_fi == null)
      return p_fi;
    if ( !bForce)
      if (nomeDB != null && nomeDB.compareTo(p_fi.toString()) == 0)
        return p_fi;
    nomeDB = p_fi.toString();
    // m_mainProps.setLastDir(nomeDB);
    if (p_setTx)
      txDBname.setText(nomeDB);
    if (null != serverId) {
      switch (serverId) {
        case HSqlDB:
          break;
        case SQLite:
        case SQLite3:
          if ( !Files.exists(p_fi, LinkOption.NOFOLLOW_LINKS)) {
            s_log.error("Il path \"{}\" non esiste !", p_fi.toString());
            return p_fi;
          }
          break;
        case SqlServer:
          break;
        default:
          break;

      }
    }
    return p_fi;
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
    txFilesFiltro.setText(p_props.getProperty(LoadBancaController.CSZ_FILTER_FILES));
    txFilesFiltro.textProperty().addListener((obj, ov, nv) -> changedFiltroFiles(nv));

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

  private Object changedFiltroFiles(String nv) {
    m_mainProps.setProperty(LoadBancaController.CSZ_FILTER_FILES, nv);
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

  @FXML
  public void cbServerIdClick(EServerId newV) {
    System.out.printf("ConfOpzioniController.cbServerIdClick(%s)\n", newV.toString());
    serverId = newV;
    switch (serverId) {
      case HSqlDB:
        visibileHost(false);
        break;
      case SQLite:
      case SQLite3:
        visibileHost(false);
        break;
      case SqlServer:
        visibileHost(true);
        break;
      default:
        visibileHost(false);
        break;
    }
  }

  private void visibileHost(boolean bv) {
    btCerca.setVisible( !bv);
    lbHost.setVisible(bv);
    txHost.setVisible(bv);
    lbService.setVisible(bv);
    txService.setVisible(bv);
    lbUser.setVisible(bv);
    txUser.setVisible(bv);
    lbPswd.setVisible(bv);
    txPswd.setVisible(bv);
  }

}
