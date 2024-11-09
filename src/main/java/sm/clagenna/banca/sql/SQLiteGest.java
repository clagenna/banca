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

public class SQLiteGest implements ISQLGest {
  private static final Logger s_log = LogManager.getLogger(SQLiteGest.class);

  private static final String QRY_INS_Mov =     //
      "INSERT INTO movimenti%s"                 //
          + "                 (dtmov"           //
          + "                 ,dtval"           //
          + "                 ,dare"            //
          + "                 ,avere"           //
          + "                 ,descr"           //
          + "                 ,abicaus"         //
          + "                 ,cardid)"         //
          + "           VALUES (?,?,?,?,?,?,?)";
  private PreparedStatement   stmtIns;

  private static final String QRY_SEL_Mov =   //
      "SELECT COUNT(*)"                       //
          + "  FROM movimenti%s"              //
          + " WHERE 1=1";                     //
  private PreparedStatement   stmtSel;

  private static final String QRY_DEL_Mov =   //
      "DELETE FROM movimenti%s"               //
          + " WHERE 1=1";                     //
  private PreparedStatement   stmtDel;

  @Getter @Setter
  private String  tableName;
  @Getter @Setter
  private DBConn  dbconn;
  @Getter @Setter
  private boolean overwrite;
  @Getter @Setter
  private int     deleted;
  @Getter @Setter
  private int     scarti;
  @Getter @Setter
  private int     added;

  public SQLiteGest() {
    init();
  }

  public SQLiteGest(String p_tbl) {
    init();
    setTableName(p_tbl);
  }

  private void init() {
    deleted = 0;
    scarti = 0;
    added = 0;
  }

  @Override
  public void write(RigaBanca ri) {
    if (existMovimento(tableName, ri)) {
      if ( !overwrite) {
        s_log.debug("Il movimento esiste! scarto {} ", ri.toString());
        scarti++;
        return;
      }
      deleted += deleteMovimento(tableName, ri);
    }
    insertMovimento(tableName, ri);
    added++;
  }

  @Override
  public boolean existMovimento(String p_tab, RigaBanca rig) {
    boolean bRet = false;
    int qta = 0;
    // TimerMeter tm = new TimerMeter("Exist");
    DataController cntrl = DataController.getInst();
    try {
      if (null == stmtSel) {
        StringBuilder qry = new StringBuilder(String.format(QRY_SEL_Mov, p_tab));
        qry.append(cntrl.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtSel = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      s_log.error("Errore prep statement SELECT on {} with err={}", p_tab, e.getMessage());
      return true;
    }

    try {
      cntrl.applicaFiltri(stmtSel, 1, dbconn, rig);
      try (ResultSet res = stmtSel.executeQuery()) {
        while (res.next())
          qta = res.getInt(1);
        bRet = qta != 0;
      }
    } catch (SQLException e) {
      s_log.error("Errore SELECT on {} with err={}", p_tab, e.getMessage());
    }
    // System.out.println(tm.stop());
    return bRet;
  }

  @Override
  public int deleteMovimento(String p_tab, RigaBanca rig) {
    int qtaDel = 0;
    // TimerMeter tm = new TimerMeter("Delete");
    DataController cntrl = DataController.getInst();
    try {
      if (null == stmtDel) {
        StringBuilder qry = new StringBuilder(String.format(QRY_DEL_Mov, p_tab));
        qry.append(cntrl.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtDel = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      s_log.error("Errore prep statement DELETE on {} with err={}", p_tab, e.getMessage());
      return 0;
    }
    try {
      cntrl.applicaFiltri(stmtDel, 1, dbconn, rig);
      qtaDel = stmtDel.executeUpdate();
    } catch (SQLException e) {
      s_log.error("Errore DELETE on {} with err={}", p_tab, e.getMessage());
    }
    // System.out.println(tm.stop());
    return qtaDel;
  }

  @Override
  public boolean insertMovimento(String p_tab, RigaBanca p_rig) {
    boolean bRet = false;
    // TimerMeter tm = new TimerMeter("Insert");
    try {
      if (null == stmtIns) {
        String qry = String.format(QRY_INS_Mov, p_tab);
        Connection conn = dbconn.getConn();
        stmtIns = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      s_log.error("Errore prep statement INSERT on {} with err={}", p_tab, e.getMessage());
      return false;
    }

    try {
      int k = 1;
      dbconn.setStmtDate(stmtIns, k++, p_rig.getDtmov());
      dbconn.setStmtDate(stmtIns, k++, p_rig.getDtval());
      dbconn.setStmtImporto(stmtIns, k++, p_rig.getDare());
      dbconn.setStmtImporto(stmtIns, k++, p_rig.getAvere());
      dbconn.setStmtString(stmtIns, k++, p_rig.getDescr());
      dbconn.setStmtString(stmtIns, k++, p_rig.getCaus());
      dbconn.setStmtString(stmtIns, k++, p_rig.getCardid());

      stmtIns.executeUpdate();
    } catch (SQLException e) {
      s_log.error("Errore DELETE on {} with err={}", p_tab, e.getMessage());
    }
    // System.out.println(tm.stop());
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
