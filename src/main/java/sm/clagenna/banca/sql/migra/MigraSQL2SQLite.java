package sm.clagenna.banca.sql.migra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.stdcla.sql.DBConnSQL;
import sm.clagenna.stdcla.sql.DBConnSQLite;
import sm.clagenna.stdcla.sql.EServerId;
import sm.clagenna.stdcla.utils.SecPwd;

public class MigraSQL2SQLite {
  private static final Logger s_log          = LogManager.getLogger(MigraSQL2SQLite.class);
  private static final String s_sqliteDbFile = "D:\\java\\conmod\\banca2\\dati\\SQLite\\fromSQLServer.db";
  private static final String s_cry          = "MDOCQP8+EW33RyGV27cd1g==";

  private static class ColumnDef {
    String  name;
    String  typeName;
    int     dataType; // java.sql.Types
    boolean notNull;
  }

  private DBConnSQLite m_dbConnLite;
  private DBConnSQL    m_dbConnSQL;

  public static void main(String[] args) {
    s_log.info("MigraSQL2SQLite - start");
    MigraSQL2SQLite migra = new MigraSQL2SQLite();
    migra.doTheJob();
    s_log.info("MigraSQL2SQLite - end");
  }

  private void doTheJob() {
    if ( !delIfExists()) {
      s_log.error("Errore cancellazione file SQLite, impossibile proseguire!");
      return;
    }
    openSQLiteDb();
    openSQLDb();
    List<String> liTables = getListTables();
    createTablesAndIndexes(liTables);
    createListViewsInSQLite();

    try {
      m_dbConnSQL.close();
      m_dbConnLite.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean delIfExists() {
    Path pth = Paths.get(s_sqliteDbFile);
    try {
      if (Files.deleteIfExists(pth)) {
        s_log.info("File \"{}\" cancellato !", s_sqliteDbFile);
      } else {
        s_log.info("File \"{}\" non trovato, nulla da cancellare! ", s_sqliteDbFile);
      }
    } catch (IOException e) {
      s_log.error("Error deleting file: " + s_sqliteDbFile, e);
      return false;
    }
    return true;
  }

  private void openSQLiteDb() {
    m_dbConnLite = new DBConnSQLite(s_sqliteDbFile);
    DBConnSQLite.setTestExistsDB(false);
    m_dbConnLite.setServerId(EServerId.SQLite);
    m_dbConnLite.doConn();
  }

  private void openSQLDb() {
    m_dbConnSQL = new DBConnSQL();
    m_dbConnSQL.setServerId(EServerId.SqlServer);
    m_dbConnSQL.setHost("localhost");
    m_dbConnSQL.setService(1433);
    m_dbConnSQL.setDbname("Banca");
    m_dbConnSQL.setUser("sqlgianni");
    SecPwd sec = new SecPwd();
    String szPass = sec.decrypt(s_cry);
    m_dbConnSQL.setPasswd(szPass);
    m_dbConnSQL.doConn();
  }

  private List<String> getListTables() {
    List<String> liTbl = new ArrayList<>();
    try {
      DatabaseMetaData md = m_dbConnSQL.getConn().getMetaData();
      try (ResultSet rs = md.getTables(null, null, "%", new String[] { "TABLE" })) {
        while (rs.next()) {
          String tableName = rs.getString("TABLE_NAME");
          if (tableName.toLowerCase().startsWith("sys") || tableName.toLowerCase().startsWith("trace"))
            continue;
          liTbl.add(tableName);
        }
      }
    } catch (SQLException e) {
      s_log.error("Errore elenco SQLServer tabelle: {} ", e.getMessage(), e);
      System.exit(257);
    }
    return liTbl;

  }

  private void createTablesAndIndexes(List<String> liTables) {

    for (String table : liTables) {
      if (table.toLowerCase().startsWith("sys"))
        continue;
      s_log.info("Elaborazione tabella:{} ", table);
      List<ColumnDef> cols = getColumns(table);
      MigraSQL2SQLite.createTableInSqlite(m_dbConnLite.getConn(), table, cols);
      createTableIndexesInSqlite(m_dbConnLite.getConn(), table);
      copyDaSqlServer2TableData(table, cols);
    }

  }

  private List<ColumnDef> getColumns(String table) {
    List<ColumnDef> cols = new ArrayList<>();
    Connection srcConn = m_dbConnSQL.getConn();
    try {
      DatabaseMetaData md = srcConn.getMetaData();
      try (ResultSet rs = md.getColumns(null, null, table, null)) {
        while (rs.next()) {
          ColumnDef c = new ColumnDef();
          c.name = rs.getString("COLUMN_NAME");
          c.typeName = rs.getString("TYPE_NAME");
          c.dataType = rs.getInt("DATA_TYPE");
          int nullable = rs.getInt("NULLABLE");
          c.notNull = nullable == DatabaseMetaData.columnNoNulls;
          cols.add(c);
        }
      }
    } catch (SQLException e) {
      s_log.error("Errore ottenimento colonne tabella {}: {}", table, e.getMessage(), e);
      System.exit(257);
    }
    return cols;
  }

  private static void createTableInSqlite(Connection destConn, String table, List<ColumnDef> cols) {
    StringBuilder sb = new StringBuilder();
    sb.append("CREATE TABLE IF NOT EXISTS \"").append(table).append("\" (\n\t");
    boolean first = true;
    for (ColumnDef c : cols) {
      if ( !first)
        sb.append(",\n\t");
      first = false;
      sb.append('"').append(c.name).append('"').append(' ');
      sb.append(MigraSQL2SQLite.mapSqlTypeToSqlite(c.typeName, c.dataType));
      if (c.notNull)
        sb.append(" NOT NULL");
    }
    sb.append(")");
    try (Statement st = destConn.createStatement()) {
      st.execute(sb.toString());
    } catch (SQLException e) {
      s_log.error("Errore creazione tabella {} in SQLite: {}", table, e.getMessage(), e);
      System.exit(257);
    }
  }

  private static String mapSqlTypeToSqlite(String typeName, int dataType) {
    if (typeName == null)
      typeName = "";
    String t = typeName.toLowerCase();
    boolean bIdentity = t.contains("identity");
    if (bIdentity) {
      t = t.replace("identity", "").trim();
    }
    String sqliteType = "";
    switch (t) {
      case "int":
      case "bigint":
      case "smallint":
      case "tinyint":
      case "bit":
        sqliteType = "INTEGER";
        if (bIdentity) {
          sqliteType += " PRIMARY KEY AUTOINCREMENT";
        }
        break;
      case "decimal":
      case "numeric":
      case "float":
      case "real":
      case "money":
      case "smallmoney":
        sqliteType = "REAL";
        break;
      case "binary":
      case "varbinary":
      case "image":
      case "rowversion":
        sqliteType = "BLOB";
        break;
      default:
        // datetime, datetime2, date, time, char, varchar, nvarchar, uniqueidentifier, xml, etc.
        sqliteType = "TEXT";
        break;
    }
    return sqliteType;
  }

  private void copyDaSqlServer2TableData(String table, List<ColumnDef> cols) {
    String colList = String.join(",", cols.stream().map(c -> '"' + c.name + '"').toArray(String[]::new));
    String placeholders = String.join(",", cols.stream().map(_ -> "?").toArray(String[]::new));

    String selectSql = "SELECT * FROM [" + table + "]";
    String insertSql = "INSERT INTO \"" + table + "\" (" + colList + ") VALUES (" + placeholders + ")";

    Connection srcConn = m_dbConnSQL.getConn();
    Connection destConn = m_dbConnLite.getConn();
    try {
      destConn.setAutoCommit(false);
      try (Statement sel = srcConn.createStatement();
          ResultSet rs = sel.executeQuery(selectSql);
          PreparedStatement ins = destConn.prepareStatement(insertSql)) {
        Map<Integer, String> mpDt = elencatColDatetime(table);
        int count = 0;
        while (rs.next()) {
          for (int i = 0; i < cols.size(); i++) {
            int ordinal = i + 1;
            if (mpDt.containsKey(ordinal)) {
              Timestamp ts = rs.getTimestamp(ordinal);
              // ins.setTimestamp(ordinal, ts);
              m_dbConnLite.setStmtDatetime(ins, ordinal, ts);
            } else {
              Object v = rs.getObject(ordinal);
              ins.setObject(ordinal, v);
            }
          }
          ins.addBatch();
          count++;
          if (count % 500 == 0) {
            ins.executeBatch();
          }
        }
        ins.executeBatch();
        destConn.commit();
        s_log.info(" Da {}  copiati con successo {} records.", table, count);
      } catch (SQLException ex) {
        destConn.rollback();
        s_log.error("Errore durante la migrazione della tabella " + table + ": " + ex.getMessage());
        System.exit(257);
      } finally {
        destConn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      s_log.error("Errore durante la preparazione della migrazione della tabella " + table + ": " + e.getMessage());
    }
  }

  private Map<Integer, String> elencatColDatetime(String table) {
    Map<Integer, String> mapCol = new HashMap<>();
    Connection srcConn = m_dbConnSQL.getConn();
    try {
      DatabaseMetaData md = srcConn.getMetaData();
      try (ResultSet rs = md.getColumns(null, null, table, null)) {
        while (rs.next()) {
          @SuppressWarnings("unused")
          String typeName = rs.getString("TYPE_NAME");
          int dataType = rs.getInt("DATA_TYPE");
          switch (dataType) {
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
              int ordinalPosition = rs.getInt("ORDINAL_POSITION");
              String columnName = rs.getString("COLUMN_NAME");
              mapCol.put(ordinalPosition, columnName);
              break;
            default:
              break;
          }
          //
          //          if (typeName.toLowerCase().contains("date") || typeName.toLowerCase().contains("time")) {
          //            int ordinalPosition = rs.getInt("ORDINAL_POSITION");
          //            String columnName = rs.getString("COLUMN_NAME");
          //            mapCol.put(ordinalPosition, columnName);
          //          }
        }
      }
    } catch (SQLException e) {
      s_log.error("Errore ottenimento colonne tabella {}: {}", table, e.getMessage(), e);
      System.exit(257);
    }
    return mapCol;
  }

  /**
   * Si puo utilizzare anche questa query per listare gli Index in SQL Server
   *
   * <pre>
   * SELECT
              t.name AS table_name,
              ind.name AS index_name,
              ind.type_desc AS index_type,
              col.name AS column_name,
              ic.key_ordinal AS key_ordinal,
              ic.is_descending_key
          FROM sys.indexes ind
          INNER JOIN sys.index_columns ic
          ON ind.object_id = ic.object_id
          AND ind.index_id = ic.index_id
          INNER JOIN sys.columns col
          ON ic.object_id = col.object_id
          AND ic.column_id = col.column_id
          INNER JOIN sys.tables t
          ON ind.object_id = t.object_id
          WHERE 1=1
      --    AND t.name = 'movimenti'
          AND ind.is_hypothetical = 0
          AND ind.index_id > 0
          AND t.name NOT LIKE 'sys%'
      ORDER BY t.name, ind.name, ic.key_ordinal
   * </pre>
   */
  private void createTableIndexesInSqlite(Connection conn, String table) {
    StringBuilder sb = new StringBuilder();

    boolean first = true;
    String lastIndexName = "*";
    boolean bExecute = false;
    try {
      DatabaseMetaData md = m_dbConnSQL.getConn().getMetaData();
      try (ResultSet rs = md.getIndexInfo(null, null, table, false, false)) {
        while (rs.next()) {
          String indexName = rs.getString("INDEX_NAME");
          String columnName = rs.getString("COLUMN_NAME");
          if (null == indexName || null == columnName || indexName.toLowerCase().startsWith("pk_"))
            continue;
          //  verificare UXImpFiles !!
          if ( !indexName.equals(lastIndexName)) {
            if (sb.length() > 2) {
              sb.append("\n);");
              bExecute = true;
            }
            lastIndexName = indexName;
          }
          if (first)
            sb.append(String.format("CREATE INDEX IF NOT EXISTS %s ON %s (\n\t", indexName, table));
          if (bExecute) {
            try (Statement st = conn.createStatement()) {
              s_log.info("Creazione indice {} in tabella {} in SQLite", lastIndexName, table);
              st.execute(sb.toString());
              bExecute = false;
              first = true;
              sb.setLength(0);
            } catch (SQLException e) {
              s_log.error("Errore creazione indici tabella {} in SQLite: {}", table, e.getMessage(), e);
              System.exit(257);
            }
          }
          String Type = rs.getString("TYPE");
          int ordinalPosition = rs.getInt("ORDINAL_POSITION");
          int cardinality = rs.getInt("CARDINALITY");
          System.out.printf("Type=%s, ordinalPosition=%d, cardinality=%d, indexName=%s, columnName=%s\n", //
              Type, ordinalPosition, cardinality, indexName, columnName);
          if (first) {
            first = false;
          } else {
            sb.append(",\n\t");
          }
          sb.append(columnName);
        }
      }
      sb.append("\n);");

      if ( !first && sb.length() > 2) {
        try (Statement st = conn.createStatement()) {
          s_log.info("Creazione indice {} in tabella {} in SQLite", lastIndexName, table);
          st.execute(sb.toString());
        } catch (SQLException e) {
          s_log.error("Errore creazione indici tabella {} in SQLite: {}", table, e.getMessage(), e);
          System.exit(257);
        }
      }
    } catch (SQLException e) {
      s_log.error("Errore ottenimento indici tabella {}: {}", table, e.getMessage(), e);
      System.exit(257);
    }

  }

  private Map<String, String> createListViewsInSQLite() {
    Map<String, String> mapViews = new HashMap<>();
    Path pthDB = Paths.get(s_sqliteDbFile);
    Path pthView = pthDB.getParent();
    try {
      Files.list(pthView) //
          .filter(p -> p.getFileName().toString().toLowerCase().startsWith("viewsqlite_")) //
          .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".sql")) //
          .forEach(p -> createViewsInSQLite(p));
    } catch (IOException e) {
      s_log.error("Errore durante listing delle Views in {}: {} ", pthView.toString(), e.getMessage(), e);
      System.exit(257);
    }
    return mapViews;
  }

  private void createViewsInSQLite(Path pthView) {
    Connection destConn = m_dbConnLite.getConn();
    String szView = pthView.getFileName().toString();
    szView = szView.substring(11, szView.length() - 4); // tolto "viewSQLite_" e ".sql"
    try (Statement st = destConn.createStatement()) {
      destConn.setAutoCommit(false);
      String qry = null;
      try {
        qry = new String(Files.readAllBytes(pthView), StandardCharsets.UTF_8);
        qry = qry.trim();
        if ( !qry.toLowerCase().contains("create view"))
          qry = "CREATE VIEW \"" + szView + "\" AS " + qry;

      } catch (IOException e) {
        s_log.error("Errore lettura View {} ", szView, e);
        System.exit(257);
      }

      try {
        st.execute(qry);
        destConn.setAutoCommit(true);
        s_log.info("View {} creata con successo.", szView);
      } catch (SQLException e) {
        s_log.error("Errore creazione view {} in SQLite: {}", szView, e.getMessage(), e);
        System.exit(257);
      }
    } catch (SQLException e) {
      s_log.error("Errore durante la creazione delle view in SQLite: {}", e.getMessage(), e);
      System.exit(257);
    }
  }

}
