package sm.clagenna.banca.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SQLiteGest extends SqlGest {
  private static final Logger s_log = LogManager.getLogger(SQLiteGest.class);

  private static final String QRY_LIST_CARDS    = "SELECT DISTINCT tipo FROM ListaMovimentiUNION";
  private static final String QRY_LIST_ANNI     = "SELECT DISTINCT strftime('%Y', dtmov) as anno FROM ListaMovimentiUNION";
  private static final String QRY_LIST_MESI     = "SELECT DISTINCT movstr FROM ListaMovimentiUNION ORDER BY movstr";
  private static final String QRY_LIST_CAUSABI  = "SELECT abicaus, descrcaus || ' (' || abicaus || ')' as descr FROM causali ORDER BY descr";
  private static final String QRY_LIST_CARDHOLD = "SELECT DISTINCT cardid FROM ListaMovimentiUNION WHERE cardid IS NOT NULL ORDER BY cardid";
  private static final String QRY_LIST_VIEWS    = "SELECT name FROM sqlite_master WHERE type = 'view'";
  private static final String QRY_VIEW_PATT     = "SELECT %s from %s WHERE 1=1 ORDER BY dtMov,dtval;";
  private static final String QRY_LAST_ROWID    = "SELECT last_insert_rowid()";

  private static final String QRY_INS_Mov = //
      "INSERT INTO movimenti%s" //
          + "                 (idfile" //
          + "                 ,dtmov" //
          + "                 ,dtval" //
          + "                 ,dare" //
          + "                 ,avere" //
          + "                 ,descr" //
          + "                 ,abicaus" //
          + "                 ,cardid" //
          + "                 ,codstat)" //
          + "           VALUES (?,?,?,?,?,?,?,?,?)";

  private static final String QRY_SEL_Mov = //
      "SELECT COUNT(*)" //
          + "  FROM movimenti%s" //
          + " WHERE 1=1"; //

  private static final String QRY_DEL_Mov = //
      "DELETE FROM movimenti%s" //
          + " WHERE 1=1"; //

  private static final String QRY_MOD_Mov = //
      "UPDATE movimenti%s" //
          + "  SET idfile=?" //
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
      "UPDATE movimenti%s" //
          + "  SET codstat=?" //
          + "  WHERE id=?";

  public SQLiteGest() {
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
