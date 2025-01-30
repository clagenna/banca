package sm.clagenna.banca.javafx;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CsvImportBanca;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.ImpFile;
import sm.clagenna.banca.dati.Versione;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ILog4jReader;
import sm.clagenna.stdcla.utils.Log4jRow;
import sm.clagenna.stdcla.utils.MioAppender;
import sm.clagenna.stdcla.utils.Utils;

public class LoadBancaController implements Initializable, ILog4jReader, IStartApp, PropertyChangeListener {
  private static final Logger s_log         = LogManager.getLogger(LoadBancaController.class);
  public static final String  CSZ_FXMLNAME  = "BancaJavaFX.fxml";
  private static final String CSZ_LOG_LEVEL = "logLevel";
  private static final String CSZ_SPLITPOS  = "splitpos";
  private static final String CSZ_COL_time  = "log_time";
  private static final String CSZ_COL_leve  = "log_lev";
  private static final String CSZ_COL_mesg  = "log_mesg";

  @FXML
  private MenuItem mnuGEstDati;
  @FXML
  private MenuItem mnuGEstOpzioni;

  @FXML
  private TextField txDirExports;
  @FXML
  private Button    btCercaDir;
  @FXML
  private SplitPane spltPane;
  private double    spltDivPos;

  @FXML
  private TableView<ImpFile>           tblvFiles;
  private TableColumn<ImpFile, String> colId;
  private TableColumn<ImpFile, String> colName;
  private TableColumn<ImpFile, String> colRelDir;
  private TableColumn<ImpFile, String> colCardHold;
  private TableColumn<ImpFile, Number> colSize;
  private TableColumn<ImpFile, Number> colQtaRecs;
  private TableColumn<ImpFile, String> colDtmin;
  private TableColumn<ImpFile, String> colDtmax;
  private TableColumn<ImpFile, String> colUltagg;

  @FXML
  private TableView<Log4jRow>           tblLogs;
  @FXML
  private TableColumn<Log4jRow, String> colTime;
  @FXML
  private TableColumn<Log4jRow, String> colLev;
  @FXML
  private TableColumn<Log4jRow, String> colMsg;
  @FXML
  private Button                        btClearMsg;
  @FXML
  private Button                        btConvCSV;
  @FXML
  private ComboBox<Level>               cbLevelMin;
  private Level                         levelMin;
  @FXML
  private Label                         lbProgressione;
  @FXML
  private ProgressBar                   prgrb;

  private DataController        cntrlr;
  private AppProperties         props;
  private ConfOpzioniController cntrlConfOpz;
  private int                   qtaActiveTasks;
  private ResultView            cntrResultView;
  private CodStatView           cntrCodStatView;
  private GuessCodStatView      cntrGuessCodStatView;
  private ViewContanti          cntrViewContanti;
  private SovrapposView         cntrViewSovrapp;
  private List<Log4jRow>        m_liMsgs;
  @Getter @Setter
  private String                styRowZeroRecs;
  private double                endProgressNo;
  // private ObservableList<FileCSV> liFilesCSV;

  public LoadBancaController() {
    endProgressNo = 0.;
    styRowZeroRecs = "gold";
    spltDivPos = 0.7;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    MioAppender.setLogReader(this);
    cntrlr = DataController.getInst();
    props = LoadBancaMainApp.getInst().getProps();
    levelMin = Level.INFO;
    initApp(props);
  }

  @Override
  public void addLog(String[] p_arr) {
    // [0] - class emitting
    // [1] - timestamp
    // [2] - Log Level
    // [3] - message
    // System.out.println("addLog=" + String.join("\t", p_arr));
    Log4jRow riga = null;
    try {
      riga = new Log4jRow(p_arr);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (riga != null)
      addRiga(riga);
  }

  private void addRiga(Log4jRow rig) {
    if (m_liMsgs == null)
      m_liMsgs = new ArrayList<>();
    m_liMsgs.add(rig);
    // if ( rig.getLevel().isInRange( Level.FATAL, levelMin )) // isLessSpecificThan(levelMin))
    if (rig.getLevel().intLevel() <= levelMin.intLevel())
      tblLogs.getItems().add(rig);
  }

  @Override
  public void initApp(AppProperties props) {
    LoadBancaMainApp main = LoadBancaMainApp.getInst();
    main.setController(this);
    impostaTitolo();
    // vedi: https://stackoverflow.com/questions/27160951/javafx-open-another-fxml-in-the-another-window-with-button
    getStage().onCloseRequestProperty().setValue(e -> Platform.exit());

    // -------- combo level -------
    if (props != null) {
      String sz = props.getProperty(CSZ_LOG_LEVEL);
      if (sz != null)
        levelMin = Level.toLevel(sz);
    }
    initSplitPos(props);
    initTblFilesCSV();
    initTblLogs();
    initTxLastDir(props);
  }

  private void initSplitPos(AppProperties props) {
    String szPos = props.getProperty(CSZ_SPLITPOS);
    if (Utils.isValue(szPos)) {
      Stage stage = getStage();
      spltDivPos = Double.valueOf(szPos);
      // spltPane.setDividerPositions(dbl);
      stage.showingProperty().addListener((obj, ov, nv) -> {
        if (nv && spltDivPos != 0) {
          spltPane.setDividerPositions(spltDivPos);
          spltDivPos = 0;
        }
      });
    }
  }

  private void impostaTitolo() {
    String szTit = "Caricamento degli Export CSV dalle Banche su DB, %s";
    String szDir = Versione.getVersionEx();
    if (null != cntrlr)
      szDir = "da:" + cntrlr.getLastDir().toString();
    final String tit = String.format(szTit, szDir);
    Platform.runLater(() -> getStage().setTitle(tit));
  }

  private void initTxLastDir(AppProperties props) {
    String szLastDir = props.getLastDir();
    if (szLastDir != null) {
      txDirExports.setText(szLastDir);
      settaFileIn(Paths.get(szLastDir), true, true);
    }
    txDirExports.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> p_observable, Boolean p_oldValue, Boolean p_newValue) {
        String sz = txDirExports.getText();
        // s_log.debug("txDirExports.focus={} text={}", p_newValue, sz);
        if ( !p_newValue) {
          if (sz != null && sz.length() > 2)
            onEnterDirCSV(null);
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  private void initTblFilesCSV() {
    // System.out.println("LoadBancaController.initTblFilesCSV()");
    colId = new TableColumn<>("Id");
    colId.setCellValueFactory(cell -> cell.getValue().getOid());

    colName = new TableColumn<>("Nome");
    colName.setCellValueFactory(cell -> cell.getValue().getOFileName());

    colRelDir = new TableColumn<>("Path Rel.");
    colRelDir.setCellValueFactory(cell -> cell.getValue().getORelDir());

    colCardHold = new TableColumn<>("Hold.");
    colCardHold.setCellValueFactory(cell -> cell.getValue().getOCardHold());

    colSize = new TableColumn<>("Dimensione");
    colSize.setCellValueFactory(cell -> cell.getValue().getOSize());

    colQtaRecs = new TableColumn<>("Qta. Righe");
    colQtaRecs.setCellValueFactory(cell -> cell.getValue().getOQtarecs());

    colDtmin = new TableColumn<>("Data min.");
    colDtmin.setCellValueFactory(cell -> cell.getValue().getODtmin());

    colDtmax = new TableColumn<>("Data max.");
    colDtmax.setCellValueFactory(cell -> cell.getValue().getODtmax());

    colUltagg = new TableColumn<>("Data Reg.");
    colUltagg.setCellValueFactory(cell -> cell.getValue().getOUltagg());

    tblvFiles.getColumns().addAll(colId, colName, colRelDir, colCardHold, colSize, colQtaRecs, colDtmin, colDtmax, colUltagg);

    tblvFiles.setRowFactory(row -> new TableRow<ImpFile>() {
      @Override
      public void updateItem(ImpFile item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
          setStyle("");
          return;
        }
        String cssForegSty = "-fx-text-fill: black;";
        StringBuilder cssBackgSty = new StringBuilder();
        var idf = item.getId();
        if (null == idf && !isSelected()) {
          //          System.out.printf("initTbl:sel(%s) class(%s) .style(%s)\n", //
          //              isSelected() ? "X" : "", //
          //              getStyleClass(), //
          //              getStyle());
          cssBackgSty //
              .append(cssForegSty) //
              .append("-fx-background-color: ") //
              .append(styRowZeroRecs) //
              .append(";");
          setStyle(cssBackgSty.toString());
        } else
          setStyle("");
      }
    });
  }

  private void initTblLogs() {
    tblLogs.setPlaceholder(new Label("Nessun messaggio da mostrare" + ""));
    tblLogs.setFixedCellSize(21.0);
    tblLogs.setRowFactory(row -> new TableRow<Log4jRow>() {
      @Override
      public void updateItem(Log4jRow item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
          setStyle("");
          return;
        }
        String cssSty = "-fx-background-color: ";
        Level tip = item.getLevel();
        StandardLevel lev = tip.getStandardLevel();
        switch (lev) {
          case TRACE:
            cssSty += "beige";
            break;
          case DEBUG:
            cssSty += "silver";
            break;
          case INFO:
            cssSty = "";
            break;
          case WARN:
            cssSty += "coral";
            break;
          case ERROR:
            cssSty += "hotpink";
            break;
          case FATAL:
            cssSty += "deeppink";
            break;
          default:
            cssSty = "";
            break;
        }
        setStyle(cssSty);
      }
    });

    colTime.setMaxWidth(80.);
    colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
    double vv = props.getDoubleProperty(CSZ_COL_time);
    if (vv > 0)
      colTime.setPrefWidth(vv);
    colLev.setMaxWidth(60.0);
    colLev.setCellValueFactory(new PropertyValueFactory<>("level"));
    vv = props.getDoubleProperty(CSZ_COL_leve);
    if (vv > 0)
      colLev.setPrefWidth(vv);
    colMsg.setCellValueFactory(new PropertyValueFactory<>("message"));
    vv = props.getDoubleProperty(CSZ_COL_mesg);
    if (vv > 0)
      colMsg.setPrefWidth(vv);
    cbLevelMin.getItems().addAll(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL);
    cbLevelMin.getSelectionModel().select(levelMin);
    // questa
    tblLogs.getItems()
        .addListener((ListChangeListener<Log4jRow>) s -> Platform.runLater(() -> tblLogs.scrollTo(s.getList().size() - 1)));
  }

  @FXML
  void btCercaClick(ActionEvent event) {
    String szMsg = null;
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    Stage stage = mainApp.getPrimaryStage();
    DirectoryChooser filChoose = new DirectoryChooser();
    // imposto la dir precedente (se c'e')
    AppProperties props = mainApp.getProps();
    String szLastDir = props.getLastDir();
    if (szLastDir != null) {
      File fi = new File(szLastDir);
      if (fi.exists())
        filChoose.setInitialDirectory(fi);
    }

    File dirScelto = filChoose.showDialog(stage);
    if (dirScelto != null) {
      settaFileIn(dirScelto.toPath(), true, false);
    } else {
      szMsg = "Non hai scelto nessun file !!";
      s_log.warn(szMsg);
      messageDialog(AlertType.WARNING, szMsg);
    }
  }

  @FXML
  public Object premutoTasto(KeyEvent p_e) {
    // System.out.printf("LoadAassController.premutoTasto(%s)\n", p_e.toString());
    KeyCode key = p_e.getCode();
    switch (key) {
      case ENTER:
      case F5:
        Path pth = Paths.get(txDirExports.getText());
        settaFileIn(pth, false, true);
        break;
      default:
        break;
    }
    return null;
  }

  @FXML
  void onEnterDirCSV(ActionEvent event) {
    String szMsg = null;
    String szCsvDir = txDirExports.getText();
    if (szCsvDir == null || szCsvDir.length() < 3) {
      szMsg = String.format("Il direttorio \"%s\" non e' valido", szCsvDir);
      messageDialog(AlertType.ERROR, szMsg);
      return;
    }
    Path fi = null;
    try {
      fi = Paths.get(szCsvDir);
      if ( !Files.exists(fi, LinkOption.NOFOLLOW_LINKS)) {
        szMsg = String.format("Non trovo il direttorio \"%s\" oppure non e' valido", szCsvDir);
        messageDialog(AlertType.ERROR, szMsg);
        return;
      }
    } catch (Exception e) {
      szMsg = String.format("Non trovo il file \"%s\" oppure non e' valido", szCsvDir);
      messageDialog(AlertType.ERROR, szMsg);
    }
    if (fi != null) {
      settaFileIn(fi);
    }
  }

  @FXML
  void mnuRescanDirs(ActionEvent event) {
    reloadListFilesCSV();
  }

  @FXML
  void mnuCheckFiles(ActionEvent event) {
    checkPresenceFilesCSV();
  }

  @FXML
  void mnuExitClick(ActionEvent event) {
    Platform.exit();
  }

  @FXML
  void mnuConfOpzioniClick(ActionEvent event) {
    // System.out.println("LoadBancaController.mnuConfOpzioniClick()");

    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    Stage primaryStage = mainApp.getPrimaryStage();

    URL url = getClass().getResource(ConfOpzioniController.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(ConfOpzioniController.CSZ_FXMLNAME);
    Parent radice;
    cntrlConfOpz = null;
    try {
      FXMLLoader fxmlLoad = new FXMLLoader(url);
      //      radice = FXMLLoader.load(url);
      radice = fxmlLoad.load();
      cntrlConfOpz = fxmlLoad.getController();
    } catch (IOException e) {
      s_log.error("Errore caricamento FXML {}", ConfOpzioniController.CSZ_FXMLNAME, e);
      return;
    }

    Stage stageViewConf = new Stage();
    Scene scene = new Scene(radice, 300, 240);
    stageViewConf.setScene(scene);
    stageViewConf.setWidth(800);
    stageViewConf.setHeight(600);
    stageViewConf.initOwner(primaryStage);
    stageViewConf.initModality(Modality.APPLICATION_MODAL);
    stageViewConf.setTitle("Gestione delle Opzioni di Import files CSV");
    stageViewConf.setX(20.);
    stageViewConf.setY(20.);
    // verifica che nel FXML ci sia la dichiarazione:
    // <userData> <fx:reference source="controller" /> </userData>
    if (cntrlConfOpz != null) {
      cntrlConfOpz.setMyScene(scene);
      cntrlConfOpz.initApp(props);
    }
    stageViewConf.show();
  }

  @FXML
  void mnuConfMostraDatiClick(ActionEvent event) {
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    Stage primaryStage = mainApp.getPrimaryStage();

    URL url = getClass().getResource(ResultView.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(ResultView.CSZ_FXMLNAME);
    Parent radice;
    cntrResultView = null;
    FXMLLoader fxmlLoad = new FXMLLoader(url);
    try {
      // radice = FXMLLoader.load(url);
      radice = fxmlLoad.load();
      cntrResultView = fxmlLoad.getController();
    } catch (IOException e) {
      s_log.error("Errore caricamento FXML {}", ResultView.CSZ_FXMLNAME, e);
      return;
    }
    //    Node nod = radice;
    //    do {
    //      controller = (ResultView) nod.getProperties().get("refToCntrl");
    //      nod = nod.getParent();
    //    } while (controller == null && nod != null);

    Stage stageResults = new Stage();
    Scene scene = new Scene(radice, 600, 440);
    stageResults.setScene(scene);
    stageResults.setWidth(800);
    stageResults.setHeight(600);
    stageResults.initOwner(primaryStage);
    stageResults.initModality(Modality.NONE);
    stageResults.setTitle("Visualizzazione dei dati del DB");
    // verifica che nel FXML ci sia la dichiarazione:
    // <userData> <fx:reference source="controller" /> </userData>
    if (cntrResultView != null) {
      cntrResultView.setMyScene(scene);
      cntrResultView.initApp(props);
    }
    stageResults.show();
  }

  @FXML
  public void mnuConfMostraCodStatClick(ActionEvent event) {
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    if (mainApp.isCodStatViewOpened()) {
      s_log.warn("La finestra dei codici statistici e' gia' aperta!");
      return;
    }
    Stage primaryStage = mainApp.getPrimaryStage();

    URL url = getClass().getResource(CodStatView.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(CodStatView.CSZ_FXMLNAME);
    Parent radice;
    cntrCodStatView = null;
    FXMLLoader fxmlLoad = new FXMLLoader(url);
    try {
      // radice = FXMLLoader.load(url);
      radice = fxmlLoad.load();
      cntrCodStatView = fxmlLoad.getController();
    } catch (IOException e) {
      s_log.error("Errore caricamento FXML {}", CodStatView.CSZ_FXMLNAME, e);
      return;
    }

    Stage stageResults = new Stage();
    Scene scene = new Scene(radice, 600, 440);
    stageResults.setScene(scene);
    stageResults.setWidth(800);
    stageResults.setHeight(600);
    stageResults.initOwner(primaryStage);
    stageResults.initModality(Modality.NONE);
    stageResults.setTitle("Visualizzazione Codici Statistici");
    if (cntrCodStatView != null) {
      cntrCodStatView.setMyScene(scene);
      cntrCodStatView.initApp(props);
    }
    stageResults.show();
  }

  @FXML
  public void mnuConfMostraGuessCodStatClick(ActionEvent event) {
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    if (mainApp.isGuessCodStatViewOpened()) {
      s_log.warn("La finestra associazione codici statistici e' gia' aperta!");
      return;
    }
    Stage primaryStage = mainApp.getPrimaryStage();

    URL url = getClass().getResource(GuessCodStatView.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(GuessCodStatView.CSZ_FXMLNAME);
    Parent radice;
    cntrGuessCodStatView = null;
    FXMLLoader fxmlLoad = new FXMLLoader(url);
    try {
      radice = fxmlLoad.load();
      cntrGuessCodStatView = fxmlLoad.getController();
    } catch (IOException e) {
      s_log.error("Errore caricamento FXML {}", GuessCodStatView.CSZ_FXMLNAME, e);
      return;
    }

    Stage stageResults = new Stage();
    Scene scene = new Scene(radice, 600, 440);
    stageResults.setScene(scene);
    stageResults.setWidth(800);
    stageResults.setHeight(600);
    stageResults.initOwner(primaryStage);
    stageResults.initModality(Modality.NONE);
    stageResults.setTitle("Indovina Codici Statistici");
    if (cntrGuessCodStatView != null) {
      cntrGuessCodStatView.setMyScene(scene);
      cntrGuessCodStatView.initApp(props);
    }
    stageResults.show();
  }

  @FXML
  void mnuMostraViewContantiClick(ActionEvent event) {
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    Stage primaryStage = mainApp.getPrimaryStage();

    URL url = getClass().getResource(ViewContanti.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(ViewContanti.CSZ_FXMLNAME);
    Parent radice;
    cntrViewContanti = null;
    FXMLLoader fxmlLoad = new FXMLLoader(url);
    try {
      radice = fxmlLoad.load();
      cntrViewContanti = fxmlLoad.getController();
    } catch (IOException e) {
      s_log.error("Errore caricamento FXML {}", ViewContanti.CSZ_FXMLNAME, e);
      return;
    }
    Stage stageViewCont = new Stage();
    Scene scene = new Scene(radice, 600, 440);
    stageViewCont.setScene(scene);
    stageViewCont.setWidth(800);
    stageViewCont.setHeight(600);
    stageViewCont.initOwner(primaryStage);
    stageViewCont.initModality(Modality.NONE);
    stageViewCont.setTitle("Gestione dei contanti su DB");
    // verifica che nel FXML ci sia la dichiarazione:
    // <userData> <fx:reference source="controller" /> </userData>
    if (cntrViewContanti != null) {
      cntrViewContanti.setMyScene(scene);
      cntrViewContanti.initApp(props);
    }
    stageViewCont.show();
  }

  @FXML
  public void mnuhAbout() {
    String szMsg = "Versione dell'applicazione\n" + Versione.getVersionEx();
    var mainapp = LoadBancaMainApp.getInst();
    mainapp.msgBox(szMsg, AlertType.INFORMATION, LoadBancaMainApp.CSZ_MAIN_ICON);
  }

  @FXML
  void btConvCSV_Click(ActionEvent event) {
    eseguiConversioneRunTask();
  }

  private void eseguiConversioneRunTask() {
    qtaActiveTasks = 0;
    ObservableList<ImpFile> sels = tblvFiles.getSelectionModel().getSelectedItems();
    s_log.debug("conversione di {} CSV in background con {} threads", sels.size(), cntrlr.getQtaThreads());
    ExecutorService backGrService = Executors.newFixedThreadPool(cntrlr.getQtaThreads());
    btConvCSV.setDisable(true);
    for (ImpFile impf : sels) {
      CsvImportBanca csvimp = new CsvImportBanca();
      try {
        csvimp.addPropertyChangeListener(this);
        csvimp.setCsvFile(impf.fullPath(cntrlr.getLastDir()));
        // lbProgressione.textProperty().bind(csvimp.messageProperty());
        // prgrb.setProgress(0);
        prgrb.progressProperty().unbind();
        prgrb.progressProperty().bind(csvimp.progressProperty());

        csvimp.setOnRunning(ev -> {
          // System.out.println("LoadBancaController.eseguiConversioneRunTask() RUNNING");
          setSemafore(1);
        });
        csvimp.setOnSucceeded(ev -> {
          // System.out.println("LoadBancaController.eseguiConversioneRunTask() SUCCEDED");
          setSemafore(0);
          //          try {
          //            csvimp.close();
          //          } catch (Exception e) {
          //            e.printStackTrace();
          //          }
          s_log.info("Fine del Task Background per {}", impf.toString());
        });
        csvimp.setOnFailed(ev -> {
          setSemafore(0);
          //          try {
          //            csvimp.close();
          //          } catch (Exception e) {
          //            e.printStackTrace();
          //          }
          Throwable ex = ev.getSource().getException();
          s_log.warn("ERRORE Conversione RunTask per {} !! FAILED !!, err={}", impf.toString(), ex.getMessage(), ex);
        });
        DBConn connSQL = LoadBancaMainApp.getInst().getConnSQL();
        csvimp.setConnSql(connSQL);
        //        prgrb.setProgress(0);
        //        prgrb.progressProperty().unbind();
        //        prgrb.progressProperty().bind(csvimp.progressProperty());
        backGrService.execute(csvimp);
      } catch (Exception e) {
        lbProgressione.textProperty().unbind();
        s_log.error("Errore {} file {}", e.getMessage(), impf.toString(), e);
      }
    }
    backGrService.shutdown();
    reloadListFilesCSV();
  }

  private synchronized void setSemafore(int nTask) {
    // nTask : 1 - start, 0 - finish
    switch (nTask) {
      case 0:
        if (qtaActiveTasks > 0)
          qtaActiveTasks--;
        else
          System.err.println("Active Tasks < 0 !");
        if (qtaActiveTasks == 0) {
          Platform.runLater(() -> {
            getStage().getScene().setCursor(Cursor.DEFAULT);
            btConvCSV.setDisable(false);
          });
        }
        break;
      case 1:
        qtaActiveTasks++;
        if (qtaActiveTasks == 1) {
          Platform.runLater(() -> {
            getStage().getScene().setCursor(Cursor.WAIT);
            btConvCSV.setDisable(true);
          });
        }
        break;
    }
  }

  public void messageDialog(AlertType typ, String p_msg) {
    LoadBancaMainApp.getInst().messageDialog(typ, p_msg);
  }

  private Path settaFileIn(Path p_fi) {
    return settaFileIn(p_fi, true, false);
  }

  /**
   * Imposta il {@link #txDirFatt} col valore passato a meno che non sia
   * specificato p_setTx = False. Inoltre se specificato bForce = True
   * indipendentemente dal valore precedente di {@link #pthDirCSV} comunque
   * ricarica l'elenco dei files nella listView
   *
   * @param p_fi
   *          path al nuovo dir delle fatture
   * @param p_setTx
   *          se aggiornare {@link #txDirFatt} col nuovo valore
   * @param bForce
   *          ricarica l'elenco dei files nella listView
   * @return
   */
  private Path settaFileIn(Path p_fi, boolean p_setTx, boolean bForce) {
    //    if (p_fi == null)
    //      return p_fi;
    //    if ( !bForce)
    //      if (pthDirCSV != null && pthDirCSV.compareTo(p_fi) == 0)
    //        return pthDirCSV;
    //    String szFiin = p_fi.toString();
    //    props.setLastDir(szFiin);
    p_fi = cntrlr.assegnaLastDir(p_fi, bForce);
    impostaTitolo();
    if (p_setTx)
      txDirExports.setText(p_fi.toString());
    // pthDirCSV = p_fi;
    reloadListFilesCSV();
    return p_fi;
  }

  private void reloadListFilesCSV() {
    s_log.debug("Ricarico la lista files CSV da: {}", cntrlr.getLastDir());
    ObservableList<ImpFile> liFilesCSV = cntrlr.getContCsv().loadListFiles();
    tblvFiles.getItems().clear();
    tblvFiles.getItems().addAll(liFilesCSV);
    colorizeTblView();

    MenuItem mi1 = new MenuItem("Import");
    mi1.setOnAction((ActionEvent ev) -> {
      btConvCSV_Click(ev);
    });

    MenuItem mi2 = new MenuItem("Vedi Documento");
    mi2.setOnAction((ActionEvent ev) -> {
      showFileDoc();
    });

    MenuItem mi3 = new MenuItem("Vai sul dir.");
    mi3.setOnAction((ActionEvent ev) -> {
      vaiAlDir();
    });

    MenuItem mi4 = new MenuItem("Mostra Sovrapposizioni");
    mi4.setOnAction((ActionEvent ev) -> {
      mnuSovrapposizioniClick(ev);
    });

    MenuItem mi5 = new MenuItem("Elimina Registrazioni");
    mi5.setOnAction((ActionEvent ev) -> {
      eliminaRegistrazioni();
    });

    ContextMenu menu = new ContextMenu();
    menu.getItems().addAll(mi1, mi2, mi3, mi4, new SeparatorMenuItem(), mi5);
    // liBanca.setContextMenu(menu);
    tblvFiles.setContextMenu(menu);
    tblvFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    btConvCSV.setDisable(false);

    s_log.debug("Ricaricata lista files dal dir \"{}\"", cntrlr.getLastDir().toString());
  }

  private void checkPresenceFilesCSV() {
    s_log.debug("Verifica presenza files registrati nel DB sul dir");
    List<ImpFile> li = cntrlr.getContCsv().controllaFilesAssenti();
    if (null == li || li.size() == 0) {
      s_log.info("Tutti i file registrati sono presenti");
      return;
    }
    Alert dial = new Alert(AlertType.INFORMATION);
    dial.setTitle("Controllo sui files");
    dial.setHeaderText("Elenco files registrati nel DB ma mancanti sul File System!");
    String sz = li.stream().map(s -> s.getFileName().toString()).collect(Collectors.joining("\n"));
    dial.setContentText(sz);
    dial.setResizable(true);

    ButtonType btClear = new ButtonType("Elimina Regs.");
    ButtonType btOk = new ButtonType("Ok");
    dial.getButtonTypes().setAll(btClear, btOk);
    Optional<ButtonType> res = dial.showAndWait();
    if (res.get() == btClear)
      cntrlr.getContCsv().cancellaRegsFiles(li);
  }

  private void colorizeTblView() {
    final String rAlign = "-fx-alignment: center-right;";
    // Default cell factory provides text field for editing and converts text in text field to int.
    Callback<TableColumn<ImpFile, String>, TableCell<ImpFile, String>> defaultCellFactory = TextFieldTableCell.forTableColumn();
    // Cell factory implementation that uses default cell factory above, and augments the implementation
    Callback<TableColumn<ImpFile, String>, TableCell<ImpFile, String>> cellFactory = col -> {
      TableCell<ImpFile, String> cell = defaultCellFactory.call(col);
      cell.itemProperty().addListener((obs, oldValue, newValue) -> {
        String value = "-fx-alignment: center-right;";
        StringBuilder szCss = new StringBuilder().append(value);
        if ( !Utils.isValue(newValue))
          szCss.append(" ").append(rAlign);
        cell.setStyle(szCss.toString());
      });
      return cell;
    };
    colId.setCellFactory(cellFactory);
    colSize.setStyle(rAlign);
    colQtaRecs.setStyle(rAlign);
    colQtaRecs.setStyle(rAlign);
  }

  private void showFileDoc() {
    // Path it = liBanca.getSelectionModel().getSelectedItem();
    ImpFile imf = tblvFiles.getSelectionModel().getSelectedItem();
    Path it = imf.fullPath(cntrlr.getLastDir());
    // System.out.println("Ctx menu: path="+it);
    try {
      if (Desktop.isDesktopSupported()) {
        s_log.info("Apro il documento {}", imf.getFileName());
        Desktop.getDesktop().open(it.toFile());
      } else {
        s_log.error("Desktop not supported");
      }
    } catch (IOException e) {
      s_log.error("Desktop launch error:{}", e.getMessage(), e);
    }
  }

  private void vaiAlDir() {
    ImpFile imf = tblvFiles.getSelectionModel().getSelectedItem();
    Path pth = cntrlr.getLastDir();
    String pth2 = imf.getRelDir();
    Path padre = Paths.get(pth.toString(), pth2);
    // System.out.printf("LoadBancaController.vaiAlDir(%s)\n", padre.toString());
    try {
      Desktop.getDesktop().open(padre.toFile());
    } catch (IOException e) {
      s_log.error("Errore apertura Explorer: {}", e.getMessage());
    }
  }

  @FXML
  void mnuSovrapposizioniClick(ActionEvent event) {
    ImpFile imf = tblvFiles.getSelectionModel().getSelectedItem();
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    if (null == imf || !imf.hasPeriodo()) {
      final String p_msg = "Devi selezionare un file per consultare le sue sovrapposizioni";
      s_log.warn(p_msg);
      mainApp.messageDialog(AlertType.WARNING, p_msg);
      return;
    }

    Stage primaryStage = mainApp.getPrimaryStage();

    URL url = getClass().getResource(SovrapposView.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(SovrapposView.CSZ_FXMLNAME);
    Parent radice;
    cntrViewSovrapp = null;
    try {
      FXMLLoader fxmlLoad = new FXMLLoader(url);
      radice = fxmlLoad.load();
      cntrViewSovrapp = fxmlLoad.getController();
      int qtaFils = cntrViewSovrapp.setImpFileStart(imf);
      if (qtaFils <= 1) {
        mainApp.messageDialog(AlertType.WARNING, "Non ci sono sufficienti periodi da analizzare");
        return;
      }
    } catch (IOException e) {
      s_log.error("Errore caricamento FXML {}", SovrapposView.CSZ_FXMLNAME, e);
      return;
    }

    Stage stageViewsovrapp = new Stage();
    Scene scene = new Scene(radice, 300, 240);
    stageViewsovrapp.setScene(scene);
    stageViewsovrapp.setWidth(800);
    stageViewsovrapp.setHeight(600);
    stageViewsovrapp.initOwner(primaryStage);
    stageViewsovrapp.initModality(Modality.NONE);
    stageViewsovrapp.setTitle("Visualizzazione delle sovrapposizioni dei files CSV");
    stageViewsovrapp.setX(20.);
    stageViewsovrapp.setY(20.);
    if (cntrViewSovrapp != null) {
      cntrViewSovrapp.setMyScene(scene);
      cntrViewSovrapp.initApp(props);
    }
    stageViewsovrapp.show();
  }

  private void eliminaRegistrazioni() {
    ImpFile imf = tblvFiles.getSelectionModel().getSelectedItem();
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    Optional<ButtonType> ret = mainApp.messageDialog(AlertType.CONFIRMATION,
        "Sei sicuro di voler eliminare le registrazioni di <br/><b>" + imf.getFileName() + "</b>");
    if (ret.isEmpty() || ret.get() != ButtonType.OK)
      return;
    // System.out.printf("LoadBancaController.eliminaRegistrazioni(%s)\n", imf.getFileName());
    s_log.warn("Elimino le registrazioini di {}", imf.getFileName());
    cntrlr.getContCsv().cancellaRegsFiles(Arrays.asList(new ImpFile[] { imf }));
    imf.garbleName(cntrlr.getLastDir());
    reloadListFilesCSV();
  }

  public Stage getStage() {
    Stage stg = LoadBancaMainApp.getInst().getPrimaryStage();
    return stg;
  }

  @FXML
  void btClearMsgClick(ActionEvent event) {
    // System.out.println("ReadFattHTMLController.btClearMsgClick()");
    tblLogs.getItems().clear();
    if (m_liMsgs != null)
      m_liMsgs.clear();
    m_liMsgs = null;
  }

  @FXML
  void cbLevelMinSel(ActionEvent event) {
    levelMin = cbLevelMin.getSelectionModel().getSelectedItem();
    // System.out.println("ReadFattHTMLController.cbLevelMinSel():" + levelMin.name());
    tblLogs.getItems().clear();
    if (m_liMsgs == null || m_liMsgs.size() == 0)
      return;
    // List<Log4jRow> li = m_liMsgs.stream().filter(s -> s.getLevel().isInRange(Level.FATAL, levelMin )).toList(); // !s.getLevel().isLessSpecificThan(levelMin)).toList();
    List<Log4jRow> li = m_liMsgs.stream().filter(s -> s.getLevel().intLevel() <= levelMin.intLevel()).toList();
    tblLogs.getItems().addAll(li);
  }

  @Override
  public void changeSkin() {
    LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    URL url = mainApp.getUrlCSS();
    Scene myScene = getStage().getScene();
    if (null == url || null == myScene)
      return;
    myScene.getStylesheets().clear();
    myScene.getStylesheets().add(url.toExternalForm());
    if (null != cntrResultView)
      cntrResultView.changeSkin();
    if (null != cntrViewContanti)
      cntrViewContanti.changeSkin();
    if (null != cntrlConfOpz)
      cntrlConfOpz.changeSkin();
    if (null != cntrCodStatView)
      cntrCodStatView.changeSkin();
  }

  @Override
  public void closeApp(AppProperties p_props) {
    p_props.setProperty(CSZ_LOG_LEVEL, levelMin.toString());
    double[] pos = spltPane.getDividerPositions();
    //    for (double dbl : pos)
    //      System.out.printf("LoadBancaController.closeApp(divp=%.4f)\n", dbl);
    String szPos = String.format("%.4f", pos[0]).replace(",", ".");
    p_props.setProperty(CSZ_SPLITPOS, szPos);
    double vv = colTime.getWidth();
    p_props.setProperty(CSZ_COL_time, Integer.valueOf((int) vv));
    vv = colLev.getWidth();
    p_props.setProperty(CSZ_COL_leve, Integer.valueOf((int) vv));
    vv = colMsg.getWidth();
    p_props.setProperty(CSZ_COL_mesg, Integer.valueOf((int) vv));
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    var sz = evt.getPropertyName();
    var val = evt.getNewValue();
    double currProgressNo = 0;
    switch (sz) {

      case CsvImportBanca.EVT_SIZEDTS:
        endProgressNo = (double) val;
        currProgressNo = 0;
        Platform.runLater(() -> lbProgressione.setText(sz));
        break;

      case CsvImportBanca.EVT_DTSROW:
        currProgressNo = (double) val;
        if (currProgressNo % 7 == 0) {
          double dbl = endProgressNo * 2 / currProgressNo * 100.;
          String sz2 = String.format("Csv:%.0f%%", dbl);
          Platform.runLater(() -> lbProgressione.setText(sz2));
        }
        break;

      case CsvImportBanca.EVT_ENDDTSROW:
        Platform.runLater(() -> lbProgressione.setText("50%"));
        break;

      case CsvImportBanca.EVT_SAVEDBROW:
        currProgressNo = (double) val + endProgressNo;
        if (currProgressNo % 7 == 0) {
          double dbl = currProgressNo / (endProgressNo * 2.) * 100.;
          String sz2 = String.format("su DB:%.0f%%", dbl);
          Platform.runLater(() -> lbProgressione.setText(sz2));
        }
        break;

      case CsvImportBanca.EVT_ENDSAVEDB:
        Platform.runLater(() -> lbProgressione.setText("Done 100%"));
        break;

    }
  }

}
