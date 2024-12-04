package prova.javafx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import sm.clagenna.banca.dati.CsvImportBanca;
import sm.clagenna.banca.dati.ImpFile;
import sm.clagenna.banca.javafx.LoadBancaMainApp;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.Utils;

public class ProvaProgrBarCla extends Application {
  private static final Logger s_log = LogManager.getLogger(ProvaProgrBarCla.class);

  private static final String CSZ_START_DIR    = "F:\\Google Drive\\gennari\\Banche";
  private static final String CSZ_FXMLNAME     = "ProvaProgrBarCla.fxml";
  private static final String CSZ_MAIN_APP_CSS = "ProvaProgrBarCla.css";

  @FXML
  private TextField                    txQta;
  @FXML
  private Button                       btStart;
  @FXML
  private Button                       btStop;
  @FXML
  private TableView<ImpFile>           tblvFiles;
  private TableColumn<ImpFile, String> colId;
  private TableColumn<ImpFile, String> colName;
  private TableColumn<ImpFile, String> colRelDir;
  private TableColumn<ImpFile, Number> colSize;
  private TableColumn<ImpFile, Number> colQtaRecs;
  private TableColumn<ImpFile, String> colDtmin;
  private TableColumn<ImpFile, String> colDtmax;
  private TableColumn<ImpFile, String> colUltagg;
  @FXML
  private ProgressBar                  progb;

  private Stage            primStage;
  private ProvaProgrBarCla controller;
  private List<ImpFile>    elenco;

  private int qtaActiveTasks;

  public ProvaProgrBarCla() {
    // System.out.printf("NEW   ProvaProgrBarCla.ProvaProgrBarCla((%d)\n", this.hashCode() % 1023);
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primStage = primaryStage;
    // System.out.printf("ProvaProgrBarCla.start(%d)\n", this.hashCode() % 1023);
    caricaFXML();
    primStage.show();
    controller.initTblFilesCSV();
    controller.initData();
  }

  @Override
  public void init() throws Exception {
    super.init();
    // System.out.printf("ProvaProgrBarCla.init(%d)\n", this.hashCode() % 1023);
  }

  private void caricaFXML() throws IOException {
    primStage.setTitle("Provo la ProgressBar e colori TableView");
    primStage.onCloseRequestProperty().setValue(e -> Platform.exit());
    URL url = getClass().getResource(CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(CSZ_FXMLNAME);
    if (url == null)
      throw new FileNotFoundException(String.format("Non trovo reource %s", CSZ_FXMLNAME));

    // Parent radice = FXMLLoader.load(url);
    FXMLLoader loader = new FXMLLoader(url);
    Parent radice = loader.load();

    URL mainCSS = getClass().getResource(CSZ_MAIN_APP_CSS);
    if (null == mainCSS)
      mainCSS = getClass().getClassLoader().getResource(CSZ_MAIN_APP_CSS);
    // <a target="_blank" href="https://icons8.com/icon/Qd0k8d5D0tSe/invoice">Invoice</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
    //     primStage.getIcons().add(new Image(CSZ_MAIN_ICON));
    Scene scene = new Scene(radice, 725, 550);
    scene.getStylesheets().add(mainCSS.toExternalForm());
    primStage.setScene(scene);
    primStage.setOnShown(e -> sceneShowed(e));
    // questa è l'istanza di this.getClass() che riceve gli @FXML controls
    controller = (ProvaProgrBarCla) loader.getController();
  }

  private Object sceneShowed(WindowEvent e) {
    // System.out.printf("ProvaProgrBarCla.sceneShowed((%d)\n", this.hashCode() % 1023);
    return null;
  }

  /**
   * Questa funzione fa leva sui @FXML controls per cui deve essere chiamata
   * dalla istanza corretta. Le istanze in questo caso sono 2:
   * <ol>
   * <li>La prima è quella che <code>Application.launch(args)</code> crea per
   * chiamare la {@link #start(Stage)}</li>
   * <li>La seconda è quella creata con la <code>FXMLLoader.load(url);</code>
   * </li>
   * </ol>
   * Solo la 2) contiene i controls @FXML inizializzati.
   */
  @SuppressWarnings("unchecked")
  private void initTblFilesCSV() {
    // System.out.printf("LoadBancaController.initTblFilesCSV((%d)\n", this.hashCode() % 1023);
    tblvFiles.getColumns().clear();
    colId = new TableColumn<>("Id");
    colId.setCellValueFactory(cell -> cell.getValue().getOid());

    colName = new TableColumn<>("Nome");
    colName.setCellValueFactory(cell -> cell.getValue().getOFileName());

    colRelDir = new TableColumn<>("Path Rel.");
    colRelDir.setCellValueFactory(cell -> cell.getValue().getORelDir());

    colSize = new TableColumn<>("Dimensione");
    colSize.setCellValueFactory(cell -> cell.getValue().getOSize());

    colQtaRecs = new TableColumn<>("Qta .rows");
    colQtaRecs.setCellValueFactory(cell -> cell.getValue().getOQtarecs());

    colDtmin = new TableColumn<>("Data min.");
    colDtmin.setCellValueFactory(cell -> cell.getValue().getODtmin());

    colDtmax = new TableColumn<>("Data Reg.");
    colDtmax.setCellValueFactory(cell -> cell.getValue().getODtmax());

    colUltagg = new TableColumn<>("Data Reg.");
    colUltagg.setCellValueFactory(cell -> cell.getValue().getOUltagg());

    tblvFiles.getColumns().addAll(colId, colName, colRelDir, colSize, colQtaRecs, colDtmin, colDtmax, colUltagg);
  }

  private void initData() {
    txQta.setText(CSZ_START_DIR);
    ObservableList<ImpFile> li = loadListFiles();
    tblvFiles.getItems().clear();
    tblvFiles.getItems().addAll(li);
    tblvFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    colorizeTblView();
    btStop.setDisable(true);
  }

  private void colorizeTblView() {
    // Default cell factory provides text field for editing and converts text in text field to int.
    Callback<TableColumn<ImpFile, String>, TableCell<ImpFile, String>> defaultCellFactory = TextFieldTableCell.forTableColumn();
    // Cell factory implementation that uses default cell factory above, and augments the implementation
    Callback<TableColumn<ImpFile, String>, TableCell<ImpFile, String>> cellFactory = col -> {
      TableCell<ImpFile, String> cell = defaultCellFactory.call(col);
      cell.itemProperty().addListener((obs, oldValue, newValue) -> {
        //        String szNa = obs.getClass().getSimpleName();
        //        String ov = "ov=*null*";
        //        String nv = "nv=*null*";
        //        if (null != oldValue)
        //          ov = "ov=" + oldValue;
        //        if (null != newValue)
        //          nv = "nv=" + newValue;
        //        String stcl = cell.getStyle();
        //        System.out.printf("LoadBancaController.colorizeTblView(%s:%s:%s) style=%s\n", szNa, ov, nv, stcl);
        if ( !Utils.isValue(newValue))
          cell.setStyle("-fx-background-color: tomato;");
        else
          cell.setStyle("");

        //        if (newValue == null) {
        //          cell.setStyle("cell-selection-color: -fx-selection-bar ;");
        //        } else {
        //          System.out.println("LoadBancaController.colorizeTblView()");
        //          String formattedColor = formatColor(color);
        //          cell.setStyle("cell-selection-color: " + formattedColor + " ;");
        //        }
      });
      return cell;
    };
    // colName.setCellFactory(cellFactory);
    // colRelDir.setCellFactory(cellFactory);
    colId.setCellFactory(cellFactory);
  }

  @FXML
  public void txQtaLostFocusClick() {
    System.out.println("ProvaProgrBarCla.txQtaLostFocusClick()");
  }

  @FXML
  public void btStartClick() {
    // System.out.printf("ProvaProgrBarCla.btStartClick((%d)\n", this.hashCode() % 1023);
    progb.setProgress(0.);
    btStart.setDisable(true);
    btStop.setDisable(false);
    eseguiConversioneRunTask();
  }

  private void eseguiConversioneRunTask() {
    ObservableList<ImpFile> sels = tblvFiles.getSelectionModel().getSelectedItems();
    s_log.debug("conversione di {} CSV in background con {} threads", sels.size(), 1);
    ExecutorService backGrService = Executors.newFixedThreadPool(1);
    for (ImpFile impf : sels) {
      try {
        CsvImportBanca cvsimp = new CsvImportBanca();
        cvsimp.setSkipSaveDB(true);
        cvsimp.setCsvFile(impf.fullPath(Paths.get(CSZ_START_DIR)));
        progb.setProgress(0.);
        progb.progressProperty().unbind();
        progb.progressProperty().bind(cvsimp.progressProperty());
        cvsimp.setOnRunning(ev -> {
          setSemafore(1);
        });
        cvsimp.setOnSucceeded(ev -> {
          setSemafore(0);
          s_log.info("Fine del Task Background per {}", impf.toString());
        });
        cvsimp.setOnFailed(ev -> {
          setSemafore(0);
          Throwable ex = ev.getSource().getException();
          s_log.warn("ERRORE Conversione RunTask per {} !! FAILED !!, err={}", impf.toString(), ex.getMessage(), ex);
        });
        DBConn connSQL = LoadBancaMainApp.getInst().getConnSQL();
        cvsimp.setConnSql(connSQL);
        backGrService.execute(cvsimp);
      } catch (Exception e) {
        progb.progressProperty().unbind();
        s_log.error("Errore conversione PDF {}", impf.toString(), e);
      }
    }
    backGrService.shutdown();
    initData();
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
              primStage.getScene().setCursor(Cursor.DEFAULT);
              btStart.setDisable(false);
              btStop.setDisable(true);
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
              btStart.setDisable(true);
              btStop.setDisable(false);
            }
          });
        }
        break;
    }
  }


  private ObservableList<ImpFile> loadListFiles() {
    elenco = new ArrayList<ImpFile>();
    String fltrFiles = "wise,estra";
    String szGlobMatch = String.format("glob:*:/**/{%s}*.{csv,xls,xlsx}", fltrFiles);

    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(szGlobMatch);
    Path lastDir = Paths.get(CSZ_START_DIR);
    try (Stream<Path> walk = Files.walk(lastDir)) {
      elenco = walk.filter(p -> !Files.isDirectory(p)) //
          .filter(f -> matcher.matches(f)) // check end with
          .map(pth -> convert(pth)) //
          .collect(Collectors.toList()); // collect all matched to a List
    } catch (IOException e) {
      System.err.printf("Errore scan dir\"%s\" msg=%s\n", lastDir.toString(), e.getMessage());
    }
    ObservableList<ImpFile> liFilesCSV = FXCollections.observableArrayList(elenco);
    return liFilesCSV;
  }

  private ImpFile convert(Path p_pth) {
    ImpFile imf = new ImpFile().assignPath(p_pth);
    return imf;
  }

  @FXML
  public void btStopClick() {
    // System.out.printf("ProvaProgrBarCla.btStopClick((%d)\n", this.hashCode() % 1023);
  }

}
