package sm.clagenna.banca.sql;

public class ConstsSQL {

  // ------------------------------------------------------------------------------
  // --------------------------------  SQLITE  ---------------------------------
  public static final String QRY_SQLITE_LIST_CARDS    = "SELECT DISTINCT tipo FROM ListaMovimenti";
  public static final String QRY_SQLITE_LIST_ANNI     = "SELECT DISTINCT strftime('%Y', dtmov) as anno FROM ListaMovimenti";
  public static final String QRY_SQLITE_LIST_MESI     = "SELECT DISTINCT movstr FROM ListaMovimenti ORDER BY movstr";
  public static final String QRY_SQLITE_LIST_CAUSABI  = "SELECT abicaus, descrcaus || ' (' || abicaus || ')' as descr FROM causali ORDER BY descr";
  public static final String QRY_SQLITE_LIST_CARDHOLD = "SELECT DISTINCT cardid FROM ListaMovimenti WHERE cardid IS NOT NULL ORDER BY cardid";
  public static final String QRY_SQLITE_LIST_VIEWS    = "SELECT name FROM sqlite_master WHERE type = 'view'";
  public static final String QRY_SQLITE_VIEW_PATT     = "SELECT %s from %s WHERE 1=1 ORDER BY dtMov,dtval,dare,avere;";
  public static final String QRY_SQLITE_LAST_ROWID    = "SELECT last_insert_rowid()";

  public static final String QRY_SQLITE_INS_Mov = """
        INSERT INTO movimenti
            (tipo
            ,idfile
            ,dtmov
            ,dtval
            ,dare
            ,avere
            ,descr
            ,abicaus
            ,cardid
            ,idcodstat)
      VALUES (?,?,?,?,?,?,?,?,?,?)""";

  public static final String QRY_SQLITE_SEL_Mov = """
      SELECT COUNT(*)
        FROM movimenti
       WHERE 1=1""";

  public static final String QRY_SQLITE_DEL_Mov = """
      DELETE FROM movimenti
           WHERE 1=1""";

  public static final String QRY_SQLITE_MOD_Mov = """
      UPDATE movimenti
         SET tipo=?
            ,idfile=?
            ,dtmov=?
            ,dtval=?
            ,dare=?
            ,avere=?
            ,descr=?
            ,abicaus=?
            ,cardid=?
            ,idcodstat=?
      WHERE 1=1";""";

  public static final String QRY_SQLITE_MOD_Mov_CodStat = """
      UPDATE movimenti
         SET idcodstat=?
       WHERE id=?""";
  public static final String QRY_SQLITE_QTACODSTATINMOV = """
      SELECT COUNT(*) as qtaMovInCodStat
        FROM movimenti
       WHERE idcodstat IS NOT NULL""";

  public static final String QRY_SQLITE_AZZERACODSTATS = """
      UPDATE movimenti SET idCodStat=NULL""";

  public static final String QRY_SQLITE_INS_CodStats = """
      INSERT INTO CodiciStat
          (codstat
          ,descrstat)
      VALUES (?, ? )""";

  public static final String QRY_SQLITE_SEL_CodStats = """
          SELECT idCodStat
          ,codstat
          ,descrstat
      FROM CodiciStat
      WHERE 1=1
      ORDER BY codstat""";

  public static final String QRY_SQLITE_DEL_CodStats = """
      DELETE FROM CodiciStat
      WHERE idCodStat = ?""";

  public static final String QRY_SQLITE_UPD_CodStats = """
          UPDATE CodiciStat SET
           codstat=?
          ,descrstat=?
      WHERE idCodStat=?""";

  // ------------------------------------------------------------------------------
  // --------------------------------  SQLSERVER  ---------------------------------

  public static final String QRY_SQLSERVER_LIST_CARDS    = "SELECT DISTINCT tipo FROM dbo.movimenti";
  public static final String QRY_SQLSERVER_LIST_ANNI     = "SELECT DISTINCT YEAR(dtmov) as anno FROM movimenti ORDER BY 1";
  public static final String QRY_SQLSERVER_LIST_MESI     = "SELECT DISTINCT movstr FROM dbo.ListaMovimenti ORDER BY movstr";
  public static final String QRY_SQLSERVER_LIST_CAUSABI  = "SELECT abicaus, concat(descrcaus,' (',abicaus ,')') as descr FROM causali ORDER BY descr";
  public static final String QRY_SQLSERVER_LIST_CARDHOLD = "SELECT DISTINCT cardid FROM Movimenti WHERE cardid IS NOT NULL AND LEN(RTRIM(cardid)) > 0  ORDER BY cardid";
  public static final String QRY_SQLSERVER_LIST_VIEWS    = "SELECT name FROM sys.views ORDER BY name";
  public static final String QRY_SQLSERVER_VIEW_PATT     = "SELECT %s from %s WHERE 1=1 ORDER BY dtMov,dtval,dare,avere";
  public static final String QRY_SQLSERVER_LAST_ROWID    = "Select @@IDENTITY  as LastId";

  public static final String QRY_SQLSERVER_INS_Mov = """
        INSERT INTO movimenti
            (tipo
            ,idfile
            ,dtmov
            ,dtval
            ,dare
            ,avere
            ,descr
            ,abicaus
            ,cardid
            ,idcodstat)
      VALUES (?,?,?,?,?,?,?,?,?,?)""";

  public static final String QRY_SQLSERVER_SEL_Mov = """
      SELECT COUNT(*)
        FROM movimenti
       WHERE 1=1""";

  public static final String QRY_SQLSERVER_DEL_Mov = """
      DELETE FROM movimenti
           WHERE 1=1""";

  public static final String QRY_SQLSERVER_MOD_Mov = """
      UPDATE movimenti
         SET tipo=?
            ,idfile=?
            ,dtmov=?
            ,dtval=?
            ,dare=?
            ,avere=?
            ,descr=?
            ,abicaus=?
            ,cardid=?
            ,idcodstat=?
      WHERE 1=1";""";

  public static final String QRY_SQLSERVER_QTACODSTATINMOV = """
      SELECT COUNT(*) as qtaMovInCodStat
        FROM movimenti
       WHERE idcodstat IS NOT NULL""";

  public static final String QRY_SQLSERVER_AZZERACODSTATS = """
      UPDATE movimenti SET idCodStat=NULL""";

  public static final String QRY_SQLSERVER_MOD_Mov_CodStat = """
      UPDATE movimenti
         SET idcodstat=?
       WHERE id=?""";

  public static final String QRY_SQLSERVER_INS_CodStats = """
      INSERT INTO CodiciStat
          (codstat
          ,descrstat)
      VALUES (?, ? )""";

  public static final String QRY_SQLSERVER_SEL_CodStats = """
          SELECT idCodStat
          ,codstat
          ,descrstat
      FROM CodiciStat
      WHERE 1=1
      ORDER BY codstat""";

  public static final String QRY_SQLSERVER_DEL_CodStats = """
      DELETE FROM CodiciStat
      WHERE idCodStat = ?""";

  public static final String QRY_SQLSERVER_UPD_CodStats = """
          UPDATE CodiciStat SET
           codstat=?
          ,descrstat=?
      WHERE idCodStat=?""";

  // ------------------------------------------------------------------------------
  // -------------------------------  ImpFile  ---------------------------------
  public static final String QRY_SQLSERVER_SEL_ImpFiles = """
      SELECT id,
           filename,
           reldir,
           size,
           qtarecs,
           dtmin,
           dtmax,
           ultagg
      FROM impFiles
      WHERE filename = ?
      AND relDir = ?""";
  public static final String QRY_SQLSERVER_UPD_ImpFiles = """
      UPDATE impFiles SET
             filename=?,
             reldir=?,
             size=?,
             qtarecs=?,
             dtmin=?,
             dtmax=?,
             ultagg=?
      WHERE id = ?""";
  public static final String QRY_SQLSERVER_INS_ImpFiles = """
      INSERT INTO impFiles (
           filename,
           reldir,
           size,
           qtarecs,
           dtmin,
           dtmax,
           ultagg)
      VALUES ( ?, ?, ?, ?, ?, ?, ? )""";
  public static final String QRY_SQLSERVER_DEL_ImpFiles = """
      DELETE FROM impFiles
      WHERE id = ?""";
  public static final String QRY_SQLITE_SEL_ImpFiles    = """
      SELECT id,
           filename,
           reldir,
           size,
           qtarecs,
           dtmin,
           dtmax,
           ultagg
      FROM impFiles
      WHERE filename = ?
      AND relDir = ?""";
  public static final String QRY_SQLITE_UPD_ImpFiles = """
      UPDATE impFiles SET
             filename=?,
             reldir=?,
             size=?,
             qtarecs=?,
             dtmin=?,
             dtmax=?,
             ultagg=?
      WHERE id = ?""";
  public static final String QRY_SQLITE_INS_ImpFiles = """
       INSERT INTO impFiles (
            filename,
            reldir,
            size,
            qtarecs,
            dtmin,
            dtmax,
            ultagg)
      VALUES ( ?, ?, ?, ?, ?, ?, ? )""";
  public static final String QRY_SQLITE_DEL_ImpFiles = """
       DELETE FROM impFiles 
       WHERE id = ?""";


  // Colonne della tabella impFiles
  public static final int CsvImpFile_ColNo_id       = 1;
  public static final int CsvImpFile_ColNo_filename = 2;
  public static final int CsvImpFile_ColNo_reldir   = 3;
  public static final int CsvImpFile_ColNo_size     = 4;
  public static final int CsvImpFile_ColNo_qtarecs  = 5;
  public static final int CsvImpFile_ColNo_dtmin    = 6;
  public static final int CsvImpFile_ColNo_dtmax    = 7;
  public static final int CsvImpFile_ColNo_ultagg   = 8;

}
