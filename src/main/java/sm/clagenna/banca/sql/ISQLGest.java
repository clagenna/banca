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

  boolean existMovimento(String p_tab, RigaBanca rig);

  int deleteMovimento(String p_tab, RigaBanca rig);

  boolean insertMovimento(String p_tab, RigaBanca p_rig);

  List<String> getListTipoCard();

  List<String> getListCardHolder();

  List<Integer> getListAnni();

  List<String> getListMeseComp();
  
  List<String> getListCausABI();

  Map<String, String> getListDBViews();

}
