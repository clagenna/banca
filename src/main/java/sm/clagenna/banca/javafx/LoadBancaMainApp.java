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
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.utils.AppProperties;

// FIXME ci sono piu voci identiche per tipo,dtmov,dtval,dare,descr
// FIXTO nei dati se nel combo del anno si mette l'anno a null la query continua a mantenere la precedente
// FIXTO nel form indovina mettere un combo con l'anno di competenza
// FIXTO nel form indovina mettere lo stesso cerca codice con il +
// FIXTO nel form indovina manca la context menu per "apri documento"

public class LoadBancaMainApp extends Application implements IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(LoadBancaMainApp.class);
  // private static final String CSZ_MAIN_APP_CSS = "LoadBancaFX.css";
  private static final String PROP_CHECK_CONV = "check.convdb";
  public static final String  CSZ_MAIN_ICON   = "sm/clagenna/banca/javafx/banca-100.png";
  public static final String  CSZ_MAIN_PROPS  = "Banca.properties";

  @Getter
  private static LoadBancaMainApp inst;
  private static boolean          debugJDBC;

  private String skin;
  // private URL           mainCSS;
  @Getter @Setter
  private AppProperties props;
  @Getter @Setter
  private Stage         primaryStage;
  @Getter @Setter
  private IStartApp     controller;
  //  @Getter @Setter
  //  private DBConn         dbConn;
  @Getter @Setter
  private DataModel model;

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
    MessageDialog.setStage(p_primaryStage);  // per i futuri messaggi di dialogo
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

  //  public void msgBox(String p_txt) {
  //    msgBox(p_txt, AlertType.INFORMATION);
  //  }

  //  public boolean msgBox(String p_txt, AlertType tipo) {
  //    return msgBox(p_txt, tipo, (String) null);
  //  }

  //  public boolean msgBox(String p_txt, AlertType tipo, String p_ico) {
  //    boolean bRet = true;
  //    // se lanciato da un Thread la chiamata ad Alert non puo funzionare
  //    // Va' lanciata solo sul JavaFX Application Thread
  //    if ( !Platform.isFxApplicationThread()) {
  //      Platform.runLater(() -> msgBox(p_txt, tipo, p_ico));
  //      return bRet;
  //    }
  //    Alert alt = new Alert(tipo);
  //    Scene sce = getPrimaryStage().getScene();
  //    if (null == sce) {
  //      // Cerchiamo di dare un'ancora all'alert se possibile
  //      Window.getWindows().stream().filter(Window::isShowing).findFirst().ifPresent(alt::initOwner);
  //    }
  //    if (null != p_ico) {
  //      URL resico = getClass().getResource(p_ico);
  //      if (null == resico)
  //        resico = getClass().getClassLoader().getResource(CSZ_MAIN_ICON);
  //      if (null != resico) {
  //        ImageView ico = new ImageView(resico.toString());
  //        alt.setGraphic(ico);
  //      }
  //    }
  //    alt.setTitle(tipo.toString());
  //    alt.setHeaderText(tipo.toString());
  //    alt.setContentText(p_txt);
  //    Optional<ButtonType> result = alt.showAndWait();
  //    switch (tipo) {
  //      case AlertType.CONFIRMATION:
  //        bRet = !result.isEmpty() && result.get() == ButtonType.YES;
  //        break;
  //      default:
  //        s_log.info("msg={}", p_txt);
  //        break;
  //    }
  //    return bRet;
  //  }

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
      //   m_viewCodStat.addPropertyChangeListener(resultView);
      cntrl.addPropertyChangeListener(resultView);
    }
  }

  public void removeResView(ResultView resultView) {
    if (m_liResViews == null || m_liResViews.size() == 0)
      return;
    DataModel cntrl = DataModel.getInst();
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
      DataModel cntrl = DataModel.getInst();
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
      DataModel cntrl = DataModel.getInst();
      // m_liResViews.stream().forEach(s -> m_viewCodStat.addPropertyChangeListener(s));
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
    // System.out.printf("ResultView.propertyChange(\"%s=%s\")\n", evt.getPropertyName(), evt.getNewValue().toString());
    String szEvt = evt.getPropertyName();

    switch (szEvt) {
      case Consts.EVT_DBCHANGE:
        // questo lo fa il model !!
        //        s_log.warn("Cambio di DB, ora sono su {}", evt.getNewValue());
        //        scegliDB();
        break;
    }
  }

  /**
   * Mostra un popup con la guida alle scorciatoie da tastiera
   *
   * @param owner
   *          stage proprietario del popup
   */
  public void showHelpPopup(Stage owner, String[][] shortcuts) {
    Stage dialog = new Stage();
    dialog.initOwner(owner);
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initStyle(StageStyle.UTILITY);
    dialog.setTitle("Guida ai tasti");
    dialog.setResizable(false);

    // Titolo
    Label title = new Label("Scorciatoie da tastiera");
    title.setFont(Font.font("System", FontWeight.BOLD, 14));

    // Griglia tasto → descrizione
    GridPane grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(8);
    grid.setPadding(new Insets(12, 0, 4, 0));

    //     String[][] shortcuts = {
    //         {"F5",      "Ripeti la ricerca"},
    //         {"Enter",   "Esegui la ricerca o conferma la selezione"},
    //         {"Shift",   "Attiva la selezione multipla su piu righe consecutive"},
    //         {"Ctrl",    "Attiva la selezione multipla non consecutive"},
    //         {"Doppio click", "Modifica il codice Stat. selezionato"},
    //         {"Ctrl + Doppio click", "Aggiunge un figlio al codice Stat. selezionato"},
    //         {"?",       "Mostra questo aiuto"},
    //         {"Esc",     "Chiudi Help"},
    //     };

    for (int i = 0; i < shortcuts.length; i++) {
      Label key = new Label(shortcuts[i][0]);
      Label desc = new Label(shortcuts[i][1]);
      key.setFont(Font.font("Monospaced", 13));
      key.setStyle("-fx-background-color: #e8e8e8;" + "-fx-border-color: #aaa;" + "-fx-border-radius: 4;"
          + "-fx-background-radius: 4;" + "-fx-padding: 2 8 2 8;");
      grid.add(key, 0, i);
      grid.add(desc, 1, i);
    }

    VBox root = new VBox(8, title, grid);
    root.setPadding(new Insets(16, 20, 16, 20));

    Scene scene = new Scene(root);

    // Chiudi con Escape o cliccando fuori
    scene.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ESCAPE)
        dialog.close();
    });

    dialog.setScene(scene);
    dialog.showAndWait();
  }

  @Override
  public void changeSkin() {
    // nothing to do, skin is set in the model and used in the scene controller
  }

}
