package sm.clagenna.banca.dati;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert.AlertType;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.javafx.LoadBancaMainApp;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class AnalizzaCodStats extends Task<String> implements ChangeListener<String> {
  private static final Logger s_log = LogManager.getLogger(AnalizzaCodStats.class);

  /** query per i record gia riconosciuti per formare il vocabolario */
  private static final String CSZ_QRY_KNOWN   =   //
      "SELECT  descr"                             //
          + " ,codstat"                           //
          + " FROM ListaMovimenti"                //
          + " WHERE 1=1"                          //
          + "   AND codstat IS NOT NULL"          //
          + " ORDER BY descr";
  /** query per i record da indovinare */
  private static final String CSZ_QRY_UNKNOWN =   //
      "SELECT id"                                 //
          + " ,tipo"                              //
          + " ,dtmov"                             //
          + " ,dare"                              //
          + " ,avere"                             //
          + " ,cardid"                            //
          + " ,descr"                             //
          + " FROM ListaMovimenti"                //
          + " WHERE 1=1"                          //
          + " %s"                                 //
          + "   AND (dare <> 0 OR avere <> 0)"    //
          + "   AND codstat IS NULL"              //
          + " ORDER BY descr";

  @Getter @Setter
  private DBConn     dbconn;
  private Connection conn;
  //  private Dataset                 m_dts;

  private PhraseComparator             compr;
  private LoadBancaMainApp             mainApp;
  private DataController               dataCntrl;
  private TreeCodStat2                 codStatData;
  @Getter @Setter
  private ArrayList<GuessCodStat>      listGuess;
  @Getter
  private ObservableList<GuessCodStat> dati;
  @Getter @Setter
  private String                       parola;
  @Getter @Setter
  private LocalDateTime                dtDa;
  @Getter @Setter
  private LocalDateTime                dtA;

  private ISQLGest m_db;

  public AnalizzaCodStats(LoadBancaMainApp p_main) {
    mainApp = p_main;
    setDbconn(p_main.getConnSQL());
    dataCntrl = p_main.getData();
    codStatData = dataCntrl.getCodStatData();
  }

  @Override
  protected String call() throws Exception {
    s_log.debug("Start dell'estrazione vocabolario X indovinare Codici Statistici");
    openDataSet();
    fillTableView();
    return "...Fine";
  }

  public void openDataSet() {
    conn = dbconn.getConn();
    popolaKnownPhrase();
    // creaDts();
    scanUnknown();
    creaDati();
  }

  private void popolaKnownPhrase() {
    compr = new PhraseComparator();
    try (PreparedStatement stmt = conn.prepareStatement(CSZ_QRY_KNOWN); ResultSet res = stmt.executeQuery()) {
      if (null == res || res.isClosed())
        return;
      while (res.next()) {
        String descr = res.getString(GuessCodStat.COL_DESCR);
        String codstat = res.getString(GuessCodStat.COL_CODSTAT);
        compr.addKnownPhrase(descr, codstat);
      }
      compr.creaVectors();
    } catch (SQLException e) {
      s_log.error("Errore su query {}, msg={}", CSZ_QRY_KNOWN, e.getMessage());
    }
  }

  private void scanUnknown() {
    listGuess = new ArrayList<GuessCodStat>();
    double dblPercIndovina = dataCntrl.getPercIndov() / 100.;

    StringBuilder whe = new StringBuilder();
    if (Utils.isValue(parola))
      whe.append(String.format(" AND descr LIKE('%%%s%%')", parola));

    if (null != dtDa)
      whe.append(String.format(" AND dtmov >= '%s'", ParseData.s_fmtTsT.format(dtDa)));
    if (null != dtA)
      whe.append(String.format(" AND dtmov <= '%s'", ParseData.s_fmtTsT.format(dtA)));
    String qry = String.format(CSZ_QRY_UNKNOWN, whe.toString());

    s_log.debug("Cerca Training con: {}", qry);
    try (PreparedStatement stmt = conn.prepareStatement(qry); ResultSet res = stmt.executeQuery()) {
      if (null == res || res.isClosed())
        return;
      while (res.next()) {
        Integer id = res.getInt(GuessCodStat.COL_ID);
        String tipo = res.getString(GuessCodStat.COL_TIPO);
        Timestamp dt = res.getTimestamp(GuessCodStat.COL_DTMOV);
        LocalDateTime dtmov = dt.toLocalDateTime();
        Double dare = res.getDouble(GuessCodStat.COL_DARE);
        Double avere = res.getDouble(GuessCodStat.COL_AVERE);
        String cardid = res.getString(GuessCodStat.COL_CARDID);
        String descr = res.getString(GuessCodStat.COL_DESCR);
        PhraseComparator.Similarity sim = compr.similarity(descr);
        Phrase phr = sim.phrase();
        GuessCodStat gcds = new GuessCodStat(id, tipo, dtmov, dare, avere, cardid, descr, null, null, false);
        if (sim.percent() >= dblPercIndovina) {
          String codstat = phr.getKey();
          // String codstDescr = codstats.getProperty(codstat);

          CodStat2 cds = codStatData.find(codstat);
          String codstDescr = "???";
          if (null != cds)
            codstDescr = cds.getDescr();
          //GuessCodStat gcds = new GuessCodStat(id, tipo, dtmov, dare, avere, cardid, descr, codstat, codstDescr, false);
          gcds.setCodstat(codstat);
          gcds.setDescrCds(codstDescr);

          s_log.trace("MATCH! {} == ({}) {} \t({}={})" //
              , descr, Utils.formatDouble(sim.percent()), phr.getPhrase() //
              , codstat, codstDescr);
        } else if (sim.percent() >= 0.1) {
          // System.out.printf("\t%-20s != (%.2f) %s\n", descr, sim.percent(), phr.getPhrase());
          s_log.trace("\t{} != ({}) {}", descr, Utils.formatDouble(sim.percent()), phr.getPhrase());
        } else {
          // System.out.printf("\t%-20s (%.2f) *sconosciuto* \n", descr, sim.percent());
          s_log.trace("\t{} ({}) *sconosciuto*", descr, Utils.formatDouble(sim.percent()));
        }
        gcds.propertyCodstat().addListener(this);
        listGuess.add(gcds);
      }
    } catch (SQLException e) {
      s_log.error("Errore comparatore frasi, err={}", e.getMessage());
    }
  }

  private void creaDati() {
    dati = FXCollections.observableArrayList();
    dati.addAll(listGuess);
  }

  private void fillTableView() {
    Platform.runLater(() -> {
      // tblview.getItems().clear();
      //      ObservableList<GuessCodStat> dati = FXCollections.observableArrayList();
      //      dati.addAll(listGuess);
      // tblview.setItems(dati);
      tableViewFilled();
    });
  }

  public void tableViewFilled() {
    dataCntrl = DataController.getInst();
    dataCntrl.firePropertyChange(DataController.EVT_GUESSDATA_CREATED, -1, dati.size());
  }

  @Override
  public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
    if (observable instanceof StringProperty szp) {
      if (szp.getName().equals(GuessCodStat.COL_CODSTAT)) {
        CodStat2 cds = codStatData.find(newValue);
        if (null == cds) {
          // System.out.println("CodStat.changed()=NULL");
          String szMsg = String.format("Il codice Statistico \"%s\" *NON* esiste !", newValue);
          s_log.error(szMsg);
          Platform.runLater(() -> mainApp.msgBox(szMsg, AlertType.ERROR));
        }
      }
    }
  }

  public void saveSuDb(List<GuessCodStat> li) {
    if (null == li || li.size() == 0)
      return;
    String szSQLType = mainApp.getProps().getProperty(AppProperties.CSZ_PROP_DB_Type);
    m_db = SqlGestFactory.get(szSQLType);
    m_db.setDbconn(mainApp.getConnSQL());
    List<RigaBanca> liRb = new ArrayList<RigaBanca>();
    for (GuessCodStat gcds : li) {
      RigaBanca rb = new RigaBanca();
      rb.setTiporec(gcds.getTipo());
      rb.setRigaid(gcds.getId());
      rb.setCodstat(gcds.getCodstat());
      liRb.add(rb);
    }
    m_db.updateCodStat(liRb);
  }

}
