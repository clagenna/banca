package sm.clagenna.banca.javafx;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.CodStatTreeData;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class ModTreeCodStat implements Initializable, IStartApp {
  private static final Logger s_log = LogManager.getLogger(ModTreeCodStat.class);

  public static final String  CSZ_FXMLNAME       = "ModTreeCodStat.fxml";
  private static final String CSZ_PROP_POSVIEW_X = "modcodstat.x";
  private static final String CSZ_PROP_POSVIEW_Y = "modcodstat.y";
  private static final String CSZ_PROP_DIMVIEW_X = "modcodstat.lx";
  private static final String CSZ_PROP_DIMVIEW_Y = "modcodstat.ly";

  @FXML
  private TextField txCd1;
  @FXML
  private TextField txCd2;
  @FXML
  private TextField txCd3;
  @FXML
  private Label     lbDescr;
  @FXML
  private TextField txDescr;
  @FXML
  private Button    btSalva;

  private AppProperties    m_mainProps;
  private Stage            lstage;
  @Getter @Setter
  private Scene            myScene;
  private LoadBancaMainApp m_appmain;
  private DataController   dataCntr;
  private CodStatTreeData  codStatData;
  @Getter @Setter
  private CodStat          cdsPadre;
  private CodStat          cdsLavoro;
  private CodStat          cdsTree;

  private boolean bSemaf;

  public ModTreeCodStat() {
    //
  }

  @Override
  public void initApp(AppProperties p_props) {
    dataCntr = DataController.getInst();
    codStatData = dataCntr.getCodStatData();
    m_appmain = LoadBancaMainApp.getInst();
    m_mainProps = m_appmain.getProps();
    cdsLavoro = new CodStat();
    impostaForma(m_mainProps);
  }

  private void impostaForma(AppProperties p_props) {
    lstage = null;
    if (myScene == null)
      myScene = btSalva.getScene();
    if (lstage == null && myScene != null)
      lstage = (Stage) myScene.getWindow();
    if (lstage == null) {
      s_log.error("Non trovo lo stage per ModTreeCodStat");
      return;
    }

    txCd1.textProperty().addListener((obj, ov, nv) -> changedCd1(nv));
    txCd2.textProperty().addListener((obj, ov, nv) -> changedCd2(nv));
    txCd3.textProperty().addListener((obj, ov, nv) -> changedCd3(nv));
    txDescr.textProperty().addListener((obj, ov, nv) -> changedDescr(nv));

    int px = p_props.getIntProperty(CSZ_PROP_POSVIEW_X, 10);
    int py = p_props.getIntProperty(CSZ_PROP_POSVIEW_Y, 10);
    int dx = p_props.getIntProperty(CSZ_PROP_DIMVIEW_X, 427);
    int dy = p_props.getIntProperty(CSZ_PROP_DIMVIEW_Y, 150);

    if (px * py != 0) {
      lstage.setX(px);
      lstage.setY(py);
      lstage.setWidth(dx);
      lstage.setHeight(dy);
    }
    lstage.setOnHiding(ev -> {
      closeApp(m_mainProps);
    });
    myScene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> gestKey(ev));
    URL url = m_appmain.getUrlCSS();
    if (null != url)
      myScene.getStylesheets().add(url.toExternalForm());
    if (null != cdsPadre)
      cdsLavoro.assign(cdsPadre);
    try {
      bSemaf = true;
      updateTxAllCds();
    } finally {
      bSemaf = false;
    }
  }

  private Object gestKey(KeyEvent ev) {
    // System.out.printf("ModTreeCodStat.gestKey(%s)\n", ev.toString());
    if (/* ev.isControlDown() && */ ev.getCode() == KeyCode.ENTER) {
      ev.consume();
      btSalvaClick(null);
    }
    return null;
  }

  private void updateTxAllCds() {
    // System.out.printf("ModTreeCodStat.updateTxAllCds(\"%s\")\n", cdsCurr.toStringEx());
    txCd1.setText(String.valueOf(cdsLavoro.getCod1()));
    txCd2.setText(String.valueOf(cdsLavoro.getCod2()));
    txCd3.setText(String.valueOf(cdsLavoro.getCod3()));
    txDescr.setText(String.valueOf(cdsLavoro.getDescr()));
    String sz = "** nessun nodo **";
    if (null != cdsPadre)
      sz = cdsPadre.getCodice() + " " + cdsPadre.getDescr();
    lbDescr.setText(sz);
    btSalva.setDisable( !cdsLavoro.isValid());
  }

  private Object changedCd1(String nv) {
    if (bSemaf || !checkNumeric(txCd1, nv))
      return null;
    try {
      bSemaf = true;
      txCd2.setText("");
      txCd3.setText("");
      cdsLavoro.assign(Integer.valueOf(nv), 0, 0);
      cercaCurrCodStat();
    } finally {
      bSemaf = false;
    }
    return null;
  }

  private Object changedCd2(String nv) {
    if (bSemaf || !checkNumeric(txCd2, nv))
      return null;
    try {
      bSemaf = true;
      txCd3.setText("");
      cdsLavoro.assign(cdsLavoro.getCod1(), Integer.valueOf(nv), 0);
      cercaCurrCodStat();
    } finally {
      bSemaf = false;
    }
    return null;
  }

  private Object changedCd3(String nv) {
    if (bSemaf || !checkNumeric(txCd3, nv))
      return null;
    try {
      bSemaf = true;
      cdsLavoro.assign(cdsLavoro.getCod1(), cdsLavoro.getCod2(), Integer.valueOf(nv));
      cercaCurrCodStat();
    } finally {
      bSemaf = false;
    }
    return null;
  }

  private Object changedDescr(String nv) {
    cdsLavoro.setDescr(nv);
    btSalva.setDisable( !cdsLavoro.isValid());
    return null;
  }

  private boolean checkNumeric(TextField tx, String nv) {
    if (null == nv || nv.length() == 0)
      return false;
    // System.out.printf("ModTreeCodStat.checkNumeric(\"%s\")\n", nv);
    if ( !Utils.isNumeric(nv)) {
      tx.setText(nv.replaceAll("[^\\d]", ""));
      return false;
    }
    return true;
  }

  private void cercaCurrCodStat() {
    cdsPadre = null;
    // System.out.printf("ModTreeCodStat.cercaCurrCodStat(for \"%s\")\n", cdsCurr.getCodice());
    CodStat root = codStatData.getRoot();
    // ---- descrizione Nodo corrente (se c'è)
    String sz = "";
    cdsTree = root.find(cdsLavoro.getCodice());
    if (null != cdsTree)
      sz = cdsTree.getDescr();
    System.out.printf("ModTreeCodStat.cercaCurrCodStat(for \"%s\")\n",
        cdsTree != null ? cdsTree.toStringEx() : "Nuovo:" + cdsLavoro.getCodice());
    final String szDescr = sz;
    Platform.runLater(() -> txDescr.setText(szDescr));
    btSalva.setDisable( !cdsLavoro.isValid());
    // ---- descrizione del padre
    cdsPadre = cdsLavoro.getCodiceUpLevel();
    if (null != cdsPadre)
      cdsPadre = root.find(cdsPadre.getCodice());
    sz = "** nessun nodo **";
    if (null != cdsPadre)
      sz = cdsPadre.getCodice() + " " + cdsPadre.getDescr();
    final String szDescrPadre = sz;
    Platform.runLater(() -> lbDescr.setText(szDescrPadre));
  }

  @FXML
  void btSalvaClick(ActionEvent event) {
    if ( btSalva.isDisabled())
      return;
    CodStat root = codStatData.getRoot();
    cdsTree = root.find(cdsLavoro.getCodice());
    if (null != cdsTree)
      cdsTree.setDescr(cdsLavoro.getDescr());
    else {
      cdsTree = new CodStat();
      cdsTree.assign(cdsLavoro);
      codStatData.addNode(cdsTree);
      codStatData.refreshTreeItems(cdsTree);
    }
    codStatData.updateCodStat(cdsTree);
    codStatData.saveAll();
    dataCntr.firePropertyChange(DataController.EVT_TREECODSTAT_CHANGED, null, cdsTree);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // System.out.println("ModTreeCodStat.initialize()");
    //
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
    double px = myScene.getWindow().getX();
    double py = myScene.getWindow().getY();
    double dx = myScene.getWindow().getWidth();
    double dy = myScene.getWindow().getHeight();

    p_props.setProperty(CSZ_PROP_POSVIEW_X, (int) px);
    p_props.setProperty(CSZ_PROP_POSVIEW_Y, (int) py);
    p_props.setProperty(CSZ_PROP_DIMVIEW_X, (int) dx);
    p_props.setProperty(CSZ_PROP_DIMVIEW_Y, (int) dy);
  }

}
