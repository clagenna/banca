package sm.clagenna.banca.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.TreeCodStat;
import sm.clagenna.banca.dati.TreeitemCodStat;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class CodStatView implements Initializable, IStartApp, PropertyChangeListener {
  // FIXTO aggiungere bottone refresh da file di properties: Fatto con F5
  // FIXTO gestire (Shift/Cntrl) + Doppio Click per aggiungere un codstat figlio 
  // FIXTO ??? aggiungere tabella del codici statistici alimentati da CodStat2.properties (per fare che'?)
  private static final Logger s_log = LogManager.getLogger(CodStatView.class);

  public static final String CSZ_FXMLNAME             = "CodStatView.fxml";
  public static final String PROP_POSview_codstatView = "cdstt";
  //  private static final String CSZ_PROP_POScdstt_X = "cdstt.x";
  //  private static final String CSZ_PROP_POScdstt_Y = "cdstt.y";
  //  private static final String CSZ_PROP_DIMcdstt_X = "cdstt.lx";
  //  private static final String CSZ_PROP_DIMcdstt_Y = "cdstt.ly";
  private static final String     CSZ_PROP_DIM_COL1  = "cdstt.col1";
  private static final String     CSZ_PROP_DIM_COL2  = "cdstt.col2";
  private static final String     CSZ_PROP_DIM_DARE  = "cdstt.dare";
  private static final String     CSZ_PROP_DIM_AVERE = "cdstt.avere";
  private static final String     CSZ_PROP_DIM_SALDO = "cdstt.saldo";
  private static final String[][] shortcuts          = {                             //
      { "F5", "Ripeti la ricerca" },                                                 //
      { "Ctrl", "Attiva la selezione multipla non consecutive" },                    //
      { "Shift", "Attiva la selezione multipla su piu righe consecutive" },          //
      { "Enter", "Esegui la ricerca o conferma la selezione" },                      //
      { "Doppio click", "Modifica il codice Stat. selezionato" },                    //
      { "Shift + Doppio click", "Aggiunge un figlio al codice Stat. selezionato" },   //
      { "Ctrl + Doppio click", "Aggiunge un figlio al codice Stat. selezionato" },   //
      { "?", "Mostra questo aiuto" },                                                //
      { "Esc", "Chiudi Help" },                                                      //
  };

  // private static final AlertType AlertType = null;

  @FXML
  private TextField                        txFileCodStat;
  @FXML
  private Button                           btCercaFile;
  @FXML
  private Button                           btImportFile;
  @FXML
  private Button                           btSaveDB;
  @FXML
  private TextField                        txDescr;
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
  @FXML
  private TreeTableColumn<CodStat, String> colSaldo;

  private LoadBancaMainApp m_appmain;
  @Getter
  private AppProperties    mainProps;
  @Getter @Setter
  private DBConn           dbconn;
  @Getter @Setter
  private Scene            myScene;
  private Stage            lstage;
  private DataModel        model;
  private ISQLGest         m_db;
  @Getter @Setter
  private Path             importFile;
  @Getter @Setter
  private String           styMatchDescr;
  private boolean          bInEventEnterFile;
  private Stage            stageModCodStat;
  private ModTreeCodStat   modTreeView;

  // menu contestuale del tree view
  private MenuItem mnuFiltraMovimenti;
  private MenuItem mnuModifica;
  private MenuItem mnuAggiungi;
  private MenuItem mnuElimina;

  public CodStatView() {
    styMatchDescr = "gold";
    //
  }

  @Override
  public void initialize(URL p_location, ResourceBundle p_resources) {
    // initApp(null);
  }

  @Override
  public void initApp(AppProperties p_props) {
    m_appmain = LoadBancaMainApp.getInst();
    m_appmain.addCodeStatView(this);
    model = DataModel.getInst();
    mainProps = model.getProps();
    dbconn = model.getDbConn();
    model.addPropertyChangeListener(this);

    m_db = SqlGestFactory.get(dbconn.getServerId());
    m_db.setDbconn(dbconn);
    txDescr.textProperty().addListener((obj, old, nv) -> txDescrSel(obj, old, nv));
    impostaTreeView(mainProps);
    impostaForma(mainProps);
    if (lstage != null)
      lstage.setOnCloseRequest(_ -> {
        closeApp(mainProps);
      });
    getMyScene().setOnKeyPressed(e -> premutoTasto(e));
    getMyScene().setOnKeyReleased(e -> rilascioTasto(e));
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

    colSaldo.setCellValueFactory(new TreeItemPropertyValueFactory<>("saldo"));
    vv = p_props.getDoubleProperty(CSZ_PROP_DIM_SALDO);
    if (vv > 0)
      colSaldo.setPrefWidth(vv);
    colSaldo.setStyle("-fx-alignment: center-right;");
    colSaldo.setCellValueFactory(param -> new SimpleObjectProperty<String>(formattaCella("saldo", param.getValue())));

    treeview.setOnMouseClicked(evt -> {
      if (/* evt.isPrimaryButtonDown() && */ evt.getClickCount() == 2) {
        var row = treeview.getSelectionModel().getSelectedItem();
        if (null != row) {
          CodStat cds = row.getValue();
          System.out.println("Doppio click su:" + cds.getCodice());
          // se è premuto Shift aggiungo un figlio, altrimenti modifico il codice Stat selezionato
          treeView_modTree( ! (evt.isShiftDown() || evt.isControlDown()), cds);
        }
      }
    });
    // Cell factory per evidenziare i match della descrizione
    treeview.setRowFactory(_ -> new TreeTableRow<CodStat>() {

      @Override
      protected void updateItem(CodStat item, boolean empty) {
        // super.updateItem(item, empty);
        if (null == item || empty) {
          setStyle("");
          super.updateItem(item, empty);
          return;
        }
        if (item.isMatched()) {
          // System.out.println(getClass().getSimpleName());
          if ( !isSelected())
            setStyle("-fx-background-color:" + styMatchDescr + ";");
          //          TreeItem<CodStat2> tri = getTreeItem().getParent();
          //          while ( null != tri) {
          //            tri.setExpanded(true);
          //            tri = tri.getParent();
          //          }
        } else
          setStyle("");
        super.updateItem(item, empty);
      }
    });
    // Listener per selezione nodo e diffusione del codice Stat selezionato
    treeview.getSelectionModel().selectedItemProperty().addListener((_, _, nv) -> {
      if (null != nv && nv.getValue().getCod1() != 0) {
        String sel = nv.getValue().getCodice();
        model.setCodStat(sel);
        // System.out.printf("CodStatView.impostaTreeView(\"%s\")\n", CodStat2);
      }
    });

    mnuFiltraMovimenti = new MenuItem("Filtra Movimenti");
    mnuFiltraMovimenti.setOnAction((ActionEvent _) -> {
      treeView_filtra(null);
    });

    mnuModifica = new MenuItem("Modifica");
    mnuModifica.setOnAction((ActionEvent _) -> {
      treeView_modTree(true, null);
    });

    mnuAggiungi = new MenuItem("Aggiungi");
    mnuAggiungi.setOnAction((ActionEvent _) -> {
      treeView_modTree(false, null);
    });

    mnuElimina = new MenuItem("Elimina");
    mnuElimina.setOnAction((ActionEvent _) -> {
      if (treeview.getSelectionModel().getSelectedItems().size() > 1)
        treeView_eliminaCodstatMulti();
      else
        treeView_eliminaCodstat();
    });

    SeparatorMenuItem sp1 = new SeparatorMenuItem();

    MenuItem mi5 = new MenuItem("Esporta CSV");
    mi5.setOnAction((ActionEvent _) -> {
      treeView_esportaCSV();
    });

    ContextMenu menu = new ContextMenu();
    menu.getItems().addAll(mnuFiltraMovimenti, mnuModifica, mnuAggiungi, mnuElimina, sp1, mi5);
    // liBanca.setContextMenu(menu);
    treeview.setContextMenu(menu);

    //    CodStatTreeData cdst = new CodStatTreeData();
    //    CodStat2 radice = cdst.readTree();
    refreshTreeCodstat();
  }

  /**
   * Refresh dell'albero dei codici statistici, mantenendo la selezione se
   * possibile
   */
  private void refreshTreeCodstat() {
    // System.out.println("CodStatView.refreshTreeCodstat()");
    TreeItem<CodStat> cds = treeview.getSelectionModel().getSelectedItem();
    TreeitemCodStat treeData = model.refreshCodstatData();
    TreeItem<CodStat> root = treeData.getTreeItemRoot();
    treeview.setRoot(root);
    treeview.refresh();
    if (null != cds) {
      String ds = cds.getValue().getDescr();
      txDescrSel(null, "", ds);
    }
  }

  /**
   * Filtra i movimenti in base al codice Stat selezionato dal menu contestuale
   * dell'albero dei codici statistici MenuItem("Filtra Movimenti");
   *
   * @param value
   */
  private void treeView_filtra(Object value) {
    // System.out.println("CodStatView.treeView_filtra()");
    TreeItem<CodStat> tricds = treeview.getSelectionModel().getSelectedItem();
    CodStat cds = null;
    if (null != tricds)
      cds = tricds.getValue();
    model.firePropertyChange(DataModel.EVT_FILTER_CODSTAT, null, cds);
  }

  /**
   * Apre la finestra di modifica del codice Stat selezionato dal menu
   * contestuale dell'albero dei codici statistici MenuItem("modifica");
   *
   * @param bModif
   *          se true modifica, se false aggiunge un figlio al codice
   *          selezionato
   */
  private void treeView_modTree(boolean bModif, CodStat cds) {
    URL url = getClass().getResource(ModTreeCodStat.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(ModTreeCodStat.CSZ_FXMLNAME);
    Parent radice;
    modTreeView = null;
    try {
      FXMLLoader fxmlLoad = new FXMLLoader(url);
      //      radice = FXMLLoader.load(url);
      radice = fxmlLoad.load();
      modTreeView = fxmlLoad.getController();
    } catch (IOException e) {
      s_log.error("Errore caricamento FXML {}", ModTreeCodStat.CSZ_FXMLNAME, e);
      return;
    }

    stageModCodStat = new Stage();
    Scene scene = new Scene(radice, 444, 211);
    stageModCodStat.setScene(scene);
    //    stageModCodStat.setWidth(427);
    //    stageModCodStat.setHeight(325);
    stageModCodStat.initOwner(lstage);
    stageModCodStat.initModality(Modality.APPLICATION_MODAL);
    stageModCodStat.setTitle("Modifica dei Codici Statistici");
    stageModCodStat.setX(20.);
    stageModCodStat.setY(20.);
    JFXUtils.readPosStage(stageModCodStat, mainProps, ModTreeCodStat.PROP_POSVIEW_modcodstat);
    //    CodStat cds = null;
    CodStat cdsLavoro = null;
    try {
      cdsLavoro = null != cds ? (CodStat) cds.clone() : null;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    if (modTreeView != null) {
      //      TreeItem<CodStat> tricds = treeview.getSelectionModel().getSelectedItem();
      //      try {
      //        // faccio una copia di lavoro
      //        if (null != tricds) {
      //          cds = (CodStat) tricds.getValue().clone();
      //        }
      //      } catch (CloneNotSupportedException e) {
      //        s_log.error("Clonazione CodStat, err={}", e.getMessage());
      //      }
      modTreeView.setMyScene(scene);

      if (bModif) {
        // vado in modifica dello stesso codice Stat, passo la copia di lavoro
        modTreeView.setCdsLavoro(cdsLavoro);
      } else {
        cdsLavoro.setCod3(0);
        cdsLavoro = model.getCodStatData().findFirstFreeCode(cdsLavoro);
        cdsLavoro.setIdCodStat(0);
        cdsLavoro.setDescr("");
        modTreeView.setCdsLavoro(cdsLavoro);
        //        CodStat newc = CodStat.parse(cds.getCodice());
        //        newc.setIdCodStat(0);
        //        newc.setDescr("");
        //        newc.setFather(cds); ??
        //        modTreeView.setCdsLavoro(newc);
      }
      // System.out.printf("Show ModTreeCodStat(%d)\n", modTreeView.hashCode() % 1023);
      modTreeView.initApp(mainProps);
    }
    stageModCodStat.show();
  }

  /**
   * Elimina il codice Stat selezionato dal menu contestuale dell'albero dei
   * codici statistici MenuItem("Elimina");
   */
  private void treeView_eliminaCodstat() {
    // m_appmain.messageDialog(AlertType.WARNING, "Cancella Cod. Stat. ancora da implementare");
    TreeItem<CodStat> tricds = treeview.getSelectionModel().getSelectedItem();
    CodStat cds = tricds.getValue();
    String szMsg = String.format("Sei sicuro di voler eliminare il codice Stat.:<br/><b> %s</b>", cds.toStringEx());
    Optional<ButtonType> btRet = m_appmain.messageDialog(AlertType.CONFIRMATION, szMsg, ButtonType.YES);
    if (btRet.isEmpty() || btRet.get().equals(ButtonType.NO))
      return;
    s_log.debug("Da CodStatView elimina Codstat {}", cds.toStringEx());
    SqlGest sqlg = model.getCodStatData().getSqlGest();
    szMsg = sqlg.deleteCodStat(cds);
    TreeCodStat treedata = model.getCodStatData();
    treedata.delete(cds);
    model.firePropertyChange(DataModel.EVT_TREECODSTAT_CHANGED, null, cds);
    m_appmain.messageDialog(AlertType.INFORMATION, szMsg);
  }

  private void treeView_eliminaCodstatMulti() {
    ObservableList<TreeItem<CodStat>> sels = treeview.getSelectionModel().getSelectedItems();
    String allStats = sels.stream().map(s -> s.getValue().toStringEx()).reduce((a, b) -> a + "<br/>" + b).orElse("");
    String szMsg = String.format("Sei sicuro di voler eliminare i codici Stat.:<br/><b> %s</b>", allStats);
    Optional<ButtonType> btRet = m_appmain.messageDialog(AlertType.CONFIRMATION, szMsg, ButtonType.YES);
    if (btRet.isEmpty() || btRet.get().equals(ButtonType.NO))
      return;
    s_log.debug("Da CodStatView elimina Codstat multipli: {}", allStats.replace("<br/>", ", "));
    SqlGest sqlg = model.getCodStatData().getSqlGest();
    for (TreeItem<CodStat> mcds : sels) {
      CodStat cds = mcds.getValue();
      String msg = sqlg.deleteCodStat(cds);
      s_log.debug("Eliminazione CodStat {}, msg={}", cds.toStringEx(), msg);
      TreeCodStat treedata = model.getCodStatData();
      treedata.delete(cds);
      model.firePropertyChange(DataModel.EVT_TREECODSTAT_CHANGED, null, cds);
    }
    szMsg = String.format("Eliminati i codici Stat.:<br/><b> %s</b>", allStats);
    m_appmain.messageDialog(AlertType.INFORMATION, szMsg);
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
      case "saldo":
        dbl = value.getValue().getTotavere() - //
            value.getValue().getTotdare();
        break;
    }
    if (dbl == 0)
      return "";
    DecimalFormat fmt = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault()); // ("#,##0.00", Locale.getDefault())
    fmt.applyPattern("#,##0.00");
    return fmt.format(dbl);
  }

  private void treeView_esportaCSV() {
    boolean bErr = false;
    TreeitemCodStat treeData = model.getCodStatData();
    int annoComp = model.getAnnoComp();
    if ( ! (Utils.isValue(annoComp) && null != treeData)) {
      m_appmain.messageDialog(AlertType.WARNING, "Mi manca l'anno competenza e la query per formare il nome da esportare");
      return;
    }
    String szNow = ParseData.formatDate(LocalDateTime.now()).replace(' ', '_').replace(':', '-');
    String szCsvFile = String.format("CodStat_Estratto_%s_%s.csv", annoComp, szNow);
    TreeItem<CodStat> root = treeData.getTreeItemRoot();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(szCsvFile))) {
      writer.append("sep=;");
      writer.newLine();
      //writer.append(";".repeat(3));
      writer.append("cat1;cat2;cat3;Descrizione;Dare;Avere;Saldo");
      writer.newLine();
      scriviRigaTreeCodstat(writer, root.getValue());
      bErr = false;
    } catch (IOException e) {
      s_log.error("Errore scrittura  CSV: {}, err={}", szCsvFile, e.getMessage());
      bErr = true;
    }
    if ( !bErr)
      m_appmain.messageDialog(AlertType.INFORMATION, String.format("Scritto CodStat CSV : %s", szCsvFile));
  }

  private void scriviRigaTreeCodstat(BufferedWriter wr, CodStat cds) {
    final String sep = ";";
    StringBuilder sb = new StringBuilder();
    if (Utils.isValue(cds.getDescr())) {
      String szCod = cds.getCodice();
      int liv1 = cds.getLivello() - 1;
      int liv2 = 4 - cds.getLivello();

      String szTabs1 = "";
      String szTabs2 = "";
      if (liv1 > 0)
        szTabs1 = sep.repeat(liv1);
      if (liv2 > 0)
        szTabs2 = sep.repeat(liv2);
      sb.append(szTabs1).append(szCod).append(szTabs2);
      sb.append(cds.getDescr()).append(sep);
      String szDa = cds.getTotdare() != 0 ? Utils.formatDouble(cds.getTotdare()) : "";
      String szAv = cds.getTotavere() != 0 ? Utils.formatDouble(cds.getTotavere()) : "";
      double saldo = cds.getTotavere() - cds.getTotdare();
      String szSa = saldo != 0 ? Utils.formatDouble(saldo) : "";

      sb.append(szDa).append(sep);
      sb.append(szAv).append(sep);
      sb.append(szSa).append(sep);

      try {
        wr.append(sb.toString());
        wr.newLine();
      } catch (IOException e) {
        s_log.error("Errore scrittura Riga CSV: {}", sb.toString());
      }
    }
    Set<CodStat> figl = cds.getFigli();
    if (null == figl || figl.size() == 0)
      return;
    figl.stream().forEach(s -> scriviRigaTreeCodstat(wr, s));
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
    JFXUtils.readPosStage(lstage, p_props, PROP_POSview_codstatView);
    URL url = m_appmain.getUrlCSS();
    if (null != url)
      myScene.getStylesheets().add(url.toExternalForm());
    txFileCodStat.focusedProperty().addListener((_, _, nw) -> {
      if ( !nw) {
        String szFiCds = txFileCodStat.getText();
        if (null == szFiCds || szFiCds.length() < 4)
          return;
        Path pth = Paths.get(szFiCds);
        settaImportFile(pth, false);
        return;
      }
    });
    btImportFile.setDisable(true);
  }

  private Object txDescrSel(ObservableValue<? extends String> obj, String old, String nv) {
    if ( !Utils.isValue(nv) || nv.length() <= 2)
      return null;
    // System.out.printf("CodStatView.txDescrSel(\"%s\")\n", nv);
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
  void onEnterFileCodStat(ActionEvent event) {
    if (bInEventEnterFile)
      return;
    bInEventEnterFile = true;
    // System.out.println("CodStatView.onEnterFileCodStat()");
    btCercaFileClick(event);
    bInEventEnterFile = false;
  }

  @FXML
  Object premutoTasto(KeyEvent p_e) {
    System.out.printf("CodstatView.premutoTasto(%s, cc=%s)=%s\n", p_e.getCode().toString(), p_e.getCharacter(), p_e.toString());
    KeyCode key = p_e.getCode();
    switch (key) {
      case ENTER:
        break;
      case F5:
        // btCercaFileClick(null);
        refreshTreeCodstat();
        break;
      case CONTROL:
      case SHIFT:
        treeview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        enableMenuContestuale(false);
        break;
      case QUOTE:
        if (p_e.isShiftDown()) {
          LoadBancaMainApp.getInst().showHelpPopup(lstage, shortcuts);
        }
        break;
      default:
        break;
    }
    return null;
  }

  /**
   * Mostra un popup con la guida alle scorciatoie da tastiera
   *
   * @param owner
   *          stage proprietario del popup
   */
  //private void   showHelpPopup(Stage owner) {
  //   Stage dialog = new Stage();
  //   dialog.initOwner(owner);
  //   dialog.initModality(Modality.APPLICATION_MODAL);
  //   dialog.initStyle(StageStyle.UTILITY);
  //   dialog.setTitle("Guida ai tasti");
  //   dialog.setResizable(false);
  //
  //   // Titolo
  //   Label title = new Label("Scorciatoie da tastiera");
  //   title.setFont(Font.font("System", FontWeight.BOLD, 14));
  //
  //   // Griglia tasto → descrizione
  //   GridPane grid = new GridPane();
  //   grid.setHgap(16);
  //   grid.setVgap(8);
  //   grid.setPadding(new Insets(12, 0, 4, 0));
  //
  //
  //   for (int i = 0; i < shortcuts.length; i++) {
  //       Label key  = new Label(shortcuts[i][0]);
  //       Label desc = new Label(shortcuts[i][1]);
  //       key.setFont(Font.font("Monospaced", 13));
  //       key.setStyle(
  //           "-fx-background-color: #e8e8e8;" +
  //           "-fx-border-color: #aaa;" +
  //           "-fx-border-radius: 4;" +
  //           "-fx-background-radius: 4;" +
  //           "-fx-padding: 2 8 2 8;"
  //       );
  //       grid.add(key,  0, i);
  //       grid.add(desc, 1, i);
  //   }
  //
  //   VBox root = new VBox(8, title, grid);
  //   root.setPadding(new Insets(16, 20, 16, 20));
  //
  //   Scene scene = new Scene(root);
  //
  //   // Chiudi con Escape o cliccando fuori
  //   scene.setOnKeyPressed(e -> {
  //       if (e.getCode() == KeyCode.ESCAPE) dialog.close();
  //   });
  //
  //   dialog.setScene(scene);
  //   dialog.showAndWait();
  //}

  private void enableMenuContestuale(boolean bSel) {
    mnuFiltraMovimenti.setDisable( !bSel);
    mnuModifica.setDisable( !bSel);
    mnuAggiungi.setDisable( !bSel);
  }

  @FXML
  Object rilascioTasto(KeyEvent p_e) {
    // System.out.printf("CodstatView.rilascioTasto(%s)\n", p_e.getCode().toString());
    KeyCode key = p_e.getCode();
    switch (key) {
      case ENTER:
      case F5:
        // btCercaFileClick(null);
        refreshTreeCodstat();
        break;
      case CONTROL:
      case SHIFT:
        treeview.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);
        enableMenuContestuale(true);
        break;
      default:
        break;
    }
    return null;
  }

  /**
   *
   * @param event
   */
  @FXML
  void btCercaFileClick(ActionEvent event) {
    // System.out.println("CodStatView.btCercaFileClick()");
    //    Path pth = Paths.get(txFileCodStat.getText());
    //    if (Files.exists(pth, LinkOption.NOFOLLOW_LINKS))
    //      datacntrlr.getCodStatData().setFileCodStats(pth);
    //    else {
    //      String szMsg = String.format("Il file %s  Non esiste!", pth.toAbsolutePath().toString());
    //      s_log.warn(szMsg);
    //      m_appmain.messageDialog(AlertType, szMsg);
    //    }
    String szMsg = null;
    Stage stage = m_appmain.getPrimaryStage();
    FileChooser filChoose = new FileChooser();
    // imposto la dir precedente (se c'e')
    String szLastDir = mainProps.getLastDir();
    if (szLastDir != null) {
      File fi = new File(szLastDir);
      if (fi.exists())
        filChoose.setInitialDirectory(fi);
    }
    filChoose.getExtensionFilters().addAll( //
        new FileChooser.ExtensionFilter("CSV Files", "*.csv"), //
        new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"), //
        new FileChooser.ExtensionFilter("Tutti Files", "*.*"));
    File fiScelto = filChoose.showOpenDialog(stage);
    if (null == fiScelto) {
      szMsg = "Non hai scelto nessun file !!";
      s_log.warn(szMsg);
      m_appmain.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    if ( !fiScelto.exists()) {
      szMsg = "Non esiste il file " + fiScelto;
      s_log.warn(szMsg);
      m_appmain.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    settaImportFile(fiScelto.toPath(), true);
  }

  private void settaImportFile(Path path, boolean bSetTx) {
    System.out.printf("CodStatView.settaImportFile(%s)\n", null != path ? path.toString() : "*null*");
    setImportFile(path);
    if (bSetTx)
      txFileCodStat.setText(path.toString());
    btImportFile.setDisable( !Files.exists(path, LinkOption.NOFOLLOW_LINKS));

  }

  @FXML
  void btImportFileClick(ActionEvent event) {

    if (null == importFile) {
      String szMsg = "Non hai specificato il file da leggere";
      m_appmain.messageDialog(AlertType.WARNING, szMsg);
      return;
    }

    if ( !Files.exists(importFile, LinkOption.NOFOLLOW_LINKS)) {
      String szMsg = String.format("Ll file \"%s\" *NON* esiste!", null != importFile ? importFile.toString() : "*null*");
      s_log.debug(szMsg);
      m_appmain.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    TreeCodStat treedata = model.getCodStatData();
    SqlGest isql = treedata.getSqlGest();
    int qtaIdCds = isql.getQtaIdCodstatsInMov();
    Optional<ButtonType> butt = null;
    if (qtaIdCds > 0) {
      String szMsg = String.format(
          "Sono presenti %d movimenti con codici Statistici assegnati!<br/>Vuoi azzerare <b>TUTTE</b> le assegnazioni fatte",
          qtaIdCds);
      butt = m_appmain.messageDialog(AlertType.CONFIRMATION, szMsg, ButtonType.YES);
    }
    if (qtaIdCds > 0 && (butt.isEmpty() || butt.get() == ButtonType.NO)) {
      String szMsg = String.format(
          "Sono presenti %d movimenti con codici Statistici assegnati!<br/>Non posso ricoprire i vecchi codici statistici",
          qtaIdCds);
      m_appmain.messageDialog(AlertType.WARNING, szMsg);
      return;
    }
    model.azzeraRifACodStat();
    treedata.readTreeCodStats(importFile);
    refreshTreeCodstat();
    // m_appmain.messageDialog(AlertType.INFORMATION, "??? Funzione Import da implementare");
  }

  //
  //  @FXML
  //  void btSaveCodStatSuDBClick(ActionEvent event) {
  //    System.out.println("CodStatView.btSaveCodStatSuDBClick()");
  //  }

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
    if (null != modTreeView)
      modTreeView.closeApp(p_props);
    model.removePropertyChangeListener(this);
    m_appmain.removeCodStatView(this);
    if (myScene == null) {
      s_log.error("Il campo Scene risulta = **null**");
      return;
    }
    JFXUtils.savePosStage(lstage, p_props, PROP_POSview_codstatView);

    //    double px = myScene.getWindow().getX();
    //    double py = myScene.getWindow().getY();
    //    double dx = myScene.getWindow().getWidth();
    //    double dy = myScene.getWindow().getHeight();
    //
    //    p_props.setProperty(CSZ_PROP_POScdstt_X, (int) px);
    //    p_props.setProperty(CSZ_PROP_POScdstt_Y, (int) py);
    //    p_props.setProperty(CSZ_PROP_DIMcdstt_X, (int) dx);
    //    p_props.setProperty(CSZ_PROP_DIMcdstt_Y, (int) dy);

    double vv = colCodStat.getWidth();
    p_props.setProperty(CSZ_PROP_DIM_COL1, Integer.valueOf((int) vv));
    vv = colDescr.getWidth();
    p_props.setProperty(CSZ_PROP_DIM_COL2, Integer.valueOf((int) vv));
    vv = colTotDare.getWidth();
    p_props.setProperty(CSZ_PROP_DIM_DARE, Integer.valueOf((int) vv));
    vv = colTotAvere.getWidth();
    p_props.setProperty(CSZ_PROP_DIM_AVERE, Integer.valueOf((int) vv));
    vv = colSaldo.getWidth();
    p_props.setProperty(CSZ_PROP_DIM_SALDO, Integer.valueOf((int) vv));

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
  //    //    m_prcsupp.firePropertyChange(DataController.EVT_CODSTAT, CodStat2, value);
  //    cntrl.firePropertyChange(DataController.EVT_CODSTAT, CodStat2, value);
  //    CodStat2 = value;
  //  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szEvtId = evt.getPropertyName();
    Object obj = evt.getNewValue();
    TreeitemCodStat treeItems = model.getCodStatData();
    switch (szEvtId) {
      case DataModel.EVT_NEW_QUERY_RESULT:
        // m_szQryResulView = evt.getNewValue().toString();
        //        datacntrlr.setQryResulView(evt.getNewValue().toString());
        //        Platform.runLater(() -> datacntrlr.aggiornaTotaliCodStat());
        // System.out.println(StackViewer.viewStackTrace("CodStatView prop_change:" + szEvtId.toString()));
        // System.out.printf("CodStatView.propertyChange(%s)\n", szEvtId);
        break;

      case DataModel.EVT_TOTCODSTAT:
        //        treeview.refresh();
        //        break;
        // fall down ...
        Platform.runLater(() -> {
          // treeview.setRoot(datacntrlr.getCodStatData().getTreeItemRoot());
          treeItems.refreshTreeItems();
          treeview.setRoot(treeItems.getTreeItemRoot());
          treeview.refresh();
        });
        break;

      case DataModel.EVT_TREECODSTAT_CHANGED:
        if (obj instanceof CodStat cds) {
          System.out.printf("CodStatView.propertyChange(%s)\n", cds.toStringEx());
          Platform.runLater(() -> {
            // refreshTreeCodstat();
            refreshTreeViewAfterUpdate(treeItems, cds);
          });
        }
        break;

      case DataModel.EVT_DBCODSTAT_CHANGED:
        break;
    }
  }

  private void refreshTreeViewAfterUpdate(TreeitemCodStat treeItems, CodStat cds) {
    int iSel = treeview.getSelectionModel().getSelectedIndex();
    refreshTreeCodstat();
    treeItems.refreshTreeItems();
    treeItems.expandNode(cds);
    treeview.setRoot(treeItems.getTreeItemRoot());
    treeview.refresh();
    if (iSel > 0)
      treeview.getSelectionModel().select(iSel);
    if (null != modTreeView)
      modTreeView.closeApp(mainProps);
    if (null != stageModCodStat)
      stageModCodStat.close();
    modTreeView = null;
    stageModCodStat = null;
  }
  //
  //  private void viewStackTrace(String szId) {
  //    StackTraceElement[] stck = Thread.currentThread().getStackTrace();
  //    StringBuilder sb = new StringBuilder();
  //    String[] scarta = { "java.", "javafx." };
  //    int coda = 0;
  //    for (StackTraceElement ste : stck) {
  //      if (coda++ < 2)
  //        continue;
  //      String sz = ste.toString();
  //      boolean bGood = true;
  //      for (String sc : scarta) {
  //        if (sz.startsWith(sc)) {
  //          bGood = false;
  //          break;
  //        }
  //      }
  //      if (bGood)
  //        sb.append("\t").append(sz).append("\n");
  //    }
  //    System.out.printf("Stack id:%s\n%s", szId, sb.toString());
  //  }

}
