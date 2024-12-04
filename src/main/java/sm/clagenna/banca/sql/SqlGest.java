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
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.stdcla.sql.DBConn;

public abstract class SqlGest implements ISQLGest {

  private PreparedStatement stmtSel;
  private PreparedStatement stmtIns;
  private PreparedStatement stmtDel;
  private PreparedStatement stmtMod;
  private PreparedStatement stmtLastRowId;

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
  @Getter @Setter
  private int     lastRowid;

  private HashMap<String, String> m_mapCausABI;

  public SqlGest() {
    init();
  }

  public SqlGest(String p_tbl) {
    init();
    setTableName(p_tbl);
  }

  private void init() {
    deleted = 0;
    scarti = 0;
    added = 0;
  }

  public abstract Logger getLog();

  public abstract String getQryListCARDS();

  public abstract String getQryListANNI();

  public abstract String getQryListMESI();

  public abstract String getQryListCAUSABI();

  public abstract String getQryListCARDHOLD();

  public abstract String getQryListVIEWS();

  public abstract String getQryListVIEW_PATT();

  public abstract String getQryLASTROWID();

  public abstract String getQryINSMov();

  public abstract String getQrySELMov();

  public abstract String getQryDELMov();

  public abstract String getQryMODMov();

  @Override
  public void write(RigaBanca ri) {
    try {
      if (existMovimento(tableName, ri)) {
        if ( !overwrite) {
          getLog().debug("Il movimento esiste! scarto {} ", ri.toString());
          scarti++;
          return;
        }
        deleted += deleteMovimento(tableName, ri);
      }
      insertMovimento(tableName, ri);
      added++;
    } catch (Exception e) {
      getLog().error("!err scrittura DB, {}", e.getMessage(), e);
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
        int fq = cntrl.getFiltriQuery();
        // resetto la ricerca sul campo "Id"
        if (ESqlFiltri.Id.isSet(fq))
          cntrl.setFiltriQuery(fq & (ESqlFiltri.AllSets.getFlag() ^ ESqlFiltri.Id.getFlag()));

        StringBuilder qry = new StringBuilder(String.format(getQrySELMov(), p_tab));
        qry.append(cntrl.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtSel = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement SELECT on {} with err={}", p_tab, e.getMessage());
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
      getLog().error("Errore SELECT on {} with err={}", p_tab, e.getMessage());
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
        StringBuilder qry = new StringBuilder(String.format(getQryDELMov(), p_tab));
        qry.append(cntrl.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtDel = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement DELETE on {} with err={}", p_tab, e.getMessage());
      return 0;
    }
    try {
      cntrl.applicaFiltri(stmtDel, 1, dbconn, rig);
      qtaDel = stmtDel.executeUpdate();
    } catch (SQLException e) {
      getLog().error("Errore DELETE on {} with err={}", p_tab, e.getMessage());
    }
    // System.out.println(tm.stop());
    return qtaDel;
  }

  @Override
  public boolean updateMovimento(String p_tab, RigaBanca p_rig) {
    boolean bRet = false;
    // TimerMeter tm = new TimerMeter("Insert");
    DataController cntrl = DataController.getInst();
    try {
      if (null == stmtMod) {
        StringBuilder qry = new StringBuilder(String.format(getQryMODMov(), p_tab));
        qry.append(cntrl.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtMod = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement UPDATE on {} with err={}", p_tab, e.getMessage());
      return false;
    }

    try {
      String szCaus = p_rig.getCaus();
      if (null != szCaus)
        szCaus = szCaus.replace(".0", "");
      int k = 1;
      dbconn.setStmtInt(stmtMod, k++, p_rig.getIdfile());
      dbconn.setStmtDatetime(stmtMod, k++, p_rig.getDtmov());
      dbconn.setStmtDatetime(stmtMod, k++, p_rig.getDtval());
      dbconn.setStmtImporto(stmtMod, k++, p_rig.getDare());
      dbconn.setStmtImporto(stmtMod, k++, p_rig.getAvere());
      dbconn.setStmtString(stmtMod, k++, p_rig.getDescr());
      dbconn.setStmtString(stmtMod, k++, szCaus);
      dbconn.setStmtString(stmtMod, k++, p_rig.getCardid());

      dbconn.setStmtInt(stmtMod, k++, p_rig.getRigaid());

      stmtMod.executeUpdate();

    } catch (SQLException e) {
      getLog().error("Errore INSERT on {} with err={}", p_tab, e.getMessage());
    }
    // System.out.println(tm.stop());
    return bRet;
  }

  @Override
  public boolean insertMovimento(String p_tab, RigaBanca p_rig) {
    boolean bRet = false;
    lastRowid = -1;
    // TimerMeter tm = new TimerMeter("Insert");
    try {
      if (null == stmtIns) {
        String qry = String.format(getQryINSMov(), p_tab);
        Connection conn = dbconn.getConn();
        stmtIns = conn.prepareStatement(qry.toString());
        stmtLastRowId = conn.prepareStatement(getQryLASTROWID());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement INSERT on {} with err={}", p_tab, e.getMessage());
      return false;
    }

    try {
      String szCaus = p_rig.getCaus();
      if (null != szCaus)
        szCaus = szCaus.replace(".0", "");
      int k = 1;
      dbconn.setStmtInt(stmtIns, k++, p_rig.getIdfile());
      dbconn.setStmtDatetime(stmtIns, k++, p_rig.getDtmov());
      dbconn.setStmtDatetime(stmtIns, k++, p_rig.getDtval());
      dbconn.setStmtImporto(stmtIns, k++, p_rig.getDare());
      dbconn.setStmtImporto(stmtIns, k++, p_rig.getAvere());
      dbconn.setStmtString(stmtIns, k++, p_rig.getDescr());
      dbconn.setStmtString(stmtIns, k++, szCaus);
      dbconn.setStmtString(stmtIns, k++, p_rig.getCardid());

      stmtIns.executeUpdate();
      lastRowid = trovaLastRowid();
    } catch (SQLException e) {
      getLog().error("Errore INSERT on {} with err={}", p_tab, e.getMessage());
    }
    // System.out.println(tm.stop());
    return bRet;
  }

  private int trovaLastRowid() {
    if (null == stmtLastRowId) {
      try {
        Connection conn = dbconn.getConn();
        stmtLastRowId = conn.prepareStatement(getQryLASTROWID());
      } catch (SQLException e) {
        getLog().error("Errore prep statement Last RowID with err={}", e.getMessage());
        return -1;
      }
    }
    lastRowid = 0;
    try {
      ResultSet res = stmtLastRowId.executeQuery();
      while (res.next()) {
        lastRowid = res.getInt(1);
      }
    } catch (SQLException e) {
      getLog().error("Errore Last Row ID with err={}", e.getMessage());
    }
    return lastRowid;
  }

  @Override
  public List<String> getListTipoCard() {
    Connection conn = dbconn.getConn();
    List<String> liTipic = new ArrayList<>();
    // liTipic.add((String) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(getQryListCARDS())) {
      while (rs.next()) {
        String anno = rs.getString(1);
        liTipic.add(anno);
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryListCARDS(), e.getMessage(), e);
    }
    return liTipic;
  }

  @Override
  public List<Integer> getListAnni() {
    Connection conn = dbconn.getConn();
    List<Integer> liAnno = new ArrayList<>();
    // liAnno.add((Integer) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(getQryListANNI())) {
      while (rs.next()) {
        int anno = rs.getInt(1);
        liAnno.add(anno);
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryListANNI(), e.getMessage(), e);
    }
    return liAnno;
  }

  @Override
  public List<String> getListMeseComp(Integer pAnno) {
    Connection conn = dbconn.getConn();
    List<String> liMesi = new ArrayList<>();
    // liMesi.add((String) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(getQryListMESI())) {
      while (rs.next()) {
        String mese = rs.getString(1);
        liMesi.add(mese);
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryListMESI(), e.getMessage(), e);
    }
    if ( null == pAnno)
       return liMesi;
    List<String> li2 = liMesi //
    		.stream() //
    		.filter( s -> s.startsWith(pAnno.toString())) //
    		.toList();
    return li2;
  }

  @Override
  public List<String> getListCausABI() {
    Connection conn = dbconn.getConn();
    m_mapCausABI = new HashMap<String, String>();
    List<String> liCausABI = new ArrayList<>();
    // liMesi.add((String) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(getQryListCAUSABI())) {
      while (rs.next()) {
        int k = 1;
        String causABI = rs.getString(k++);
        String descrABI = rs.getString(k++);
        liCausABI.add(descrABI);
        m_mapCausABI.put(causABI, descrABI);
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryListCAUSABI(), e.getMessage(), e);
    }
    return liCausABI;
  }

  @Override
  public String getDescrCausABI(String causABI) {
    String szRet = null;
    if (null == causABI || null == m_mapCausABI)
      return szRet;
    szRet = m_mapCausABI.get(causABI);
    return szRet;
  }

  @Override
  public List<String> getListCardHolder() {
    Connection conn = dbconn.getConn();
    List<String> liCardHold = new ArrayList<>();
    // liMesi.add((String) null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(getQryListCARDHOLD())) {
      while (rs.next()) {
        int k = 1;
        //        String causABI = rs.getString(k++);
        String descrHold = rs.getString(k++);
        liCardHold.add(descrHold);
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryListCARDHOLD(), e.getMessage(), e);
    }
    return liCardHold;
  }

  @Override
  public Map<String, String> getListDBViews() {
    Connection conn = dbconn.getConn();
    Map<String, String> liViews = new HashMap<>();
    // liViews.put((String)null, null);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(getQryListVIEWS())) {
      while (rs.next()) {
        String view = rs.getString(1);
        liViews.put(view, String.format(getQryListVIEW_PATT(), view));
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryListVIEWS(), e.getMessage(), e);
    }
    return liViews;
  }

}
