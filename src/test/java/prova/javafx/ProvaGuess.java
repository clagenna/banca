package prova.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.CodStatTreeData;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.GuessCodStat;
import sm.clagenna.banca.dati.IRigaBanca;
import sm.clagenna.banca.dati.PhraseComparator;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class ProvaGuess extends Application implements PropertyChangeListener {

  private static final Logger s_log = LogManager.getLogger(ProvaGuess.class);

  private static final String CSZ_PROP_PREFIX  = "main";
  private static final String CSZ_MAIN_PROPS   = "ProvaGuess.properties";
  private static final String CSZ_FXMLNAME     = "ProvaGuess.fxml";
  private static final String CSZ_MAIN_APP_CSS = "ProvaGuess.css";

  @FXML
  private TextField txFiltro;
  @FXML
  private Button    btStart;

  @FXML
  private TableView<RigaBanca>           tblRiga;
  @FXML
  private TableColumn<RigaBanca, Number> colId;
  @FXML
  private TableColumn<RigaBanca, String> colTipo;
  @FXML
  private TableColumn<RigaBanca, String> colDtmov;
  @FXML
  private TableColumn<RigaBanca, String> colDtval;
  @FXML
  private TableColumn<RigaBanca, String> colDare;
  @FXML
  private TableColumn<RigaBanca, String> colAvere;
  @FXML
  private TableColumn<RigaBanca, String> colCardid;
  @FXML
  private TableColumn<RigaBanca, String> colDescr;
  @FXML
  private TableColumn<RigaBanca, String> colCodstat;
  @FXML
  private TableColumn<RigaBanca, String> colCodCdsdescr;

  @FXML
  private TableView<GuessCodStat>           tblGuess;
  @FXML
  private TableColumn<GuessCodStat, Number> colGuesIdriga;
  @FXML
  private TableColumn<GuessCodStat, String> colGuesDescr;
  @FXML
  private TableColumn<GuessCodStat, String> colGuesRank;
  @FXML
  private TableColumn<GuessCodStat, String> colGuesCodstat;
  @FXML
  private TableColumn<GuessCodStat, String> colGuesCdsdescr;
  @FXML
  private SplitPane                         spltPane;

  private Stage           primaryStage;
  private ProvaGuess      controller;
  private DataController  data;
  private AppProperties   props;
  private DBConn          connSQL;
  private String          parolaFiltro;
  private List<RigaBanca> liKnown;

  private PhraseComparator phrComp;

  public ProvaGuess() {
    // System.out.printf("NEW   ProvaGuess.ProvaGuess((%d)\n", this.hashCode() % 1023);
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage p_Stage) throws Exception {
    primaryStage = p_Stage;
    // System.out.printf("ProvaGuess.start(%d)\n", this.hashCode() % 1023);
    caricaFXML();
    primaryStage.show();
    controller.appStarted(primaryStage);
  }

  private void caricaFXML() throws IOException {
    primaryStage.setTitle("Studio il Ranking di approssimazione della descrizione");
    primaryStage.onCloseRequestProperty().setValue(e -> Platform.exit());
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
    primaryStage.setScene(scene);
    primaryStage.setOnShown(e -> sceneShowed(e));
    // questa è l'istanza di this.getClass() che riceve gli @FXML controls
    controller = (ProvaGuess) loader.getController();
  }

  @Override
  public void init() throws Exception {
    super.init();
    // System.out.printf("ProvaGuess.init(%d)\n", this.hashCode() % 1023);
  }

  @Override
  public void stop() {
    controller.appStopped();
  }

  public void appStopped() {
    JFXUtils.savePosStage(primaryStage, props, CSZ_PROP_PREFIX);
    saveTableCols();
    props.salvaSuProperties();
  }

  private void saveTableCols() {
    double[] pos = spltPane.getDividerPositions();

    String szPos = String.format("%.4f", pos[0]).replace(",", ".");
    props.setProperty("splitPos", szPos);
    double vv;

    vv = colId.getWidth();
    props.setProperty("colId", Integer.valueOf((int) vv));
    vv = colTipo.getWidth();
    props.setProperty("colTipo", Integer.valueOf((int) vv));
    vv = colDtmov.getWidth();
    props.setProperty("colDtmov", Integer.valueOf((int) vv));
    vv = colDtval.getWidth();
    props.setProperty("colDtval", Integer.valueOf((int) vv));
    vv = colDare.getWidth();
    props.setProperty("colDare", Integer.valueOf((int) vv));
    vv = colAvere.getWidth();
    props.setProperty("colAvere", Integer.valueOf((int) vv));
    vv = colCardid.getWidth();
    props.setProperty("colCardid", Integer.valueOf((int) vv));
    vv = colDescr.getWidth();
    props.setProperty("colDescr", Integer.valueOf((int) vv));
    vv = colCodstat.getWidth();
    props.setProperty("colCodstat", Integer.valueOf((int) vv));
    vv = colCodCdsdescr.getWidth();
    props.setProperty("colCodCdsdescr", Integer.valueOf((int) vv));

    vv = tblGuess.getWidth();
    props.setProperty("tblGuess", Integer.valueOf((int) vv));
    vv = colGuesIdriga.getWidth();
    props.setProperty("colGuesIdriga", Integer.valueOf((int) vv));
    vv = colGuesDescr.getWidth();
    props.setProperty("colGuesDescr", Integer.valueOf((int) vv));
    vv = colGuesRank.getWidth();
    props.setProperty("colGuesRank", Integer.valueOf((int) vv));
    vv = colGuesCodstat.getWidth();
    props.setProperty("colGuesCodstat", Integer.valueOf((int) vv));
    vv = colGuesCdsdescr.getWidth();
    props.setProperty("colGuesCdsdescr", Integer.valueOf((int) vv));

  }

  private void appStarted(Stage prim) {
    primaryStage = prim;
    apriProperties();
    buildForm();
    buildColsUnknownRigaBanca();
    buildColsGuesses();
    apriDb();
    caricaTblUnknownCodstat();
    caricaPhraseComparator();

  }

  private void apriProperties() {
    data = new DataController();
    AppProperties.setSingleton(false);
    DBConnFactory.setSingleton(false);
    try {
      props = new AppProperties();
      props.leggiPropertyFile(new File(ProvaGuess.CSZ_MAIN_PROPS), false, false);
    } catch (AppPropsException e) {
      e.printStackTrace();
      System.exit(1957);
    }
    data.initApp(props);
    data.addPropertyChangeListener(this);
  }

  private void buildForm() {
    JFXUtils.readPosStage(primaryStage, props, CSZ_PROP_PREFIX);
    double vv = 0;
    vv = props.getDoubleProperty("colId");
    if (vv > 0)
      colId.setPrefWidth(vv);
    vv = props.getDoubleProperty("colTipo");
    if (vv > 0)
      colTipo.setPrefWidth(vv);
    vv = props.getDoubleProperty("colDtmov");
    if (vv > 0)
      colDtmov.setPrefWidth(vv);
    vv = props.getDoubleProperty("colDtval");
    if (vv > 0)
      colDtval.setPrefWidth(vv);
    vv = props.getDoubleProperty("colDare");
    if (vv > 0)
      colDare.setPrefWidth(vv);
    vv = props.getDoubleProperty("colAvere");
    if (vv > 0)
      colAvere.setPrefWidth(vv);
    vv = props.getDoubleProperty("colCardid");
    if (vv > 0)
      colCardid.setPrefWidth(vv);
    vv = props.getDoubleProperty("colDescr");
    if (vv > 0)
      colDescr.setPrefWidth(vv);
    vv = props.getDoubleProperty("colCodstat");
    if (vv > 0)
      colCodstat.setPrefWidth(vv);
    vv = props.getDoubleProperty("colCodCdsdescr");
    if (vv > 0)
      colCodCdsdescr.setPrefWidth(vv);

    vv = props.getDoubleProperty("colGuesIdriga");
    if (vv > 0)
      colGuesIdriga.setPrefWidth(vv);
    vv = props.getDoubleProperty("colGuesDescr");
    if (vv > 0)
      colGuesDescr.setPrefWidth(vv);
    vv = props.getDoubleProperty("colGuesRank");
    if (vv > 0)
      colGuesRank.setPrefWidth(vv);
    vv = props.getDoubleProperty("colGuesCodstat");
    if (vv > 0)
      colGuesCodstat.setPrefWidth(vv);
    vv = props.getDoubleProperty("colGuesCdsdescr");
    if (vv > 0)
      colGuesCdsdescr.setPrefWidth(vv);

    txFiltro.textProperty().addListener((obj, old, nv) -> txFiltroSel(obj, old, nv));

  }

  private Object sceneShowed(WindowEvent e) {
    // System.out.printf("ProvaGuess.sceneShowed((%d)\n", this.hashCode() % 1023);
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
  private void buildColsUnknownRigaBanca() {
    // System.out.println("RigaBancaView.initTableView()");
    // String cssAlignL = "-fx-alignment: center-left;";
    String cssAlignR = "-fx-alignment: center-right;";

    tblRiga.getItems().clear();
    tblRiga.getColumns().clear();

    colId = new TableColumn<>("Id");
    colId.setCellValueFactory(new PropertyValueFactory<RigaBanca, Number>("rigaid"));
    colId.setStyle(cssAlignR);
    tblRiga.getColumns().add(colId);

    colTipo = new TableColumn<RigaBanca, String>("Tipo");
    colTipo.setCellValueFactory(new PropertyValueFactory<RigaBanca, String>("tiporec"));
    tblRiga.getColumns().add(colTipo);

    colDtmov = new TableColumn<RigaBanca, String>("Dt. mov.");
    colDtmov.setCellValueFactory(param -> {
      var vv = param.getValue().getDtmov();
      var newval = ParseData.formatDate(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDtmov.setStyle(cssAlignR);
    tblRiga.getColumns().add(colDtmov);

    colDtval = new TableColumn<RigaBanca, String>("Dt. val.");
    colDtval.setCellValueFactory(param -> {
      var vv = param.getValue().getDtval();
      var newval = ParseData.formatDate(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDtval.setStyle(cssAlignR);
    tblRiga.getColumns().add(colDtval);

    colDare = new TableColumn<RigaBanca, String>("Dare");
    colDare.setCellValueFactory(param -> {
      Number vv = param.getValue().getDare();
      var newval = formattaCella(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDare.setStyle(cssAlignR);
    tblRiga.getColumns().add(colDare);

    colAvere = new TableColumn<RigaBanca, String>("Avere");
    colAvere.setCellValueFactory(param -> {
      Number vv = param.getValue().getAvere();
      var newval = formattaCella(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colAvere.setStyle(cssAlignR);
    tblRiga.getColumns().add(colAvere);

    colCardid = new TableColumn<RigaBanca, String>("Card. Id");
    colCardid.setCellValueFactory(new PropertyValueFactory<RigaBanca, String>("cardid"));
    tblRiga.getColumns().add(colCardid);

    colDescr = new TableColumn<RigaBanca, String>("Descrizione");
    colDescr.setCellValueFactory(new PropertyValueFactory<RigaBanca, String>("descr"));
    tblRiga.getColumns().add(colDescr);

    colCodstat = new TableColumn<RigaBanca, String>("Cod. Stat.");
    colCodstat.setCellValueFactory(new PropertyValueFactory<RigaBanca, String>("codstat"));
    tblRiga.getColumns().add(colCodstat);

    tblRiga.setOnMouseClicked(evt -> {
      if (/* evt.isPrimaryButtonDown() && */ evt.getClickCount() == 2) {
        RigaBanca row = tblRiga.getSelectionModel().getSelectedItem();
        Platform.runLater(() -> rigaBancaSelected(row));
      }
    });
  }

  private void buildColsGuesses() {
    // System.out.println("RigaBancaView.initTableView()");
    // String cssAlignL = "-fx-alignment: center-left;";
    String cssAlignR = "-fx-alignment: center-right;";

    tblGuess.getItems().clear();
    tblGuess.getColumns().clear();

    colGuesIdriga = new TableColumn<GuessCodStat, Number>("id Riga");
    colGuesIdriga.setCellValueFactory(new PropertyValueFactory<GuessCodStat, Number>("id"));
    colGuesIdriga.setStyle(cssAlignR);
    tblGuess.getColumns().add(colGuesIdriga);

    colGuesDescr = new TableColumn<GuessCodStat, String>("Descr");
    colGuesDescr.setCellValueFactory(new PropertyValueFactory<GuessCodStat, String>("descr"));
    tblGuess.getColumns().add(colGuesDescr);

    colGuesRank = new TableColumn<GuessCodStat, String>("Rank");
    colGuesRank.setCellValueFactory(param -> {
      Number vv = param.getValue().getRank();
      var newval = formattaCella(vv.doubleValue() * 100.);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colGuesRank.setStyle(cssAlignR);
    tblGuess.getColumns().add(colGuesRank);

    colGuesCodstat = new TableColumn<GuessCodStat, String>("Cod. Stat.");
    colGuesCodstat.setCellValueFactory(new PropertyValueFactory<GuessCodStat, String>("codstat"));
    tblGuess.getColumns().add(colGuesCodstat);

  }

  private String formattaCella(Object p_o) {
    if (p_o == null)
      return "**null**";
    String szCls = p_o.getClass().getSimpleName();
    switch (szCls) {
      case "String":
        return p_o.toString();
      case "Integer":
        if ((Integer) p_o == 0)
          return "";
        return Utils.s_fmtInt.format(p_o);
      case "Float":
        if ((Float) p_o == 0)
          return "";
        return Utils.s_fmtDbl.format( ((Float) p_o).doubleValue());
      case "Double":
        if ((Double) p_o == 0)
          return "";
        // return Utils.formatDouble((Double) p_o);
        return Utils.s_fmtDbl.format(p_o);
    }
    return p_o.toString();
  }

  private void apriDb() {
    DBConnFactory conFact = new DBConnFactory();
    connSQL = conFact.get(EServerId.SqlServer);
    connSQL.readProperties(props);
    connSQL.doConn();
  }

  private void caricaTblUnknownCodstat() {
    List<RigaBanca> li = leggiUnknownCodstat();
    Platform.runLater(() -> {
      tblRiga.getItems().clear();
      tblGuess.getItems().clear();
      if (null != li)
        tblRiga.getItems().addAll(li);
    });
    tblRiga.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    tblRiga.setOnKeyPressed(e -> tblRigaKeyPressed(e));
  }

  private Object tblRigaKeyPressed(KeyEvent e) {
    System.out.printf("ProvaGuess.tblRigaKeyPressed(%s)\n", e.toString());
    switch (e.getCode()) {
      case KeyCode.SPACE:
        e.consume();
        loadCercaCodStat();
        break;
      default:
        break;
    }
    return null;
  }

  private void loadCercaCodStat() {
    try {
      FXMLLoader fxmll = new FXMLLoader(getClass().getResource("CercaCodStat.fxml"));
      Parent radice = fxmll.load();
      Scene scene = new Scene(radice);
      Stage stage = new Stage();
      stage.setScene(scene);
      stage.initModality(Modality.WINDOW_MODAL);
      stage.initOwner(primaryStage);
      stage.show();
      CercaCodStat figlio = fxmll.getController();
      figlio.initApp(props);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<RigaBanca> leggiKnownCodstat() {
    String szQry = "SELECT id,tipo,idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat" //
        + " FROM movimenti" //
        + " WHERE codstat IS NOT NULL";
    return leggiDb(szQry);
  }

  private List<RigaBanca> leggiUnknownCodstat() {
    StringBuilder szQry = new StringBuilder("SELECT id,tipo,idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat" //
    ).append(" FROM movimenti" //
    ).append(" WHERE codstat IS NULL");
    if (Utils.isValue(parolaFiltro)) {
      szQry.append(String.format(" AND descr LIKE '%%%s%%'", parolaFiltro));
    }
    return leggiDb(szQry.toString());
  }

  private List<RigaBanca> leggiDb(String szQry) {
    List<RigaBanca> li = new ArrayList<RigaBanca>();
    CodStatTreeData cdss = data.getCodStatData();
    try (Dataset dts = new Dataset(connSQL)) {
      dts.executeQuery(szQry);
      for (DtsRow row : dts.getRighe()) {
        RigaBanca rb = new RigaBanca();
        rb.setRigaid((Integer) row.get(IRigaBanca.ID.getColNam()));
        rb.setTiporec((String) row.get(IRigaBanca.TIPO.getColNam()));
        rb.setDtmov(ParseData.toLocalDateTime((Timestamp) row.get(IRigaBanca.DTMOV.getColNam())));
        rb.setDtval(ParseData.toLocalDateTime((Timestamp) row.get(IRigaBanca.DTVAL.getColNam())));
        rb.setDare((Double) row.get(IRigaBanca.DARE.getColNam()));
        rb.setAvere((Double) row.get(IRigaBanca.AVERE.getColNam()));
        rb.setCardid((String) row.get(IRigaBanca.CARDID.getColNam()));
        rb.setDescr((String) row.get(IRigaBanca.DESCR.getColNam()));
        rb.setCodstat((String) row.get(IRigaBanca.CODSTAT.getColNam()));
        if (Utils.isValue(rb.getCodstat())) {
          CodStat cds = cdss.decodeCodStat(rb.getCodstat());
          rb.setCdsdescr(cds.getDescr());
        }
        li.add(rb);
      }
      s_log.info("ProvaGuess leggiDB,qtaRecs = {})", dts.size());
    } catch (IOException e) {
      s_log.error("Errore lettura db, err ={}", e.getMessage());
    }
    return li;
  }

  private void rigaBancaSelected(RigaBanca row) {
    if (null == row)
      return;
    List<GuessCodStat> liGuess = new ArrayList<GuessCodStat>();
    for (RigaBanca rb : liKnown) {
      GuessCodStat gue = new GuessCodStat(rb);
      //      PhraseComparator.Similarity sim = phrComp.similarity(rb.getDescr());
      try {
        PhraseComparator phc = new PhraseComparator();
        phc.addKnownPhrase(rb.getDescr(), rb.getCodstat());
        PhraseComparator.Similarity sim = phc.similarity(row.getDescr());
        gue.setRank(sim.percent());
      } catch (Exception e) {
        e.printStackTrace();
        continue;
      }
      if (gue.getRank() > 0)
        liGuess.add(gue);
    }
    Collections.sort(liGuess, Collections.reverseOrder());
    List<GuessCodStat> liFirs = liGuess.stream().limit(100).collect(Collectors.toList());
    tblGuess.getItems().clear();
    tblGuess.getItems().addAll(liFirs);
  }

  private void caricaPhraseComparator() {
    phrComp = new PhraseComparator();
    liKnown = leggiKnownCodstat();
    for (RigaBanca ri : liKnown) {
      phrComp.addKnownPhrase(ri.getDescr(), ri.getCodstat());
    }
    phrComp.creaVectors();
  }

  private Object txFiltroSel(ObservableValue<? extends String> obj, String old, String nv) {
    System.out.printf("ProvaGuess.txFiltroSel(\"%s\")\n", nv);
    if (Utils.isValue(nv))
      parolaFiltro = nv;
    else
      parolaFiltro = null;
    return null;
  }

  @FXML
  public void btStartClick() {
    caricaTblUnknownCodstat();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    Object obj = evt.getNewValue();
    if (evt.getPropertyName().equals(DataController.EVT_SELCODSTAT))
      if (obj instanceof CodStat cds) {
        // System.out.printf("ProvaGuess.propertyChange(%s)\n", cds.toString());
        RigaBanca itm = tblRiga.getSelectionModel().getSelectedItem();
        itm.setCodstat(cds.getCodice());
        Platform.runLater(() -> tblRiga.refresh());
      }

  }

}
