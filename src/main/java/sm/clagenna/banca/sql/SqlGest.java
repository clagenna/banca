package sm.clagenna.banca.sql;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.Utils;

public abstract class SqlGest implements ISQLGest, PropertyChangeListener {

  public static List<String> allTables;

  private PreparedStatement stmtSel;
  private PreparedStatement stmtIns;
  private PreparedStatement stmtMod;
  private PreparedStatement stmtDel;
  // private PreparedStatement stmtLastRowId;
  private PreparedStatement stmtInsCodStat;
  private PreparedStatement stmtModCodStat;
  private PreparedStatement stmtDelCodStat;

  //  @Getter @Setter
  //  private String  tableName;
  @Getter @Setter
  private DBConn                  dbconn;
  @Getter @Setter
  private boolean                 overwrite;
  @Getter @Setter
  private int                     deleted;
  @Getter @Setter
  private int                     scarti;
  @Getter @Setter
  private int                     added;
  @Getter @Setter
  private int                     lastRowid;
  private HashMap<String, String> m_mapCausABI;

  private DataModel model;

  static {
    allTables = Arrays.asList(new String[] { //
        "impFiles", //
        "movimenti", //
        "CodiciStat" });
  }

  public SqlGest() {
    init();
  }

  private void init() {
    deleted = 0;
    scarti = 0;
    added = 0;
    model = DataModel.getInst();
  }

  public abstract Logger getLog();

  public abstract String getQryListCARDS();

  public abstract String getQryListANNI();

  public abstract String getQryListMESI();

  public abstract String getQryListCAUSABI();

  public abstract String getQryListCARDHOLD();

  public abstract String getQryListVIEWS();

  public abstract String getQryQtaIdCodstat();

  /** Deve tornare la SELECT %s con elenco colonne libero */
  public abstract String getQryListVIEW_PATT();

  public abstract String getQryLASTROWID();

  // ----- gestione MOVIMENTI -----------------

  public abstract String getQryINSMov();

  public abstract String getQrySELMov();

  public abstract String getQryDELMov();

  public abstract String getQryMODMov();

  public abstract String getQryMODMovCodstat();

  public abstract String getQryAzzeraIdCodStats();

  // ----- gestione CODICI STATISTICI  -----------------

  public abstract String getQryINSCodstat();

  public abstract String getQrySELCodstat();

  public abstract String getQryDELCodstat();

  public abstract String getQryMODCodstat();

  @Override
  public void beginTrans() {
    getDbconn().beginTrans();
    //    try {
    //      Connection conn = getDbconn().getConn();
    //      conn.setAutoCommit(false);
    //      m_savePoint = conn.setSavepoint();
    //    } catch (SQLException e) {
    //      getLog().error("BEGIN TRAN Error {}", e.getMessage());
    //    }
  }

  @Override
  public void commitTrans() {
    getDbconn().commitTrans();
    //    try {
    //      getDbconn().getConn().setAutoCommit(true);
    //      m_savePoint = null;
    //    } catch (SQLException e) {
    //      getLog().error("COMMIT TRAN Error {}", e.getMessage());
    //    }
  }

  @Override
  public void rollBackTrans() {
    getDbconn().rollBackTrans();
    //    try {
    //      Connection conn = getDbconn().getConn();
    //      conn.rollback(m_savePoint);
    //      m_savePoint = null;
    //    } catch (SQLException e) {
    //      getLog().error("BEGIN TRAN Error {}", e.getMessage());
    //    }
  }

  @Override
  public void writeMovimento(RigaBanca ri) {
    try {
      if (existMovimento(ri)) {
        if ( !overwrite) {
          getLog().debug("Il movimento esiste! scarto {} ", ri.toString());
          scarti++;
          return;
        }
        deleted += deleteMovimento(ri);
      }
      insertMovimento(ri);
      added++;
    } catch (Exception e) {
      getLog().error("!err scrittura DB, {}", e.getMessage(), e);
    }
  }

  @Override
  public boolean existMovimento(RigaBanca rig) {
    boolean bRet = false;
    int qta = 0;
    // 
    StringBuilder qry = new StringBuilder();
    try {
      if (null == stmtSel) {
        int fq = model.getFiltriQuery();
        // resetto la ricerca sul campo "Id"
        if (ESqlFiltri.Id.isSet(fq))
          model.setFiltriQuery(fq & (ESqlFiltri.AllSets.getFlag() ^ ESqlFiltri.Id.getFlag()));
        qry.append(getQrySELMov());
        qry.append(model.getCampiFiltro());
        getLog().debug("prepare existMov:{}", qry);
        Connection conn = dbconn.getConn();
        stmtSel = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement {} with err={}", rig.getTiporec(), e.getMessage());
      return true;
    }

    try {
      model.applicaFiltri(stmtSel, 1, dbconn, rig);
      try (ResultSet res = stmtSel.executeQuery()) {
        while (res.next())
          qta = res.getInt(1);
        bRet = qta != 0;
      }
    } catch (SQLException e) {
      getLog().error("Errore query {} with err={}", rig.getTiporec(), e.getMessage());
    }
    // System.out.println(tm.stop());
    return bRet;
  }

  @Override
  public boolean insertMovimento(RigaBanca p_rig) {
    boolean bRet = false;
    lastRowid = -1;
    // TimerMeter tm = new TimerMeter("Insert");
    try {
      if (null == stmtIns) {
        String qry = getQryINSMov();
        Connection conn = dbconn.getConn();
        stmtIns = conn.prepareStatement(qry.toString());
        // stmtLastRowId = conn.prepareStatement(getQryLASTROWID());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement INSERT on {} with err={}", p_rig.getTiporec(), e.getMessage());
      return false;
    }

    try {
      String szCaus = p_rig.getAbicaus();
      if (null != szCaus)
        szCaus = szCaus.replace(".0", "");
      String szDescr = p_rig.getDescr();
      if (Utils.isValue(szDescr) && szDescr.length() > 512)
        szDescr = szDescr.substring(0, 512);
      int k = 1;
      dbconn.setStmtString(stmtIns, k++, p_rig.getTiporec());
      dbconn.setStmtInt(stmtIns, k++, p_rig.getIdfile());
      dbconn.setStmtDatetime(stmtIns, k++, p_rig.getDtmov());
      dbconn.setStmtDatetime(stmtIns, k++, p_rig.getDtval());
      dbconn.setStmtImporto(stmtIns, k++, p_rig.getDare());
      dbconn.setStmtImporto(stmtIns, k++, p_rig.getAvere());
      dbconn.setStmtString(stmtIns, k++, szDescr);
      dbconn.setStmtString(stmtIns, k++, szCaus);
      dbconn.setStmtString(stmtIns, k++, p_rig.getCardid());
      dbconn.setStmtInt(stmtIns, k++, p_rig.getIdcodstat());

      stmtIns.executeUpdate();
      lastRowid = dbconn.getLastIdentity();
    } catch (SQLException e) {
      getLog().error("Errore INSERT on {} with err={}", p_rig.getTiporec(), e.getMessage());
    }
    // System.out.println(tm.stop());
    return bRet;
  }

  @Override
  public int deleteMovimento(RigaBanca rig) {
    int qtaDel = 0;
    // TimerMeter tm = new TimerMeter("Delete");
    StringBuilder qry = null;
    try {
      if (null == stmtDel) {
        qry = new StringBuilder(getQryDELMov());
        qry.append(model.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtDel = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement DELETE on {} with err={}", qry, e.getMessage());
      return 0;
    }
    try {
      model.applicaFiltri(stmtDel, 1, dbconn, rig);
      qtaDel = stmtDel.executeUpdate();
    } catch (SQLException e) {
      getLog().error("Errore DELETE on {} with err={}", qry, e.getMessage());
    }
    // System.out.println(tm.stop());
    return qtaDel;
  }

  @Override
  public boolean updateMovimento(RigaBanca p_rig) {
    boolean bRet = false;
    StringBuilder qry = null;
    try {
      if (null == stmtMod) {
        qry = new StringBuilder(getQryMODMov());
        qry.append(model.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtMod = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore UPDATE on {} with err={}", qry, e.getMessage());
      return false;
    }

    try {
      String szCaus = p_rig.getAbicaus();
      if (null != szCaus)
        szCaus = szCaus.replace(".0", "");
      int k = 1;
      dbconn.setStmtInt(stmtMod, k++, p_rig.getTiporec());
      dbconn.setStmtInt(stmtMod, k++, p_rig.getIdfile());
      dbconn.setStmtDatetime(stmtMod, k++, p_rig.getDtmov());
      dbconn.setStmtDatetime(stmtMod, k++, p_rig.getDtval());
      dbconn.setStmtImporto(stmtMod, k++, p_rig.getDare());
      dbconn.setStmtImporto(stmtMod, k++, p_rig.getAvere());
      dbconn.setStmtString(stmtMod, k++, p_rig.getDescr());
      dbconn.setStmtString(stmtMod, k++, szCaus);
      dbconn.setStmtString(stmtMod, k++, p_rig.getCardid());
      dbconn.setStmtString(stmtMod, k++, p_rig.getCodstat());

      dbconn.setStmtInt(stmtMod, k++, p_rig.getRigaid());

      stmtMod.executeUpdate();

    } catch (SQLException e) {
      getLog().error("Errore INSERT in {} with err={}", p_rig.getTiporec(), e.getMessage());
    }
    // System.out.println(tm.stop());
    return bRet;
  }

  /**
   * Verifica se nei movimenti e' mai stato assegnato un codice statistico.
   * Questo serve per poter decidere se e' possibile leggere ed aggiornare la
   * tabella dei codici statistici (CodiciStat) da un file da leggere.
   */
  @Override
  public int getQtaIdCodstatsInMov() {
    int retQta = -1;
    Connection conn = dbconn.getConn();
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(getQryQtaIdCodstat())) {
      while (rs.next()) {
        retQta = rs.getInt(1);
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryQtaIdCodstat(), e.getMessage(), e);
    }
    return retQta;
  }

  public void insertCodStat(CodStat cdsCurr) {
    try {
      if (null == stmtInsCodStat) {
        Connection conn = dbconn.getConn();
        stmtInsCodStat = conn.prepareStatement(getQryINSCodstat());
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryINSCodstat(), e.getMessage(), e);
    }
    try {
      int k = 1;
      dbconn.setStmtString(stmtInsCodStat, k++, cdsCurr.getCodice());
      dbconn.setStmtString(stmtInsCodStat, k++, cdsCurr.getDescr());

      stmtInsCodStat.executeUpdate();
      lastRowid = dbconn.getLastIdentity();
      cdsCurr.setIdCodStat(lastRowid);
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryMODCodstat(), e.getMessage(), e);
    }
  }

  @Override
  public boolean updateCodStat(RigaBanca rig) {
    String qry1 = getQryMODMovCodstat();
    String qry2 = String.format(qry1, rig.getTiporec());

    Connection conn = dbconn.getConn();
    try (PreparedStatement stmtModCod = conn.prepareStatement(qry2)) {
      int k = 1;
      dbconn.setStmtInt(stmtModCod, k++, rig.getIdcodstat());
      dbconn.setStmtInt(stmtModCod, k++, rig.getRigaid());

      stmtModCod.executeUpdate();
    } catch (SQLException e) {
      getLog().error("Errore MODIF codstat on {} with err={}", rig.getTiporec(), e.getMessage());
      return false;
    }
    return true;
  }

  @Override
  public boolean updateCodStat(List<RigaBanca> rigs) {
    String qry1 = getQryMODMovCodstat();
    Connection conn = dbconn.getConn();
    beginTrans();
    int qtaTrans = 0;

    for (RigaBanca rig : rigs) {
      String qry2 = String.format(qry1, rig.getTiporec());

      try (PreparedStatement stmtModCod = conn.prepareStatement(qry2)) {
        int k = 1;
        dbconn.setStmtInt(stmtModCod, k++, rig.getIdcodstat());
        dbconn.setStmtInt(stmtModCod, k++, rig.getRigaid());

        stmtModCod.executeUpdate();

        if (++qtaTrans > 50) {
          commitTrans();
          qtaTrans = 0;
          beginTrans();
        }
      } catch (SQLException e) {
        getLog().error("Errore MODIF codstat on {} with err={}", rig.getTiporec(), e.getMessage());
        return false;
      }
    }
    commitTrans();
    return true;
  }

  public String deleteCodStat(CodStat cds) {
    String szRet = null;
    if (null == cds || cds.getIdCodStat() == 0) {
      szRet = String.format("Insufficienti info per cancellare <br/>%s", null != cds ? cds.toStringEx() : "*null*");
      return szRet;
    }
    Connection conn = dbconn.getConn();
    try {
      if (null == stmtDelCodStat) {
        stmtDelCodStat = conn.prepareStatement(getQryDELCodstat());
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryDELCodstat(), e.getMessage());
    }
    try {
      int k = 1;
      dbconn.setStmtInt(stmtDelCodStat, k++, cds.getIdCodStat());
      int qta = stmtDelCodStat.executeUpdate();
      if (qta > 0)
        szRet = String.format("Cancellato %s", cds.toStringEx());
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryDELCodstat(), e.getMessage(), e);
    }
    return szRet;
  }

  /**
   * Azzera tutti i riferimenti ai Codici Statistici nella tabella
   * Movimenti(idCodStat) in vista del import della tabella CodiciStat
   *
   * @return
   */
  @Override
  public int azzeraIdCodStats() {
    int qtaRecs = -1;
    String qry = getQryAzzeraIdCodStats();
    Connection conn = dbconn.getConn();
    try (PreparedStatement stmtAzzeraIdCds = conn.prepareStatement(qry)) {
      qtaRecs = stmtAzzeraIdCds.executeUpdate();
    } catch (SQLException e) {
      getLog().error("Errore AzZERAMENTO riferimenti codstat con err={}", e.getMessage());
    }
    return qtaRecs;
  }

  public int deleteAllCodStats() {
    int qtaRecs = -1;
    String szQry1 = getQryDELCodstat();
    String szQry2 = szQry1.substring(0, szQry1.toLowerCase().indexOf("where "));
    Connection conn = dbconn.getConn();
    try (PreparedStatement stmtAzzeraIdCds = conn.prepareStatement(szQry2)) {
      qtaRecs = stmtAzzeraIdCds.executeUpdate();
    } catch (SQLException e) {
      getLog().error("Errore AzZERAMENTO riferimenti codstat con err={}", e.getMessage());
    }
    return qtaRecs;
  }

  public int insAllCodStats(List<CodStat> righe) {
    List<CodStat> li2 = new ArrayList<CodStat>(righe);
    Collections.sort(li2);
    for (CodStat cds : li2) {
      insertCodStat(cds);
    }
    return righe.size();
  }

  //  private int trovaLastRowid() {
  //    if (null == stmtLastRowId) {
  //      try {
  //        Connection conn = dbconn.getConn();
  //        stmtLastRowId = conn.prepareStatement(getQryLASTROWID());
  //      } catch (SQLException e) {
  //        getLog().error("Errore prep statement Last RowID with err={}", e.getMessage());
  //        return -1;
  //      }
  //    }
  //    lastRowid = 0;
  //    try {
  //      ResultSet res = stmtLastRowId.executeQuery();
  //      while (res.next()) {
  //        lastRowid = res.getInt(1);
  //      }
  //    } catch (SQLException e) {
  //      getLog().error("Errore Last Row ID with err={}", e.getMessage());
  //    }
  //    return lastRowid;
  //  }
  // FIXTO Creare il CodStat "99" se non esiste sul DB per la somma degli importi sconosciuti

  /**
   * Legge tutti i codici statistici presenti sul DB e li restituisce in una
   * lista ordinata per codice. <br/>
   * Inoltre se <b>non</b> esiste il codice statistico <code>"99"</code> lo crea
   * con descrizione <code>"Spese Non Classificate"</code> per poter assegnare
   * (sommare) gli importi senza codstat
   * 
   * @return
   */
  public List<CodStat> getListCodStat() {
    Connection conn = dbconn.getConn();
    CodStat cds99 = new CodStat(9999999, "99", "Spese Non Classificate");
    boolean bNoCds99 = true;
    List<CodStat> liCodStat = new ArrayList<>();
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(getQrySELCodstat())) {
      while (rs.next()) {
        int k = 1;
        int idCd = rs.getInt(k++);
        String cods = rs.getString(k++);
        String desc = rs.getString(k++);
        CodStat co = new CodStat(idCd, cods, desc);
        if (co.getCodice().equals(cds99.getCodice()))
          bNoCds99 = false;
        liCodStat.add(co);
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQrySELCodstat(), e.getMessage(), e);
    }
    if (bNoCds99) {
      // insertCodStat(cds99); per ora non lo inserisco, lo creo solo in memoria e lo aggiungo alla lista
      liCodStat.add(cds99);
    }
    return liCodStat;
  }

  /**
   * Verifica se esiste un codice statistico con lo stesso
   * <b><code>idcodstat</code></b> di quello passato come parametro. Se si,
   * restituisce true, altrimenti false. <br/>
   * Se il codice statistico passato e' null o ha id=0, restituisce false.
   * 
   * @param cdsCurr
   * @return
   */
  public boolean existCodStat(CodStat cdsCurr) {
    boolean bRet = false;
    if (null == cdsCurr || cdsCurr.getIdCodStat() == 0)
      return bRet;
    Connection conn = dbconn.getConn();
    String szQry = getQrySELCodstat();
    int n = szQry.indexOf("1=1") + 3;
    String szSin = szQry.substring(0, n);
    String szDes = szQry.substring(n);
    String szQryOk = String.format("%s AND idCodStat=%d %s", szSin, cdsCurr.getIdCodStat(), szDes);
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(szQryOk)) {
      while (rs.next()) {
        int idCd = rs.getInt(1);
        bRet = idCd == cdsCurr.getIdCodStat();
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", szQryOk, e.getMessage(), e);
    }
    return bRet;
  }

  public void updadetCodStat(CodStat cdsCurr) {
    if (null == cdsCurr || cdsCurr.getIdCodStat() == 0)
      return;
    Connection conn = dbconn.getConn();
    try {
      if (null == stmtModCodStat) {
        stmtModCodStat = conn.prepareStatement(getQryMODCodstat());
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryMODCodstat(), e.getMessage(), e);
    }
    try {
      int k = 1;
      dbconn.setStmtString(stmtModCodStat, k++, cdsCurr.getCodice());
      dbconn.setStmtString(stmtModCodStat, k++, cdsCurr.getDescr());
      dbconn.setStmtInt(stmtModCodStat, k++, cdsCurr.getIdCodStat());

      stmtModCodStat.executeUpdate();
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQryMODCodstat(), e.getMessage(), e);
    }
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
    Collections.sort(liAnno);
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
    if (null == pAnno)
      return liMesi;
    Collections.sort(liMesi);
    List<String> li2 = liMesi //
        .stream() //
        .filter(s -> s.startsWith(pAnno.toString())) //
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
    Map<String, String> liViews = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    // liViews.put((String)null, null);
    String szQryLiViewa = getQryListVIEWS();
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(szQryLiViewa)) {
      while (rs.next()) {
        String view = rs.getString(1);
        liViews.put(view, String.format(getQryListVIEW_PATT(), EColsTableView.allColumns(), view));
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", szQryLiViewa, e.getMessage(), e);
    }
    return liViews;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szEvtId = evt.getPropertyName();
    // Object obj = evt.getNewValue();
    switch (szEvtId) {
      // devo ricreare/riaprire lo stmt se cambia il filtro
      case DataModel.EVT_OPTZ_FILTR_CHANGE:
        for (PreparedStatement pst : new PreparedStatement[] { stmtSel, stmtDel, stmtMod }) {
          if (null != pst) {
            try {
              pst.close();
            } catch (SQLException e) {
              //
            }
          }
        }
        stmtSel = stmtDel = stmtMod = null;
        break;
      default:
        break;
    }
  }
}
