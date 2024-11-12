package sm.clagenna.banca.javafx;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.utils.AppProperties;

public class LoadBancaMainApp extends Application implements IStartApp {
  private static final Logger s_log            = LogManager.getLogger(LoadBancaMainApp.class);
  private static final String CSZ_MAIN_APP_CSS = "LoadBancaFX.css";
  private static final String CSZ_MAIN_ICON    = "sm/clagenna/banca/javafx/banca-100.png";
  private static final String CSZ_MAIN_PROPS   = "Banca.properties";

  @Getter
  private static LoadBancaMainApp inst;

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

  List<ResultView> m_liResViews;

  public LoadBancaMainApp() {
    //
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage p_primaryStage) throws Exception {
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

    url = getClass().getResource(LoadBancaMainApp.CSZ_MAIN_APP_CSS);
    if (url == null)
      url = getClass().getClassLoader().getResource(LoadBancaMainApp.CSZ_MAIN_APP_CSS);
    scene.getStylesheets().add(url.toExternalForm());

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  @Override
  public void initApp(AppProperties p_props) {
    String szDbType = null;
    try {
      data = new DataController();
      AppProperties.setSingleton(false);
      DBConnFactory.setSingleton(false);
      props = p_props;
      if (props == null) {
        props = new AppProperties();
        props.leggiPropertyFile(new File(LoadBancaMainApp.CSZ_MAIN_PROPS), false, false);
      }
      data.initApp(props);
      szDbType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);

      int px = props.getIntProperty(AppProperties.CSZ_PROP_POSFRAME_X);
      int py = props.getIntProperty(AppProperties.CSZ_PROP_POSFRAME_Y);
      int dx = props.getIntProperty(AppProperties.CSZ_PROP_DIMFRAME_X);
      int dy = props.getIntProperty(AppProperties.CSZ_PROP_DIMFRAME_Y);
      if (px * py != 0) {
        primaryStage.setX(px);
        primaryStage.setY(py);
        primaryStage.setWidth(dx);
        primaryStage.setHeight(dy);
      }
    } catch (Exception l_e) {
      LoadBancaMainApp.s_log.error("Errore in main initApp: {}", l_e.getMessage(), l_e);
      System.exit(1957);
    }
    try {
      // connSQL = new DBConnSQL();
      DBConnFactory conFact = new DBConnFactory();
      connSQL = conFact.get(szDbType);
      connSQL.readProperties(props);
      connSQL.doConn();
      // TODO Apri le tabelle ausiliarie
    } catch (Exception e) {
      s_log.error("Errore apertura DB, error={}", e.getMessage(), e);
    }
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

    prop.salvaSuProperties();
  }

  public void messageDialog(AlertType typ, String p_msg) {
    messageDialog(typ, p_msg, ButtonType.CLOSE);
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
   */
  public void messageDialog(AlertType typ, String p_msg, ButtonType bt) {
    Alert alert = new Alert(typ, p_msg, bt);
    Scene scene = primaryStage.getScene();
    double posx = scene.getWindow().getX();
    double posy = scene.getWindow().getY();
    double widt = scene.getWidth();
    double px = posx + widt / 2 - 366;
    double py = posy + 50;
    alert.setX(px);
    alert.setY(py);
    // alert.setWidth(300);

    switch (typ) {
      case INFORMATION:
        alert.setTitle("Informa");
        alert.setHeaderText("Ok !");
        break;

      case WARNING:
        alert.setTitle("Attenzione");
        alert.setHeaderText("Fai Attenzione !");
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
    alert.showAndWait();
  }

  public void addResView(ResultView resultView) {
    if (m_liResViews == null)
      m_liResViews = new ArrayList<>();
    m_liResViews.add(resultView);
  }

  public void removeResView(ResultView resultView) {
    if (m_liResViews == null || m_liResViews.size() == 0)
      return;
    if (m_liResViews.contains(resultView))
      m_liResViews.remove(resultView);
  }

}
