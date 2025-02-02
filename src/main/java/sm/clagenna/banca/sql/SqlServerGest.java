package sm.clagenna.banca.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SqlServerGest extends SqlGest {
  private static final Logger s_log = LogManager.getLogger(SqlServerGest.class);

  private static final String QRY_LIST_CARDS    = "SELECT DISTINCT tipo FROM dbo.movimenti";
  private static final String QRY_LIST_ANNI     = "SELECT DISTINCT YEAR(dtmov) as anno FROM movimenti ORDER BY 1";
  private static final String QRY_LIST_MESI     = "SELECT DISTINCT movstr FROM dbo.ListaMovimenti ORDER BY movstr";
  private static final String QRY_LIST_CAUSABI  = "SELECT abicaus, concat(descrcaus,' (',abicaus ,')') as descr FROM causali ORDER BY descr";
  private static final String QRY_LIST_CARDHOLD = "SELECT DISTINCT cardid FROM Movimenti WHERE cardid IS NOT NULL AND LEN(RTRIM(cardid)) > 0  ORDER BY cardid";
  private static final String QRY_LIST_VIEWS    = "SELECT name FROM sys.views ORDER BY name";
  private static final String QRY_VIEW_PATT     = "SELECT %s from %s WHERE 1=1 ORDER BY dtMov,dtval";
  private static final String QRY_LAST_ROWID    = "Select @@IDENTITY  as LastId";

  private static final String QRY_INS_Mov = //
      "INSERT INTO dbo.movimenti" //
          + "                 (tipo"
          + "                 ,idfile" //
          + "                 ,dtmov" //
          + "                 ,dtval" //
          + "                 ,dare" //
          + "                 ,avere" //
          + "                 ,descr" //
          + "                 ,abicaus" //
          + "                 ,cardid" //
          + "                 ,codstat)" //
          + "           VALUES (?,?,?,?,?,?,?,?,?,?)";

  private static final String QRY_SEL_Mov = //
      "SELECT COUNT(*)" //
          + "  FROM dbo.movimenti" //
          + " WHERE 1=1"; //

  private static final String QRY_DEL_Mov = //
      "DELETE FROM dbo.movimenti" //
          + " WHERE 1=1"; //

  private static final String QRY_MOD_Mov = //
      "UPDATE movimenti" //
          + "  SET tipo=?" //
          + "     ,idfile=?" //
          + "     ,dtmov=?" //
          + "     ,dtval=?" //
          + "     ,dare=?" //
          + "     ,avere=?" //
          + "     ,descr=?" //
          + "     ,abicaus=?" //
          + "     ,cardid=?" //
          + "     ,codstat=?" //
          + "  WHERE 1=1";

  private static final String QRY_MOD_CodStat = //
      "UPDATE movimenti" //
          + "  SET codstat=?" //
          + "  WHERE id=?";

  public SqlServerGest() {
    super();
  }

  @Override
  public Logger getLog() {
    return s_log;
  }

  @Override
  public String getQryListCARDS() {
    return QRY_LIST_CARDS;
  }

  @Override
  public String getQryListANNI() {
    return QRY_LIST_ANNI;
  }

  @Override
  public String getQryListMESI() {
    return QRY_LIST_MESI;
  }

  @Override
  public String getQryListCAUSABI() {
    return QRY_LIST_CAUSABI;
  }

  @Override
  public String getQryListCARDHOLD() {
    return QRY_LIST_CARDHOLD;
  }

  @Override
  public String getQryListVIEWS() {
    return QRY_LIST_VIEWS;
  }

  @Override
  public String getQryListVIEW_PATT() {
    return QRY_VIEW_PATT;
  }

  @Override
  public String getQryLASTROWID() {
    return QRY_LAST_ROWID;
  }

  @Override
  public String getQryINSMov() {
    return QRY_INS_Mov;
  }

  @Override
  public String getQrySELMov() {
    return QRY_SEL_Mov;
  }

  @Override
  public String getQryDELMov() {
    return QRY_DEL_Mov;
  }

  @Override
  public String getQryMODMov() {
    return QRY_MOD_Mov;
  }

  @Override
  public String getQryMODCodstat() {
    return QRY_MOD_CodStat;
  }

}
