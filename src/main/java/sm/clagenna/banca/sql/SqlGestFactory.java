package sm.clagenna.banca.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.stdcla.enums.EServerId;

public class SqlGestFactory {

  private static final Logger s_log = LogManager.getLogger(SqlGestFactory.class);

  public SqlGestFactory() {
    //
  }

  public static ISQLGest get(String p_type) {
    EServerId tip = EServerId.parse(p_type);
    if (tip == null) {
      s_log.error("Non capisco il tipo di DB: {}", p_type);
      throw new UnsupportedOperationException("Non capisco il tipo di DB:" + p_type);
    }
    ISQLGest conn = null;
    switch (tip) {
      case HSqlDB:
        break;
      case SQLite:
      case SQLite3:
        conn = new SQLiteGest();
        break;
      case SqlServer:
        conn = new SqlServerGest();
        break;
    }
    s_log.info("Connessione al DB di tipo {}", tip);
    return conn;

  }

}
