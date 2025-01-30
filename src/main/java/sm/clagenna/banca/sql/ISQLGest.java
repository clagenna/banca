package sm.clagenna.banca.sql;

import java.util.List;
import java.util.Map;

import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.stdcla.sql.DBConn;

public interface ISQLGest {
  void setTableName(String szTblNam);

  DBConn getDbconn();

  void setDbconn(DBConn conn);

  void setOverwrite(boolean bv);

  void write(RigaBanca ri);

  void beginTrans();

  void commitTrans();

  void rollBackTrans();

  boolean existMovimento(String p_tab, RigaBanca rig);

  boolean updateMovimento(String p_tab, RigaBanca rig);
  
  boolean updateCodStat(RigaBanca rig);

  boolean updateCodStat(List<RigaBanca> liRb);

  int deleteMovimento(String p_tab, RigaBanca rig);

  boolean insertMovimento(String p_tab, RigaBanca p_rig);

  List<String> getListTipoCard();

  List<String> getListCardHolder();

  List<Integer> getListAnni();

  List<String> getListMeseComp(Integer m_fltrAnnoComp);

  List<String> getListCausABI();

  String getDescrCausABI(String causABI);

  int getLastRowid();

  Map<String, String> getListDBViews();

}
