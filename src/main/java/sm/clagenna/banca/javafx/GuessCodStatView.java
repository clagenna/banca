package sm.clagenna.banca.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.AnalizzaCodStats;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.GuessCodStat;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class GuessCodStatView implements Initializable, IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(GuessCodStatView.class);

  public static final String  CSZ_FXMLNAME          = "GuessCodStatView.fxml";
  private static final String CSZ_PROP_POS_X        = "gcdstview.x";
  private static final String CSZ_PROP_POS_Y        = "gcdstview.y";
  private static final String CSZ_PROP_DIM_X        = "gcdstview.lx";
  private static final String CSZ_PROP_DIM_Y        = "gcdstview.ly";
  private static final String CSZ_PROP_COL_Id       = "gcdstview.colId";
  private static final String CSZ_PROP_COL_Tipo     = "gcdstview.colTipo";
  private static final String CSZ_PROP_COL_Dtmov    = "gcdstview.colDtmov";
  private static final String CSZ_PROP_COL_Dare     = "gcdstview.colDare";
  private static final String CSZ_PROP_COL_Avere    = "gcdstview.colAvere";
  private static final String CSZ_PROP_COL_Cardid   = "gcdstview.colCardid";
  private static final String CSZ_PROP_COL_Descr    = "gcdstview.colDescr";
  private static final String CSZ_PROP_COL_Codstat  = "gcdstview.colCodstat";
  private static final String CSZ_PROP_COL_Descrcds = "gcdstview.colDescrcds";
  private static final String CSZ_PROP_COL_Assigned = "gcdstview.colAssigned";

  @FXML
  protected TextField                        txParola;
  @FXML
  private Button                             btCerca;
  @FXML
  private Button                             btSalva;
  @FXML
  private Button                             btAssignCodStat;
  @FXML
  private Label                              lbMsg;
  @FXML
  private TableView<GuessCodStat>            tblview;
  @FXML
  private TableColumn<GuessCodStat, String>  colId;
  @FXML
  private TableColumn<GuessCodStat, String>  colTipo;
  @FXML
  private TableColumn<GuessCodStat, String>  colDtmov;
  @FXML
  private TableColumn<GuessCodStat, String>  colDare;
  @FXML
  private TableColumn<GuessCodStat, String>  colAvere;
  @FXML
  private TableColumn<GuessCodStat, String>  colCardid;
  @FXML
  private TableColumn<GuessCodStat, String>  colDescr;
  @FXML
  private TableColumn<GuessCodStat, String>  colCodstat;
  @FXML
  private TableColumn<GuessCodStat, String>  colDescrcds;
  @FXML
  private TableColumn<GuessCodStat, Boolean> colAssigned;

  @Getter @Setter
  private Scene            myScene;
  private Stage            lstage;
  private LoadBancaMainApp m_appmain;
  private DataController   datacntrlr;
  private ISQLGest         m_db;
  private AppProperties    mainProps;
  private boolean          bSemaf;
  private String           m_codStatSel;
  private AnalizzaCodStats m_tbvf;

  public GuessCodStatView() {
    //
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    //

  }

  @Override
  public void initApp(AppProperties p_props) {
    m_appmain = LoadBancaMainApp.getInst();
    m_appmain.addGuessCodeStatView(this);
    mainProps = m_appmain.getProps();
    datacntrlr = m_appmain.getData();
    datacntrlr.addPropertyChangeListener(this);
    String szSQLType = p_props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    m_db = SqlGestFactory.get(szSQLType);
    m_db.setDbconn(LoadBancaMainApp.getInst().getConnSQL());

    impostaForma(mainProps);
    buildTableView();
    // txParola.textProperty().addListener((obj, old, nv) -> txParolaSel(obj, old, nv));

    if (lstage != null)
      lstage.setOnCloseRequest(e -> {
        closeApp(mainProps);
      });

  }

  private void impostaForma(AppProperties p_props) {
    lstage = null;
    if (myScene == null)
      myScene = tblview.getScene();
    if (lstage == null && myScene != null)
      lstage = (Stage) myScene.getWindow();
    if (lstage == null) {
      s_log.error("Non trovo lo stage per CodStatView");
      return;
    }

    int px = p_props.getIntProperty(CSZ_PROP_POS_X);
    int py = p_props.getIntProperty(CSZ_PROP_POS_Y);
    int dx = p_props.getIntProperty(CSZ_PROP_DIM_X);
    int dy = p_props.getIntProperty(CSZ_PROP_DIM_Y);
    var mm = JFXUtils.getScreenMinMax(px, py, dx, dy);
    if (mm.poxX() != -1 && mm.posY() != -1 && mm.poxX() * mm.posY() != 0) {
      lstage.setX(mm.poxX());
      lstage.setY(mm.posY());
      lstage.setWidth(mm.width());
      lstage.setHeight(mm.height());
    }

    URL url = m_appmain.getUrlCSS();
    if (null != url)
      myScene.getStylesheets().add(url.toExternalForm());
    myScene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> gestKey(ev));
  }

  private Object gestKey(KeyEvent ev) {
    // System.out.printf("ResultView.gestKey(%s)\n", ev.toString());
    if (txParola.isFocused() && ev.getCode() == KeyCode.ENTER) {
      ev.consume();
      btCercaClick(null);
    }
    return null;
  }

  private void buildTableView() {
    buildColumsTableView();
    if (null != m_tbvf) {
      var dati = m_tbvf.getDati();
      tblview.getItems().addAll(dati);
    }
    System.out.println("Fine buildTableView()");
  }

  private void buildColumsTableView() {
    // System.out.println("GuessCodStatView.initTableView()");
    // String cssAlignL = "-fx-alignment: center-left;";
    String cssAlignR = "-fx-alignment: center-right;";

    tblview.getItems().clear();
    tblview.getColumns().clear();

    colId = new TableColumn<GuessCodStat, String>("Id");
    colId.setCellValueFactory(param -> {
      Number vv = param.getValue().getId();
      var newval = formattaCella(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colId.setStyle(cssAlignR);
    tblview.getColumns().add(colId);

    colTipo = new TableColumn<GuessCodStat, String>("Tipo");
    colTipo.setCellValueFactory(celldata -> celldata.getValue().propertyTipo());
    tblview.getColumns().add(colTipo);

    colDtmov = new TableColumn<GuessCodStat, String>("Dt. mov.");
    colDtmov.setCellValueFactory(param -> {
      var vv = param.getValue().getDtmov();
      var newval = ParseData.formatDate(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDtmov.setStyle(cssAlignR);
    tblview.getColumns().add(colDtmov);

    colDare = new TableColumn<GuessCodStat, String>("Dare");
    colDare.setCellValueFactory(param -> {
      Number vv = param.getValue().getDare();
      var newval = formattaCella(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDare.setStyle(cssAlignR);
    tblview.getColumns().add(colDare);

    colAvere = new TableColumn<GuessCodStat, String>("Avere");
    colAvere.setCellValueFactory(param -> {
      Number vv = param.getValue().getAvere();
      var newval = formattaCella(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colAvere.setStyle(cssAlignR);
    tblview.getColumns().add(colAvere);

    colCardid = new TableColumn<GuessCodStat, String>("Card. Id");
    colCardid.setCellValueFactory(celldata -> celldata.getValue().propertyCardid());
    tblview.getColumns().add(colCardid);

    colDescr = new TableColumn<GuessCodStat, String>("Descrizione");
    colDescr.setCellValueFactory(celldata -> celldata.getValue().propertyDescr());
    tblview.getColumns().add(colDescr);

    colCodstat = new TableColumn<GuessCodStat, String>("Cod. Stat.");
    colCodstat.setCellValueFactory(celldata -> celldata.getValue().propertyCodstat());
    tblview.getColumns().add(colCodstat);

    colDescrcds = new TableColumn<GuessCodStat, String>("Descr Statis.");
    colDescrcds.setCellValueFactory(celldata -> celldata.getValue().propertyDescrcds());
    tblview.getColumns().add(colDescrcds);

    colAssigned = new TableColumn<GuessCodStat, Boolean>("Assegnare");
    colAssigned.setCellValueFactory(celldata -> celldata.getValue().propertyAssigned());
    tblview.getColumns().add(colAssigned);

    colCodstat.setCellFactory(TextFieldTableCell.forTableColumn());
    colAssigned.setCellFactory(celldata -> new CheckBoxTableCell<>());
    colCodstat.setEditable(true);
    colAssigned.setEditable(true);

    colCodstat.setOnEditCommit((TableColumn.CellEditEvent<GuessCodStat, String> t) -> { //
      var row = t.getTableView().getItems().get(t.getTablePosition().getRow());
      row.setCodstat(t.getNewValue());
      assegnaCodStatAiSelected(t.getNewValue());
    });
    colAssigned.setOnEditCommit((TableColumn.CellEditEvent<GuessCodStat, Boolean> t) -> { //
      var row = t.getTableView().getItems().get(t.getTablePosition().getRow());
      row.setAssigned(t.getNewValue());
    });

    tblview.setEditable(true);
    tblview.setRowFactory(row -> new TableRow<GuessCodStat>() {
      @Override
      public void updateItem(GuessCodStat item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
          setStyle("");
          return;
        }
        String cssForegSty = "-fx-text-fill: black;";
        StringBuilder cssBackgSty = new StringBuilder();
        String styMod = "gold";
        if ( (item.isModified() || item.isAssigned()) && !isSelected()) {
          if (item.isAssigned())
            styMod = "lightgreen";
          cssBackgSty //
              .append(cssForegSty) //
              .append("-fx-background-color: ") //
              .append(styMod) //
              .append(";");
          setStyle(cssBackgSty.toString());
          // System.out.printf("GuessCodStatView.initTableView(%s)\n", cssBackgSty.toString());
        } else
          setStyle("");
      }
    });

    //    ObservableList<GuessCodStat> dati = getDati();
    //    tblview.setItems(dati);

    double vv;
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Id, -1);
    if (vv > 0)
      colId.setPrefWidth(vv);
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Tipo, -1);
    if (vv > 0)
      colTipo.setPrefWidth(vv);
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Dtmov, -1);
    if (vv > 0)
      colDtmov.setPrefWidth(vv);
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Dare, -1);
    if (vv > 0)
      colDare.setPrefWidth(vv);
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Avere, -1);
    if (vv > 0)
      colAvere.setPrefWidth(vv);
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Cardid, -1);
    if (vv > 0)
      colCardid.setPrefWidth(vv);
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Descr, -1);
    if (vv > 0)
      colDescr.setPrefWidth(vv);
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Codstat, -1);
    if (vv > 0)
      colCodstat.setPrefWidth(vv);
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Descrcds, -1);
    if (vv > 0)
      colDescrcds.setPrefWidth(vv);
    vv = mainProps.getDoubleProperty(CSZ_PROP_COL_Assigned, -1);
    if (vv > 0)
      colAssigned.setPrefWidth(vv);

  }

  private void assegnaCodStatAiSelected(String newValue) {
    Platform.runLater(() -> tblview //
        .getSelectionModel() //
        .getSelectedItems() //
        .forEach(s -> s.setCodstat(newValue)) //
    );

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

  @FXML
  private void btCercaClick(ActionEvent event) {
    if (bSemaf)
      return;
    bSemaf = true;
    Platform.runLater(() -> lbMsg.setText("Cerco di indovinare i Codici Statistici ..."));
    creaTableResultThread();
    abilitaBottoni();
  }

  @FXML
  private void btSalvaClick(ActionEvent event) {
    System.out.println("GuessCodStatView.btSalvaClick()");
    List<GuessCodStat> li = tblview.getItems().stream().filter(s -> s.isAssigned()).collect(Collectors.toList());
    m_tbvf.saveSuDb(li);
    btCercaClick(null);
  }

  private void abilitaBottoni() {
    boolean bv = true;
    btCerca.setDisable( !bv);
    btAssignCodStat.setDisable( !Utils.isValue(m_codStatSel));
    if (bv) {
      ObservableList<GuessCodStat> li = tblview.getItems();
      bv = li != null && li.size() > 2;
      // btExportCsv.setDisable( !bv);
    }
  }

  private void creaTableResultThread() {
    System.out.println("creaTableResultThread()");
    m_tbvf = new AnalizzaCodStats(m_appmain);
    m_tbvf.setParola(txParola.getText());
    ExecutorService backGrService = Executors.newFixedThreadPool(1);
    Platform.runLater(() -> {
      lstage.getScene().setCursor(Cursor.WAIT);
      btCerca.setDisable(true);
    });

    try {
      m_tbvf.setOnRunning(ev -> {
        s_log.debug("Cerca CodStat task running...");
      });
      m_tbvf.setOnSucceeded(ev -> {
        s_log.debug("Cerca CodStat task Finished!");
        Platform.runLater(() -> {
          lstage.getScene().setCursor(Cursor.DEFAULT);
          btCerca.setDisable(false);
          bSemaf = false;
        });
      });
      m_tbvf.setOnFailed(ev -> {
        s_log.debug("Cerca CodStat task failure");
        Platform.runLater(() -> {
          lstage.getScene().setCursor(Cursor.DEFAULT);
          btCerca.setDisable(false);
          bSemaf = false;
        });
      });
      backGrService.execute(m_tbvf);
    } catch (Exception e) {
      s_log.error("Errore task TableViewFiller");
    }
    backGrService.shutdown();

    // Context menu accetta tutti
    MenuItem mi1 = new MenuItem("Accetta Tutti");
    mi1.setOnAction((ActionEvent ev) -> {
      accettaTutti_click(null);
    });
    MenuItem mi2 = new MenuItem("Rifiuta Tutti");
    mi2.setOnAction((ActionEvent ev) -> {
      rifiutaTutti_click(null);
    });
    MenuItem mi3 = new MenuItem("Accetta Selezione");
    mi3.setOnAction((ActionEvent ev) -> {
      accettaSel_click(null);
    });
    ContextMenu menu = new ContextMenu();
    menu.getItems().addAll(mi1, mi2, mi3);
    // liBanca.setContextMenu(menu);
    tblview.setContextMenu(menu);

    tblview.setRowFactory(tbl -> new TableRow<GuessCodStat>() {
      {
        setOnMouseClicked(ev -> {
          if (isEmpty())
            return;
          if (ev.getClickCount() == 2) {
            riga_dblclick();
          }
        });
      }
    });
    tblview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

  private void accettaTutti_click(Object object) {
    tblview.getItems().forEach(s -> s.setAssigned(true));
    Platform.runLater(() -> tblview.refresh());
  }

  private void rifiutaTutti_click(Object object) {
    tblview.getItems().forEach(s -> s.setAssigned(false));
    Platform.runLater(() -> tblview.refresh());
  }

  private void accettaSel_click(Object object) {
    tblview.getSelectionModel().getSelectedItems().forEach(s -> s.setAssigned(true));
    Platform.runLater(() -> tblview.refresh());
  }

  private void riga_dblclick() {
    System.out.println("GuessCodStatView.riga_dblclick()");
  }

  @FXML
  void btMostraCodStatClick(ActionEvent event) {
    LoadBancaController cntr = (LoadBancaController) m_appmain.getController();
    cntr.mnuConfMostraCodStatClick(event);
  }

  @FXML
  void btAssignCodStatClick(ActionEvent event) {
    if ( !Utils.isValue(m_codStatSel))
      return;
    System.out.println("GuessCodStatView.btAssignCodStatClick()");
  }

  @Override
  public void changeSkin() {
    URL url = m_appmain.getUrlCSS();
    if (null == url || null == myScene)
      return;
    myScene.getStylesheets().clear();
    myScene.getStylesheets().add(url.toExternalForm());
  }

  @Override
  public void closeApp(AppProperties p_props) {
    datacntrlr.removePropertyChangeListener(this);
    m_appmain.removeGuessCodStatView(this);
    if (myScene == null) {
      s_log.error("Il campo Scene risulta = **null**");
      return;
    }

    double px = myScene.getWindow().getX();
    double py = myScene.getWindow().getY();
    double dx = myScene.getWindow().getWidth();
    double dy = myScene.getWindow().getHeight();

    // double splPos = spltPane.getDividerPositions()[0];
    // String szDiv = String.format("%0.6f", splPos).replace(",", ".");
    // String szDiv = s_xfmt.format(splPos).replace(",", ".");
    p_props.setProperty(CSZ_PROP_POS_X, (int) px);
    p_props.setProperty(CSZ_PROP_POS_Y, (int) py);
    p_props.setProperty(CSZ_PROP_DIM_X, (int) dx);
    p_props.setProperty(CSZ_PROP_DIM_Y, (int) dy);

    p_props.setProperty(CSZ_PROP_COL_Id, Double.valueOf(colId.getWidth()).intValue());
    p_props.setProperty(CSZ_PROP_COL_Tipo, Double.valueOf(colTipo.getWidth()).intValue());
    p_props.setProperty(CSZ_PROP_COL_Dtmov, Double.valueOf(colDtmov.getWidth()).intValue());
    p_props.setProperty(CSZ_PROP_COL_Dare, Double.valueOf(colDare.getWidth()).intValue());
    p_props.setProperty(CSZ_PROP_COL_Avere, Double.valueOf(colAvere.getWidth()).intValue());
    p_props.setProperty(CSZ_PROP_COL_Cardid, Double.valueOf(colCardid.getWidth()).intValue());
    p_props.setProperty(CSZ_PROP_COL_Descr, Double.valueOf(colDescr.getWidth()).intValue());
    p_props.setProperty(CSZ_PROP_COL_Codstat, Double.valueOf(colCodstat.getWidth()).intValue());
    p_props.setProperty(CSZ_PROP_COL_Descrcds, Double.valueOf(colDescrcds.getWidth()).intValue());
    p_props.setProperty(CSZ_PROP_COL_Assigned, Double.valueOf(colAssigned.getWidth()).intValue());

  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // System.out.printf("ResultView.propertyChange(\"%s=%s\")\n", evt.getPropertyName(), evt.getNewValue().toString());
    String szEvt = evt.getPropertyName();
    switch (szEvt) {

      case DataController.EVT_CODSTAT:
        m_codStatSel = evt.getNewValue().toString();
        break;

      case DataController.EVT_DATASET_CREATED:
        if (evt.getNewValue() instanceof Integer nv) {
          var fmt = NumberFormat.getInstance(Locale.getDefault());
          String szMsg = String.format("Letti %s recs", fmt.format(nv));
          Platform.runLater(() -> lbMsg.setText(szMsg));
        }
        break;

      case DataController.EVT_GUESSDATA_CREATED:
        // System.out.println("EVT_GUESSDATA_CREATED");
        if (evt.getNewValue() instanceof Integer nv) {
          String szMsg = String.format("Letti %s recs", Utils.s_fmtInt.format(nv));
          Platform.runLater(() -> lbMsg.setText(szMsg));
        }
        buildTableView();
        break;
    }

  }

}
