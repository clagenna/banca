package sm.clagenna.banca.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.stdcla.sql.DBConn;

public class SqlServerGest implements ISQLGest {
  private static final Logger s_log = LogManager.getLogger(SqlServerGest.class);

  private static final String QRY_INS_Mov =     //
      "INSERT INTO dbo.movimenti%s"             //
          + "                 (dtmov"           //
          + "                 ,dtval"           //
          + "                 ,dare"            //
          + "                 ,avere"           //
          + "                 ,descr"           //
          + "                 ,abicaus"         //
          + "                 ,cardid)"         //
          + "           VALUES (?,?,?,?,?,?,?)";
  private static final String QRY_SEL_Mov =     //
      "SELECT COUNT(*)"                         //
          + "  FROM dbo.movimenti%s"            //
          + " WHERE 1=1";                       //
  private static final String QRY_DEL_Mov =     //
      "DELETE FROM dbo.movimenti%s"             //
          + " WHERE 1=1";                       //

  @Getter @Setter
  private String  tableName;
  @Getter @Setter
  private DBConn  dbconn;
  @Getter @Setter
  private boolean overwrite;

  /** Quanti campi filtro devo applicare, 0 - tutti */
  //  @Getter @Setter
  //  private int    maxFilter;

  public SqlServerGest() {
    //
  }

  public SqlServerGest(String p_tbl) {
    //    maxFilter = 0;
    setTableName(p_tbl);
  }

  @Override
  public void write(RigaBanca ri) {
    if (existMovimento(tableName, ri)) {
      if ( !overwrite) {
        s_log.debug("Il movimento esiste! scarto {} ", ri.toString());
        return;
      }
      deleteMovimento(tableName, ri);
    }
    insertMovimento(tableName, ri);
  }

  @Override
  public boolean existMovimento(String p_tab, RigaBanca rig) {
    boolean bRet = false;
    int qta = 0;
    StringBuilder qry = new StringBuilder(String.format(QRY_SEL_Mov, p_tab));
    DataController cntrl = DataController.getInst();
    qry.append(cntrl.getCampiFiltro());

    Connection conn = dbconn.getConn();
    try (PreparedStatement stmt = conn.prepareStatement(qry.toString())) {
      cntrl.applicaFiltri(stmt, 1, dbconn, rig);
      try (ResultSet res = stmt.executeQuery()) {
        while (res.next())
          qta = res.getInt(1);
        bRet = qta != 0;
      }
    } catch (SQLException e) {
      s_log.error("Errore SELECT on {} with err={}", p_tab, e.getMessage());
    }
    return bRet;
  }

  @Override
  public int deleteMovimento(String p_tab, RigaBanca rig) {
    int qtaDel = 0;
    DataController cntrl = DataController.getInst();
    StringBuilder qry = new StringBuilder(String.format(QRY_DEL_Mov, p_tab));
    qry.append(cntrl.getCampiFiltro());
    Connection conn = dbconn.getConn();
    try (PreparedStatement stmt = conn.prepareStatement(qry.toString())) {
      cntrl.applicaFiltri(stmt, 1, dbconn, rig);
      qtaDel = stmt.executeUpdate();
      s_log.debug("Deleted {} recs from {}", qtaDel, p_tab);
    } catch (SQLException e) {
      s_log.error("Errore DELETE on {} with err={}", p_tab, e.getMessage());
    }
    return qtaDel;
  }

  @Override
  public boolean insertMovimento(String p_tab, RigaBanca p_rig) {
    boolean bRet = false;
    String qry = String.format(QRY_INS_Mov, p_tab);
    Connection conn = dbconn.getConn();
    try (PreparedStatement stmt = conn.prepareStatement(qry.toString())) {

      int k = 1;
      dbconn.setStmtDate(stmt, k++, p_rig.getDtmov());
      dbconn.setStmtDate(stmt, k++, p_rig.getDtval());
      dbconn.setStmtImporto(stmt, k++, p_rig.getDare());
      dbconn.setStmtImporto(stmt, k++, p_rig.getAvere());
      dbconn.setStmtString(stmt, k++, p_rig.getDescr());
      dbconn.setStmtString(stmt, k++, p_rig.getCaus());
      dbconn.setStmtString(stmt, k++, p_rig.getCardid());

      stmt.executeUpdate();
    } catch (SQLException e) {
      s_log.error("Errore DELETE on {} with err={}", p_tab, e.getMessage());
    }
    return bRet;
  }
  //
  //  private PreparedStatement applicaFiltri(PreparedStatement p_stmt, RigaBanca p_rig) throws SQLException {
  //    int qtFiltri = maxFilter > 0 ? maxFilter : QRY_Filtri.length;
  //    int k = 1;
  //    DataController cntr = DataController.getInst();
  //    int filtriSQL = cntr.getFiltriQuery();
  //
  //    if (ESqlFiltri.Dtmov.isSet(filtriSQL))
  //      dbconn.setStmtDate(p_stmt, k++, p_rig.getDtmov());
  //    if (ESqlFiltri.Dtval.isSet(filtriSQL))
  //      dbconn.setStmtDate(p_stmt, k++, p_rig.getDtval());
  //    if (ESqlFiltri.Dare.isSet(filtriSQL))
  //      dbconn.setStmtImporto(p_stmt, k++, p_rig.getDare());
  //    if (ESqlFiltri.Avere.isSet(filtriSQL))
  //      dbconn.setStmtImporto(p_stmt, k++, p_rig.getAvere());
  //    if (ESqlFiltri.Descr.isSet(filtriSQL))
  //      dbconn.setStmtString(p_stmt, k++, p_rig.getDescr());
  //    if (ESqlFiltri.ABICaus.isSet(filtriSQL))
  //      dbconn.setStmtString(p_stmt, k++, p_rig.getCaus());
  //    if (ESqlFiltri.Cardid.isSet(filtriSQL))
  //      dbconn.setStmtString(p_stmt, k++, p_rig.getCardid());
  //    return p_stmt;
  //  }

}
