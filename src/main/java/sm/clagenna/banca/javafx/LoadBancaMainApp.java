package sm.clagenna.banca.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.utils.AppProperties;

/**
 * FIXME nel main manca la voce di menu "Importa CSV" per importare un file CSV
 * di movimenti bancari <br/>
 * FIXTO ci sono piu voci identiche per tipo,dtmov,dtval,dare,descr. C'era la
 * existMovimento che faceva uso della setDtmov() su "tipo" :-(( <br/>
 * FIXTO nei dati se nel combo del anno si mette l'anno a null la query continua
 * a mantenere la precedente <br/>
 * FIXTO nel form indovina mettere un combo con l'anno di competenza <br/>
 * FIXTO nel form indovina mettere lo stesso cerca codice con il + <br/>
 * FIXTO nel form indovina manca la context menu per "apri documento"
 */
public class LoadBancaMainApp extends Application implements IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(LoadBancaMainApp.class);
  // private static final String CSZ_MAIN_APP_CSS = "LoadBancaFX.css";
  private static final String PROP_CHECK_CONV = "check.convdb";
  public static final String  CSZ_MAIN_ICON   = "sm/clagenna/banca/javafx/banca-100.png";
  public static final String  CSZ_MAIN_PROPS  = "Banca.properties";

  @Getter
  private static LoadBancaMainApp inst;
  private static boolean          debugJDBC;

  private String        skin;
  @Getter @Setter
  private AppProperties props;
  @Getter @Setter
  private Stage         primaryStage;
  @Getter @Setter
  private IStartApp     controller;
  @Getter @Setter
  private DataModel     model;

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
    doSomeDebugThings();
    setPrimaryStage(p_primaryStage);
    MessageDialog.setStage(p_primaryStage); // per i futuri messaggi di dialogo
    LoadBancaMainApp.inst = this;
    initApp(null);
    URL url = getClass().getResource(LoadBancaController.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(LoadBancaController.CSZ_FXMLNAME);
    if (url == null)
      throw new FileNotFoundException(String.format("Non trovo reource %s", LoadBancaController.CSZ_FXMLNAME));
    /* url = */ model.getUrlSkin(); // prima load prima della scene controller
    Parent radice = FXMLLoader.load(url);
    Scene scene = new Scene(radice, 725, 550);

    // <a target="_blank" href="https://icons8.com/icon/Qd0k8d5D0tSe/invoice">Invoice</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
    primaryStage.getIcons().add(new Image(CSZ_MAIN_ICON));
    scene.getStylesheets().add(url.toExternalForm());
    MessageDialog.setStage(primaryStage);

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void doSomeDebugThings() {
    s_log.info("Java Version:{}", System.getProperty("java.runtime.version"));
    s_log.info("JavaFX Version:{}", System.getProperty("javafx.runtime.version"));
    if (LoadBancaMainApp.debugJDBC) {
      s_log.info("Abilito il debug JDBC");
      // System.setProperty("java.util.logging.config.file", "logging.properties");
      // System.setProperty("com.microsoft.sqlserver.jdbc.level", "FINEST");
      // System.setProperty("com.microsoft.sqlserver.jdbc.handlers", "java.util.logging.ConsoleHandler");
      // System.setProperty("java.util.logging.ConsoleHandler.level", "FINEST");
      // meglio se fatta prima di caricare il driver "java.util.logging.Logger"
      java.util.logging.Logger logger = java.util.logging.Logger.getLogger("com.microsoft.sqlserver.jdbc");
      logger.setLevel(java.util.logging.Level.FINEST);
      java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
      handler.setLevel(java.util.logging.Level.FINEST);
      logger.addHandler(handler);
      logger.setUseParentHandlers(false);
      // verifica se il logging di JUL passa attraverso log4j2
      System.out.println(System.getProperty("java.util.logging.manager"));
      System.out.println(java.util.logging.LogManager.getLogManager().getClass().getName());
    }
  }

  @Override
  public void initApp(AppProperties p_props) {
    try {
      model = new DataModel();
      model.initApp(props);
      props = model.getProps();
      skin = props.getProperty(AppProperties.CSZ_PROP_SKIN);
      if (null == skin)
        skin = "LoadBancaFX";
      JFXUtils.readPosStage(primaryStage, props, "frame");
    } catch (Exception e) {
      LoadBancaMainApp.s_log.error("Errore in main initApp: {}", e.getMessage(), e);
      System.exit(1957);
    }
    model.addPropertyChangeListener(this);
    // checkConvDB();
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
    model.firePropertyChange(Consts.EVT_APP_CLOSE, null, prop);
    if (model != null)
      model.closeApp(prop);
    prop.setBooleanProperty(PROP_CHECK_CONV, bCheckConvDb);
    prop.salvaSuProperties();
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
    DataModel cntrl = DataModel.getInst();
    if (null != m_viewCodStat) {
      cntrl.addPropertyChangeListener(resultView);
    }
  }

  public void removeResView(ResultView resultView) {
    if (m_liResViews == null || m_liResViews.size() == 0)
      return;
    DataModel cntrl = DataModel.getInst();
    if (null != m_viewCodStat) {
      cntrl.removePropertyChangeListener(resultView);
    }
    if (m_liResViews.contains(resultView))
      m_liResViews.remove(resultView);
  }

  public void addCodeStatView(CodStatView codStatView) {
    m_viewCodStat = codStatView;
    if (null != m_liResViews) {
      DataModel cntrl = DataModel.getInst();
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
      DataModel cntrl = DataModel.getInst();
      m_liResViews.stream().forEach(s -> cntrl.addPropertyChangeListener(s));
    }
  }

  public void removeGuessCodStatView(GuessCodStatView view) {
    s_log.info("Rimuovo vista GuessCodStatView view");
    m_viewGuessCodStat = null;
  }

  public boolean isGuessCodStatViewOpened() {
    return null != m_viewGuessCodStat;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szEvt = evt.getPropertyName();

    switch (szEvt) {
      case Consts.EVT_DBCHANGE:
        // questo lo fa il model !!
        //        s_log.warn("Cambio di DB, ora sono su {}", evt.getNewValue());
        //        scegliDB();
        break;
    }
  }

  @Override
  public void changeSkin() {
    // nothing to do, skin is set in the model and used in the scene controller
  }

}
