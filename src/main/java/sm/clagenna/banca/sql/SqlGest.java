package sm.clagenna.banca.sql;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.dati.csv.CsvImpFile;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

/**
 * Classe generica per la gestione dei dati bancari su DB. <br/>
 * Non dipende dal tipo di DB, ma solo dalle query SQL che devono essere
 * implementate nelle sottoclassi. <br/>
 * Utilizza un oggetto {@link DBConn} per la connessione al DB definito nel file
 * di properties sotto la voce &quot;DB.Type&quot; (es:&quotSqlServer&quot).
 * <br/>
 * Poi classi specializzate per ogni tipo di DB (SQLite, SQLServer, HSQLDB,
 * ecc.) implementano le query specifiche (vedi {@link SQLiteGest},
 * {@link SqlServerGest}) . <br/>
 *
 */
public abstract class SqlGest implements ISQLGest, PropertyChangeListener {

  public static List<String> allTables;

  private PreparedStatement stmtSelMov;
  private PreparedStatement stmtInsMov;
  private PreparedStatement stmtModMov;
  private PreparedStatement stmtDelMov;
  private PreparedStatement stmtSelCsvImpFile;
  private PreparedStatement stmtInsCsvImpFile;
  private PreparedStatement stmtModCsvImpFile;
  private PreparedStatement stmtDelCsvImpFile;
  private PreparedStatement stmtInsCodStat;
  private PreparedStatement stmtModCodStat;
  private PreparedStatement stmtDelCodStat;

  @Getter @Setter
  private DBConn dbconn;
  @Getter @Setter
  private int                     qtaRecsUpd;
  @Getter @Setter
  private int                     deleted;
  @Getter @Setter
  private int                     scarti;
  @Getter @Setter
  private int                     added;
  @Getter @Setter
  private int                     lastRowid;
  private HashMap<String, String> m_mapCausABI;
  private DataModel               model;

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

  // ----- gestione dei CSV Files -----------------

  public abstract String getQryINSCsvImpFile();

  public abstract String getQrySELCsvImpFile();

  public abstract String getQryDELCsvImpFile();

  public abstract String getQryMODCsvImpFile();

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
        if ( !model.isOverwrite()) {
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
      if (null == stmtSelMov) {
        int fq = model.getFiltriQuery();
        // resetto la ricerca sul campo "Id"
        if (ESqlFiltri.Id.isSet(fq))
          model.setFiltriQuery(fq & (ESqlFiltri.AllSets.getFlag() ^ ESqlFiltri.Id.getFlag()));
        qry.append(getQrySELMov());
        qry.append(model.getCampiFiltro());
        getLog().debug("prepare existMov:{}", qry);
        Connection conn = dbconn.getConn();
        stmtSelMov = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement {} with err={}", rig.getTiporec(), e.getMessage());
      return true;
    }

    try {
      model.applicaFiltri(stmtSelMov, 1, dbconn, rig);
      try (ResultSet res = stmtSelMov.executeQuery()) {
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
      if (null == stmtInsMov) {
        String qry = getQryINSMov();
        Connection conn = dbconn.getConn();
        stmtInsMov = conn.prepareStatement(qry.toString());
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
      dbconn.setStmtString(stmtInsMov, k++, p_rig.getTiporec());
      dbconn.setStmtInt(stmtInsMov, k++, p_rig.getIdfile());
      dbconn.setStmtDatetime(stmtInsMov, k++, p_rig.getDtmov());
      dbconn.setStmtDatetime(stmtInsMov, k++, p_rig.getDtval());
      dbconn.setStmtImporto(stmtInsMov, k++, p_rig.getDare());
      dbconn.setStmtImporto(stmtInsMov, k++, p_rig.getAvere());
      dbconn.setStmtString(stmtInsMov, k++, szDescr);
      dbconn.setStmtString(stmtInsMov, k++, szCaus);
      dbconn.setStmtString(stmtInsMov, k++, p_rig.getCardid());
      dbconn.setStmtInt(stmtInsMov, k++, p_rig.getIdcodstat());

      stmtInsMov.executeUpdate();
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
      if (null == stmtDelMov) {
        qry = new StringBuilder(getQryDELMov());
        qry.append(model.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtDelMov = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement DELETE on {} with err={}", qry, e.getMessage());
      return 0;
    }
    try {
      model.applicaFiltri(stmtDelMov, 1, dbconn, rig);
      qtaDel = stmtDelMov.executeUpdate();
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
      if (null == stmtModMov) {
        qry = new StringBuilder(getQryMODMov());
        qry.append(model.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtModMov = conn.prepareStatement(qry.toString());
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
      dbconn.setStmtInt(stmtModMov, k++, p_rig.getTiporec());
      dbconn.setStmtInt(stmtModMov, k++, p_rig.getIdfile());
      dbconn.setStmtDatetime(stmtModMov, k++, p_rig.getDtmov());
      dbconn.setStmtDatetime(stmtModMov, k++, p_rig.getDtval());
      dbconn.setStmtImporto(stmtModMov, k++, p_rig.getDare());
      dbconn.setStmtImporto(stmtModMov, k++, p_rig.getAvere());
      dbconn.setStmtString(stmtModMov, k++, p_rig.getDescr());
      dbconn.setStmtString(stmtModMov, k++, szCaus);
      dbconn.setStmtString(stmtModMov, k++, p_rig.getCardid());
      dbconn.setStmtString(stmtModMov, k++, p_rig.getCodstat());

      dbconn.setStmtInt(stmtModMov, k++, p_rig.getRigaid());

      stmtModMov.executeUpdate();

    } catch (SQLException e) {
      getLog().error("Errore INSERT in {} with err={}", p_rig.getTiporec(), e.getMessage());
    }
    // System.out.println(tm.stop());
    return bRet;
  }

  public List<CsvImpFile> getListCsvImpFiles() {
    List<CsvImpFile> liDbFiles = new ArrayList<>();
    // SQLite e SQLServer sono compatibili
    String szQry = ConstsSQL.QRY_SQLITE_SEL_ImpFiles.substring(0, ConstsSQL.QRY_SQLITE_SEL_ImpFiles.indexOf("WHERE"));
    szQry += " order by id";
    PreparedStatement lstmt = null;
    try {
      Connection conn = getDbconn().getConn();
      lstmt = conn.prepareStatement(szQry);
    } catch (SQLException e) {
      getLog().error("Errore prep statement {} on ImpFiles with err={}", szQry, e.getMessage());
    }
    try {
      try (ResultSet res = lstmt.executeQuery()) {
        if (res.isClosed()) {
          getLog().warn("dataset closed on SEL info ImpFiles");
          return liDbFiles;
        }
        while (res.next()) {
          CsvImpFile csvImpf = new CsvImpFile();
          csvImpf.setId(res.getInt(ConstsSQL.CsvImpFile_ColNo_id));
          csvImpf.setFileName(res.getString(ConstsSQL.CsvImpFile_ColNo_filename));
          csvImpf.setRelDir(res.getString(ConstsSQL.CsvImpFile_ColNo_reldir));
          if ( !Utils.isValue(csvImpf.getSize()))
            csvImpf.setSize(res.getInt(ConstsSQL.CsvImpFile_ColNo_size));
          if ( !Utils.isValue(csvImpf.getQtarecs()))
            csvImpf.setQtarecs(res.getInt(ConstsSQL.CsvImpFile_ColNo_qtarecs));
          if ( !Utils.isValue(csvImpf.getDtmin()))
            csvImpf.setDtmin(ParseData.parseData(res.getString(ConstsSQL.CsvImpFile_ColNo_dtmin)));
          if ( !Utils.isValue(csvImpf.getDtmax()))
            csvImpf.setDtmax(ParseData.parseData(res.getString(ConstsSQL.CsvImpFile_ColNo_dtmax)));
          if ( !Utils.isValue(csvImpf.getUltagg()))
            csvImpf.setUltagg(ParseData.parseData(res.getString(ConstsSQL.CsvImpFile_ColNo_ultagg)));
          liDbFiles.add(csvImpf);
        }
      }
    } catch (SQLException e) {
      getLog().error("Errore get info ImpFiles with err={}", e.getMessage(), e);
    }
    return liDbFiles;
  }

  @Override
  public void writeCsvImpFile(CsvImpFile ri) {
    try {
      if (existCsvImpFile(ri)) {
        if ( !model.isOverwrite()) {
          getLog().debug("Il CsvImpFile esiste! scarto {} ", ri.toString());
          scarti++;
          return;
        }
        deleted += deleteCsvImpFile(ri);
      }
      insertCsvImpFile(ri);
      added++;
    } catch (Exception e) {
      getLog().error("!err scrittura DB, {}", e.getMessage(), e);
    }
  }

  @Override
  public boolean existCsvImpFile(CsvImpFile p_csvimp) {
    boolean bRet = false;
    int qta = 0;
    //
    StringBuilder qry = new StringBuilder();
    try {
      if (null == stmtSelCsvImpFile) {
        qry.append(getQrySELCsvImpFile());
        getLog().debug("prepare existCsvImpFile:{}", qry);
        Connection conn = dbconn.getConn();
        stmtSelCsvImpFile = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement existCsvImpFile with err={}", e.getMessage());
      return true;
    }

    try {
      stmtSelCsvImpFile.setString(1, p_csvimp.getFileName());
      stmtSelCsvImpFile.setString(2, p_csvimp.getRelDir());
      try (ResultSet res = stmtSelCsvImpFile.executeQuery()) {
        while (res.next())
          qta = res.getInt(1);
        bRet = qta != 0;
      }
    } catch (SQLException e) {
      getLog().error("Errore query {} with err={}", getQrySELCsvImpFile(), e.getMessage());
    }
    // System.out.println(tm.stop());
    return bRet;
  }

  /**
   * Inserisce un record di importazione CSV nella tabella ImpFiles. <br/>
   * Se il record esiste gia' e' attivo il flag overwrite, allora viene prima
   * cancellato e poi reinserito.<br/>
   * Al file vengono aggiornate 2 informazioni:
   * <ol>
   * <li>ultagg: data e ora dell'ultimo aggiornamento</li>
   * <li>id: viene aggiornato con l'ID del record appena inserito</li>
   * </ol>
   *
   * @param p_csvfile
   * @return true se inserito correttamente, false altrimenti
   */
  @Override
  public boolean insertCsvImpFile(CsvImpFile p_csvfile) {
    boolean bRet = false;
    lastRowid = -1;
    // TimerMeter tm = new TimerMeter("Insert");
    try {
      if (null == stmtInsCsvImpFile) {
        String qry = getQryINSCsvImpFile();
        Connection conn = dbconn.getConn();
        stmtInsCsvImpFile = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement INSERT File {} with err={}", p_csvfile.getFileName(), e.getMessage());
      return false;
    }

    try {
      int k = 1;
      p_csvfile.setUltagg(LocalDateTime.now());
      dbconn.setStmtString(stmtInsCsvImpFile, k++, p_csvfile.getFileName());
      dbconn.setStmtString(stmtInsCsvImpFile, k++, p_csvfile.getRelDir());
      dbconn.setStmtInt(stmtInsCsvImpFile, k++, p_csvfile.getSize());
      dbconn.setStmtInt(stmtInsCsvImpFile, k++, p_csvfile.getQtarecs());
      dbconn.setStmtDate(stmtInsCsvImpFile, k++, p_csvfile.getDtmin());
      dbconn.setStmtDate(stmtInsCsvImpFile, k++, p_csvfile.getDtmax());
      dbconn.setStmtDate(stmtInsCsvImpFile, k++, p_csvfile.getUltagg());

      stmtInsCsvImpFile.executeUpdate();
      int ii = dbconn.getLastIdentity();
      p_csvfile.setId(ii);
      setLastRowid(ii);
    } catch (SQLException e) {
      getLog().error("Errore INSERT on file {} with err={}", p_csvfile.getFileName(), e.getMessage());
    }
    return bRet;
  }

  /**
   * Cancella il record di importazione CSV indicat0 nella variabile csvif.
   * <br/>
   *
   * @param liFiles
   * @return numero totale di record cancellati
   */
  @Override
  public int deleteCsvImpFile(CsvImpFile csvif) {
    // throw new UnsupportedOperationException("La deleteCsvImpFile() non e' supportata !");
    int qtaDel = 0;
    // TimerMeter tm = new TimerMeter("Delete");
    StringBuilder qry = null;
    Integer idFile = csvif.getId();
    if ( !Utils.isValue(idFile)) {
      getLog().warn("deleteCsvImpFile() *NON* possibile - idFile non valorizzato, file={}", csvif.getFileName());
      return 0;
    }
    try {
      if (null == stmtDelCsvImpFile) {
        qry = new StringBuilder(getQryDELCsvImpFile());
        // la delete e' sempre sul campo ID, quindi non serve applicare i filtri
        // qry.append(model.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtDelCsvImpFile = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore prep statement DELETE on {} with err={}", qry, e.getMessage());
      return 0;
    }
    try {
      // model.applicaFiltri(stmtDelCsvImpFile, 1, dbconn, csvif);
      if (Utils.isValue(csvif.getId())) {
        stmtDelCsvImpFile.setInt(1, idFile);
        qtaDel = stmtDelCsvImpFile.executeUpdate();
      }
    } catch (SQLException e) {
      getLog().error("Errore DELETE on {} with err={}", qry, e.getMessage());
    }
    // System.out.println(tm.stop());
    return qtaDel;
  }

  /**
   * Vengono cancellati <b>prima</b> tutti i movimenti (tabella Movimenti) che
   * fanno riferimento a questi file (idfile). <br/>
   * Cancella tutti i record di importazione CSV indicati nella lista liFiles.
   * <br/>
   *
   * @param liFiles
   * @return numero totale di record cancellati
   */
  public int deleteCsvImpFiles(List<CsvImpFile> liFiles) {
    String szWhe = liFiles.stream().map(s -> String.valueOf(s.getId())).collect(Collectors.joining(","));
    long qtaDel = 0;
    final String szQryMas = "DELETE FROM %s WHERE %s IN (%s)";
    Connection conn = getDbconn().getConn();
    for (String szTb : SqlGest.allTables) {
      String szId = "id";
      if (szTb.startsWith("mov"))
        szId = "idfile";
      String szQry = String.format(szQryMas, szTb, szId, szWhe);
      try (Statement stmt = conn.createStatement()) {
        qtaDel += stmt.executeLargeUpdate(szQry);
        getLog().warn("Delete da tabella {} con ID files {}", szTb, szWhe);
      } catch (Exception e) {
        getLog().error("Errore SQL \"{}\"", szQry, e);
      }
    }
    return (int) qtaDel;
  }

  @Override
  public boolean updateCsvImpFile(CsvImpFile p_impf) {
    boolean bRet = false;
    StringBuilder qry = null;
    try {
      if (null == stmtModCsvImpFile) {
        qry = new StringBuilder(getQryMODCsvImpFile());
        // qry.append(model.getCampiFiltro());
        Connection conn = dbconn.getConn();
        stmtModCsvImpFile = conn.prepareStatement(qry.toString());
      }
    } catch (SQLException e) {
      getLog().error("Errore UPDATE on {} with err={}", qry, e.getMessage());
      return false;
    }

    try {
      int k = 1;
      dbconn.setStmtString(stmtModCsvImpFile, k++, p_impf.getFileName());
      dbconn.setStmtString(stmtModCsvImpFile, k++, p_impf.getRelDir());
      dbconn.setStmtInt(stmtModCsvImpFile, k++, p_impf.getSize());
      dbconn.setStmtInt(stmtModCsvImpFile, k++, p_impf.getQtarecs());
      dbconn.setStmtDate(stmtModCsvImpFile, k++, p_impf.getDtmin());
      dbconn.setStmtDate(stmtModCsvImpFile, k++, p_impf.getDtmax());
      dbconn.setStmtDate(stmtModCsvImpFile, k++, p_impf.getUltagg());
      dbconn.setStmtInt(stmtModCsvImpFile, k++, p_impf.getId());

      qtaRecsUpd = stmtModCsvImpFile.executeUpdate();
    } catch (SQLException e) {
      getLog().error("Errore UPDATE file {} with err={}", p_impf.getFileName(), e.getMessage());
    }
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
  public List<RigaBanca> getListMovimenti(int ini, int fin, String where) {
    Connection conn = dbconn.getConn();
    List<RigaBanca> liMovs = new ArrayList<>();
    StringBuilder szQry = new StringBuilder("SELECT * FROM listamovimenti WHERE 1=1 ");
    if ( !Utils.isValue(where))
      szQry.append(" order by dtmov, dtval, tipo, cardid, descr");
    else
      szQry.append(" and ").append(where).append(" order by dtmov, dtval, tipo, cardid, descr");
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(szQry.toString())) {
      int qta = 0;
      while (rs.next()) {
        if (++qta < ini)
          continue;
        if (qta > fin)
          break;
        RigaBanca ri = RigaBanca.popolaDaResultSet(rs, dbconn);
        if ( !ri.isValido())
          getLog().warn("Riga non valida: {}", ri.toStringShort());
        liMovs.add(ri);
      }
    } catch (SQLException e) {
      getLog().error("Query {}; err={}", getQrySELMov(), e.getMessage(), e);
    }
    return liMovs;
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
      case Consts.EVT_OPTZ_FILTR_CHANGE:
        for (PreparedStatement pst : new PreparedStatement[] { stmtSelMov, stmtDelMov, stmtModMov }) {
          if (null != pst) {
            try {
              pst.close();
            } catch (SQLException e) {
              //
            }
          }
        }
        stmtSelMov = stmtDelMov = stmtModMov = null;
        break;
      default:
        break;
    }
  }
}
