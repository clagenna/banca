package prova.junit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.javafx.TableViewFillerBanca;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.MessageDialog;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class ProvaResultView extends Application implements Initializable, IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(ProvaResultView.class);

  public static final String CSZ_FXMLNAME = "ResultView.fxml";

  @FXML
  protected ComboBox<String>      cbTipoBanca;
  @FXML
  protected ComboBox<String>      cbQuery;
  @FXML
  protected ComboBox<Integer>     cbAnnoComp;
  @FXML
  protected ComboBox<String>      cbMeseComp;
  @FXML
  protected TextField             txParola;
  @FXML
  protected TextArea              txWhere;
  @FXML
  private Button                  btCerca;
  @FXML
  protected ComboBox<String>      cbSaveQuery;
  @FXML
  private Button                  btSaveQuery;
  @FXML
  private Button                  btExportCsv;
  @FXML
  private Button                  btMostraCodStat;
  @FXML
  private Button                  btAssignCodStat;
  @FXML
  private Label                   lbAssignCodStat;
  @FXML
  private Button                  btIndovinaCodStat;
  @FXML
  protected CheckBox              ckRegExp;
  @FXML
  private CheckBox                ckScartaImp;
  @FXML
  private CheckBox                ckLanciaExcel;
  @FXML
  private CheckBox                ckCvsBlankOnZero;
  @FXML
  private TableView<List<Object>> tblview;
  @FXML
  private Label                   lbMsg;

  @Getter @Setter
  private DataModel model;
  @Getter @Setter
  private Stage     myStage;
  @Getter @Setter
  private Scene     myScene;

  private Integer m_fltrAnnoComp;
  private String  m_fltrMeseComp;
  private String  m_fltrWhere;
  private String  m_fltrParola;
  private String  m_fltrTipoBanca;

  private CodStat              m_codStatSel;
  private Path                 m_CSVfile;
  @SuppressWarnings("unused")
  private TableViewFillerBanca m_tbvf;
  private String               m_qry;
  @SuppressWarnings("unused")
  private boolean              bSemaf;

  public ProvaResultView() {
    m_fltrTipoBanca = null;
    m_fltrAnnoComp = null;
    m_fltrMeseComp = null;
    m_fltrWhere = null;
    m_fltrParola = null;
    bSemaf = false;
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    // System.out.printf("ProvaResultView.start() - primaryStage=%s\n", primaryStage);
    s_log.info("ProvaResultView.start() - primaryStage={}\n", primaryStage);
    setMyStage(primaryStage);
    buildForm();

    // scene.widthProperty().addListener(_ -> resized());
    // scene.heightProperty().addListener(_ -> resized());
    // primaryStage.setOnShown(_ -> buildForm());
    assert myScene != null : "Scene is null, cannot set on primary stage";
    myStage.setScene(myScene);
    myStage.setWidth(800);
    myStage.setHeight(600);
    myStage.setTitle("Java FX Result View Example");
    myStage.show();
  }

  @Override
  public void initialize(URL p_location, ResourceBundle p_resources) {
    // System.out.printf("ProvaResultView.initialize() - location=%s, resources=%s\n", p_location, p_resources);
    s_log.info("ProvaResultView.initialize() - location={}, resources={}", p_location, p_resources);
  }

  @Override
  public void initApp(AppProperties p_props) {
    // System.out.printf("ProvaResultView.initApp()\n");
    s_log.info("ProvaResultView.initApp()\n");
    buildForm();
    //    URL url = getClass().getResource(CSZ_FXMLNAME);
    //    if (url == null)
    //      url = getClass().getClassLoader().getResource(CSZ_FXMLNAME);
    //    Parent radice;
    //    FXMLLoader fxmlLoad = new FXMLLoader(url);
    //    try {
    //      radice = fxmlLoad.load();
    //    } catch (IOException e) {
    //      s_log.error("Errore caricamento FXML {}", CSZ_FXMLNAME, e);
    //      Platform.runLater(() -> {
    //        MessageDialog.messageDialog(AlertType.ERROR, "Errore caricamento FXML " + CSZ_FXMLNAME + "\n" + e.getMessage());
    //      });
    //      Platform.exit();
    //      return; // inutile, ma serve al compilatore per non lamentarsi di radice non inizializzata
    //    }
    //    myScene = new Scene(radice);
    if (null != myStage)
      myStage.setScene(myScene);
  }

  @Override
  public void closeApp(AppProperties p_props) {
    // TODO Auto-generated method stub

  }

  /**
   * Costruisce il form e lo restituisce come Scene. Se myStage non è null,
   * imposta la scena sullo stage.
   *
   * @param pstage
   *          lo stage su cui impostare la scena, se non è null
   * @return la scena costruita
   */
  public Scene buildForm() {
    URL url = getClass().getResource(CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(CSZ_FXMLNAME);
    Parent radice;
    FXMLLoader fxmlLoad = new FXMLLoader(url);
    try {
      // radice = FXMLLoader.load(url);
      radice = fxmlLoad.load();
      // cntrResultView = fxmlLoad.getController();
    } catch (IOException e) {
      Platform.runLater(() -> {
        MessageDialog.messageDialog(AlertType.ERROR, "Errore caricamento FXML " + CSZ_FXMLNAME + "\n" + e.getMessage());
      });
      Platform.exit();
      return null;
    }
    myScene = new Scene(radice);
    return myScene;
  }

  @FXML
  void cbTipoBancaSel(ActionEvent event) {
    m_fltrTipoBanca = cbTipoBanca.getSelectionModel().getSelectedItem();
    // System.out.printf("ProvaResultView.cbTipoBancaSel(%s)\n", m_fltrTipoBanca);
    s_log.info("ProvaResultView.cbTipoBancaSel({})", m_fltrTipoBanca);
    //    s_log.debug("ResultView.cbTipoBancaSel(\"{}\"):", m_fltrTipoBanca);
    //    abilitaBottoni();
  }

  @FXML
  void cbAnnoCompSel(ActionEvent event) {
    m_fltrAnnoComp = cbAnnoComp.getSelectionModel().getSelectedItem();
    // System.out.printf("ProvaResultView.cbAnnoCompSel(%s)\n", m_fltrAnnoComp);
    s_log.info("ProvaResultView.cbAnnoCompSel({})", m_fltrAnnoComp);
    //    if (ii == null) {
    //      m_fltrAnnoComp = null;
    //      model.setAnnoComp(0);
    //      caricaComboMesecomp();
    //      abilitaBottoni();
    //      return;
    //    }
    //    m_fltrAnnoComp = ii;
    //    model.setAnnoComp(m_fltrAnnoComp);
    //    s_log.debug("ResultView.cbAnnoCompSel({}):", m_fltrAnnoComp);
    //    caricaComboMesecomp();
    //    abilitaBottoni();
  }

  @FXML
  void cbMeseCompSel(ActionEvent event) {
    m_fltrMeseComp = cbMeseComp.getSelectionModel().getSelectedItem();
    // System.out.printf("ProvaResultView.cbMeseCompSel(%s)\n", m_fltrMeseComp);
    s_log.info("ProvaResultView.cbMeseCompSel({})", m_fltrMeseComp);
    //    if (bSemaf)
    //      return;
    //    s_log.debug("ResultView.cbMeseCompSel(\"{}\"):", m_fltrMeseComp);
    //    abilitaBottoni();
  }

  @FXML
  void cbQuerySel(ActionEvent event) {
    String szK = cbQuery.getSelectionModel().getSelectedItem();
    // System.out.printf("ProvaResultView.cbQuerySel(%s)\n", szK);
    s_log.info("ProvaResultView.cbQuerySel({})", szK);
    //    if (null == szK)
    //      m_qry = null;
    //    else
    //      m_qry = m_mapQry.get(szK);
    //    model.setComboQuery(m_qry); // ??
    //    s_log.debug("ResultView.cbQuerySel():" + szK);
    //    abilitaBottoni();
  }

  @FXML
  void txWhereSel(ObservableValue<? extends String> obj, String old, String nval) {
    //    if (bSemaf)
    //      return;
    m_fltrWhere = nval;
    // System.out.printf("ProvaResultView.txWhereSel(%s)\n", m_fltrWhere);
    s_log.info("ProvaResultView.txWhereSel({})", m_fltrWhere);
    //    // s_log.debug("ResultView.txWhereSel({}):", m_fltrWhere);
    //    abilitaBottoni();
  }

  @FXML
  void txParolaSel(ObservableValue<? extends String> obj, String old, String nval) {
    //    // System.out.printf("ResultView.txParolaSel(%s)\n", nval);
    m_fltrParola = nval;
    // System.out.printf("ProvaResultView.txParolaSel(%s)\n", m_fltrParola);
    s_log.info("ProvaResultView.txParolaSel({})", m_fltrParola);
    //    // s_log.debug("ResultView.txWhereSel({}):", m_fltrWhere);
    //    abilitaBottoni();
  }

  @FXML
  void btSaveQueryClick(ActionEvent event) {
    String szNam = cbSaveQuery.getSelectionModel().getSelectedItem();
    // System.out.printf("ProvaResultView.btSaveQueryClick(%s)\n", szNam);
    s_log.info("ProvaResultView.btSaveQueryClick({})", szNam);
    //    if ( !m_gestQry.saveQuery(szNam))
    //      MessageDialog.messageDialog(AlertType.ERROR, m_gestQry.getErrorMesg());
    //    else
    //      m_gestQry.caricaCombo(cbSaveQuery);
  }

  @FXML
  void cbSaveQuerySel(ActionEvent event) {
    String szNamQry = cbSaveQuery.getSelectionModel().getSelectedItem();
    // System.out.printf("ProvaResultView.cbSaveQuerySel(%s)\n", szNamQry);
    s_log.info("ProvaResultView.cbSaveQuerySel({})", szNamQry);
    // System.out.printf("ResultView.cbSaveQuerySel(%s)\n", cbSaveQuery.getSelectionModel().getSelectedItem());
    //    if (bSemaf)
    //      return;
    //    try {
    //      bSemaf = true;
    //      m_gestQry.readQuery(cbSaveQuery.getSelectionModel().getSelectedItem());
    //    } finally {
    //      bSemaf = false;
    //    }
  }

  @FXML
  void ckRegExpClick(ActionEvent event) {
    // System.out.println("ProvaResultView.ckRegExpClick()");
    s_log.info("ProvaResultView.ckRegExpClick()");
    //    setFltrParolaRegEx(ckRegExp.isSelected());
  }

  @FXML
  void btMostraCodStatClick(ActionEvent event) {
    // System.out.println("ResultView.btMostraCodStatClick()");
    s_log.info("ResultView.btMostraCodStatClick()");
    // LoadBancaController cntr = (LoadBancaController) m_appmain.getController();
    // cntr.mnuConfMostraCodStatClick(event);
  }

  @FXML
  void btAssignCodStatClick(ActionEvent event) {
    // System.out.println("ProvaResultView.btAssignCodStatClick()");
    s_log.info("ProvaResultView.btAssignCodStatClick()");
    // s_log.debug("ResultView.btAssignCodStatClick() - codStatSel={}", m_codStatSel);
    // System.out.println(StackViewer.viewStackTrace("btAssignCodStatClick()"));
    //    if ( !Utils.isValue(m_codStatSel))
    //      return;
    //    ObservableList<List<Object>> locLi = tblview.getSelectionModel().getSelectedItems();
    //    if (null == locLi || locLi.size() == 0) {
    //      String szMsg = String.format("Nessun record di ResView selezionato per l'assegnamento di %s", m_codStatSel);
    //      s_log.debug(szMsg);
    //      // MessageDialog.messageDialog(AlertType.WARNING, szMsg);
    //      liSelRowsForCodStat = null;
    //      return;
    //    }
    //    // faccio una copia dei table row selezionati sulla ResView
    //    liSelRowsForCodStat = new ArrayList<List<Object>>();
    //    for (List<Object> el : locLi) {
    //      List<Object> tmp = new ArrayList<Object>();
    //      tmp.addAll(el);
    //      liSelRowsForCodStat.add(tmp);
    //    }
    //    Platform.runLater(() -> {
    //      lstage.getScene().setCursor(Cursor.WAIT);
    //      btAssignCodStat.setDisable(true);
    //    });
    //    // System.out.printf("ResultView.btAssignCodStatClick(sel=%d)\n", li.size());
    //    try {
    //      m_db.beginTrans();
    //      for (List<Object> elem : liSelRowsForCodStat) {
    //        RigaBanca riga = RigaBanca.parse(elem);
    //        riga.setCodstat(m_codStatSel.getCodice());
    //        m_db.updateCodStat(riga);
    //        elem.set(EColsTableView.codstat.getColNo(), m_codStatSel);
    //      }
    //    } finally {
    //      m_db.commitTrans();
    //    }
    //    s_log.info("Aggegnato cod. stat. {} a {} records", m_codStatSel, liSelRowsForCodStat.size());
    //    Platform.runLater(() -> {
    //      btAssignCodStat.setDisable(false);
    //      lstage.getScene().setCursor(Cursor.DEFAULT);
    //      btCercaClick(null);
    //    });

    // s_log.debug("END -- btAssignCodStatClick() (lanciato btCercaClick() alla fine)");
  }

  @FXML
  void btCercaClick(ActionEvent event) {
    // System.out.printf("ResultView.btCercaClick(semaf=%s)\n", Boolean.toString(bSemafCercaClick));
    // System.out.println(StackViewer.viewStackTrace("btCercaClick()"));
    //    if (bSemafCercaClick)
    //      return;
    // chiamando btCercaClick() dal propertyChange( EVT_FILTER_CODSTAT )(piu sotto)
    // ricevo 2 chiamate consecutive !?! Per cui bSema viene spento solo alla fine del thread
    // creaTableResultThread(szQryFltr);
    //    System.out.printf("ResultView.btCercaClick(semaf=%s)\n", Boolean.toString(bSemafCercaClick));
    //    printStackTrace();
    //    bSemafCercaClick = true;
    try {
      if (null != event) {
        if (event.getSource() instanceof String szCodstat) {
          m_fltrWhere = String.format("codstat like '%s%%'", szCodstat);
          txWhere.setText(m_fltrWhere);
          // System.out.printf("ProvaResultView.btCercaClick(codstat=%s)\n", szCodstat);
          s_log.info("ProvaResultView.btCercaClick(codstat={})\n", szCodstat);
        }
      }
    } finally {
      // bSemafCercaClick = false;
    }
    //    try {
    //      String szQryFltr = creaQuery();
    //      // test validita query
    //      DBConn dbc = m_db.getDbconn();
    //      if ( !dbc.testQuery(szQryFltr))
    //        return;
    //      creaTableResultThread(szQryFltr);
    //      abilitaBottoni();
    //    } catch (Exception e) {
    //      e.printStackTrace();
    //    } finally {
    //      bSemafCercaClick = false;
    //      // System.out.println("FINE ResultView.btCercaClick()\n");
    //    }
  }

  @FXML
  void btIndovinaCodStatClick(ActionEvent event) {
    // System.out.println("ProvaResultView.btIndovinaCodStatClick()");
    s_log.info("ProvaResultView.btIndovinaCodStatClick()");
    // LoadBancaController cntr = (LoadBancaController) m_appmain.getController();
    // cntr.mnuConfMostraGuessCodStatClick(null);
  }

  @FXML
  void btExportCsvClick(ActionEvent event) {
    //    if (m_qry == null) {
    //      s_log.warn("Non hai selezionato una query");
    //      return;
    //    }
    StringBuilder szFilNam = new StringBuilder().append(cbQuery.getSelectionModel().getSelectedItem());
    if (m_fltrTipoBanca != null)
      szFilNam.append("_").append(m_fltrTipoBanca);
    if (m_fltrAnnoComp != null) {
      szFilNam.append("_").append(m_fltrAnnoComp);
    }
    //    @SuppressWarnings("unused") LoadBancaController cntrl = (LoadBancaController) LoadBancaMainApp.getInst().getController();
    szFilNam.append("_").append(ParseData.s_fmtDtDate.format(new Date()).replace(' ', '_').replace(':', '-'));
    szFilNam.append(".csv");
    m_CSVfile = Paths.get(szFilNam.toString());
    //    try {
    //      Dataset dts = m_tbvf.getDataset();
    //      dts.setCsvBlankOnZero(ckCvsBlankOnZero.isSelected());
    //      dts.savecsv(m_CSVfile);
    //      if (ckLanciaExcel.isSelected())
    //        lanciaExcel2();
    //        } catch (DatasetException e) {
    //          s_log.error("Errore export to CSV, err={}", e.getMessage(), e);
    //        }
    //    Dts2Csv csv = new Dts2Csv(dts);
    //    m_CSVfile = szFilNam.toString();
    //    csv.saveFile(m_CSVfile);
    //    if (cnt
    // rl.isLanciaExc())
    //      lanciaExcel2();
    String szMsg = String.format("Creato il file di export CSV : %s", m_CSVfile.toString());
    MessageDialog.messageDialog(AlertType.INFORMATION, szMsg);
    abilitaBottoni();
  }

  private void abilitaBottoni() {
    boolean bv = Utils.isValue(m_qry);
    bSemaf = false;
    btCerca.setDisable( !bv);
    btExportCsv.setDisable( !bv);
    btAssignCodStat.setDisable( !Utils.isValue(m_codStatSel));
    if (bv) {
      ObservableList<List<Object>> li = tblview.getItems();
      bv = li != null && li.size() > 2;
      btExportCsv.setDisable( !bv);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // TODO Auto-generated method stub

  }

  @Override
  public void changeSkin() {
    // TODO Auto-generated method stub

  }

  public static void main(String[] args) {
    Application.launch(args);
  }

}
