package sm.clagenna.banca.sql;

import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.stdcla.sql.DBConn;

public interface ISQLGest {
  void setTableName(String szTblNam);
  
  void setDbconn(DBConn conn);
  
  void setOverwrite(boolean bv);
  
  void write(RigaBanca ri);

  boolean existMovimento(String p_tab, RigaBanca rig);

  int deleteMovimento(String p_tab, RigaBanca rig);

  boolean insertMovimento(String p_tab, RigaBanca p_rig);
}
