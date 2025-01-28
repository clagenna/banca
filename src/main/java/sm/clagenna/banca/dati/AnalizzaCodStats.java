package sm.clagenna.banca.dati;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.sql.DBConn;

public class AnalizzaCodStats extends Task<String> {
  private static final Logger s_log = LogManager.getLogger(AnalizzaCodStats.class);

  private static final String COL_ID     = "id";
  private static final String COL_TIPO   = "tipo";
  private static final String COL_DTMOV  = "dtmov";
  private static final String COL_DARE   = "dare";
  private static final String COL_AVERE  = "avere";
  private static final String COL_CARDID = "cardid";
  private static final String COL_DESCR  = "descr";
  //  private static final String COL_CODSTAT  = "codstat";
  //  private static final String COL_CDSDESCR = "cdsdescr";
  //  private static final String COL_ASSIGN   = "assign";

  /** query per i record gia riconosciuti per formare il vocabolario */
  private static final String CSZ_QRY_KNOWN   =   //
      "SELECT  descr"                             //
          + " ,codstat"                           //
          + " FROM ListaMovimentiUNION"           //
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
          + " FROM ListaMovimentiUNION"           //
          + " WHERE 1=1"                          //
          + "   AND codstat IS NULL"              //
          + " ORDER BY descr";

  @Getter @Setter
  private TableView<GuessCodStat> tblview;
  @Getter @Setter
  private DBConn                  dbconn;
  private Connection              conn;
  //  private Dataset                 m_dts;
  private PhraseComparator        compr;
  private CodStatTreeData         codStatData;
  @Getter @Setter
  private ArrayList<GuessCodStat> listGuess;

  public AnalizzaCodStats(TableView<GuessCodStat> tbl, DBConn dbc) {
    setTblview(tbl);
    setDbconn(dbc);
  }

  @Override
  protected String call() throws Exception {
    s_log.debug("Start dell'estrazione vocabolrio X indovinare Codici Statistici");
    openDataSet();
    fillTableView();
    return "...Fine";
  }

  public void openDataSet() {
    conn = dbconn.getConn();
    codStatData = DataController.getInst().getCodStatData();
    popolaKnownPhrase();
    // creaDts();
    scanUnknown();
  }

  private void popolaKnownPhrase() {
    compr = new PhraseComparator();
    try (PreparedStatement stmt = conn.prepareStatement(CSZ_QRY_KNOWN); ResultSet res = stmt.executeQuery()) {
      if (null == res || res.isClosed())
        return;
      while (res.next()) {
        String descr = res.getString("descr");
        String codstat = res.getString("codstat");
        compr.addKnownPhrase(descr, codstat);
      }
      compr.creaVectors();
    } catch (SQLException e) {
      s_log.error("Errore su query {}, msg={}", CSZ_QRY_KNOWN, e.getMessage());
    }
  }
  //
  //  private void creaDts() {
  //    m_dts = new Dataset();
  //    m_dts.addCol(COL_ID, SqlTypes.INTEGER);
  //    m_dts.addCol(COL_TIPO, SqlTypes.VARCHAR);
  //    m_dts.addCol(COL_DTMOV, SqlTypes.TIMESTAMP);
  //    m_dts.addCol(COL_DARE, SqlTypes.DOUBLE);
  //    m_dts.addCol(COL_AVERE, SqlTypes.DOUBLE);
  //    m_dts.addCol(COL_CARDID, SqlTypes.VARCHAR);
  //    m_dts.addCol(COL_DESCR, SqlTypes.VARCHAR);
  //    m_dts.addCol(COL_CODSTAT, SqlTypes.VARCHAR);
  //    m_dts.addCol(COL_CDSDESCR, SqlTypes.VARCHAR);
  //    m_dts.addCol(COL_ASSIGN, SqlTypes.BOOLEAN);
  //  }

  private void scanUnknown() {
    listGuess = new ArrayList<GuessCodStat>();
    try (PreparedStatement stmt = conn.prepareStatement(CSZ_QRY_UNKNOWN); ResultSet res = stmt.executeQuery()) {
      if (null == res || res.isClosed())
        return;
      while (res.next()) {
        Integer id = res.getInt(COL_ID);
        String tipo = res.getString(COL_TIPO);
        Timestamp dt = res.getTimestamp(COL_DTMOV);
        LocalDateTime dtmov = dt.toLocalDateTime();
        Double dare = res.getDouble(COL_DARE);
        Double avere = res.getDouble(COL_AVERE);
        String cardid = res.getString(COL_CARDID);
        String descr = res.getString(COL_DESCR);
        PhraseComparator.Similarity sim = compr.similarity(descr);
        Phrase phr = sim.phrase();
        if (sim.percent() >= 0.4) {
          String codstat = phr.getKey();
          // String codstDescr = codstats.getProperty(codstat);

          CodStat cds = codStatData.decodeCodStat(codstat);
          String codstDescr = "???";
          if (null != cds)
            codstDescr = cds.getDescr();
          GuessCodStat gcds = new GuessCodStat(id, tipo, dtmov, dare, avere, cardid, descr, codstat, codstDescr, false);
          listGuess.add(gcds);
          s_log.debug("MATCH! {} == ({}) {} \t({}={})" //
              , descr, sim.percent(), phr.getPhrase() //
              , codstat, codstDescr);
          //          System.out.printf("MATCH! %-20s == (%.2f) %s \t(%s=%s)\n" //
          //              , descr, sim.percent(), phr.getPhrase() //
          //              , codstat, codstDescr);
        } else if (sim.percent() >= 0.1) {
          // System.out.printf("\t%-20s != (%.2f) %s\n", descr, sim.percent(), phr.getPhrase());
          s_log.debug("\t{} != ({}) {}", descr, sim.percent(), phr.getPhrase());
        } else {
          // System.out.printf("\t%-20s (%.2f) *sconosciuto* \n", descr, sim.percent());
          s_log.debug("\t{} ({}) *sconosciuto*", descr, sim.percent());
        }
      }
    } catch (SQLException e) {
      s_log.error("Errore comparetore frasi, err={}", e.getMessage());
    }
  }

  private void fillTableView() {
    Platform.runLater(() -> {
      tblview.getItems().clear();
      ObservableList<GuessCodStat> dati = FXCollections.observableArrayList();
      dati.addAll(listGuess);
      tblview.setItems(dati);
      tableViewFilled();
    });
  }

  public void tableViewFilled() {
    //
  }

}
