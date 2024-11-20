package sm.clagenna.banca.javafx;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.utils.AppProperties;

public class ViewContanti implements Initializable, IStartApp {
  private static final Logger s_log                  = LogManager.getLogger(ViewContanti.class);
  public static final String  CSZ_FXMLNAME           = "ViewContanti.fxml";
  private static final String CSZ_PROP_POSVIEWCONT_X = "ViewCont.x";
  private static final String CSZ_PROP_POSVIEWCONT_Y = "ViewCont.y";
  private static final String CSZ_PROP_DIMVIEWCONT_X = "ViewCont.lx";
  private static final String CSZ_PROP_DIMVIEWCONT_Y = "ViewCont.ly";
  private static final String CSZ_PROP_SPLITPOS      = "ViewCont.spltpos";

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
  @FXML
  private Button                  btModifica;
  @FXML
  private Button                  btElimina;

  @FXML
  private TableView<List<Object>> tblview;
  @FXML
  private Label                   lblMesg;

  @Getter @Setter
  private Scene            myScene;
  private Stage            lstage;
  private LoadBancaMainApp m_appmain;
  private AppProperties    m_mainProps;
  private ISQLGest         m_db;
  private EModalitaView    modalita;

  private Integer       id;
  private LocalDateTime dtmov;
  private LocalDateTime dtval;
  private Double        dare;
  private Double        avere;
  private String        descr;
  private String        caus;
  private String        cardid;

  public ViewContanti() {
    //
  }

  @Override
  public void initApp(AppProperties p_props) {
    m_mainProps = p_props;
    m_appmain = LoadBancaMainApp.getInst();
    m_appmain.addViewContanti(this);

    String szSQLType = p_props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    m_db = SqlGestFactory.get(szSQLType);
    m_db.setDbconn(LoadBancaMainApp.getInst().getConnSQL());

    impostaForma(p_props);
    caricaComboModalita();
    caricaComboCausABI();
    caricaComboCardHolder();
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
    if (px != -1 && py != -1 && px * py != 0) {
      lstage.setX(px);
      lstage.setY(py);
      lstage.setWidth(dx);
      lstage.setHeight(dy);
    }
    double spltPos = p_props.getDoubleProperty(CSZ_PROP_SPLITPOS, -1.);
    if (spltPos > 0.)
      spltPane.setDividerPositions(spltPos);
  }

  private void caricaComboModalita() {
    cbModalita.getItems().clear();
    cbModalita.getItems().add((EModalitaView) null);
    cbModalita.getItems().addAll(EModalitaView.values());
    modalita = null;
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

  private void abilitaBottoni() {
    if (null == modalita) {
      btCerca.setDisable(true);
      btModifica.setDisable(true);
      btElimina.setDisable(true);
      abilitaCampi(true);
      return;
    }
    switch (modalita) {
      case Eliminazione:
        btCerca.setDisable(true);
        btModifica.setDisable(true);
        btElimina.setDisable(false);
        abilitaCampi(true);
        break;
      case Modifica:
        btCerca.setDisable(true);
        btModifica.setDisable(false);
        btElimina.setDisable(true);
        abilitaCampi(false);
        break;
      case Ricerca:
        btCerca.setDisable(false);
        btModifica.setDisable(true);
        btElimina.setDisable(true);
        abilitaCampi(false);
        break;
      default:
        break;
    }
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

  private void azzeraRigaBanca() {
    id = null;
    dtmov = null;
    dtval = null;
    dare = null;
    avere = null;
    descr = null;
    caus = null;
    cardid = null;
  }

  @FXML
  void cbModalitaSel(ActionEvent event) {
    modalita = cbModalita.getSelectionModel().getSelectedItem();
    s_log.debug("ViewContanti.cbModalita({}):", modalita.toString());
    azzeraRigaBanca();
    abilitaBottoni();
  }

  @FXML
  void cbCausABISel(ActionEvent event) {
    modalita = cbModalita.getSelectionModel().getSelectedItem();
    s_log.debug("ViewContanti.cbModalita({}):", modalita);
  }

  @FXML
  void cbProprietarioSel(ActionEvent event) {
    cardid = cbProprietario.getSelectionModel().getSelectedItem();
    s_log.debug("ViewContanti.cbProprietario({}):", cardid);
  }

  @FXML
  void btCercaClick(ActionEvent event) {
    caricaListView();
  }

  @FXML
  void btModificaClick(ActionEvent event) {
    updateRecord();
  }

  @FXML
  void btEliminaClick(ActionEvent event) {
    deleteRecord();
  }

  private void caricaListView() {
    // TODO Auto-generated method stub

  }

  private void updateRecord() {
    // TODO Auto-generated method stub

  }

  private void deleteRecord() {
    // TODO Auto-generated method stub

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
