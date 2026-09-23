package sm.clagenna.banca.sql;

import java.util.List;
import java.util.Map;

import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.dati.csv.CsvImpFile;
import sm.clagenna.stdcla.sql.DBConn;

public interface ISQLGest {
  // void setTableName(String szTblNam);

  DBConn getDbconn();

  void setDbconn(DBConn conn);

  void setOverwrite(boolean bv);

  void beginTrans();

  void commitTrans();

  void rollBackTrans();

  int getLastRowid();

  // specifiche per il progetto "Banca"

  Map<String, String> getListDBViews();

  // --------- Views sui movimenti -----------
  List<RigaBanca> getListMovimenti(int ini, int fin, String where);

  boolean existMovimento(RigaBanca rig);

  void writeMovimento(RigaBanca ri);

  boolean updateMovimento(RigaBanca rig);

  int deleteMovimento(RigaBanca rig);

  boolean insertMovimento(RigaBanca p_rig);

  // --------------  CODICI STATISTICI ------------
  int getQtaIdCodstatsInMov();

  boolean updateCodStat(RigaBanca rig);

  boolean updateCodStat(List<RigaBanca> liRb);

  /**
   * Azzera tutti i riferimenti ai Codici Statistici nella tabella
   * Movimenti(idCodStat) in vista del import della tabella CodiciStat
   *
   * @return
   */
  int azzeraIdCodStats();

  // --------------  FILES DI IMPORTAZIONE ------------
  boolean existCsvImpFile(CsvImpFile rig);

  void writeCsvImpFile(CsvImpFile ri);

  boolean updateCsvImpFile(CsvImpFile rig);

  /** elimina tutte le registrazioni pertinenti al file di importazione */
  int deleteCsvImpFile(CsvImpFile rig);

  boolean insertCsvImpFile(CsvImpFile p_rig);

  // -------------  Card Holder, TipoCard, Anni, Mesi, CausABI  ----------------

  List<String> getListTipoCard();

  List<String> getListCardHolder();

  List<Integer> getListAnni();

  List<String> getListMeseComp(Integer m_fltrAnnoComp);

  List<String> getListCausABI();

  String getDescrCausABI(String causABI);

}
