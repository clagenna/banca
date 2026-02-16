package sm.clagenna.banca.sql;

import java.util.List;
import java.util.Map;

import sm.clagenna.banca.dati.RigaBanca;
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

  // elenco delle Viewa sui movimenti
  Map<String, String> getListDBViews();

  boolean existMovimento(RigaBanca rig);

  void writeMovimento(RigaBanca ri);

  boolean updateMovimento(RigaBanca rig);

  int deleteMovimento(RigaBanca rig);

  boolean insertMovimento(RigaBanca p_rig);

  // --------------  CODICI STATISTICI ------------

  boolean updateCodStat(RigaBanca rig);

  boolean updateCodStat(List<RigaBanca> liRb);

  List<String> getListTipoCard();

  List<String> getListCardHolder();

  List<Integer> getListAnni();

  List<String> getListMeseComp(Integer m_fltrAnnoComp);

  List<String> getListCausABI();

  String getDescrCausABI(String causABI);

}
