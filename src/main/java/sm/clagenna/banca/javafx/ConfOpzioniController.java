package sm.clagenna.banca.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.strt.StartOptions;
import sm.clagenna.banca.dati.strt.StrtOpt;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.javafx.MessageDialog;
import sm.clagenna.stdcla.sql.EServerId;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class ConfOpzioniController implements Initializable, IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(ConfOpzioniController.class);

  public static final String  CSZ_FXMLNAME       = "ConfOpzioni.fxml";
  private static final String CSZ_PROP_POSVIEW_X = "viewopts.x";
  private static final String CSZ_PROP_POSVIEW_Y = "viewopts.y";
  private static final String CSZ_PROP_DIMVIEW_X = "viewopts.lx";
  private static final String CSZ_PROP_DIMVIEW_Y = "viewopts.ly";

  @FXML
  private TextField         txStrtOptz;
  @FXML
  private ComboBox<StrtOpt> cbStrtOptz;
  @FXML
  private Button            btStrtOptz;
  @FXML
  private CheckBox          ckStrtOptzOverwrite;
  @FXML
  private Button            btStrtDupl;
  @FXML
  private Button            btStrtDele;

  @FXML
  private CheckBox         ckoverwrite;
  @FXML
  private Spinner<Integer> spinQtaThread;
  @FXML
  private Spinner<Integer> spinPercIndovina;
  @FXML
  private TextField        txFilesFiltro;
  @FXML
  private ComboBox<String> cbSkins;

  @FXML
  private Button   btTutti;
  @FXML
  private Button   btNessuno;
  @FXML
  private CheckBox ckTipo;
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
  private CheckBox ckExclId;
  @FXML
  private CheckBox ckExclIdfile;
  @FXML
  private CheckBox ckExclDtmov;
  @FXML
  private CheckBox ckExclDtval;
  @FXML
  private CheckBox ckExclDtmovstr;
  @FXML
  private CheckBox ckExclDtvalstr;
  @FXML
  private CheckBox ckExclDare;
  @FXML
  private CheckBox ckExclAvere;
  @FXML
  private CheckBox ckExclCardid;
  @FXML
  private CheckBox ckExclDescr;
  @FXML
  private CheckBox ckExclAbicaus;
  @FXML
  private CheckBox ckExclDescrcaus;
  @FXML
  private CheckBox ckExclCosto;
  @FXML
  private CheckBox ckExclidCodstat;
  @FXML
  private CheckBox ckExclCodstat;

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

  private AppProperties        m_mainProps;
  private Stage                lstage;
  private LoadBancaMainApp     m_appmain;
  @Getter @Setter
  private Scene                myScene;
  private DataModel            model;
  private StartOptions         startOptions;
  private boolean              bSema;
  private static final boolean VERDE = true;
  private static final boolean ROSSO = false;

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
    model = DataModel.getInst();
    m_appmain = LoadBancaMainApp.getInst();
    m_mainProps = m_appmain.getProps();
    startOptions = model.getStartOptz();
    preparaStartOptz();
    prepareFiltro();
    prepareDbVal(p_props);
    preparaExcludeCols();
    impostaForma(m_mainProps);
    model.addPropertyChangeListener(this);
  }

  private void preparaStartOptz() {
    btStrtOptz.setDisable(true);
    btStrtDupl.setDisable(true);
    btStrtDele.setDisable(true);
    String szCurr = model.getStartOptz().getCurrStrtOptz().getName();
    txStrtOptz.setText(szCurr);
    // lo devo poter editare per la duplicazione
    // txStrtOptz.setEditable(false);
    cbStrtOptz.getItems().clear();
    cbStrtOptz.getItems().addAll(model.getStartOptz().getLsStrtOptz());
    ckStrtOptzOverwrite.setSelected(true);

    cbStrtOptz.setOnAction(event -> {
      StrtOpt opt = cbStrtOptz.getValue();
      final boolean bSel = opt != null;
      btStrtOptz.setDisable( !bSel);
      btStrtDupl.setDisable( !bSel);
      btStrtDele.setDisable( !bSel);
    });
  }

  private void prepareFiltro() {
    bSema = ROSSO;
    int filtr = model.getFiltriQueryMovimenti();
    ckTipo.setSelected(ESqlFiltri.tipo.isSet(filtr));
    ckDtmov.setSelected(ESqlFiltri.Dtmov.isSet(filtr));
    ckDtval.setSelected(ESqlFiltri.Dtval.isSet(filtr));
    ckImpdare.setSelected(ESqlFiltri.Dare.isSet(filtr));
    ckImpavere.setSelected(ESqlFiltri.Avere.isSet(filtr));
    ckDescr.setSelected(ESqlFiltri.Descr.isSet(filtr));
    ckCausABI.setSelected(ESqlFiltri.ABICaus.isSet(filtr));
    ckcredhold.setSelected(ESqlFiltri.Cardid.isSet(filtr));
    bSema = VERDE;
  }

  private void prepareDbVal(AppProperties p_props) {
    String szServerId = p_props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    serverId = EServerId.parse(szServerId);
    cbServerIdClick(serverId);
    cbServerId.getItems().clear();
    cbServerId.getItems().add((EServerId) null);
    cbServerId.getItems().addAll(Arrays.asList(EServerId.values()));
    if (null != serverId)
      cbServerId.getSelectionModel().select(serverId);
    cbServerId.valueProperty().addListener((_, _, nv) -> cbServerIdClick(nv));

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
        if (null != p_newValue) {
          String szServ = p_newValue.replaceAll("[^\\d]", "");
          txService.setText(szServ);
          service = Integer.parseInt(szServ);
        }
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

  private void preparaExcludeCols() {
    ckExclId.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.id, n);
    });
    ckExclIdfile.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.idfile, n);
    });
    ckExclDtmov.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.dtmov, n);
    });
    ckExclDtval.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.dtval, n);
    });
    ckExclDtmovstr.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.movstr, n);
    });
    ckExclDtvalstr.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.valstr, n);
    });
    ckExclDare.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.dare, n);
    });
    ckExclAvere.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.avere, n);
    });
    ckExclCardid.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.cardid, n);
    });
    ckExclDescr.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.descr, n);
    });
    ckExclAbicaus.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.abicaus, n);
    });
    ckExclDescrcaus.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.descrcaus, n);
    });
    ckExclCosto.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.costo, n);
    });
    ckExclidCodstat.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.idcodstat, n);
    });
    ckExclCodstat.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.addExcludeCol(EColsTableView.codstat, n);
    });

    List<EColsTableView> excl = model.getExcludeCols();
    try {
      if (null != excl) {
        bSema = ROSSO; //    true;

        for (EColsTableView ir : excl) {
          switch (ir) {
            case avere:
              ckExclAvere.setSelected(true);
              break;
            case cardid:
              ckExclCardid.setSelected(true);
              break;
            case abicaus:
              ckExclAbicaus.setSelected(true);
              break;
            case codstat:
              ckExclCodstat.setSelected(true);
              break;
            case costo:
              ckExclCosto.setSelected(true);
              break;
            case dare:
              ckExclDare.setSelected(true);
              break;
            case descr:
              ckExclDescr.setSelected(true);
              break;
            case descrcaus:
              ckExclDescrcaus.setSelected(true);
              break;
            case dtmov:
              ckExclDtmov.setSelected(true);
              break;
            case movstr:
              ckExclDtmovstr.setSelected(true);
              break;
            case dtval:
              ckExclDtval.setSelected(true);
              break;
            case valstr:
              ckExclDtvalstr.setSelected(true);
              break;
            case id:
              ckExclId.setSelected(true);
              break;
            case idfile:
              ckExclId.setSelected(true);
              break;
            default:
              break;
          }
        }
      }
    } finally {
      bSema = VERDE;// false;
    }
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
    String oldDbName = m_mainProps.getProperty(AppProperties.CSZ_PROP_DB_name);
    if (Utils.isValueEq(oldDbName, nomeDB))
      return;
    if (model.isRilanciaApp()) {
      String szMsg = "Per applicare le modifiche al DataBase devi <b>ri-lanciare</b> l'applicazione!";
      s_log.warn(szMsg);
      MessageDialog.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_Type, serverId.toString());
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_name, nomeDB);
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_Host, nomeHost);
    if (null != service)
      m_mainProps.setIntProperty(AppProperties.CSZ_PROP_DB_service, service);
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_user, userName);
    m_mainProps.setProperty(AppProperties.CSZ_PROP_DB_passwd, password);
    s_log.info("Salvato le properties per il Nuovo Data Base {}", nomeDB);

    model.firePropertyChange(Consts.EVT_DBCHANGE, "null", nomeDB);
    model.firePropertyChange(Consts.EVT_CLOSE_WIN, "null", nomeDB);
    // FIXME dopo il cambio di DB, l'applicazione deve essere riavviata per applicare le modifiche
    // boolean riavviata = AppRestartService.restartCurrentApp();
    // boolean riavviata = AppRestartService.restartApp();
    boolean riavviata = AppRestartService.riavvia((String[]) null);
    if ( !riavviata)
      MessageDialog.messageDialog(AlertType.WARNING,
          "Non sono riuscito a riavviare automaticamente l'applicazione. Chiudila e rilanciala manualmente.");

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
    // System.out.println("ConfOpzioniController.initialize()");
    //
  }

  @FXML
  public void cbSkinsSel(String newV) {
    // m_appmain.setSkin(newV);
    model.setSkin(newV);
  }

  @Override
  public void changeSkin() {
    URL mainCSS = model.getMainCSS();
    if (null == mainCSS || null == myScene)
      return;
    myScene.getStylesheets().clear();
    myScene.getStylesheets().add(mainCSS.toExternalForm());
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

    ckoverwrite.selectedProperty().addListener((_, _, n) -> {
      if (bSema) { // VERDE
        model.setOverwrite(n);
        System.out.println("model.overwrite=" + model.isOverwrite());
      }
    });
    int qtaTh = model.getQtaThreads();
    int percIndov = model.getPercIndov();
    spinQtaThread.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, qtaTh, 1));
    spinQtaThread.valueProperty().addListener((_, _, nv) -> changeQtaThreads(nv));
    spinPercIndovina.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 100, percIndov, 1));
    spinPercIndovina.valueProperty().addListener((_, _, nv) -> changePercIndov(nv));
    caricaCbSkins();
    cbSkins.valueProperty().addListener((_, _, nv) -> cbSkinsSel(nv));
    if (null != model.getSkin())
      cbSkins.getSelectionModel().select(model.getSkin());
    txFilesFiltro.setText(p_props.getProperty(Consts.PROP_FILTER_FILES));
    txFilesFiltro.textProperty().addListener((_, _, nv) -> changedFiltroFiles(nv));

    ckTipo.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.mettiFiltro(ESqlFiltri.tipo, n);
    });
    ckDtmov.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.mettiFiltro(ESqlFiltri.Dtmov, n);
    });
    ckDtval.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.mettiFiltro(ESqlFiltri.Dtval, n);
    });
    ckImpdare.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.mettiFiltro(ESqlFiltri.Dare, n);
    });
    ckImpavere.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.mettiFiltro(ESqlFiltri.Avere, n);
    });
    ckDescr.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.mettiFiltro(ESqlFiltri.Descr, n);
    });
    ckCausABI.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.mettiFiltro(ESqlFiltri.ABICaus, n);
    });
    ckcredhold.selectedProperty().addListener((_, _, n) -> {
      if (bSema)
        model.mettiFiltro(ESqlFiltri.Cardid, n);
    });
    int px = p_props.getIntProperty(CSZ_PROP_POSVIEW_X, 10);
    int py = p_props.getIntProperty(CSZ_PROP_POSVIEW_Y, 10);
    int dx = p_props.getIntProperty(CSZ_PROP_DIMVIEW_X, 300);
    int dy = p_props.getIntProperty(CSZ_PROP_DIMVIEW_Y, 240);
    var mm = JFXUtils.getScreenMinMax(px, py, dx, dy);
    if (mm.poxX() != -1 && mm.posY() != -1 && mm.poxX() * mm.posY() != 0) {
      lstage.setX(mm.poxX());
      lstage.setY(mm.posY());
      lstage.setWidth(mm.width());
      lstage.setHeight(mm.height());
    }
    lstage.setOnHiding(_ -> {
      closeApp(m_mainProps);
    });
    changeSkin();
  }

  private void caricaCbSkins() {
    String li[] = { "LoadBancaFX", //
        "cupertino-dark", //
        "cupertino-light", //
        "dracula", //
        "nord-dark", //
        "nord-light", //
        "primer-dark", //
        "primer-light" };
    List<String> lis = Arrays.asList(li);
    cbSkins.getItems().addAll(lis);
  }

  private Object changeQtaThreads(Integer nv) {
    // System.out.printf("ConfOpzioniController.changeQtaThreads(%d)\n", nv);
    model.setQtaThreads(nv);
    return null;
  }

  private Object changePercIndov(Integer nv) {
    // System.out.printf("ConfOpzioniController.changeQtaThreads(%d)\n", nv);
    model.setPercIndov(nv);
    return null;
  }

  private Object changedFiltroFiles(String nv) {
    m_mainProps.setProperty(Consts.PROP_FILTER_FILES, nv);
    return null;
  }

  @FXML
  public void btTuttiClick(ActionEvent event) {
    //System.out.println("ConfOpzioniController.btTuttiClick()");
    setCheckBoxes(true);
  }

  private void setCheckBoxes(boolean bv) {
    ckTipo.setSelected(bv);
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
    s_log.debug("ConfOpzioniController.cbServerIdClick({})", newV.toString());
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

  @FXML
  public void cbStrtOptzClick(ActionEvent event) {
    System.out.println("ConfOpzioniController.cbStrtOptzClick()");
    StrtOpt strt = cbStrtOptz.getSelectionModel().getSelectedItem();
    if (strt.equals(startOptions.getCurrStrtOptz()))
      return;
    startOptions.setCurrStrtOptz(strt);
  }

  @FXML
  public void btStrtOptzSaveClick(ActionEvent event) {
    System.out.println("ConfOpzioniController.btStrtOptzClick()");
    StrtOpt strt = cbStrtOptz.getSelectionModel().getSelectedItem();

    if ( !Utils.isValue(strt)) {
      String szMsg = "Devi inserire un nome valido per le opzioni di avvio!";
      s_log.warn(szMsg);
      MessageDialog.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    if (strt.equals(startOptions.getCurrStrtOptz()))
      return;
    if ( !startOptions.contains(strt)) {
      String szMsg = "L' opzione di avvio \"" + strt.getName() + "\" non esiste";
      szMsg += "<br>Devi prima crearla con il pulsante \"Duplica\"";
      s_log.warn(szMsg);
      MessageDialog.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    startOptions.setCurrStrtOptz(strt);
    boolean riavviata = AppRestartService.riavvia((String[]) null);
    if ( !riavviata)
      MessageDialog.messageDialog(AlertType.WARNING,
          "Non sono riuscito a riavviare automaticamente l'applicazione. Chiudila e rilanciala manualmente.");
  }

  @FXML
  public void btStrtDuplClick(ActionEvent event) {
    System.out.println("ConfOpzioniController.btStrtDuplClick()");
    // ??   duplicare l'opzione di avvio corrente su questa nuova
    //    implica duplicare anche il file di properties di avvio, se esiste, con il nuovo nome
    String szNewOptz = txStrtOptz.getText();
    if ( !Utils.isValue(szNewOptz) || szNewOptz.length() < 4) {
      String szMsg = "Devi inserire un nome valido (min 4 chars) per le opzioni di avvio!";
      s_log.warn(szMsg);
      MessageDialog.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    startOptions.setOverwrite(ckStrtOptzOverwrite.isSelected());
    startOptions.duplica(szNewOptz);
    Platform.runLater(() -> {
      cbStrtOptz.getItems().clear();
      cbStrtOptz.getItems().addAll(startOptions.getLsStrtOptz());
      cbStrtOptz.getSelectionModel().select(startOptions.get(szNewOptz));
    });
  }

  @FXML
  public void btStrtDeleClick(ActionEvent event) {
    System.out.println("ConfOpzioniController.btStrtDeleClick()");
    StrtOpt optz = cbStrtOptz.getSelectionModel().getSelectedItem();
    if (null == optz) {
      String szMsg = "Devi selezionare un'opzione di avvio (nel ComboBox) da eliminare!";
      s_log.warn(szMsg);
      MessageDialog.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    if (optz.equals(startOptions.getCurrStrtOptz())) {
      String szMsg = "Non puoi eliminare l'opzione di avvio corrente! Se vuoi eliminarla, prima seleziona un'altra opzione di avvio come corrente.";
      s_log.warn(szMsg);
      MessageDialog.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    String szMsg = String.format("Sei sicuro di voler eliminare l'opzione di avvio :<br/>&nbsp; <b>\"%s\"</b>", optz.getName());
    Optional<ButtonType> bOk = MessageDialog.messageDialog(AlertType.CONFIRMATION, szMsg);
    if ( !bOk.isPresent() || bOk.get() != ButtonType.OK)
      return;
    startOptions.delete(optz);
    szMsg = String.format("Eliminata l'opzione di avvio :<br/><b>\"%s\"</b>", optz.getName());
    s_log.info(szMsg);
    preparaStartOptz();
    MessageDialog.messageDialog(AlertType.INFORMATION, szMsg);
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

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szEvtId = evt.getPropertyName();
    // Object obj = evt.getNewValue();
    switch (szEvtId) {
      case Consts.EVT_CHANGESKIN:
        changeSkin();
        break;

      case Consts.EVT_CLOSE_WIN:
        forceClose();
        break;

    }
  }

  @Override
  public void forceClose() {
    if (null == lstage)
      return;
    if (Platform.isFxApplicationThread()) {
      lstage.close();
    } else {
      Platform.runLater(lstage::close);
    }
  }
}
