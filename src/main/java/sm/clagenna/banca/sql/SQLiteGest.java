package sm.clagenna.banca.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Gestore delle query di gestione del DB delle transazioni Bancarie per SQLite
 *
 * @author clagenna
 *
 */
public class SQLiteGest extends SqlGest {
  private static final Logger s_log = LogManager.getLogger(SQLiteGest.class);

  public SQLiteGest() {
    super();
  }

  @Override
  public Logger getLog() {
    return s_log;
  }

  @Override
  public String getQryListCARDS() {
    return ConstsSQL.QRY_SQLITE_LIST_CARDS;
  }

  @Override
  public String getQryListANNI() {
    return ConstsSQL.QRY_SQLITE_LIST_ANNI;
  }

  @Override
  public String getQryListMESI() {
    return ConstsSQL.QRY_SQLITE_LIST_MESI;
  }

  @Override
  public String getQryListCAUSABI() {
    return ConstsSQL.QRY_SQLITE_LIST_CAUSABI;
  }

  @Override
  public String getQryListCARDHOLD() {
    return ConstsSQL.QRY_SQLITE_LIST_CARDHOLD;
  }

  @Override
  public String getQryListVIEWS() {
    return ConstsSQL.QRY_SQLITE_LIST_VIEWS;
  }

  @Override
  public String getQryListVIEW_PATT() {
    return ConstsSQL.QRY_SQLITE_VIEW_PATT;
  }

  @Override
  public String getQryLASTROWID() {
    return ConstsSQL.QRY_SQLITE_LAST_ROWID;
  }

  @Override
  public String getQryINSMov() {
    return ConstsSQL.QRY_SQLITE_INS_Mov;
  }

  @Override
  public String getQrySELMov() {
    return ConstsSQL.QRY_SQLITE_SEL_Mov;
  }

  @Override
  public String getQryDELMov() {
    return ConstsSQL.QRY_SQLITE_DEL_Mov;
  }

  @Override
  public String getQryMODMov() {
    return ConstsSQL.QRY_SQLITE_MOD_Mov;
  }

  @Override
  public String getQryAzzeraIdCodStats() {
    return ConstsSQL.QRY_SQLITE_AZZERACODSTATS;
  }

  @Override
  public String getQryMODMovCodstat() {
    return ConstsSQL.QRY_SQLITE_MOD_Mov_CodStat;
  }

  @Override
  public String getQryINSCodstat() {
    return ConstsSQL.QRY_SQLITE_INS_CodStats;
  }

  @Override
  public String getQrySELCodstat() {
    return ConstsSQL.QRY_SQLITE_SEL_CodStats;
  }

  @Override
  public String getQryDELCodstat() {
    return ConstsSQL.QRY_SQLITE_DEL_CodStats;
  }

  @Override
  public String getQryMODCodstat() {
    return ConstsSQL.QRY_SQLITE_UPD_CodStats;
  }

  @Override
  public String getQryQtaIdCodstat() {
    return ConstsSQL.QRY_SQLITE_QTACODSTATINMOV;
  }

  @Override
  public String getQryINSCsvImpFile() {
    return ConstsSQL.QRY_SQLITE_INS_ImpFiles;
  }

  @Override
  public String getQrySELCsvImpFile() {
    return ConstsSQL.QRY_SQLITE_SEL_ImpFiles;
  }

  @Override
  public String getQryDELCsvImpFile() {
    return ConstsSQL.QRY_SQLITE_DEL_ImpFiles;
  }

  @Override
  public String getQryMODCsvImpFile() {
    return ConstsSQL.QRY_SQLITE_UPD_ImpFiles;
  }

}
