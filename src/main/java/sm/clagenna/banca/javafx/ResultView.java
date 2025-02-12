package sm.clagenna.banca.javafx;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.CsvFileContainer;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.IRigaBanca;
import sm.clagenna.banca.dati.ImpFile;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.javafx.AutoCompleteComboBoxListener;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.javafx.TableViewFiller;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sys.ex.DatasetException;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class ResultView implements Initializable, IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(ResultView.class);

  public static final String  CSZ_FXMLNAME          = "ResultView.fxml";
  private static final String CSZ_PROP_POSRESVIEW_X = "resview.x";
  private static final String CSZ_PROP_POSRESVIEW_Y = "resview.y";
  private static final String CSZ_PROP_DIMRESVIEW_X = "resview.lx";
  private static final String CSZ_PROP_DIMRESVIEW_Y = "resview.ly";
  private static final String CSZ_QRY_TRUE          = "1=1";

  @FXML
  protected ComboBox<String>  cbTipoBanca;
  @FXML
  protected ComboBox<String>  cbQuery;
  @FXML
  protected ComboBox<Integer> cbAnnoComp;
  @FXML
  protected ComboBox<String>  cbMeseComp;
  @FXML
  protected TextField         txParola;
  @FXML
  protected TextArea          txWhere;
  @FXML
  private Button              btCerca;
  @FXML
  protected ComboBox<String>  cbSaveQuery;
  @FXML
  private Button              btSaveQuery;
  @FXML
  private Button              btExportCsv;
  @FXML
  private Button              btMostraCodStat;
  @FXML
  private Button              btAssignCodStat;
  @FXML
  private Label               lbAssignCodStat;
  @FXML
  private Button              btIndovinaCodStat;
  @FXML
  protected CheckBox          ckRegExp;
  @FXML
  private CheckBox            ckScartaImp;
  @FXML
  private CheckBox            ckLanciaExcel;
  @FXML
  private CheckBox            ckCvsBlankOnZero;

  @FXML
  private TableView<List<Object>> tblview;
  @FXML
  private Label                   lbMsg;

  @Getter @Setter
  private Scene myScene;
  private Stage lstage;
  //   private AppProperties       m_prQries;
  private LoadBancaMainApp    m_appmain;
  @Getter
  private AppProperties       mainProps;
  private ISQLGest            m_db;
  private Map<String, String> m_mapQry;

  private Integer                m_fltrAnnoComp;
  private String                 m_fltrMeseComp;
  private String                 m_fltrWhere;
  private String                 m_fltrParola;
  @Getter @Setter
  private boolean                fltrParolaRegEx;
  private String                 m_qry;
  @Getter @Setter
  private GestResViewQueryParams m_gestQry;

  private TableViewFillerBanca m_tbvf;
  private Path                 m_CSVfile;
  private String               m_fltrTipoBanca;
  private DataController       dataCntrl;
  @Getter @Setter
  private boolean              csvBlankOnZero;
  private String               m_codStatSel;
  private boolean              bSemaf;

  @SuppressWarnings("unused")
  private AutoCompleteComboBoxListener<String> autoCbComp;

  public ResultView() {
    //
  }

  @Override
  public void initialize(URL p_location, ResourceBundle p_resources) {
    // initApp(null);
  }

  @Override
  public void initApp(AppProperties p_props) {
    TableViewFiller.setNullRetValue("");
    m_appmain = LoadBancaMainApp.getInst();
    m_appmain.addResView(this);
    mainProps = m_appmain.getProps();
    dataCntrl = m_appmain.getData();
    dataCntrl.addPropertyChangeListener(this);

    scegliDB(p_props);
    caricaComboTipoBanca();
    caricaComboAnno();
    caricaComboMesecomp();
    // caricaComboQueries();
    caricaComboQueriesFromDB();
    txParola.textProperty().addListener((obj, old, nv) -> txParolaSel(obj, old, nv));
    txWhere.textProperty().addListener((obj, old, nv) -> txWhereSel(obj, old, nv));
    caricaComboQrySalvate();
    impostaForma(mainProps);
    if (lstage != null)
      lstage.setOnCloseRequest(e -> {
        closeApp(mainProps);
      });
    abilitaBottoni();
  }

  private void scegliDB(AppProperties p_props) {
    String szSQLType = p_props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    m_db = SqlGestFactory.get(szSQLType);
    m_db.setDbconn(LoadBancaMainApp.getInst().getConnSQL());
  }

  private void caricaComboQrySalvate() {
    m_gestQry = new GestResViewQueryParams(this);
    m_gestQry.caricaCombo(cbSaveQuery);
    btSaveQuery.setDisable(true);
    autoCbComp = new AutoCompleteComboBoxListener<String>(cbSaveQuery);
    cbSaveQuery //
        .getEditor() //
        .textProperty() //
        .addListener( //
            (obj, old, nv) -> cbSaveQueryUpd(obj, old, nv) //
        );
  }

  private void caricaComboTipoBanca() {
    List<String> li = m_db.getListTipoCard();
    cbTipoBanca.getItems().clear();
    cbTipoBanca.getItems().add((String) null);
    cbTipoBanca.getItems().addAll(li);
  }

  private void caricaComboAnno() {
    List<Integer> li = m_db.getListAnni();
    cbAnnoComp.getItems().clear();
    cbAnnoComp.getItems().add((Integer) null);
    cbAnnoComp.getItems().addAll(li);
  }

  private void caricaComboMesecomp() {
    List<String> li = m_db.getListMeseComp(m_fltrAnnoComp);
    cbMeseComp.getItems().clear();
    cbMeseComp.getItems().add((String) null);
    cbMeseComp.getItems().addAll(li);
  }

  @SuppressWarnings("unused")
  private void caricaComboQueries() {
    m_mapQry = new HashMap<>();
    List<String> liQry = new ArrayList<>();
    Set<Object> keys = mainProps.getProperties().keySet();
    for (Object k : keys) {
      String szKey = k.toString();
      if ( !szKey.startsWith("QRY."))
        continue;
      String szDiz = szKey.substring(4).replace('.', ' ');
      m_mapQry.put(szDiz, szKey);
      liQry.add(szDiz);
    }
    cbQuery.getItems().clear();
    cbQuery.getItems().add((String) null);
    cbQuery.getItems().addAll(liQry);
  }

  private void caricaComboQueriesFromDB() {
    m_mapQry = m_db.getListDBViews();
    List<String> liNam = new ArrayList<String>(m_mapQry.keySet());
    Collections.sort(liNam);
    cbQuery.getItems().clear();
    cbQuery.getItems().add((String) null);
    cbQuery.getItems().addAll(liNam);
  }

  private void impostaForma(AppProperties p_props) {
    lstage = null;
    if (myScene == null)
      myScene = btCerca.getScene();
    if (lstage == null && myScene != null)
      lstage = (Stage) myScene.getWindow();
    if (lstage == null) {
      s_log.error("Non trovo lo stage per ResultView");
      return;
    }

    int px = p_props.getIntProperty(CSZ_PROP_POSRESVIEW_X);
    int py = p_props.getIntProperty(CSZ_PROP_POSRESVIEW_Y);
    int dx = p_props.getIntProperty(CSZ_PROP_DIMRESVIEW_X);
    int dy = p_props.getIntProperty(CSZ_PROP_DIMRESVIEW_Y);
    var mm = JFXUtils.getScreenMinMax(px, py, dx, dy);
    if (mm.poxX() != -1 && mm.posY() != -1 && mm.poxX() * mm.posY() != 0) {
      lstage.setX(mm.poxX());
      lstage.setY(mm.posY());
      lstage.setWidth(mm.width());
      lstage.setHeight(mm.height());
      
    }
    myScene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> gestKey(ev));
    URL url = m_appmain.getUrlCSS();
    if (null != url)
      myScene.getStylesheets().add(url.toExternalForm());
  }

  private void caricaCercaCodStat() {
    try {
      FXMLLoader fxmll = new FXMLLoader(getClass().getResource(CercaCodStat.CSZ_FXMLNAME));
      Parent radice = fxmll.load();
      Scene scene = new Scene(radice);
      Stage stage = new Stage();
      stage.setScene(scene);
      stage.initModality(Modality.WINDOW_MODAL);
      stage.initOwner(lstage);
      stage.show();
      CercaCodStat figlio = fxmll.getController();
      figlio.initApp(mainProps);
    } catch (Exception e) {
      s_log.error("Errore caricamento CercaCodStat, msg = {}", e.getMessage(),e);
    }
  }

  private Object gestKey(KeyEvent ev) {
    // System.out.printf("ResultView.gestKey(%s)\n", ev.toString());
    if (/* ev.isControlDown() && */ ev.getCode() == KeyCode.ENTER) {
      ev.consume();
      btCercaClick(null);
    }
    return null;
  }

  private Object tblRigaKeyPressed(KeyEvent e) {
    // System.out.printf("ProvaGuess.tblRigaKeyPressed(%s)\n", e.toString());
    switch (e.getCode()) {
      case KeyCode.SPACE:
        e.consume();
        caricaCercaCodStat();
        break;
      default:
        break;
    }
    return null;
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
    dataCntrl.removePropertyChangeListener(this);
    m_appmain.removeResView(this);
    autoCbComp = null;
    if (null != m_gestQry)
      m_gestQry.closeApp(p_props);
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

    p_props.setProperty(CSZ_PROP_POSRESVIEW_X, (int) px);
    p_props.setProperty(CSZ_PROP_POSRESVIEW_Y, (int) py);
    p_props.setProperty(CSZ_PROP_DIMRESVIEW_X, (int) dx);
    p_props.setProperty(CSZ_PROP_DIMRESVIEW_Y, (int) dy);
    // p_props.setProperty(CSZ_PROP_SPLITPOS, szDiv);

  }

  @FXML
  void cbTipoBancaSel(ActionEvent event) {
    m_fltrTipoBanca = cbTipoBanca.getSelectionModel().getSelectedItem();
    s_log.debug("ResultView.cbTipoBancaSel(\"{}\"):", m_fltrTipoBanca);
    abilitaBottoni();
  }

  @FXML
  void cbAnnoCompSel(ActionEvent event) {
    m_fltrAnnoComp = cbAnnoComp.getSelectionModel().getSelectedItem();
    s_log.debug("ResultView.cbAnnoCompSel({}):", m_fltrAnnoComp);
    caricaComboMesecomp();
    abilitaBottoni();
  }

  @FXML
  void cbMeseCompSel(ActionEvent event) {
    m_fltrMeseComp = cbMeseComp.getSelectionModel().getSelectedItem();
    s_log.debug("ResultView.cbMeseCompSel(\"{}\"):", m_fltrMeseComp);
    abilitaBottoni();
  }

  @FXML
  void cbQuerySel(ActionEvent event) {
    String szK = cbQuery.getSelectionModel().getSelectedItem();
    if (null == szK)
      m_qry = null;
    else
      m_qry = m_mapQry.get(szK);
    s_log.debug("ResultView.cbQuerySel():" + szK);
    abilitaBottoni();
  }

  @FXML
  void txWhereSel(ObservableValue<? extends String> obj, String old, String nval) {
    if (bSemaf)
      return;
    m_fltrWhere = nval;
    // s_log.debug("ResultView.txWhereSel({}):", m_fltrWhere);
    abilitaBottoni();
  }

  @FXML
  void txParolaSel(ObservableValue<? extends String> obj, String old, String nval) {
    // System.out.printf("ResultView.txParolaSel(%s)\n", nval);
    m_fltrParola = nval;
    // s_log.debug("ResultView.txWhereSel({}):", m_fltrWhere);
    abilitaBottoni();
  }

  private Object cbSaveQueryUpd(ObservableValue<? extends String> obj, String old, String nv) {
    btSaveQuery.setDisable( ! (Utils.isValue(nv) && nv.length() > 2));
    return null;
  }

  private void abilitaBottoni() {
    boolean bv = Utils.isValue(m_qry);
    btCerca.setDisable( !bv);
    btExportCsv.setDisable( !bv);
    btAssignCodStat.setDisable( !Utils.isValue(m_codStatSel));
    if (bv) {
      ObservableList<List<Object>> li = tblview.getItems();
      bv = li != null && li.size() > 2;
      btExportCsv.setDisable( !bv);
    }
  }

  @FXML
  private void btCercaClick(ActionEvent event) {
    if (bSemaf)
      return;
    bSemaf = true;
    // chiamando btCercaClick() dal propertyChange( EVT_FILTER_CODSTAT )(piu sotto)
    // ricevo 2 chiamate consecutive !?! Per cui bSema viene spento solo alla fine del thread
    // creaTableResultThread(szQryFltr);
    //    System.out.println("ResultView.btCercaClick()");
    //    printStackTrace();
    try {
      if (null != event) {
        if (event.getSource() instanceof String szCodstat) {
          m_fltrWhere = String.format("codstat like '%s%%'", szCodstat);
          txWhere.setText(m_fltrWhere);
        }
      }
    } finally {
      // bSemaf = false;
    }
    String szQryFltr = creaQuery();
    // test validita query
    DBConn dbc = m_db.getDbconn();
    if ( !dbc.testQuery(szQryFltr))
      return;
    creaTableResultThread(szQryFltr);
    abilitaBottoni();
  }

  @SuppressWarnings("unused")
  private void printStackTrace() {
    StackTraceElement[] ll = Thread.currentThread().getStackTrace();
    int k = 0;
    final int MAX = 50;
    for (StackTraceElement stk : ll) {
      String sz = stk.toString();
      if (sz.contains("getStackTrace") || sz.contains("printStackTrace"))
        continue;
      System.out.println("\t" + sz);
      if (k++ > MAX)
        break;

    }
  }

  @FXML
  void btSaveQueryClick(ActionEvent event) {
    String szNam = cbSaveQuery.getSelectionModel().getSelectedItem();
    if ( !m_gestQry.saveQuery(szNam))
      m_appmain.msgBox(m_gestQry.getErrorMesg(), AlertType.ERROR);
    else
      m_gestQry.caricaCombo(cbSaveQuery);
  }

  @FXML
  void cbSaveQuerySel(ActionEvent event) {
    // System.out.printf("ResultView.cbSaveQuerySel(%s)\n", cbSaveQuery.getSelectionModel().getSelectedItem());
    m_gestQry.readQuery(cbSaveQuery.getSelectionModel().getSelectedItem());
  }

  @FXML
  void ckRegExpClick(ActionEvent event) {
    setFltrParolaRegEx(ckRegExp.isSelected());
  }

  @FXML
  void btMostraCodStatClick(ActionEvent event) {
    // System.out.println("ResultView.btMostraCodStatClick()");
    LoadBancaController cntr = (LoadBancaController) m_appmain.getController();
    cntr.mnuConfMostraCodStatClick(event);
  }

  @FXML
  void btAssignCodStatClick(ActionEvent event) {
    if ( !Utils.isValue(m_codStatSel))
      return;
    ObservableList<List<Object>> li = tblview.getSelectionModel().getSelectedItems();
    if (null == li || li.size() == 0) {
      s_log.warn("Nessun record selezionato per l'assegnamento di {}", m_codStatSel);
      return;
    }
    Platform.runLater(() -> {
      lstage.getScene().setCursor(Cursor.WAIT);
      btAssignCodStat.setDisable(true);
    });
    // System.out.printf("ResultView.btAssignCodStatClick(sel=%d)\n", li.size());
    try {
      m_db.beginTrans();
      for (List<Object> elem : li) {
        RigaBanca riga = RigaBanca.parse(elem);
        riga.setCodstat(m_codStatSel);
        m_db.updateCodStat(riga);
        elem.set(EColsTableView.codstat.getColNo(), m_codStatSel);
      }
    } finally {
      m_db.commitTrans();
    }
    s_log.info("Aggegnato cod. stat. {} a {} records", m_codStatSel, li.size());
    btCercaClick(null);
    Platform.runLater(() -> {
      lstage.getScene().setCursor(Cursor.DEFAULT);
      btAssignCodStat.setDisable(false);
    });
  }

  @FXML
  void btIndovinaCodStatClick(ActionEvent event) {
    LoadBancaController cntr = (LoadBancaController) m_appmain.getController();
    cntr.mnuConfMostraGuessCodStatClick(null);
  }

  private String creaQuery() {
    String szQryFltr = null;
    if (m_qry == null) {
      s_log.warn("Non hai selezionato una query");
      return szQryFltr;
    }
    int n = m_qry.indexOf(CSZ_QRY_TRUE);
    if (n < 0) {
      s_log.warn("Query \"{}\" malformata", m_qry);
      return szQryFltr;
    }
    String szLeft = m_qry.substring(0, n + CSZ_QRY_TRUE.length());
    String szRight = m_qry.substring(n + CSZ_QRY_TRUE.length());
    StringBuilder szFiltr = new StringBuilder();
    if (Utils.isValue(m_fltrTipoBanca)) {
      szFiltr.append(String.format(" AND tipo='%s'", m_fltrTipoBanca));
    }
    if (Utils.isValue(m_fltrAnnoComp)) {
      szFiltr.append(String.format(" AND movStr like '%d%%'", m_fltrAnnoComp));
    }
    if (Utils.isValue(m_fltrMeseComp)) {
      szFiltr.append(String.format(" AND movStr='%s'", m_fltrMeseComp));
    }

    if (Utils.isValue(m_fltrParola) && m_fltrParola.trim().length() >= 1) {
      if ( !fltrParolaRegEx)
        szFiltr.append(String.format(" AND descr LIKE '%%%s%%'", m_fltrParola));
    }
    if (Utils.isValue(m_fltrWhere) && m_fltrWhere.length() > 3) {
      szFiltr.append(String.format(" AND %s", m_fltrWhere));
    }
    szQryFltr = String.format("%s %s %s", szLeft, szFiltr.toString(), szRight);
    return szQryFltr;
  }

  private void creaTableResultThread(String szQryFltr) {
    // System.out.println("ResultView.creaTableResultThread()");
    TableViewFiller.setNullRetValue("");

    m_tbvf = new TableViewFillerBanca(tblview, m_appmain.getConnSQL());

    // m_tbvf.setResView(this);
    if (fltrParolaRegEx) {
      m_tbvf.setFltrParolaRegEx(fltrParolaRegEx);
      m_tbvf.setFltrParola(m_fltrParola);
    }
    m_tbvf.setSzQry(szQryFltr);
    m_tbvf.setScartaImpTrasf(ckScartaImp.isSelected());

    ExecutorService backGrService = Executors.newFixedThreadPool(1);
    Platform.runLater(() -> {
      lstage.getScene().setCursor(Cursor.WAIT);
      btCerca.setDisable(true);
    });

    try {
      m_tbvf.setOnRunning(ev -> {
        s_log.debug("TableViewFiller task running...");
      });
      m_tbvf.setOnSucceeded(ev -> {
        s_log.debug("TableViewFiller task Finished!");
        Platform.runLater(() -> {
          lstage.getScene().setCursor(Cursor.DEFAULT);
          btCerca.setDisable(false);
          btExportCsv.setDisable(false);
          bSemaf = false;
        });
      });
      m_tbvf.setOnFailed(ev -> {
        s_log.debug("TableViewFiller task failure");
        Platform.runLater(() -> {
          lstage.getScene().setCursor(Cursor.DEFAULT);
          btCerca.setDisable(false);
          btExportCsv.setDisable(false);
          bSemaf = false;
        });
      });
      backGrService.execute(m_tbvf);
    } catch (Exception e) {
      s_log.error("Errore task TableViewFiller");
    }
    backGrService.shutdown();

    // Context menu open document
    MenuItem mi1 = new MenuItem("Vedi Documento");
    mi1.setOnAction((ActionEvent ev) -> {
      tableRow_dblclick(null);
    });
    ContextMenu menu = new ContextMenu();
    menu.getItems().add(mi1);
    // liBanca.setContextMenu(menu);
    tblview.setContextMenu(menu);

    tblview.setRowFactory(tbl -> new TableRow<List<Object>>() {
      {
        setOnMouseClicked(ev -> {
          if (isEmpty())
            return;
          if (ev.getClickCount() == 2) {
            tableRow_dblclick(this);
          }
        });
      }
    });
    tblview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tblview.setOnKeyPressed(e -> tblRigaKeyPressed(e));
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // System.out.printf("ResultView.propertyChange(\"%s=%s\")\n", evt.getPropertyName(), evt.getNewValue().toString());
    String szEvt = evt.getPropertyName();
    switch (szEvt) {

      case DataController.EVT_CODSTAT:
        m_codStatSel = evt.getNewValue().toString();
        Platform.runLater(() -> {
          DataController data = m_appmain.getData();
          CodStat cds = data.getCodStatData().decodeCodStat(m_codStatSel);
          String szLb = "...";
          if (null != cds)
            szLb = cds.getDescr();
          btAssignCodStat.setText(m_codStatSel);
          lbAssignCodStat.setText(szLb);
          abilitaBottoni();
        });
        break;

      case DataController.EVT_FILTER_CODSTAT:
        if (evt.getNewValue() instanceof CodStat cds) {
          String szFltrCodstat = cds.getCodice();
          ActionEvent nevt = new ActionEvent(szFltrCodstat, null);
          Platform.runLater(() -> btCercaClick(nevt));
        }
        break;

      case DataController.EVT_DATASET_CREATED:
        if (evt.getNewValue() instanceof Integer nv) {
          var fmt = NumberFormat.getInstance(Locale.getDefault());
          String szMsg = String.format("Letti %s recs", fmt.format(nv));
          Platform.runLater(() -> lbMsg.setText(szMsg));
        }
        break;

      case DataController.EVT_SELCODSTAT:
        if (evt.getNewValue() instanceof CodStat cds) {
          m_codStatSel = cds.getCodice();
          btAssignCodStatClick(null);
        }
        break;
    }
  }

  protected void tableRow_dblclick(TableRow<List<Object>> row) {
    //    System.out.println("ResultView.tableRow_dblclick(row):" + (null != row ? row.getClass().getSimpleName() : "**null**"));
    List<Object> r = tblview.getSelectionModel().getSelectedItem();
    Dataset dts = m_tbvf.getDataset();
    int nCol = dts.getColumNo(IRigaBanca.IDFILE.getColNam());
    if (nCol < 0 || r.size() <= nCol) {
      s_log.warn("Non trovo la colonna {} sulla Table", IRigaBanca.IDFILE.getColNam());
      return;
    }
    Integer iidFil = (Integer) r.get(nCol);
    if (null == iidFil) {
      s_log.warn("IdFile = {} sulla Table", IRigaBanca.IDFILE.getColNam());
      return;
    }
    Path lastd = dataCntrl.getLastDir();
    CsvFileContainer csvf = dataCntrl.getContCsv();
    ImpFile impf = csvf.getFromIndex(iidFil);
    if (null == impf) {
      s_log.warn("IdFile = {} non memorizzato ?", iidFil);
      return;
    }
    Path fullp = impf.fullPath(lastd);
    try {
      if (Desktop.isDesktopSupported()) {
        s_log.info("Apro il documento  {}", fullp.toString());
        Desktop.getDesktop().open(fullp.toFile());
      } else {
        s_log.error("Desktop not supported");
      }
    } catch (IOException e) {
      s_log.error("Desktop launch error:{}", e.getMessage(), e);
    }
  }

  @FXML
  void btExportCsvClick(ActionEvent event) {
    if (m_qry == null) {
      s_log.warn("Non hai selezionato una query");
      return;
    }
    StringBuilder szFilNam = new StringBuilder().append(cbQuery.getSelectionModel().getSelectedItem());
    if (m_fltrTipoBanca != null) {
      szFilNam.append("_").append(m_fltrTipoBanca);
    }
    if (m_fltrAnnoComp != null) {
      szFilNam.append("_").append(m_fltrAnnoComp);
    }
    @SuppressWarnings("unused") LoadBancaController cntrl = (LoadBancaController) LoadBancaMainApp.getInst().getController();
    szFilNam.append("_").append(ParseData.s_fmtDtDate.format(new Date()).replace(' ', '_').replace(':', '-'));
    szFilNam.append(".csv");
    // System.out.println("ResultView.btExportCsvClick():" + szFilNam.toString());
    m_CSVfile = Paths.get(szFilNam.toString());
    try {
      Dataset dts = m_tbvf.getDataset();
      dts.setCsvBlankOnZero(ckCvsBlankOnZero.isSelected());
      dts.savecsv(m_CSVfile);
      if (ckLanciaExcel.isSelected())
        lanciaExcel2();
    } catch (DatasetException e) {
      s_log.error("Errore export to CSV, err={}", e.getMessage(), e);
    }
    //    Dts2Csv csv = new Dts2Csv(dts);
    //    m_CSVfile = szFilNam.toString();
    //    csv.saveFile(m_CSVfile);
    //    if (cnt
    // rl.isLanciaExc())
    //      lanciaExcel2();
    String szMsg = String.format("Creato il file di export CSV : %s", m_CSVfile.toString());
    m_appmain.messageDialog(AlertType.INFORMATION, szMsg);
    abilitaBottoni();
  }

  public void lanciaExcel2() {
    File fi = m_CSVfile.toFile();
    String sz = fi.getAbsolutePath();
    //    String szCmd = String.format("cmd /c start excel \"%s\"", sz);
    //    szCmd = String.format("\"%s\"", sz);
    ProcessBuilder pb = new ProcessBuilder();
    pb.command("cmd.exe", "/c", "start", "excel.exe", sz);
    pb.redirectErrorStream(true);
    int rc = -1;
    try {
      Process process = pb.start();
      process.getInputStream().transferTo(System.out);
      rc = process.waitFor();
    } catch (IOException e) {
      s_log.error("Errore lancio Excel: {}", e.getMessage(), e);
    } catch (InterruptedException e) {
      s_log.error("Interruzione lancio Excel: {}", e.getMessage(), e);
    }
    if (rc != 0)
      throw new RuntimeException("Start Excel failed rc=" + rc);
  }

}
