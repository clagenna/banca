package sm.clagenna.banca.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;

public class ConvDBBanca {
  private static final Logger s_log = LogManager.getLogger(ConvDBBanca.class);

  private static final String QRY_MOV = //
      "CREATE TABLE IF NOT EXISTS movimenti (" //
          + "    id      INTEGER PRIMARY KEY ASC,"//
          + "    tipo    NVARCHAR (32),"//
          + "    idfile  INTEGER DEFAULT NULL,"//
          + "    dtmov                  DEFAULT NULL,"//
          + "    dtval                  DEFAULT NULL,"//
          + "    dare    FLOAT (19, 4)  DEFAULT NULL,"//
          + "    avere   FLOAT (19, 4)  DEFAULT NULL,"//
          + "    descr   NVARCHAR (512) DEFAULT NULL,"//
          + "    abicaus VARCHAR (20)   DEFAULT NULL,"//
          + "    cardid  NVARCHAR (20)  DEFAULT NULL,"//
          + "    codstat VARCHAR (20)   DEFAULT NULL"//
          + ");";

  private static final String QRY_NDXMOV = //
      " CREATE  INDEX IF NOT EXISTS IX_Movim_dtMov ON movimenti " //
          + "( "//
          + "  tipo ASC, " //
          + "  dtmov ASC "//
          + ");";

  private static final String QRY_NDXCODSTST = //
      "CREATE  INDEX IF NOT EXISTS IX_Movim_codstat ON movimenti " //
          + "( " //
          + "  codstat ASC" //
          + ");";

  private static final String QRY_VIEW_MOV = //
      "CREATE VIEW IF NOT EXISTS listaMovimenti AS" //
          + "    SELECT id," //
          + "           tipo," //
          + "           idfile," //
          + "           dtmov," //
          + "           dtval," //
          + "           strftime('%Y.%m', mo.dtmov) AS movstr," //
          + "           strftime('%Y.%m', mo.dtval) AS valstr," //
          + "           dare," //
          + "           avere," //
          + "           cardid," //
          + "           descr," //
          + "           mo.abicaus," //
          + "           ca.descrcaus," //
          + "           ca.costo," //
          + "     codstat" //
          + "      FROM movimenti mo" //
          + "        LEFT OUTER JOIN causali ca " //
          + "           ON mo.abicaus = ca.abicaus;";

  private static final String QRY_COPYFROM = //
      "INSERT INTO movimenti (" //
          + "                          tipo," //
          + "                          idfile," //
          + "                          dtmov," //
          + "                          dtval," //
          + "                          dare," //
          + "                          avere," //
          + "                          descr," //
          + "                          abicaus," //
          + "                          cardid," //
          + "                          codstat" //
          + "                      )" //
          + "  SELECT " //
          + "                          tipo," //
          + "                          idfile," //
          + "                          dtmov," //
          + "                          dtval," //
          + "                          dare," //
          + "                          avere," //
          + "                          descr," //
          + "                          abicaus," //
          + "                          cardid," //
          + "                          codstat" //
          + "          FROM ListaMovimentiUNION";

  private List<String>  drops;
  private DBConnFactory dbfact;
  private String        dbFile;
  private ISQLGest      m_db;

  public ConvDBBanca() {
    //
  }

  public static void main(String[] args) {
    DBConnFactory.setSingleton(false);
    ConvDBBanca app = new ConvDBBanca();
    app.checkConversione(args[0]);
  }

  public void checkConversione(String p_fileDB) {
    // dbFile = "F://winapp//Banca//Banca.db";
    dbFile = p_fileDB;
    s_log.info("Check convertibilita di {}", dbFile);
    dbfact = new DBConnFactory();
    try (DBConn dbcon = dbfact.get(EServerId.SQLite3)) {
      dbcon.setDbname(dbFile);
      dbcon.doConn();
      m_db = SqlGestFactory.get(EServerId.SQLite3);
      m_db.setDbconn(dbcon);
      Map<String, String> mapQry = m_db.getListDBViews();
      if (mapQry.containsKey("listamovimentiUNION") && !mapQry.containsKey("Listamovimenti")) {
        convDataBase(m_db);
        s_log.info("Database {} convertito nel nuovo formato", p_fileDB);
      }
    } catch (IOException e) {
      s_log.error("Errore check convertibilita di {}, err={}", p_fileDB, e.getMessage(), e);
    }

  }

  private void convDataBase(ISQLGest db2) {
    creaDrops();
    creaMovim(db2);
    copyData(db2);
    creaNdxMov(db2);
    creaNdxCds(db2);
    dropAll(db2);
    creaViewMov(db2);
  }

  private void creaDrops() {
    drops = new ArrayList<String>(Arrays.asList(new String[] { "DROP TABLE IF EXISTS movimentiBSI;", //
        "DROP TABLE IF EXISTS movimentiBSICredit;", //
        "DROP TABLE IF EXISTS movimentiCarisp;", //
        "DROP TABLE IF EXISTS movimentiCarispCredit;", //

        "DROP TABLE IF EXISTS movimentiContanti;", //
        "DROP TABLE IF EXISTS movimentiPaypal;", //
        "DROP TABLE IF EXISTS movimentiSmac;", //
        "DROP TABLE IF EXISTS movimentiWise;", //

        "DROP VIEW IF EXISTS listaMovimentiBSI;", //
        "DROP VIEW IF EXISTS listaMovimentiBSICredit;", //
        "DROP VIEW IF EXISTS listaMovimentiCarisp;", //
        "DROP VIEW IF EXISTS listaMovimentiCarispCredit;", //

        "DROP VIEW IF EXISTS listaMovimentiContanti;", //
        "DROP VIEW IF EXISTS listaMovimentiPaypal;", //
        "DROP VIEW IF EXISTS listaMovimentiSmac;", //
        "DROP VIEW IF EXISTS listaMovimentiWise;", //
        "DROP VIEW IF EXISTS listaMovimentiUNION;" //
    }));
  }

  private void creaMovim(ISQLGest db2) {
    Connection conn = db2.getDbconn().getConn();
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(QRY_MOV);
    } catch (SQLException e) {
      s_log.error("Errore crea tb Movimenti, err={}", e.getMessage(), e);
    }
  }

  private void copyData(ISQLGest db2) {
    Connection conn = db2.getDbconn().getConn();
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(QRY_COPYFROM);
    } catch (SQLException e) {
      s_log.error("Errore copia Movimenti, err={}", e.getMessage(), e);
    }
  }

  private void creaNdxMov(ISQLGest db2) {
    Connection conn = db2.getDbconn().getConn();
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(QRY_NDXMOV);
    } catch (SQLException e) {
      s_log.error("Errore crea Index Movimenti, err={}", e.getMessage(), e);
    }
  }

  private void creaNdxCds(ISQLGest db2) {
    Connection conn = db2.getDbconn().getConn();
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(QRY_NDXCODSTST);
    } catch (SQLException e) {
      s_log.error("Errore crea index CodStat Movimenti, err={}", e.getMessage(), e);
    }

  }

  private void dropAll(ISQLGest db2) {
    Connection conn = db2.getDbconn().getConn();
    try (Statement stmt = conn.createStatement()) {
      for (String qry : drops) {
        stmt.execute(qry);
      }
    } catch (SQLException e) {
      s_log.error("Errore drop del resto, err={}", e.getMessage(), e);
    }
  }

  private void creaViewMov(ISQLGest db2) {
    Connection conn = db2.getDbconn().getConn();
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(QRY_VIEW_MOV);
    } catch (SQLException e) {
      s_log.error("Errore crea View Movimenti, err={}", e.getMessage(), e);
    }

  }
}
