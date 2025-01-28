package sm.clagenna.banca.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.AnalizzaCodStats;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.GuessCodStat;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class GuessCodStatView implements Initializable, IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(GuessCodStatView.class);

  public static final String  CSZ_FXMLNAME   = "GuessCodStatView.fxml";
  private static final String CSZ_PROP_POS_X = "guescodstatview.x";
  private static final String CSZ_PROP_POS_Y = "guescodstatview.y";
  private static final String CSZ_PROP_DIM_X = "guescodstatview.lx";
  private static final String CSZ_PROP_DIM_Y = "guescodstatview.ly";

  @FXML
  protected TextField                        txParola;
  @FXML
  private Button                             btCerca;
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
    initTableView();
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
    if (px != -1 && py != -1 && px * py != 0) {
      lstage.setX(px);
      lstage.setY(py);
      lstage.setWidth(dx);
      lstage.setHeight(dy);
    }
    URL url = m_appmain.getUrlCSS();
    if (null != url)
      myScene.getStylesheets().add(url.toExternalForm());
  }

  private void initTableView() {

    // String cssAlignL = "-fx-alignment: center-left;";
    String cssAlignR = "-fx-alignment: center-right;";

    colId.setCellValueFactory(param -> {
      Number vv = param.getValue().getId();
      var newval = formattaCella(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colId.setStyle(cssAlignR);

    colTipo.setCellValueFactory(celldata -> celldata.getValue().propertyTipo());
    colDtmov.setCellValueFactory(param -> {
      var vv = param.getValue().getDtmov();
      var newval = ParseData.formatDate(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDtmov.setStyle(cssAlignR);

    colDare.setCellValueFactory(param -> {
      Number vv = param.getValue().getDare();
      var newval = formattaCella(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDare.setStyle(cssAlignR);

    colAvere.setCellValueFactory(param -> {
      Number vv = param.getValue().getAvere();
      var newval = formattaCella(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colAvere.setStyle(cssAlignR);

    colCardid.setCellValueFactory(celldata -> celldata.getValue().propertyCardid());
    colDescr.setCellValueFactory(celldata -> celldata.getValue().propertyDescr());
    colCodstat.setCellValueFactory(celldata -> celldata.getValue().propertyCodstat());
    colDescrcds.setCellValueFactory(celldata -> celldata.getValue().propertyDescrcds());
    colAssigned.setCellValueFactory(celldata -> celldata.getValue().propertyAssigned());

    colCodstat.setCellFactory(TextFieldTableCell.forTableColumn());
    colAssigned.setCellFactory(celldata -> new CheckBoxTableCell<>());
    colCodstat.setEditable(true);
    colAssigned.setEditable(true);

    colCodstat.setOnEditCommit((TableColumn.CellEditEvent<GuessCodStat, String> t) -> { //
      var row = t.getTableView().getItems().get(t.getTablePosition().getRow());
      row.setCodstat(t.getNewValue());
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
        } else
          setStyle("");
      }
    });

//    ObservableList<GuessCodStat> dati = getDati();
//    tblview.setItems(dati);
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

    creaTableResultThread();
    abilitaBottoni();

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
    m_tbvf = new AnalizzaCodStats(tblview, m_appmain.getConnSQL());
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

    // Context menu open document
    MenuItem mi1 = new MenuItem("Accetta Tutti");
    mi1.setOnAction((ActionEvent ev) -> {
      accettaTutti_click(null);
    });
    ContextMenu menu = new ContextMenu();
    menu.getItems().add(mi1);
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
    System.out.println("GuessCodStatView.accettaTutti_click()");
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
    }

  }

}
