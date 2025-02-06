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

  void write(RigaBanca ri);

  void beginTrans();

  void commitTrans();

  void rollBackTrans();

  int getLastRowid();
  
  Map<String, String> getListDBViews();
  
  // specifiche per il progetto "Banca"

  boolean existMovimento(RigaBanca rig);

  boolean updateMovimento(RigaBanca rig);
  
  boolean updateCodStat(RigaBanca rig);

  boolean updateCodStat(List<RigaBanca> liRb);

  int deleteMovimento(RigaBanca rig);

  boolean insertMovimento(RigaBanca p_rig);

  List<String> getListTipoCard();

  List<String> getListCardHolder();

  List<Integer> getListAnni();

  List<String> getListMeseComp(Integer m_fltrAnnoComp);

  List<String> getListCausABI();

  String getDescrCausABI(String causABI);

  


}
