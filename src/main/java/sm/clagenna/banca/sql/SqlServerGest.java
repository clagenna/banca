package sm.clagenna.banca.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.stdcla.sql.DBConn;

public class SqlServerGest implements ISQLGest {
  private static final Logger s_log = LogManager.getLogger(SqlServerGest.class);

  private static final String QRY_LIST_CARDS    = "SELECT DISTINCT tipo FROM dbo.ListaMovimentiUNION";
  private static final String QRY_LIST_ANNI     = "SELECT DISTINCT YEAR(dtmov) as anno FROM ListaMovimentiUNION ORDER BY 1";
  private static final String QRY_LIST_MESI     = "SELECT DISTINCT movstr FROM dbo.ListaMovimentiUNION ORDER BY movstr";
  private static final String QRY_LIST_CAUSABI  = "SELECT abicaus, concat(descrcaus,' (',abicaus ,')') as descr FROM causali ORDER BY descr";
  private static final String QRY_LIST_CARDHOLD = "SELECT DISTINCT cardid FROM ListaMovimentiUNION WHERE cardid IS NOT NULL AND LEN(RTRIM(cardid)) > 0  ORDER BY cardid";
  private static final String QRY_LIST_VIEWS    = "SELECT name FROM sys.views ORDER BY name";
  private static final String QRY_VIEW_PATT     = "SELECT * from %s WHERE 1=1 ORDER BY dtMov,dtval";

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
  private PreparedStatement   stmtIns;

  private static final String QRY_SEL_Mov =   //
      "SELECT COUNT(*)"                       //
          + "  FROM dbo.movimenti%s"          //
          + " WHERE 1=1";                     //
  private PreparedStatement   stmtSel;

  private static final String QRY_DEL_Mov =   //
      "DELETE FROM dbo.movimenti%s"           //
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

  public SqlServerGest() {
    init();
  }

  public SqlServerGest(String p_tbl) {
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
    try {
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
    } catch (Exception e) {
      s_log.error("!err scrittura DB, {}", e.getMessage(), e);
    }
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
      String szCaus = p_rig.getCaus();
      if (null != szCaus)
        szCaus = szCaus.replace(".0", "");
      dbconn.setStmtDate(stmtIns, k++, p_rig.getDtmov());
      dbconn.setStmtDate(stmtIns, k++, p_rig.getDtval());
      dbconn.setStmtImporto(stmtIns, k++, p_rig.getDare());
      dbconn.setStmtImporto(stmtIns, k++, p_rig.getAvere());
      dbconn.setStmtString(stmtIns, k++, p_rig.getDescr());
      dbconn.setStmtString(stmtIns, k++, szCaus);
      dbconn.setStmtString(stmtIns, k++, p_rig.getCardid());

      stmtIns.executeUpdate();
    } catch (SQLException e) {
      s_log.error("Errore DELETE on {} with err={}", p_tab, e.getMessage());
    }
    // System.out.println(tm.stop());
    return bRet;
  }

  @Override
  public List<String> getListTipoCard() {
    Connection conn = dbconn.getConn();
    List<String> liTipic = new ArrayList<>();
    // liTipic.add((String) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(QRY_LIST_CARDS)) {
      while (rs.next()) {
        String anno = rs.getString(1);
        liTipic.add(anno);
      }
    } catch (SQLException e) {
      s_log.error("Query {}; err={}", QRY_LIST_CARDS, e.getMessage(), e);
    }
    return liTipic;
  }

  @Override
  public List<Integer> getListAnni() {
    Connection conn = dbconn.getConn();
    List<Integer> liAnno = new ArrayList<>();
    // liAnno.add((Integer) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(QRY_LIST_ANNI)) {
      while (rs.next()) {
        int anno = rs.getInt(1);
        liAnno.add(anno);
      }
    } catch (SQLException e) {
      s_log.error("Query {}; err={}", QRY_LIST_ANNI, e.getMessage(), e);
    }
    return liAnno;
  }

  @Override
  public List<String> getListMeseComp() {
    Connection conn = dbconn.getConn();
    List<String> liMesi = new ArrayList<>();
    // liMesi.add((String) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(QRY_LIST_MESI)) {
      while (rs.next()) {
        String mese = rs.getString(1);
        liMesi.add(mese);
      }
    } catch (SQLException e) {
      s_log.error("Query {}; err={}", QRY_LIST_MESI, e.getMessage(), e);
    }
    return liMesi;
  }

  @Override
  public List<String> getListCausABI() {
    Connection conn = dbconn.getConn();
    List<String> liCausABI = new ArrayList<>();
    // liMesi.add((String) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(QRY_LIST_CAUSABI)) {
      while (rs.next()) {
        int k = 1;
        @SuppressWarnings("unused")
        String causABI = rs.getString(k++);
        String descrABI = rs.getString(k++);
        liCausABI.add(descrABI);
      }
    } catch (SQLException e) {
      s_log.error("Query {}; err={}", QRY_LIST_CAUSABI, e.getMessage(), e);
    }
    return liCausABI;
  }

  @Override
  public List<String> getListCardHolder() {
    Connection conn = dbconn.getConn();
    List<String> liCardHold = new ArrayList<>();
    // liMesi.add((String) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(QRY_LIST_CARDHOLD)) {
      while (rs.next()) {
        int k = 1;
        //        String causABI = rs.getString(k++);
        String descrHold = rs.getString(k++);
        liCardHold.add(descrHold);
      }
    } catch (SQLException e) {
      s_log.error("Query {}; err={}", QRY_LIST_CARDHOLD, e.getMessage(), e);
    }
    return liCardHold;
  }

  @Override
  public Map<String, String> getListDBViews() {
    Connection conn = dbconn.getConn();
    Map<String, String> liViews = new HashMap<>();
    // liViews.put((String)null, null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(QRY_LIST_VIEWS)) {
      while (rs.next()) {
        String view = rs.getString(1);
        liViews.put(view, String.format(QRY_VIEW_PATT, view));
      }
    } catch (SQLException e) {
      s_log.error("Query {}; err={}", QRY_LIST_VIEWS, e.getMessage(), e);
    }
    return liViews;
  }

}
