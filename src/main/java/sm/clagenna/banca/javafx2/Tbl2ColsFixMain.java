package sm.clagenna.banca.javafx2;

import java.io.FileNotFoundException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.javafx.MessageDialog;
import sm.clagenna.stdcla.utils.AppProperties;

public class Tbl2ColsFixMain extends Application implements IStartApp {
  private static final Logger s_log = LogManager.getLogger(Tbl2ColsFixMain.class);

  @Getter @Setter
  private static Tbl2ColsFixMain inst;

  @Getter @Setter
  private Stage         primaryStage;
  @Getter @Setter
  private DataModel     model;
  @Getter @Setter
  private AppProperties props;
  @Getter @Setter
  private String        skin;
  @Getter @Setter
  private Parameters    params;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(javafx.stage.Stage p_primaryStage) throws Exception {
    Tbl2ColsFixMain.setInst(this);
    setPrimaryStage(p_primaryStage);
    MessageDialog.setStage(p_primaryStage); // per i futuri messaggi di dialogo
    params = getParameters();
    initApp(null);
    URL url = getClass().getResource(Tbl2ColsFixResultView.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(Tbl2ColsFixResultView.CSZ_FXMLNAME);
    if (url == null)
      throw new FileNotFoundException(String.format("Non trovo reource %s", Tbl2ColsFixResultView.CSZ_FXMLNAME));
    /* url = */ model.getUrlSkin(); // prima load prima della scene controller
    Parent radice = null;
    try {
      radice = FXMLLoader.load(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Scene scene = new Scene(radice, 725, 550);

    // <a target="_blank" href="https://icons8.com/icon/Qd0k8d5D0tSe/invoice">Invoice</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
    scene.getStylesheets().add(url.toExternalForm());
    MessageDialog.setStage(primaryStage);

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  @Override
  public void initApp(AppProperties p_props) {
    try {
      model = new DataModel();
      model.setMainApp(this);
      model.setPropsFile("dati\\Tbl2ColsFix.properties");
      model.setParams(params);
      model.initApp(props);
      props = model.getProps();
      Tbl2ColsFixResultView.setNullVal("");
      skin = props.getProperty(AppProperties.CSZ_PROP_SKIN);
      if (null == skin)
        skin = "LoadBancaFX";
      JFXUtils.readPosStage(primaryStage, props, "frame");
    } catch (Exception e) {
      Tbl2ColsFixMain.s_log.error("Errore in main initApp: {}", e.getMessage(), e);
      System.exit(1957);
    }
  }

  @Override
  public void stop() throws Exception {
    AppProperties prop = getProps();
    closeApp(prop);
    super.stop();
  }

  @Override
  public void closeApp(AppProperties p_props) {
    model.firePropertyChange(Consts.EVT_APP_CLOSE, null, props);
    if (model != null)
      model.closeApp(props);
    props.salvaSuProperties();
  }

  @Override
  public void changeSkin() {
    // nothing to do 
  }

}
