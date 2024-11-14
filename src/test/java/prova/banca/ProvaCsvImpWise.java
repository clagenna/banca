package prova.banca;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import sm.clagenna.banca.dati.CsvImportBanca;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SQLiteGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.sys.TimerMeter;
import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;

public class ProvaCsvImpWise {
  private static final String CSZ_MAIN_PROPS = "Banca.properties";
  private CsvImportBanca      csvi;
  private AppProperties       props;
  @SuppressWarnings("unused")
  private DataController      controller;
  private DBConn              connSQL;

  public ProvaCsvImpWise() {
    //
  }

  @Test
  public void provalo() throws AppPropsException {
    openProperties();
    controller = new DataController();
    openDb();
    Path pth = Paths.get("F:\\Google Drive\\gennari\\Banche\\wise\\estrattoconto_wise-2024-11-14_cla.csv");
    CsvImportBanca cs = importFile(pth);
    writeDb(cs);
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

  private CsvImportBanca importFile(Path pth) {
    TimerMeter tm1 = new TimerMeter("CVS import");
    csvi = new CsvImportBanca();
    csvi.importCSV(pth);
    @SuppressWarnings("unused") List<RigaBanca> li = csvi.analizzaBanca();
    System.out.printf("ProvaCsvImport.importFile(time=%s)\n", tm1.stop());
    return csvi;
  }

  private void writeDb(CsvImportBanca cs) {
    String szDbType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    ISQLGest sqlg = SqlGestFactory.get(szDbType);
    sqlg.setTableName(csvi.getSqlTableName());
    sqlg.setDbconn(connSQL);
    sqlg.setOverwrite(true);
    for (RigaBanca ri : csvi.getRigheBanca()) {
      TimerMeter tm1 = new TimerMeter("Write row");
      sqlg.write(ri);
      System.out.printf("ProvaCsvImport.writeDb(time=%s)\n", tm1.stop());
    }

    System.out.printf("Write DB %s , del:%d, add:%d, scarti:%d\n", //
        csvi.getSqlTableName(), //
        ((SQLiteGest) sqlg).getDeleted(), //
        ((SQLiteGest) sqlg).getAdded(), //
        ((SQLiteGest) sqlg).getScarti());
  }

}
