package sm.clagenna.banca.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.sql.ConvDBBanca;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class LoadBancaMainApp extends Application implements IStartApp, PropertyChangeListener {
  private static final Logger s_log            = LogManager.getLogger(LoadBancaMainApp.class);
  private static final String CSZ_MAIN_APP_CSS = "LoadBancaFX.css";
  private static final String PROP_CHECK_CONV  = "check.convdb";
  public static final String  CSZ_MAIN_ICON    = "sm/clagenna/banca/javafx/banca-100.png";
  public static final String  CSZ_MAIN_PROPS   = "Banca.properties";

  @Getter
  private static LoadBancaMainApp inst;

  private String         skin;
  private URL            mainCSS;
  @Getter @Setter
  private AppProperties  props;
  @Getter @Setter
  private Stage          primaryStage;
  @Getter @Setter
  private IStartApp      controller;
  @Getter @Setter
  private DBConn         connSQL;
  @Getter @Setter
  private DataController data;

  private List<ResultView> m_liResViews;
  private ViewContanti     m_viewContanti;
  private CodStatView      m_viewCodStat;
  private GuessCodStatView m_viewGuessCodStat;
  private boolean          bCheckConvDb;

  public LoadBancaMainApp() {
    //
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage p_primaryStage) throws Exception {
    s_log.info("Java Version:{}", System.getProperty("java.runtime.version"));
    s_log.info("JavaFX Version:{}", System.getProperty("javafx.runtime.version"));
    setPrimaryStage(p_primaryStage);
    LoadBancaMainApp.inst = this;
    initApp(null);
    URL url = getClass().getResource(LoadBancaController.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(LoadBancaController.CSZ_FXMLNAME);
    if (url == null)
      throw new FileNotFoundException(String.format("Non trovo reource %s", LoadBancaController.CSZ_FXMLNAME));
    Parent radice = FXMLLoader.load(url);
    Scene scene = new Scene(radice, 725, 550);

    // <a target="_blank" href="https://icons8.com/icon/Qd0k8d5D0tSe/invoice">Invoice</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
    primaryStage.getIcons().add(new Image(CSZ_MAIN_ICON));

    url = getUrlCSS();
    scene.getStylesheets().add(url.toExternalForm());

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public URL getUrlCSS() {
    if (null != mainCSS)
      return mainCSS;
    if (null == skin)
      skin = LoadBancaMainApp.CSZ_MAIN_APP_CSS;
    String skinCss = String.format("%s.css", skin);
    mainCSS = getClass().getResource(skinCss);
    if (null == mainCSS)
      mainCSS = getClass().getClassLoader().getResource(skinCss);
    return mainCSS;
  }

  public void setSkin(String skinName) {
    if ( !Utils.isChanged(skin, skinName))
      return;
    skin = skinName;
    // props.setProperty(skinName, 0);
    mainCSS = null;
    props.setProperty(AppProperties.CSZ_PROP_SKIN, skin);
    /* URL url = */ getUrlCSS();
    controller.changeSkin();
  }

  public String getSkin() {
    return skin;
  }

  @Override
  public void initApp(AppProperties p_props) {
    try {
      AppProperties.setSingleton(false);
      DBConnFactory.setSingleton(false);
      props = p_props;
      if (props == null) {
        props = new AppProperties();
        props.leggiPropertyFile(new File(LoadBancaMainApp.CSZ_MAIN_PROPS), false, false);
      }
      data = new DataController();
      data.initApp(props);
      skin = props.getProperty(AppProperties.CSZ_PROP_SKIN);
      if (null == skin)
        skin = "LoadBancaFX";

      int px = props.getIntProperty(AppProperties.CSZ_PROP_POSFRAME_X);
      int py = props.getIntProperty(AppProperties.CSZ_PROP_POSFRAME_Y);
      int dx = props.getIntProperty(AppProperties.CSZ_PROP_DIMFRAME_X);
      int dy = props.getIntProperty(AppProperties.CSZ_PROP_DIMFRAME_Y);

      var mm = JFXUtils.getScreenMinMax(px, py, dx, dy);
      if (mm.poxX() != -1 && mm.posY() != -1 && mm.poxX() * mm.posY() != 0) {
        primaryStage.setX(mm.poxX());
        primaryStage.setY(mm.posY());
        primaryStage.setWidth(mm.width());
        primaryStage.setHeight(mm.height());
      }
    } catch (Exception l_e) {
      LoadBancaMainApp.s_log.error("Errore in main initApp: {}", l_e.getMessage(), l_e);
      System.exit(1957);
    }
    data.addPropertyChangeListener(this);
    checkConvDB();
    scegliDB();
  }

  private void checkConvDB() {
    bCheckConvDb = props.getBooleanProperty(PROP_CHECK_CONV, true);
    if ( !bCheckConvDb)
      return;
    String szDbType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    EServerId srvty = EServerId.parse(szDbType);
    if (srvty != EServerId.SQLite3)
      return;
    String szDbFile = props.getProperty(AppProperties.CSZ_PROP_DB_name);
    s_log.warn("Verifico convertibilita del \"{}\" al nuovo formato", szDbFile);
    int n = szDbFile.lastIndexOf(".");
    String szBak = szDbFile.substring(0, n) + ParseData.s_fmtDtFile.format(LocalDateTime.now()) + ".db";
    try {
      Files.copy(Paths.get(szDbFile), Paths.get(szBak), StandardCopyOption.REPLACE_EXISTING);
      s_log.info("Eseguito copia di backup di {} su {}", szDbType, szBak);
    } catch (IOException e) {
      s_log.error("Errore crea BAckup DB {} su {}, err={}", szDbFile, szBak, e.getMessage(), e);
    }
    ConvDBBanca cnv = new ConvDBBanca();
    cnv.checkConversione(szDbFile);

  }

  public void scegliDB() {
    String szDbType;
    try {
      szDbType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
      // connSQL = new DBConnSQL();
      DBConnFactory conFact = new DBConnFactory();
      connSQL = conFact.get(szDbType);
      connSQL.readProperties(props);
      connSQL.doConn();
    } catch (Exception e) {
      s_log.error("Errore apertura DB, error={}", e.getMessage(), e);
      Platform.exit();
      System.exit(1957);
    }
  }

  @Override
  public void changeSkin() {
    // nothing to do
  }

  @Override
  public void stop() throws Exception {
    AppProperties prop = getProps();
    closeApp(prop);
    super.stop();
  }

  @Override
  public void closeApp(AppProperties prop) {
    // TODO salva le updates rimaste in sospeso
    Scene sce = primaryStage.getScene();
    double px = sce.getWindow().getX();
    double py = sce.getWindow().getY();
    double dx = sce.getWindow().getWidth();
    double dy = sce.getWindow().getHeight();

    prop.setProperty(AppProperties.CSZ_PROP_POSFRAME_X, (int) px);
    prop.setProperty(AppProperties.CSZ_PROP_POSFRAME_Y, (int) py);
    prop.setProperty(AppProperties.CSZ_PROP_DIMFRAME_X, (int) dx);
    prop.setProperty(AppProperties.CSZ_PROP_DIMFRAME_Y, (int) dy);

    if (controller != null)
      controller.closeApp(prop);
    if (data != null)
      data.closeApp(prop);

    prop.setBooleanProperty(PROP_CHECK_CONV, bCheckConvDb);
    prop.salvaSuProperties();

  }

  public Optional<ButtonType> messageDialog(AlertType typ, String p_msg) {
    return messageDialog(typ, p_msg, ButtonType.CLOSE);
  }

  /**
   * per abilitare il display HTML ho messo un WebView embedded nel alert pero'
   * ho dovuto specificare <b>javafx.media,javafx.web</b>
   *
   * <pre>
   * --module-path "C:/Program Files/Java/javafx-sdk-20.0.2/lib"
   * --add-modules=javafx.swing,javafx.graphics,javafx.fxml,javafx.media,javafx.web
   * </pre>
   *
   * @param typ
   *          Il tipo di {@link AlertType}
   * @param p_msg
   *          Il messaggio (anche HTML) da emettere
   * @param bt
   *          Il tipo di {@link ButtonType}
   * @return
   */
  public Optional<ButtonType> messageDialog(AlertType typ, String p_msg, ButtonType bt) {
    Alert alert = new Alert(typ);
    alert.setResizable(true);
    Scene scene = primaryStage.getScene();
    double posx = scene.getWindow().getX();
    double posy = scene.getWindow().getY();
    double widt = scene.getWidth();
    double px = posx + widt / 2 - 366;
    double py = posy + 50;
    alert.setX(px);
    alert.setY(py);
    alert.setWidth(400);

    switch (typ) {
      case CONFIRMATION:
        alert.setTitle("Verifica");
        alert.setHeaderText("Scegli cosa fare");
        break;
      case INFORMATION:
        alert.setTitle("Informa");
        alert.setHeaderText("Comunicazione");
        break;

      case WARNING:
        alert.setTitle("Attenzione");
        alert.setHeaderText("Occhio !");
        break;

      case ERROR:
        alert.setTitle("Errore !");
        alert.setHeaderText("Ahi ! Ahi !");
        break;

      default:
        break;
    }
    //    alert.setContentText(p_msg);
    WebView webView = new WebView();
    webView.getEngine().loadContent(p_msg);
    webView.setPrefSize(300, 60);
    alert.getDialogPane().setContent(webView);
    Optional<ButtonType> btret = alert.showAndWait();
    return btret;
  }

  public void msgBox(String p_txt) {
    msgBox(p_txt, AlertType.INFORMATION);
  }

  public boolean msgBox(String p_txt, AlertType tipo) {
    return msgBox(p_txt, tipo, (String) null);
  }

  public boolean msgBox(String p_txt, AlertType tipo, String p_ico) {
    boolean bRet = true;
    Alert alt = new Alert(tipo);
    Scene sce = getPrimaryStage().getScene();
    if (null != p_ico) {
      URL resico = getClass().getResource(p_ico);
      if (null == resico)
        resico = getClass().getClassLoader().getResource(CSZ_MAIN_ICON);
      if (null != resico) {
        ImageView ico = new ImageView(resico.toString());
        alt.setGraphic(ico);
      }
    }

    Window wnd = null;
    if (sce != null)
      wnd = sce.getWindow();
    if (wnd != null) {
      alt.initOwner(wnd);
      alt.setTitle(tipo.toString());
      alt.setHeaderText(tipo.toString());
      alt.setContentText(p_txt);
      Optional<ButtonType> result = alt.showAndWait();
      if (tipo == AlertType.CONFIRMATION) {
        bRet = result.get() == ButtonType.OK;
      }
    } else
      s_log.error("Windows==null; msg={}", p_txt);
    return bRet;
  }

  public void addViewContanti(ViewContanti pview) {
    m_viewContanti = pview;
  }

  public void removeViewContanti(ViewContanti pview) {
    if (null == m_viewContanti)
      s_log.warn("Non ci sono viste sui contanti da rimuovere!");
    m_viewContanti = null;
  }

  public void addResView(ResultView resultView) {
    if (m_liResViews == null)
      m_liResViews = new ArrayList<>();
    m_liResViews.add(resultView);
    DataController cntrl = DataController.getInst();
    if (null != m_viewCodStat) {
      //   m_viewCodStat.addPropertyChangeListener(resultView);
      cntrl.addPropertyChangeListener(resultView);
    }
  }

  public void removeResView(ResultView resultView) {
    if (m_liResViews == null || m_liResViews.size() == 0)
      return;
    DataController cntrl = DataController.getInst();
    if (null != m_viewCodStat) {
      //      m_viewCodStat.removePropertyChangeListener(resultView);
      cntrl.removePropertyChangeListener(resultView);
    }
    if (m_liResViews.contains(resultView))
      m_liResViews.remove(resultView);
  }

  public void addCodeStatView(CodStatView codStatView) {
    m_viewCodStat = codStatView;
    if (null != m_liResViews) {
      DataController cntrl = DataController.getInst();
      // m_liResViews.stream().forEach(s -> m_viewCodStat.addPropertyChangeListener(s));
      m_liResViews.stream().forEach(s -> cntrl.addPropertyChangeListener(s));
    }
  }

  public void removeCodStatView(CodStatView codStatView) {
    m_viewCodStat = null;
  }

  public boolean isCodStatViewOpened() {
    return null != m_viewCodStat;
  }

  public void addGuessCodeStatView(GuessCodStatView view) {
    m_viewGuessCodStat = view;
    if (null != m_liResViews) {
      DataController cntrl = DataController.getInst();
      // m_liResViews.stream().forEach(s -> m_viewCodStat.addPropertyChangeListener(s));
      m_liResViews.stream().forEach(s -> cntrl.addPropertyChangeListener(s));
    }
  }

  public void removeGuessCodStatView(GuessCodStatView view) {
    m_viewGuessCodStat = null;
  }

  public boolean isGuessCodStatViewOpened() {
    return null != m_viewGuessCodStat;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // System.out.printf("ResultView.propertyChange(\"%s=%s\")\n", evt.getPropertyName(), evt.getNewValue().toString());
    String szEvt = evt.getPropertyName();

    switch (szEvt) {
      case DataController.EVT_DBCHANGE:
        s_log.warn("Cambio di DB, ora sono su {}", evt.getNewValue());
        scegliDB();
        break;
    }
  }

}
