package sm.clagenna.banca.javafx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sys.ex.DatasetException;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

// FIXME Emettere un errore su clausola WHERE sbagliata
public class ResultView implements Initializable, IStartApp {
  private static final Logger s_log = LogManager.getLogger(ResultView.class);

  public static final String  CSZ_FXMLNAME          = "ResultView.fxml";
  private static final String CSZ_PROP_POSRESVIEW_X = "resview.x";
  private static final String CSZ_PROP_POSRESVIEW_Y = "resview.y";
  private static final String CSZ_PROP_DIMRESVIEW_X = "resview.lx";
  private static final String CSZ_PROP_DIMRESVIEW_Y = "resview.ly";
  private static final String CSZ_QRY_TRUE          = "1=1";

  @FXML
  private ComboBox<String>  cbTipoBanca;
  @FXML
  private ComboBox<Integer> cbAnnoComp;
  @FXML
  private ComboBox<String>  cbMeseComp;
  @FXML
  private TextField         txParola;
  @FXML
  private TextArea          txWhere;
  @FXML
  private ComboBox<String>  cbQuery;
  @FXML
  private Button            btCerca;
  @FXML
  private Button            btExportCsv;
  @FXML
  private CheckBox          ckLanciaExcel;
  @FXML
  private CheckBox          ckCvsBlankOnZero;

  @FXML
  private TableView<List<Object>> tblview;

  @Getter @Setter
  private Scene myScene;
  private Stage lstage;
  //   private AppProperties       m_prQries;
  private LoadBancaMainApp    m_appmain;
  private AppProperties       m_mainProps;
  private ISQLGest            m_db;
  private Map<String, String> m_mapQry;

  private Integer m_fltrAnnoComp;
  private String  m_fltrMeseComp;
  private String  m_fltrWhere;
  private String  m_fltrParola;
  private String  m_qry;

  private TableViewFiller m_tbvf;
  private Path            m_CSVfile;
  private String          m_fltrTipoBanca;
  @Getter @Setter
  private boolean         csvBlankOnZero;

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
    m_mainProps = m_appmain.getProps();
    String szSQLType = p_props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    m_db = SqlGestFactory.get(szSQLType);
    m_db.setDbconn(LoadBancaMainApp.getInst().getConnSQL());

    caricaComboTipoBanca();
    caricaComboAnno();
    caricaComboMesecomp();
    // caricaComboQueries();
    caricaComboQueriesFromDB();
    txParola.textProperty().addListener((obj, old, nv) -> txParolaSel(obj, old, nv));
    txWhere.textProperty().addListener((obj, old, nv) -> txWhereSel(obj, old, nv));
    impostaForma(m_mainProps);
    if (lstage != null)
      lstage.setOnCloseRequest(e -> {
        closeApp(m_mainProps);
      });
    abilitaBottoni();
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
    List<String> li = m_db.getListMeseComp();
    cbMeseComp.getItems().clear();
    cbMeseComp.getItems().add((String) null);
    cbMeseComp.getItems().addAll(li);
  }

  @SuppressWarnings("unused")
  private void caricaComboQueries() {
    m_mapQry = new HashMap<>();
    List<String> liQry = new ArrayList<>();
    Set<Object> keys = m_mainProps.getProperties().keySet();
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
    if (px != -1 && py != -1 && px * py != 0) {
      lstage.setX(px);
      lstage.setY(py);
      lstage.setWidth(dx);
      lstage.setHeight(dy);
    }
    myScene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> gestKey(ev));
    URL url = m_appmain.getUrlCSS();
    if (null != url)
      myScene.getStylesheets().add(url.toExternalForm());
  }

  private Object gestKey(KeyEvent ev) {
    // System.out.printf("ResultView.gestKey(%s)\n", ev.toString());
    if (/* ev.isControlDown() && */ ev.getCode() == KeyCode.ENTER) {
      ev.consume();
      btCercaClick(null);
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
    m_appmain.removeResView(this);
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
    m_fltrWhere = nval;
    // s_log.debug("ResultView.txWhereSel({}):", m_fltrWhere);
    abilitaBottoni();
  }

  @FXML
  void txParolaSel(ObservableValue<? extends String> obj, String old, String nval) {
    m_fltrParola = nval;
    // s_log.debug("ResultView.txWhereSel({}):", m_fltrWhere);
    abilitaBottoni();
  }

  private void abilitaBottoni() {
    boolean bv = Utils.isValue(m_qry);
    btCerca.setDisable( !bv);
    btExportCsv.setDisable( !bv);
    if (bv) {
      ObservableList<List<Object>> li = tblview.getItems();
      bv = li != null && li.size() > 2;
      btExportCsv.setDisable( !bv);
    }
  }

  @FXML
  void btCercaClick(ActionEvent event) {
    System.out.println("ResultView.btCercaClick()");
    String szQryFltr = creaQuery();
    creaTableResultThread(szQryFltr);
    abilitaBottoni();
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
    if (m_fltrTipoBanca != null) {
      szFiltr.append(String.format(" AND tipo='%s'", m_fltrTipoBanca));
    }
    if (m_fltrAnnoComp != null) {
      szFiltr.append(String.format(" AND movStr like '%d%%'", m_fltrAnnoComp));
    }
    if (m_fltrMeseComp != null) {
      szFiltr.append(String.format(" AND movStr='%s'", m_fltrMeseComp));
    }

    if (null != m_fltrParola && m_fltrParola.trim().length() >= 1) {
      szFiltr.append(String.format(" AND descr LIKE '%%%s%%'", m_fltrParola));
    }
    if (null != m_fltrWhere && m_fltrWhere.length() > 3) {
      szFiltr.append(String.format(" AND %s", m_fltrWhere));
    }
    szQryFltr = String.format("%s %s %s", szLeft, szFiltr.toString(), szRight);
    return szQryFltr;
  }

  //  @SuppressWarnings("unused")
  //  private void creaTableResult(String szQryFltr) {
  //    m_tbvf = new TableViewFiller(tblview);
  //    tblview = m_tbvf.openQuery(szQryFltr);
  //    tblview.setRowFactory(tbl -> new TableRow<List<Object>>() {
  //      {
  //        setOnMouseClicked(ev -> {
  //          if (isEmpty())
  //            return;
  //          if (ev.getClickCount() == 2) {
  //            tableRow_dblclick(this);
  //          }
  //        });
  //      }
  //    });
  //    tblview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  //  }

  private void creaTableResultThread(String szQryFltr) {
    TableViewFiller.setNullRetValue("");
    m_tbvf = new TableViewFiller(tblview);
    m_tbvf.setSzQry(szQryFltr);

    ExecutorService backGrService = Executors.newFixedThreadPool(1);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        lstage.getScene().setCursor(Cursor.WAIT);
        btCerca.setDisable(true);
      }
    });

    try {
      m_tbvf.setOnRunning(ev -> {
        s_log.debug("TableViewFiller task running...");
      });
      m_tbvf.setOnSucceeded(ev -> {
        s_log.debug("TableViewFiller task Finished!");
        endTask();
      });
      m_tbvf.setOnFailed(ev -> {
        s_log.debug("TableViewFiller task failure");
        endTask();
      });
      backGrService.execute(m_tbvf);
    } catch (Exception e) {
      s_log.error("Errore task TableViewFiller");
    }
    backGrService.shutdown();
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
  }

  private void endTask() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        lstage.getScene().setCursor(Cursor.DEFAULT);
        btCerca.setDisable(false);
        btExportCsv.setDisable(false);
      }
    });
  }

  protected void tableRow_dblclick(TableRow<List<Object>> row) {
    //    System.out.println("ResultView.tableRow_dblclick(row):" + (null != row ? row.getClass().getSimpleName() : "**null**"));
    List<Object> r = tblview.getSelectionModel().getSelectedItem();
    @SuppressWarnings("unused") String szPdf = null;
    if (null != r) {
      // System.out.println("r.=" + r.toString());
      for (Object e : r) {
        if (null != e) {
          String sz = e.toString();
          if (sz.toLowerCase().endsWith(".pdf")) {
            szPdf = sz;
            break;
          }
        }
      }
      // System.out.println("PDF = " + szPdf);
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
