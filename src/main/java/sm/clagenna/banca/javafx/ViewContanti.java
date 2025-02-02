package sm.clagenna.banca.javafx;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.javafx.TableViewFiller;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class ViewContanti implements Initializable, IStartApp {
  private static final Logger s_log                  = LogManager.getLogger(ViewContanti.class);
  public static final String  CSZ_FXMLNAME           = "ViewContanti.fxml";
  private static final String CSZ_PROP_POSVIEWCONT_X = "ViewCont.x";
  private static final String CSZ_PROP_POSVIEWCONT_Y = "ViewCont.y";
  private static final String CSZ_PROP_DIMVIEWCONT_X = "ViewCont.lx";
  private static final String CSZ_PROP_DIMVIEWCONT_Y = "ViewCont.ly";
  private static final String CSZ_PROP_SPLITPOS      = "ViewCont.spltpos";
  private static final String CSZ_Contanti           = "Contanti";
  private static final String CSZ_AND                = " and ";
  private static final String CSZ_QRY_TRUE           = "1=1";
  private static final String CSZ_QRY_MOVIMALL       = "SELECT "                                //
      + "id,"                                                                                   //
      + "dtmov,"                                                                                //
      + "dtval,"                                                                                //
      + "dare,"                                                                                 //
      + "avere,"                                                                                //
      + "descr,"                                                                                //
      + "abicaus,"                                                                              //
      + "cardid"                                                                                //
      + " FROM Movimenti WHERE 1=1 ORDER BY dtmov";

  @FXML
  private SplitPane               spltPane;
  @FXML
  private ComboBox<EModalitaView> cbModalita;
  @FXML
  private TextField               txId;
  @FXML
  private TextField               txDtmov;
  @FXML
  private TextField               txDtval;
  @FXML
  private ComboBox<String>        cbCausABI;
  @FXML
  private TextField               txDare;
  @FXML
  private TextField               txAvere;
  @FXML
  private ComboBox<String>        cbProprietario;
  @FXML
  private TextField               txDescr;
  @FXML
  private Button                  btCerca;
  //  @FXML
  //  private Button                  btInsert;
  //  @FXML
  //  private Button                  btModifica;
  //  @FXML
  //  private Button                  btElimina;

  @FXML
  private TableView<List<Object>> tblview;
  @FXML
  private Label                   lblMesg;

  @Getter @Setter
  private Scene            myScene;
  private Stage            lstage;
  private LoadBancaMainApp m_appmain;
  // private AppProperties     m_mainProps;
  private ISQLGest        m_db;
  private EModalitaView   modalita;
  private TableViewFiller m_tbvf;
  private RigaBanca       contante;
  //  private PreparedStatement stmtIns;
  // private PreparedStatement stmtMod;
  //  private PreparedStatement stmtDel;
  private String szQryWhere;

  public ViewContanti() {
    //
  }

  @Override
  public void initApp(AppProperties p_props) {
    // m_mainProps = p_props;
    m_appmain = LoadBancaMainApp.getInst();
    m_appmain.addViewContanti(this);
    contante = new RigaBanca(CSZ_Contanti);
    String szSQLType = p_props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    m_db = SqlGestFactory.get(szSQLType);
    m_db.setDbconn(LoadBancaMainApp.getInst().getConnSQL());

    impostaForma(p_props);
    caricaComboModalita();
    caricaComboCausABI();
    caricaComboCardHolder();
    addChangeListeners();
    abilitaBottoni();
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

    int px = p_props.getIntProperty(CSZ_PROP_POSVIEWCONT_X);
    int py = p_props.getIntProperty(CSZ_PROP_POSVIEWCONT_Y);
    int dx = p_props.getIntProperty(CSZ_PROP_DIMVIEWCONT_X);
    int dy = p_props.getIntProperty(CSZ_PROP_DIMVIEWCONT_Y);
    var mm = JFXUtils.getScreenMinMax(px, py, dx, dy);
    if (mm.poxX() != -1 && mm.posY() != -1 && mm.poxX() * mm.posY() != 0) {
      lstage.setX(mm.poxX());
      lstage.setY(mm.posY());
      lstage.setWidth(mm.width());
      lstage.setHeight(mm.height());
    }
    double spltPos = p_props.getDoubleProperty(CSZ_PROP_SPLITPOS, -1.);
    if (spltPos > 0.)
      spltPane.setDividerPositions(spltPos);
    modalita = EModalitaView.Ricerca;
    btCerca.setText("Cerca");
    URL url = m_appmain.getUrlCSS();
    if (null != url)
      myScene.getStylesheets().add(url.toExternalForm());
  }

  private void caricaComboModalita() {
    cbModalita.getItems().clear();
    cbModalita.getItems().add((EModalitaView) null);
    cbModalita.getItems().addAll(EModalitaView.values());
    cbModalita.getSelectionModel().select(modalita);
  }

  private void caricaComboCausABI() {
    List<String> li = m_db.getListCausABI();
    cbCausABI.getItems().clear();
    cbCausABI.getItems().add((String) null);
    cbCausABI.getItems().addAll(li);
  }

  private void caricaComboCardHolder() {
    List<String> li = m_db.getListCardHolder();
    cbProprietario.getItems().clear();
    cbProprietario.getItems().add((String) null);
    cbProprietario.getItems().addAll(li);
  }

  private void addChangeListeners() {
    txId.textProperty().addListener((ob, old, nv) -> {
      contante.setRigaid(Integer.parseInt(nv));
      // updateRowList();
    });
    txDtmov.textProperty().addListener((ob, old, nv) -> {
      contante.setDtmov(ParseData.guessData(nv));
      if (null == contante.getDtval()) {
        if (null != nv && nv.length() >= 10)
          txDtval.setText(nv);
      }
    });
    txDtval.textProperty().addListener((ob, old, nv) -> {
      contante.setDtval(ParseData.guessData(nv));
    });
    txDare.textProperty().addListener((ob, old, nv) -> {
      contante.setDare(Utils.parseDouble(nv));
    });
    txAvere.textProperty().addListener((ob, old, nv) -> {
      contante.setAvere(Utils.parseDouble(nv));
    });
    txDescr.textProperty().addListener((ob, old, nv) -> {
      contante.setDescr(nv);
    });
    cbProprietario.getSelectionModel().selectedItemProperty().addListener((opt, old, nv) -> {
      contante.setCardid(nv);
    });
    cbCausABI.getSelectionModel().selectedItemProperty().addListener((opt, old, nv) -> {
      contante.setAbicaus(estraiCausABI(nv));
    });
  }
  //
  //  private void updateRowList() {
  //    List<Object> row = tblview.getSelectionModel().getSelectedItem();
  //    System.out.println("ViewContanti.updateRowList() ---  DA COMPLETARE");
  //  }

  private void abilitaBottoni() {
    if (null == modalita) {
      btCerca.setDisable(true);
      abilitaCampi(true);
      return;
    }
    switch (modalita) {
      case Eliminazione:
        abilitaCampi(true);
        break;
      case Modifica:
        abilitaCampi(false);
        break;
      case Inserimento:
        abilitaCampi(false);
        // trovaMaxId();
        break;
      case Ricerca:
        azzeraCampi();
        abilitaCampi(false);
        break;
      default:
        abilitaCampi(true);
        break;
    }
  }

  private void azzeraCampi() {
    txId.setText("");
    txDtmov.setText("");
    txDtval.setText("");
    cbCausABI.getSelectionModel().select(0);
    txDare.setText("");
    txAvere.setText("");
    cbProprietario.getSelectionModel().select(0);
    txDescr.setText("");
  }

  private void abilitaCampi(boolean bv) {
    txId.setDisable( !bv);
    txDtmov.setDisable(bv);
    txDtval.setDisable(bv);
    cbCausABI.setDisable(bv);
    txDare.setDisable(bv);
    txAvere.setDisable(bv);
    cbProprietario.setDisable(bv);
    txDescr.setDisable(bv);
  }

  @FXML
  void cbModalitaSel(ActionEvent event) {
    EModalitaView lmod = cbModalita.getSelectionModel().getSelectedItem();
    if (null == lmod || modalita == lmod)
      return;
    modalita = lmod;
    switch (modalita) {
      case Eliminazione:
        btCerca.setText("Elimina");
        break;
      case Inserimento:
        btCerca.setText("Insert");
        break;
      case Modifica:
        btCerca.setText("Modifica");
        break;
      case Ricerca:
        btCerca.setText("Cerca");
        break;
      default:
        break;
    }
    s_log.debug("ViewContanti.cbModalita({}):", modalita.toString());
    abilitaBottoni();
  }

  @FXML
  void btCercaClick(ActionEvent event) {
    switch (modalita) {
      case Eliminazione:
        deleteRecord();
        caricaListView();
        break;
      case Inserimento:
        insertRecord();
        caricaListView();
        break;
      case Modifica:
        updateRecord();
        caricaListView();
        break;
      case Ricerca:
        szQryWhere = creaWhere();
        caricaListView();
        break;
      default:
        break;
    }
  }

  private void caricaListView() {
    if (null == szQryWhere)
      szQryWhere = creaWhere();
    creaTableResultThread(szQryWhere);
    abilitaBottoni();
  }

  private String creaWhere() {
    String szWhere = String.format("tipo='%s'", CSZ_Contanti);
    String szAnd = CSZ_AND;
    String sz = txDtmov.getText().trim();
    if (null != sz && sz.length() > 1) {
      szWhere += String.format("%sdtmov='%s'", szAnd, sz);
      szAnd = CSZ_AND;
    }

    sz = txDtval.getText().trim();
    if (null != sz && sz.length() > 1) {
      szWhere += String.format("%sdtval='%s'", szAnd, sz);
      szAnd = CSZ_AND;
    }

    sz = txDare.getText().trim();
    if (null != sz && sz.length() > 0) {
      szWhere += String.format("%sdare=%s", szAnd, sz);
      szAnd = CSZ_AND;
    }

    sz = txAvere.getText().trim();
    if (null != sz && sz.length() > 0) {
      szWhere += String.format("%savere=%s", szAnd, sz);
      szAnd = CSZ_AND;
    }

    sz = cbCausABI.getSelectionModel().getSelectedItem();
    sz = estraiCausABI(sz);
    if (null != sz) {
      szWhere += String.format("%sabicaus='%s'", szAnd, sz);
      szAnd = CSZ_AND;
    }

    sz = cbProprietario.getSelectionModel().getSelectedItem();
    if (null != sz) {
      szWhere += String.format("%scardid='%s'", szAnd, sz);
      szAnd = CSZ_AND;
    }
    int n = CSZ_QRY_MOVIMALL.indexOf(CSZ_QRY_TRUE);
    String szLeft = CSZ_QRY_MOVIMALL.substring(0, n + CSZ_QRY_TRUE.length());
    String szRight = CSZ_QRY_MOVIMALL.substring(n + CSZ_QRY_TRUE.length());
    String szQryFltr = String.format("%s %s %s", szLeft, szWhere, szRight);
    return szQryFltr;
  }

  @SuppressWarnings("unused")
  private void creaTableResult(String szQryFltr) {
    m_tbvf = new TableViewFiller(tblview, m_appmain.getConnSQL());
    m_tbvf.setSzQry(szQryFltr);
    try {
      m_tbvf.call();
    } catch (Exception e) {
      e.printStackTrace();
    }
    tblview = m_tbvf.getTableview();
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
    tblview.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
  }

  private void creaTableResultThread(String szQryFltr) {
    TableViewFiller.setNullRetValue("");
    m_tbvf = new TableViewFiller(tblview, m_appmain.getConnSQL());
    m_tbvf.setSzQry(szQryFltr);

    ExecutorService backGrService = Executors.newFixedThreadPool(1);
    Platform.runLater(() -> {
      lstage.getScene().setCursor(Cursor.WAIT);
      btCerca.setDisable(true);
    });

    try {
      m_tbvf.setOnRunning(ev -> {
        s_log.debug("TableViewFiller(2) task running...");
      });
      m_tbvf.setOnSucceeded(ev -> {
        s_log.debug("TableViewFiller(2) task Finished!");
        endTask();
      });
      m_tbvf.setOnFailed(ev -> {
        s_log.debug("TableViewFiller(2) task failure");
        endTask();
      });
      backGrService.execute(m_tbvf);
    } catch (Exception e) {
      s_log.error("Errore task TableViewFiller(2)");
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
    Platform.runLater(() -> {
      lstage.getScene().setCursor(Cursor.DEFAULT);
      btCerca.setDisable(false);
    });
  }

  protected void tableRow_dblclick(TableRow<List<Object>> row) {
    switch (modalita) {
      case Eliminazione:
        break;
      case Inserimento:
        vaiInModalitaModifica();
        break;
      case Modifica:
        break;
      case Ricerca:
        vaiInModalitaModifica();
        break;
    }
    List<Object> currRow = tblview.getSelectionModel().getSelectedItem();
    if (null == currRow) {
      s_log.warn("Null su doppio click di riga ?!");
      return;
    }
    //    id{INTEGER}=%16d    col=col:id(0)[INTEGER]
    // dtmov{DATE}=%16s       col=col:dtmov(1)[DATE]
    // dtval{DATE}=%16s       col=col:dtval(2)[DATE]
    //  dare{DECIMAL}=%16.6f  col=col:dare(3)[DECIMAL]
    // avere{DECIMAL}=%16.6f  col=col:avere(4)[DECIMAL]
    // descr{VARCHAR}=%-16s   col=col:descr(5)[NVARCHAR]
    //abicaus{VARCHAR}=%-16s  col=col:abicaus(6)[VARCHAR]
    //cardid{VARCHAR}=%-16s   col=col:cardid(7)[NVARCHAR]
    Locale locale = Utils.getLocale();
    Utils.setLocale(Locale.US);
    int k = 0;
    for (Object e : currRow) {
      switch (k++) {
        case 0: // id
          contante.setRigaid(Integer.parseInt(e.toString()));
          txId.setText(String.format("%d", contante.getRigaid()));
          break;
        case 1: // dtmov
          contante.setDtmov(ParseData.parseData(e.toString()));
          txDtmov.setText(ParseData.formatDate(contante.getDtmov()));
          break;
        case 2: // dtval
          contante.setDtval(ParseData.parseData(e.toString()));
          txDtval.setText(ParseData.formatDate(contante.getDtval()));
          break;
        case 3: // dare
          contante.setDare(0.);
          if (null != e)
            contante.setDare(Utils.parseDouble(e.toString()));
          txDare.setText(Utils.formatDouble(contante.getDare()));
          break;
        case 4: // avere
          contante.setAvere(0.);
          if (null != e)
            contante.setAvere(Utils.parseDouble(e.toString()));
          txAvere.setText(Utils.formatDouble(contante.getAvere()));
          break;
        case 5: // descr
          contante.setDescr(null);
          if (null != e)
            contante.setDescr(e.toString());
          txDescr.setText(contante.getDescr());
          break;
        case 6: // abicaus
          contante.setAbicaus(null);
          if (null != e)
            contante.setAbicaus(e.toString());
          cbCausABI.getSelectionModel().select(m_db.getDescrCausABI(contante.getAbicaus()));
          break;
        case 7: // cardid
          contante.setCardid(null);
          if (null != e)
            contante.setCardid(e.toString());
          cbProprietario.getSelectionModel().select(contante.getCardid());
          break;
        default:
          s_log.warn("Troppi campi nella select");
          break;
      }
    }
    Utils.setLocale(locale);
  }

  private void vaiInModalitaModifica() {
    EModalitaView modali = cbModalita.getSelectionModel().getSelectedItem();
    if (null != modali || modali != EModalitaView.Modifica)
      cbModalita.getSelectionModel().select(EModalitaView.Modifica);
  }

  private String estraiCausABI(String p_descr) {
    String szCaus = null;
    if (null == p_descr || p_descr.length() < 4)
      return szCaus;
    int n = p_descr.lastIndexOf("(");
    if (n < 1)
      return szCaus;
    String sz = p_descr.substring(n);
    szCaus = sz.replaceAll("[()]", "");
    return szCaus;
  }

  private void updateRecord() {
    DataController cntr = DataController.getInst();
    cntr.setFiltriQuery(ESqlFiltri.Id.getFlag());
    m_db.updateMovimento(contante);
    s_log.info("Modificato records  {}", contante.toString().replace("\t", ";"));
  }

  private void insertRecord() {
    DataController cntr = DataController.getInst();
    cntr.setFiltriQuery(ESqlFiltri.Id.getFlag());
    m_db.insertMovimento(contante);
    contante.setRigaid(m_db.getLastRowid());
    Platform.runLater(() -> txId.setText(String.valueOf(contante.getRigaid())));
  }

  private void deleteRecord() {
    DataController cntr = DataController.getInst();
    cntr.setFiltriQuery(ESqlFiltri.Id.getFlag());
    int qtaDel = m_db.deleteMovimento(contante);
    s_log.info("Cancellato {} records con {}", qtaDel, contante.toString().replace("\t", ";"));
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
    m_appmain.removeViewContanti(this);
    if (myScene == null) {
      s_log.error("Il campo Scene risulta = **null**");
      return;
    }

    double px = myScene.getWindow().getX();
    double py = myScene.getWindow().getY();
    double dx = myScene.getWindow().getWidth();
    double dy = myScene.getWindow().getHeight();

    p_props.setProperty(CSZ_PROP_POSVIEWCONT_X, (int) px);
    p_props.setProperty(CSZ_PROP_POSVIEWCONT_Y, (int) py);
    p_props.setProperty(CSZ_PROP_DIMVIEWCONT_X, (int) dx);
    p_props.setProperty(CSZ_PROP_DIMVIEWCONT_Y, (int) dy);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    //

  }

}
