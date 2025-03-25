package prova.banca;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import sm.clagenna.banca.dati.ScartaDescr;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.sys.TimerMeter;
import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class ProvaScartaDesc {
  private static final String CSZ_MAIN_PROPS = "Banca.properties";
  private static final String QRY_SEL        = """
          SELECT TOP (1000) descr
      FROM movimenti
      WHERE ( descr like '%ACCRED%' )
         OR ( descr like '%PAG.%' )
          """;

  private AppProperties props;
  private DBConn        connSQL;
  private ScartaDescr   scarta;

  @Test
  public void provalo() throws AppPropsException, SQLException {
    openProperties();
    openDb();
    openScarta();
    readDb();
  }

  private void openProperties() throws AppPropsException {
    AppProperties.setSingleton(false);
    if (props == null) {
      props = new AppProperties();
      props.leggiPropertyFile(new File(CSZ_MAIN_PROPS), false, false);
    }
  }

  private void openDb() {
    DBConnFactory.setSingleton(false);
    String szDbType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    try {
      // connSQL = new DBConnSQL();
      DBConnFactory conFact = new DBConnFactory();
      connSQL = conFact.get(szDbType);
      connSQL.readProperties(props);
      TimerMeter tm1 = new TimerMeter("Open DB");
      connSQL.doConn();
      System.out.printf("ProvaCsvImport.openDb(time=%s)\n", tm1.stop());
    } catch (Exception e) {
      System.out.printf("Errore apertura DB, error=%s\n", e.getMessage());
    }
  }

  private void openScarta() {
    scarta = new ScartaDescr();
    scarta.readProp(props);
  }

  private void readDb() throws SQLException {
    String szDbType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    ISQLGest sqlg = SqlGestFactory.get(szDbType);
    sqlg.setDbconn(connSQL);
    Connection conn = sqlg.getDbconn().getConn();
    try (PreparedStatement stmt = conn.prepareStatement(QRY_SEL); ResultSet res = stmt.executeQuery()) {
      if (null == res || res.isClosed())
        return;
      while (res.next()) {
        String descr = res.getString(1);
        convert(descr);
      }
    }
  }

  private void convert(String descr) {
    String szConv = scarta.convert(descr);
    if (Utils.isValue(szConv) && szConv.length() != descr.length()) {
      System.out.printf("%s \t\t==> %-20s\n", descr, szConv);
    } else {
      System.out.printf("\tno conv! %s", descr);
      System.out.println();
    }
  }

}
