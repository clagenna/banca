package sm.clagenna.banca.javafx2;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.TableViewFiller;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsCol;
import sm.clagenna.stdcla.sql.DtsCols;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;
import sm.clagenna.stdcla.utils.sys.ex.DatasetException;

@SuppressWarnings("unused")
public class Tbl2ColsFixResultView implements Initializable, IStartApp {
  private static final Logger s_log        = LogManager.getLogger(Tbl2ColsFixResultView.class);
  public static final String  CSZ_FXMLNAME = "Tbl2ColsFixResultView.fxml";
  @Getter @Setter
  private static String       nullVal      = "**null**";

  public record ValoreCol(String nome, int type, Object valore) {
  };
  
  @Getter @Setter
  private Stage         primaryStage;
  private DataModel     model;
  private AppProperties props;
  private String        styRowZeroRecs;
  private String        styNoFileOnDir;
  private Integer       nFixCols;

  @FXML
  private ComboBox<String>                   cbTipoBanca;
  @FXML
  private ComboBox<String>                   cbQuery;
  @FXML
  private ComboBox<Integer>                  cbAnnoComp;
  @FXML
  private ComboBox<String>                   cbMeseComp;
  @FXML
  private TextField                          txParola;
  @FXML
  private TextArea                           txWhere;
  @FXML
  private Button                             btCerca;
  @FXML
  private ComboBox<String>                   cbSaveQuery;
  @FXML
  private Button                             btSaveQuery;
  @FXML
  private Button                             btExportCsv;
  @FXML
  private Button                             btMostraCodStat;
  @FXML
  private Button                             btAssignCodStat;
  @FXML
  private Label                              lbAssignCodStat;
  @FXML
  private Button                             btIndovinaCodStat;
  @FXML
  private CheckBox                           ckRegExp;
  @FXML
  private CheckBox                           ckScartaImp;
  @FXML
  private CheckBox                           ckLanciaExcel;
  @FXML
  private CheckBox                           ckCvsBlankOnZero;
  @FXML
  private HBox                               hbox2Tbls;
  private TableView<List<Object>>            tblvSinistra;
  private TableView<List<Object>>            tblvDestra;
  private List<TableColumn<List<Object>, ?>> liTbColsSin;
  private List<TableColumn<List<Object>, ?>> liTbColsDes;

  @FXML
  private TextField txTotImporto;
  @FXML
  private TextField txTotMovimenti;
  private Dataset   dataset;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    model = DataModel.getInst();
    props = model.getProps();
    // per i colori vedi: https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html
    styRowZeroRecs = "gold";
    styNoFileOnDir = "pink";
    initApp(props);
  }

  @Override
  public void initApp(AppProperties p_props) {
    Tbl2ColsFixMain main = Tbl2ColsFixMain.getInst();
    primaryStage = main.getPrimaryStage();
    // vedi: https://stackoverflow.com/questions/27160951/javafx-open-another-fxml-in-the-another-window-with-button
    primaryStage.onCloseRequestProperty().setValue(_ -> Platform.exit());
    nFixCols = 2;
    openDataset();
    createTableViews();
  }

  private void openDataset() {
    String szQry = "SELECT * FROM listaMovimenti";
    dataset = null;
    try (Dataset dts = new Dataset()) {
      dts.setIntToDouble(true);
      dts.setDb(model.getDbConn());
      dts.executeQuery(szQry);
      dataset = dts;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createTableViews() {
    // --- le due tblView ---
    tblvSinistra = new TableView<>();
    tblvDestra = new TableView<>();
    creaTableViewCols();
    riempiTableViews();
  }

  private void creaTableViewCols() {
    // creo le colonne per entrambe le tabelle
    liTbColsSin = new ArrayList<>();
    liTbColsDes = new ArrayList<>();
    DtsCols cols = dataset.getColumns();
    int nCol = 0;
    for (DtsCol col : cols.getColumns()) {
      int idx = nCol++;
      String szColNam = Utils.capitalize(col.getName()).replaceAll("_", " ");
      String cssAlign = TableViewFiller.FX_ALIGNMENT_CENTER_LEFT;

      switch (col.getType()) {
        case BIGINT:
        case DATE:
        case DOUBLE:
        case DECIMAL:
        case FLOAT:
        case INTEGER:
        case NUMERIC:
        case REAL: {
          // ---------------  Colonne Numeriche -----------------
          TableColumn<List<Object>, Number> tbcol = new TableColumn<>(szColNam);
          cssAlign = TableViewFiller.FX_ALIGNMENT_CENTER_RIGHT;
          // setCellValueFactory definisce cosa far vedere come valore della cella, in questo caso viene convertito in Number
          tbcol.setCellValueFactory(obj -> {
            // -----------------------------------------------------------------------------------
            // FIXME dopo la risoluzione del problema con i valori nulli, rivedere questa parte di codice
            // ???? ATTENZIONE: sto lottando con un null pointer exception !!!
            SimpleObjectProperty<Number> cel = new SimpleObjectProperty<>(0);
            var vv = obj.getValue().get(idx);
            if (null == vv)
              return cel;
            if (vv instanceof Number n) {
              cel.set(n);
              return cel;
            }
            if (vv instanceof String sz) {
              Double d = Double.valueOf( -1);
              cel.set(d);
              return cel;
            }
            return cel;
            // -----------------------------------------------------------------------------------
          });
          // setCellFactory definisce come viene visualizzato il contenuto della cella, in questo caso allineato a destra e formattato come importo
          tbcol.setCellFactory(_ -> new TableCell<List<Object>, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
              super.updateItem(item, empty);
              setStyle(TableViewFiller.FX_ALIGNMENT_CENTER_RIGHT);
              if (empty || null == item) {
                setText("");
                return;
              }
              setText(Utils.formatImporto(item, true));
            }
          });

          tbcol.setId(szColNam);
          (idx < nFixCols ? liTbColsSin : liTbColsDes).add(tbcol);
        }
          break;

        default: {
          // ---------------  Colonne Testuali -----------------
          TableColumn<List<Object>, Object> tbcol = new TableColumn<>(szColNam);

          tbcol.setCellValueFactory(param -> {
            List<Object> li = param.getValue();
            Object vv = null;
            if (idx < li.size()) 
              vv = li.get(idx);
            else
              System.out.printf("Col no %d oltre lim riga %s\n", idx, li);
            SimpleObjectProperty<Object> cel = new SimpleObjectProperty<>(formattaCella(vv));
            return cel;
          });
          tbcol.setId(szColNam);
          tbcol.setStyle(cssAlign);
          (idx < nFixCols ? liTbColsSin : liTbColsDes).add(tbcol);
        }
          break;
      }

    }
    tblvSinistra.getColumns().addAll(liTbColsSin);
    tblvDestra.getColumns().addAll(liTbColsDes);
    hbox2Tbls.getChildren().addAll(tblvSinistra, tblvDestra);
    HBox.setHgrow(tblvSinistra, Priority.ALWAYS);
  }

  private void riempiTableViews() {
    ObservableList<List<Object>> liObsSin = FXCollections.observableArrayList();
    ObservableList<List<Object>> liObsDes = FXCollections.observableArrayList();
    List<DtsRow> liRows = dataset.getRighe();
    for (DtsRow row : liRows) {
      int indx = 0;
      ObservableList<Object> liSin = FXCollections.observableArrayList();
      ObservableList<Object> liDes = FXCollections.observableArrayList();
      boolean bDebug = false;
      for (Object o : row.getValues()) {
        if (o instanceof String sz && sz.contains("Pagamento bar al mare")) {
          bDebug = true;
        }
        (indx++ < nFixCols ? liSin : liDes).add(o);
        liObsSin.add(liSin);
        liObsDes.add(liDes);
      }
      if (bDebug) {
        System.out.println("----------------------------");
        int ii = indx;
        liSin.forEach(a -> {
          if (null != a)
            System.out.printf("sin(%d):[%s]%s\n", ii, a.getClass().getSimpleName(), a);
        });
        liDes.forEach(a -> {
          if (null != a)
            System.out.printf("des(%d):[%s]%s\n", ii, a.getClass().getSimpleName(), a);
        });
      }
    }
    tblvSinistra.setItems(liObsSin);
    tblvDestra.setItems(liObsDes);
  }

  /**
   * Torna una stringa formattata in base al tipo di dato, oppure null
   *
   * @param p_o
   * @return
   */
  private Object formattaCella(Object p_o) {
    if (null == p_o)
      return nullVal;
    switch (p_o) {
      case String sz -> {
        return sz;
      }
      case Integer i -> {
        if (i == 0)
          return "";
        return i;
      }
      case Float f -> {
        if (f == 0)
          return "";
        return Utils.formatDouble(f.doubleValue());
      }
      case Double d -> {
        if (d == 0)
          return "";
        return Utils.formatDouble(d);
      }
      default -> throw new IllegalArgumentException("Unexpected value: " + p_o);
    }
  }

  private void initTxLastDir(AppProperties props2) {
    // TODO Auto-generated method stub

  }

  @Override
  public void changeSkin() {
    // nothing to do
  }

  @Override
  public void closeApp(AppProperties p_props) {
    // TODO Auto-generated method stub

  }

  @FXML
  void btAssignCodStatClick(ActionEvent event) {

  }

  @FXML
  void btCercaClick(ActionEvent event) {

  }

  @FXML
  void btExportCsvClick(ActionEvent event) {

  }

  @FXML
  void btIndovinaCodStatClick(ActionEvent event) {

  }

  @FXML
  void btMostraCodStatClick(ActionEvent event) {

  }

  @FXML
  void btSaveQueryClick(ActionEvent event) {

  }

  @FXML
  void cbAnnoCompSel(ActionEvent event) {

  }

  @FXML
  void cbMeseCompSel(ActionEvent event) {

  }

  @FXML
  void cbQuerySel(ActionEvent event) {

  }

  @FXML
  void cbSaveQuerySel(ActionEvent event) {

  }

  @FXML
  void cbTipoBancaSel(ActionEvent event) {

  }

  @FXML
  void ckRegExpClick(ActionEvent event) {

  }

}
