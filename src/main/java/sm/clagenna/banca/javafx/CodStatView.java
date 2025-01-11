package sm.clagenna.banca.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class CodStatView implements Initializable, IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(CodStatView.class);

  public static final String  CSZ_FXMLNAME        = "CodStatView.fxml";
  private static final String CSZ_PROP_POScdstt_X = "cdstt.x";
  private static final String CSZ_PROP_POScdstt_Y = "cdstt.y";
  private static final String CSZ_PROP_DIMcdstt_X = "cdstt.lx";
  private static final String CSZ_PROP_DIMcdstt_Y = "cdstt.ly";
  private static final String CSZ_PROP_DIM_COL1   = "cdstt.col1";
  private static final String CSZ_PROP_DIM_COL2   = "cdstt.col2";
  private static final String CSZ_PROP_DIM_DARE   = "cdstt.dare";
  private static final String CSZ_PROP_DIM_AVERE  = "cdstt.avere";

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
  @FXML
  private TreeTableColumn<CodStat, String> colTotDare;
  @FXML
  private TreeTableColumn<CodStat, String> colTotAvere;

  @Getter @Setter
  private Scene            myScene;
  private Stage            lstage;
  private LoadBancaMainApp m_appmain;
  private DataController   datacntrlr;
  private ISQLGest         m_db;
  @Getter
  private AppProperties    mainProps;
  @Getter @Setter
  private String           styMatchDescr;

  public CodStatView() {
    styMatchDescr = "gold";
    // m_prcsupp = new PropertyChangeSupport(this);
  }

  @Override
  public void initialize(URL p_location, ResourceBundle p_resources) {
    // initApp(null);
  }

  @Override
  public void initApp(AppProperties p_props) {
    m_appmain = LoadBancaMainApp.getInst();
    m_appmain.addCodeStatView(this);
    mainProps = m_appmain.getProps();
    datacntrlr = m_appmain.getData();
    datacntrlr.addPropertyChangeListener(this);
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

    colTotDare.setCellValueFactory(new TreeItemPropertyValueFactory<>("totdare"));
    vv = p_props.getDoubleProperty(CSZ_PROP_DIM_DARE);
    if (vv > 0)
      colTotDare.setPrefWidth(vv);
    colTotDare.setStyle("-fx-alignment: center-right;");
    colTotDare.setCellValueFactory(param -> new SimpleObjectProperty<String>(formattaCella("dare", param.getValue())));

    colTotAvere.setCellValueFactory(new TreeItemPropertyValueFactory<>("totavere"));
    vv = p_props.getDoubleProperty(CSZ_PROP_DIM_AVERE);
    if (vv > 0)
      colTotAvere.setPrefWidth(vv);
    colTotAvere.setStyle("-fx-alignment: center-right;");
    colTotAvere.setCellValueFactory(param -> new SimpleObjectProperty<String>(formattaCella("avere", param.getValue())));

    treeview.setRowFactory(row -> new TreeTableRow<CodStat>() {
      @Override
      protected void updateItem(CodStat item, boolean empty) {
        // super.updateItem(item, empty);
        if (null == item || empty) {
          setStyle("");
          super.updateItem(item, empty);
          return;
        }
        if (item.isMatched()) {
          System.out.println(getClass().getSimpleName());
          if ( !isSelected())
            setStyle("-fx-background-color:" + styMatchDescr + ";");
          //          TreeItem<CodStat> tri = getTreeItem().getParent();
          //          while ( null != tri) {
          //            tri.setExpanded(true);
          //            tri = tri.getParent();
          //          }
        } else
          setStyle("");
        super.updateItem(item, empty);
      }
    });
    treeview.getSelectionModel().selectedItemProperty().addListener((obj, old, nv) -> {
      if (null != nv && nv.getValue().getCod1() != 0) {
        //  setCodStat(nv.getValue().getCodice());
        datacntrlr.setCodStat(nv.getValue().getCodice());
        // System.out.printf("CodStatView.impostaTreeView(\"%s\")\n", codStat);
      }
    });

    CodStatTreeItem cdst = new CodStatTreeItem();
    CodStat radice = cdst.readTree();
    TreeItem<CodStat> root = cdst.getTree(radice);

    treeview.setRoot(root);
  }

  private String formattaCella(String colNam, TreeItem<CodStat> value) {
    Double dbl = 0.;
    switch (colNam) {
      case "dare":
        dbl = value.getValue().getTotdare();
        break;
      case "avere":
        dbl = value.getValue().getTotavere();
        break;
    }
    if (dbl == 0)
      return "";
    DecimalFormat fmt = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault()); // ("#,##0.00", Locale.getDefault())
    fmt.applyPattern("#,##0.00");
    return fmt.format(dbl);
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
    if ( !Utils.isValue(nv) || nv.length() <= 2)
      return null;
    System.out.printf("CodStatView.txDescrSel(\"%s\")\n", nv);
    searchTree(treeview.getRoot(), nv);
    treeview.refresh();
    TreeItem<CodStat> ro = treeview.getRoot();
    Platform.runLater(() -> expandMatched(ro));
    return null;
  }

  private Object expandMatched(TreeItem<CodStat> tri) {
    CodStat cds = tri.getValue();
    if (cds.isMatched())
      retroExpand(tri.getParent());
    for (TreeItem<CodStat> no : tri.getChildren())
      expandMatched(no);
    return null;
  }

  private void retroExpand(TreeItem<CodStat> tri) {
    if (null == tri)
      return;
    if ( !tri.isLeaf()) {
      tri.setExpanded(true);
      // System.out.printf("CodStatView.retroExpand(%s)\n", tri.getValue().getCodice());
    }
    retroExpand(tri.getParent());
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
    //    for (PropertyChangeListener pl : m_prcsupp.getPropertyChangeListeners())
    //      m_prcsupp.removePropertyChangeListener(pl);
    datacntrlr.removePropertyChangeListener(this);
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
    vv = colTotDare.getWidth();
    p_props.setProperty(CSZ_PROP_DIM_DARE, Integer.valueOf((int) vv));
    vv = colTotAvere.getWidth();
    p_props.setProperty(CSZ_PROP_DIM_AVERE, Integer.valueOf((int) vv));

  }

  //  public void addPropertyChangeListener(PropertyChangeListener pcl) {
  //    m_prcsupp.addPropertyChangeListener(pcl);
  //  }
  //
  //  public void removePropertyChangeListener(PropertyChangeListener pcl) {
  //    m_prcsupp.removePropertyChangeListener(pcl);
  //  }

  //  public void setCodStat(String value) {
  //    if (null == value || value.equals("00"))
  //      return;
  //    DataController cntrl = DataController.getInst();
  //    //    m_prcsupp.firePropertyChange(DataController.EVT_CODSTAT, codStat, value);
  //    cntrl.firePropertyChange(DataController.EVT_CODSTAT, codStat, value);
  //    codStat = value;
  //  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szEvtId = evt.getPropertyName();
    switch (szEvtId) {
      case DataController.EVT_RESULTVIEW:
        // m_szQryResulView = evt.getNewValue().toString();
        datacntrlr.setQryResulView(evt.getNewValue().toString());
        Platform.runLater(() -> datacntrlr.aggiornaTotaliCodStat());
        break;

      case DataController.EVT_TOTCODSTAT:
        treeview.refresh();
        break;
    }
  }

}
