package sm.clagenna.banca.javafx;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.utils.AppProperties;

public class CodStatView implements Initializable, IStartApp {
  private static final Logger s_log = LogManager.getLogger(CodStatView.class);

  public static final String  CSZ_FXMLNAME        = "CodStatView.fxml";
  private static final String CSZ_PROP_POScdstt_X = "cdstt.x";
  private static final String CSZ_PROP_POScdstt_Y = "cdstt.y";
  private static final String CSZ_PROP_DIMcdstt_X = "cdstt.lx";
  private static final String CSZ_PROP_DIMcdstt_Y = "cdstt.ly";
  private static final String CSZ_PROP_DIM_COL1   = "cdstt.col1";
  private static final String CSZ_PROP_DIM_COL2   = "cdstt.col2";

  @FXML
  private TextField                        txDescr;
  @FXML
  private Button                           btCerca;
  @FXML
  private TreeTableView<CodStat>           treeview;
  @FXML
  private TreeTableColumn<CodStat, String> colCodStat;
  @FXML
  private TreeTableColumn<CodStat, String> colDescr;

  @Getter @Setter
  private Scene            myScene;
  private Stage            lstage;
  private LoadBancaMainApp m_appmain;
  private ISQLGest         m_db;
  @Getter
  private AppProperties    mainProps;
  @Getter @Setter
  private String           styMatchDescr;

  public CodStatView() {
    styMatchDescr = "gold";
  }

  @Override
  public void initialize(URL p_location, ResourceBundle p_resources) {
    // initApp(null);
  }

  @Override
  public void initApp(AppProperties p_props) {
    TableViewFiller.setNullRetValue("");
    m_appmain = LoadBancaMainApp.getInst();
    m_appmain.addCodeStatView(this);
    mainProps = m_appmain.getProps();
    String szSQLType = p_props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    m_db = SqlGestFactory.get(szSQLType);
    m_db.setDbconn(LoadBancaMainApp.getInst().getConnSQL());
    txDescr.textProperty().addListener((obj, old, nv) -> txDescrSel(obj, old, nv));
    impostaTreeView(mainProps);
    impostaForma(mainProps);
    if (lstage != null)
      lstage.setOnCloseRequest(e -> {
        closeApp(mainProps);
      });
  }

  private void impostaTreeView(AppProperties p_props) {
    colCodStat.setCellValueFactory(new TreeItemPropertyValueFactory<>("codice"));
    double vv = p_props.getDoubleProperty(CSZ_PROP_DIM_COL1);
    if (vv > 0)
      colCodStat.setPrefWidth(vv);

    colDescr.setCellValueFactory(new TreeItemPropertyValueFactory<>("descr"));
    vv = p_props.getDoubleProperty(CSZ_PROP_DIM_COL2);
    if (vv > 0)
      colDescr.setPrefWidth(vv);
    treeview.setRowFactory(row -> new TreeTableRow<CodStat>() {
      @Override
      protected void updateItem(CodStat item, boolean empty) {
        if (null == item || empty) {
          setStyle("");
          super.updateItem(item, empty);
          return;
        }
        if (item.isMatched())
          setStyle("-fx-background-color:" + styMatchDescr + ";");
        else
          setStyle("");
        super.updateItem(item, empty);
      }
    });

    CodStatTreeItem cdst = new CodStatTreeItem();
    CodStat radice = cdst.readTree();
    TreeItem<CodStat> root = cdst.getTree(radice);

    treeview.setRoot(root);
  }

  private void impostaForma(AppProperties p_props) {
    lstage = null;
    if (myScene == null)
      myScene = treeview.getScene();
    if (lstage == null && myScene != null)
      lstage = (Stage) myScene.getWindow();
    if (lstage == null) {
      s_log.error("Non trovo lo stage per CodStatView");
      return;
    }

    int px = p_props.getIntProperty(CSZ_PROP_POScdstt_X);
    int py = p_props.getIntProperty(CSZ_PROP_POScdstt_Y);
    int dx = p_props.getIntProperty(CSZ_PROP_DIMcdstt_X);
    int dy = p_props.getIntProperty(CSZ_PROP_DIMcdstt_Y);
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

  private Object txDescrSel(ObservableValue<? extends String> obj, String old, String nv) {
    searchTree(treeview.getRoot(), nv);
    treeview.refresh();
    return null;
  }

  private void searchTree(TreeItem<CodStat> cdsi, String p_val) {
    if (null == cdsi)
      return;
    CodStat cds = cdsi.getValue();
    if (null == cds)
      return;
    cds.matchDescr(p_val);
    for (TreeItem<CodStat> child : cdsi.getChildren())
      searchTree(child, p_val);
  }

  @FXML
  void btCercaClick(ActionEvent event) {
    System.out.println("CodStatView.btCercaClick()");
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
    m_appmain.removeCodStatView(this);
    if (myScene == null) {
      s_log.error("Il campo Scene risulta = **null**");
      return;
    }

    double px = myScene.getWindow().getX();
    double py = myScene.getWindow().getY();
    double dx = myScene.getWindow().getWidth();
    double dy = myScene.getWindow().getHeight();

    p_props.setProperty(CSZ_PROP_POScdstt_X, (int) px);
    p_props.setProperty(CSZ_PROP_POScdstt_Y, (int) py);
    p_props.setProperty(CSZ_PROP_DIMcdstt_X, (int) dx);
    p_props.setProperty(CSZ_PROP_DIMcdstt_Y, (int) dy);

    double vv = colCodStat.getWidth();
    p_props.setProperty(CSZ_PROP_DIM_COL1, Integer.valueOf((int) vv));
    vv = colDescr.getWidth();
    p_props.setProperty(CSZ_PROP_DIM_COL2, Integer.valueOf((int) vv));

  }

}
