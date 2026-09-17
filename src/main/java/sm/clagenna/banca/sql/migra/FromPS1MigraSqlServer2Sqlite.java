package sm.clagenna.banca.sql.migra;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Coding da PowerShell script per migrazione da SQL Server a SQLite tradotto in
 * Java da Copilot.<br/>
 * Migra da SQL Server a SQLite.<br/>
 *
 * Uso (argomenti opzionali): java -cp <jar-with-deps>
 * migra.MigraSqlServer2Sqlite [sqlserverHost] [database] [user] [password]
 * [sqliteFile] Se user è vuoto (""), prova a usare integratedSecurity (richiede
 * driver nativo).
 */
public class FromPS1MigraSqlServer2Sqlite {

  public static void main(String[] args) throws Exception {
    String sqlServerHost = args.length > 0 && args[0] != null && !args[0].isEmpty() ? args[0] : "localhost";
    String sqlDatabase = args.length > 1 && args[1] != null && !args[1].isEmpty() ? args[1] : "Banca";
    String user = args.length > 2 ? args[2] : null;
    String password = args.length > 3 ? args[3] : null;
    String sqliteFile = args.length > 4 && args[4] != null && !args[4].isEmpty() ? args[4]
        : Paths.get(System.getProperty("user.dir"), "dati", "SQLite", "fromSQLServer.db").toString();

    System.out.println("SQL Server host: " + sqlServerHost + " DB:" + sqlDatabase);
    System.out.println("SQLite file: " + sqliteFile);

    String sqlServerUrl;
    if (user == null || user.isEmpty()) {
      // Try integrated security
      sqlServerUrl = String.format(
          "jdbc:sqlserver://%s;databaseName=%s;encrypt=false;trustServerCertificate=true;integratedSecurity=true", sqlServerHost,
          sqlDatabase);
    } else {
      sqlServerUrl = String.format("jdbc:sqlserver://%s;databaseName=%s;encrypt=false;trustServerCertificate=true", sqlServerHost,
          sqlDatabase);
    }

    // Ensure parent dir exists
    Path sqlitePath = Paths.get(sqliteFile);
    Files.createDirectories(sqlitePath.getParent());

    // Remove existing file to mirror PowerShell behaviour
    File f = sqlitePath.toFile();
    if (f.exists()) {
      System.out.println("Rimuovo DB SQLite esistente: " + sqliteFile);
      if ( !f.delete()) {
        System.err.println("Impossibile rimuovere il file SQLite: " + sqliteFile);
        return;
      }
    }

    String sqliteUrl = "jdbc:sqlite:" + sqliteFile;

    // Load drivers are usually auto-registered by JDBC, but we attempt to load explicit classes for clarity
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      System.out.println(
          "Warning: sqlite JDBC driver non trovato nel classpath (org.sqlite.JDBC). Assicurati che la dipendenza sia presente.");
    }
    try {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    } catch (ClassNotFoundException e) {
      System.out.println(
          "Warning: mssql JDBC driver non trovato nel classpath (com.microsoft.sqlserver.jdbc.SQLServerDriver). Assicurati che la dipendenza sia presente.");
    }

    try (Connection srcConn = openSqlServerConnection(sqlServerUrl, user, password);
        Connection destConn = DriverManager.getConnection(sqliteUrl)) {

      destConn.setAutoCommit(true);

      List<String> tableNames = listTables(srcConn);

      for (String table : tableNames) {
        if (table.toLowerCase().startsWith("sys"))
          continue;
        System.out.println("Elaborazione tabella: " + table);
        List<ColumnDef> cols = getColumns(srcConn, table);
        createTableInSqlite(destConn, table, cols);
        copyTableData(srcConn, destConn, table, cols);
      }

      migrateViews(srcConn, destConn, sqlitePath.getParent().toString());

      System.out.println("Migrazione completata su: " + sqliteFile);
    }
  }

  private static Connection openSqlServerConnection(String url, String user, String password) throws SQLException {
    if (user == null || user.isEmpty()) {
      // integrated security
      Properties props = new Properties();
      return DriverManager.getConnection(url, props);
    } else {
      return DriverManager.getConnection(url, user, password);
    }
  }

  private static List<String> listTables(Connection srcConn) throws SQLException {
    List<String> result = new ArrayList<>();
    DatabaseMetaData md = srcConn.getMetaData();
    try (ResultSet rs = md.getTables(null, null, "%", new String[] { "TABLE" })) {
      while (rs.next()) {
        String tableName = rs.getString("TABLE_NAME");
        result.add(tableName);
      }
    }
    return result;
  }

  private static class ColumnDef {
    String  name;
    String  typeName;
    int     dataType; // java.sql.Types
    boolean notNull;
  }

  private static List<ColumnDef> getColumns(Connection srcConn, String table) throws SQLException {
    List<ColumnDef> cols = new ArrayList<>();
    DatabaseMetaData md = srcConn.getMetaData();
    try (ResultSet rs = md.getColumns(null, null, table, null)) {
      while (rs.next()) {
        ColumnDef c = new ColumnDef();
        c.name = rs.getString("COLUMN_NAME");
        c.typeName = rs.getString("TYPE_NAME");
        c.dataType = rs.getInt("DATA_TYPE");
        int nullable = rs.getInt("NULLABLE");
        c.notNull = (nullable == DatabaseMetaData.columnNoNulls);
        cols.add(c);
      }
    }
    return cols;
  }

  private static void createTableInSqlite(Connection destConn, String table, List<ColumnDef> cols) throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append("CREATE TABLE IF NOT EXISTS \"").append(table).append("\" (");
    boolean first = true;
    for (ColumnDef c : cols) {
      if ( !first)
        sb.append(',');
      first = false;
      sb.append('"').append(c.name).append('"').append(' ');
      sb.append(mapSqlTypeToSqlite(c.typeName, c.dataType));
      if (c.notNull)
        sb.append(" NOT NULL");
    }
    sb.append(")");

    try (Statement st = destConn.createStatement()) {
      st.execute(sb.toString());
    }
  }

  private static String mapSqlTypeToSqlite(String typeName, int dataType) {
    if (typeName == null)
      typeName = "";
    String t = typeName.toLowerCase();
    switch (t) {
      case "int":
      case "bigint":
      case "smallint":
      case "tinyint":
      case "bit":
        return "INTEGER";
      case "decimal":
      case "numeric":
      case "float":
      case "real":
      case "money":
      case "smallmoney":
        return "REAL";
      case "binary":
      case "varbinary":
      case "image":
      case "rowversion":
        return "BLOB";
      default:
        // datetime, datetime2, date, time, char, varchar, nvarchar, uniqueidentifier, xml, etc.
        return "TEXT";
    }
  }

  private static void copyTableData(Connection srcConn, Connection destConn, String table, List<ColumnDef> cols)
      throws SQLException {
    String colList = String.join(",", cols.stream().map(c -> '"' + c.name + '"').toArray(String[]::new));
    String placeholders = String.join(",", cols.stream().map(_ -> "?").toArray(String[]::new));

    String selectSql = "SELECT * FROM [" + table + "]";
    String insertSql = "INSERT INTO \"" + table + "\" (" + colList + ") VALUES (" + placeholders + ")";

    destConn.setAutoCommit(false);
    try (Statement sel = srcConn.createStatement();
        ResultSet rs = sel.executeQuery(selectSql);
        PreparedStatement ins = destConn.prepareStatement(insertSql)) {

      int count = 0;
      while (rs.next()) {
        for (int i = 0; i < cols.size(); i++) {
          Object v = rs.getObject(i + 1);
          ins.setObject(i + 1, v);
        }
        ins.addBatch();
        count++;
        if (count % 500 == 0) {
          ins.executeBatch();
        }
      }
      ins.executeBatch();
      destConn.commit();
      System.out.println("-> Da " + table + " copiati con successo " + count + " record.");
    } catch (SQLException ex) {
      destConn.rollback();
      System.err.println("Errore durante la migrazione della tabella " + table + ": " + ex.getMessage());
    } finally {
      destConn.setAutoCommit(true);
    }
  }

  private static void migrateViews(Connection srcConn, Connection destConn, String sqliteDir) throws SQLException {
    String q = "SELECT TABLE_NAME AS ViewName, VIEW_DEFINITION AS ViewScript FROM INFORMATION_SCHEMA.VIEWS";
    try (Statement st = srcConn.createStatement(); ResultSet rs = st.executeQuery(q)) {
      int found = 0;
      while (rs.next()) {
        found++;
        String name = rs.getString("ViewName");
        String script = rs.getString("ViewScript");
        if (script == null)
          script = "";
        // basic cleanup: remove schema qualifiers and square brackets
        String adapted = script.replaceAll("\\[|\\]", "").replaceAll("dbo\\.", "");
        String create = "CREATE VIEW \"" + name + "\" AS " + adapted;
        try (Statement st2 = destConn.createStatement()) {
          // drop if exists
          st2.execute("DROP VIEW IF EXISTS \"" + name + "\"");
          st2.execute(create);
          System.out.println("Vista migrata: " + name);
        } catch (SQLException ex) {
          System.err.println("Impossibile creare vista " + name + " su SQLite: " + ex.getMessage());
          // salva lo script su file per revisione manuale
          saveViewToFile(sqliteDir, name, adapted);
        }
      }
      System.out.println("Trovate " + found + " viste.");
    }
  }

  private static void saveViewToFile(String dir, String name, String script) {
    try {
      String fn = Paths.get(dir, "viewSQLite_" + name + ".sql").toString();
      try (FileWriter fw = new FileWriter(fn)) {
        fw.write("CREATE VIEW " + name + " AS \n" + script + ";\n");
      }
      System.out.println("Saved problematic view to: " + fn);
    } catch (IOException e) {
      System.err.println("Errore salvataggio vista " + name + ": " + e.getMessage());
    }
  }
}
