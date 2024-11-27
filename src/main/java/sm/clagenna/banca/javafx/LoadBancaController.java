package sm.clagenna.banca.javafx;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sm.clagenna.banca.dati.CsvImportBanca;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ILog4jReader;
import sm.clagenna.stdcla.utils.Log4jRow;
import sm.clagenna.stdcla.utils.MioAppender;

public class LoadBancaController implements Initializable, ILog4jReader, IStartApp {
  private static final Logger s_log            = LogManager.getLogger(LoadBancaController.class);
  public static final String  CSZ_FXMLNAME     = "BancaJavaFX.fxml";
  private static final String CSZ_LOG_LEVEL    = "logLevel";
  private static final String CSZ_SPLITPOS     = "splitpos";
  private static final String CSZ_COL_time     = "log_time";
  private static final String CSZ_COL_leve     = "log_lev";
  private static final String CSZ_COL_mesg     = "log_mesg";
  public static final String  CSZ_FILTER_FILES = "filter_files";

  private List<Log4jRow> m_liMsgs;

  @FXML
  private MenuItem mnuGEstDati;
  @FXML
  private MenuItem mnuGEstOpzioni;

  @FXML
  private TextField                     txDirExports;
  @FXML
  private Button                        btCercaDir;
  @FXML
  private SplitPane                     spltPane;
  @FXML
  private ListView<Path>                liBanca;
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

  private Path                  pthDirCSV;
  private AppProperties         props;
  private ConfOpzioniController cntrlConfOpz;
  private int                   qtaActiveTasks;
  private ResultView            cntrResultView;
  private ViewContanti          cntrViewContanti;

  public LoadBancaController() {
    //
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    MioAppender.setLogReader(this);
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
    getStage().setTitle("Caricamento degli Export CSV dalle Banche su DB");
    // vedi: https://stackoverflow.com/questions/27160951/javafx-open-another-fxml-in-the-another-window-with-button
    getStage().onCloseRequestProperty().setValue(e -> Platform.exit());
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

    // -------- combo level -------
    if (props != null) {
      String sz = props.getProperty(CSZ_LOG_LEVEL);
      if (sz != null)
        levelMin = Level.toLevel(sz);
    }
    String szPos = props.getProperty(CSZ_SPLITPOS);
    if (szPos != null) {
      double dbl = Double.valueOf(szPos);
      spltPane.setDividerPositions(dbl);
    }
    initTblLogs();
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
    //    tblView.getItems().addListener(new ListChangeListener<Log4jRow>(){
    //
    //      @Override
    //      public void onChanged(ListChangeListener.Change<? extends Log4jRow> c) {
    //          // tblView.scrollTo(c.getList().size()-1);
    //        Platform.runLater( () -> tblView.scrollTo(c.getList().size()-1) );
    //      }
    //
    //  });
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
  void btConvCSV_Click(ActionEvent event) {
    // System.out.println("LoadBancaController.btConvPDF()");
    eseguiConversioneRunTask();
  }

  private void eseguiConversioneRunTask() {
    qtaActiveTasks = 0;
    ObservableList<Path> sels = liBanca.getSelectionModel().getSelectedItems();
    DataController data = DataController.getInst();
    s_log.debug("conversione di {} CSV in background con {} threads", sels.size(), data.getQtaThreads());
    ExecutorService backGrService = Executors.newFixedThreadPool(data.getQtaThreads());
    btConvCSV.setDisable(true);
    for (Path pth : sels) {
      try {
        CsvImportBanca cvsimp = new CsvImportBanca();
        cvsimp.setCsvFile(pth);
        lbProgressione.textProperty().bind(cvsimp.messageProperty());
        cvsimp.setOnRunning(ev -> {
          // System.out.println("LoadBancaController.eseguiConversioneRunTask() RUNNING");
          setSemafore(1);
        });
        cvsimp.setOnSucceeded(ev -> {
          // System.out.println("LoadBancaController.eseguiConversioneRunTask() SUCCEDED");
          setSemafore(0);
          s_log.info("Fine del Task Background per {}", pth.toString());
        });
        cvsimp.setOnFailed(ev -> {
          setSemafore(0);
          Throwable ex = ev.getSource().getException();
          s_log.warn("ERRORE Conversione RunTask per {} !! FAILED !!, err={}", pth.toString(), ex.getMessage(), ex);
        });
        DBConn connSQL = LoadBancaMainApp.getInst().getConnSQL();
        cvsimp.setConnSql(connSQL);
        backGrService.execute(cvsimp);
      } catch (Exception e) {
        lbProgressione.textProperty().unbind();
        s_log.error("Errore conversione PDF {}", pth.toString(), e);
      }
    }
    backGrService.shutdown();
    // btConvCSV.setDisable(false);
    // s_log.debug("Fine conversione in background");
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
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              getStage().getScene().setCursor(Cursor.DEFAULT);
              btConvCSV.setDisable(false);
            }
          });
        }
        break;
      case 1:
        qtaActiveTasks++;
        if (qtaActiveTasks == 1) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              getStage().getScene().setCursor(Cursor.WAIT);
              btConvCSV.setDisable(true);
            }
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
    if (p_fi == null)
      return p_fi;
    if ( !bForce)
      if (pthDirCSV != null && pthDirCSV.compareTo(p_fi) == 0)
        return pthDirCSV;
    String szFiin = p_fi.toString();
    props.setLastDir(szFiin);
    if (p_setTx)
      txDirExports.setText(szFiin);
    pthDirCSV = p_fi;
    if ( !Files.exists(pthDirCSV, LinkOption.NOFOLLOW_LINKS)) {
      s_log.error("Il path \"{}\" non esiste !", pthDirCSV.toString());
      return p_fi;
    }
    reloadListFilesCSV();
    return p_fi;
  }

  private void reloadListFilesCSV() {
    List<Path> result = null;
    String fltrFiles = props.getProperty(CSZ_FILTER_FILES);
    if (null == fltrFiles)
      fltrFiles = "wise,estra";

    String szGlobMatch = creaGlobMatch(fltrFiles);
    // String szGlobMatch = "glob:*:/**/{estra*,wise*}*.csv";
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(szGlobMatch);
    try (Stream<Path> walk = Files.walk(pthDirCSV)) {
      result = walk.filter(p -> !Files.isDirectory(p)) //
          // not a directory
          // .map(p -> p.toString().toLowerCase()) // convert path to string
          .filter(f -> matcher.matches(f)) // check end with
          .collect(Collectors.toList()); // collect all matched to a List
    } catch (IOException e) {
      e.printStackTrace();
    }

    // DefaultListModel<Path> l1 = new DefaultListModel<>();
    ObservableList<Path> li = FXCollections.observableArrayList(result);
    liBanca.setItems(li);
    liBanca.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    // listViewColorize();

    MenuItem mi1 = new MenuItem("Vedi Fattura");
    mi1.setOnAction((ActionEvent ev) -> {
      showPdfDoc();
    });
    ContextMenu menu = new ContextMenu();
    menu.getItems().add(mi1);
    liBanca.setContextMenu(menu);
    btConvCSV.setDisable(false);

    s_log.debug("Ricaricata lista files dal dir \"{}\"", pthDirCSV.toString());
  }

  private String creaGlobMatch(String fltr) {
    String arr[] = fltr.split(",");
    StringBuilder fils = new StringBuilder();
    String vir = "";
    // String prefix = "estratt";
    for (String pat : arr) {
      fils.append(String.format("%s%s*", vir, pat));
      vir = ",";
    }
    return String.format("glob:*:/**/{%s}*.{csv,xls,xlsx}", fils.toString());
  }

  private void showPdfDoc() {
    Path it = liBanca.getSelectionModel().getSelectedItem();
    // System.out.println("Ctx menu: path="+it);
    try {
      if (Desktop.isDesktopSupported()) {
        s_log.info("Apro lettore PDF per {}", it.toString());
        Desktop.getDesktop().open(it.toFile());
      } else {
        s_log.error("Desktop not supported");
      }
    } catch (IOException e) {
      s_log.error("Desktop PDF launch error:" + e.getMessage(), e);
    }
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
  }

  @Override
  public void closeApp(AppProperties p_props) {
    p_props.setProperty(CSZ_LOG_LEVEL, levelMin.toString());
    double[] pos = spltPane.getDividerPositions();
    String szPos = String.format("%.4f", pos[0]).replace(",", ".");
    p_props.setProperty(CSZ_SPLITPOS, szPos);
    double vv = colTime.getWidth();
    p_props.setProperty(CSZ_COL_time, Integer.valueOf((int) vv));
    vv = colLev.getWidth();
    p_props.setProperty(CSZ_COL_leve, Integer.valueOf((int) vv));
    vv = colMsg.getWidth();
    p_props.setProperty(CSZ_COL_mesg, Integer.valueOf((int) vv));
  }

}
