package prova.banca2.sql;

import lombok.Data;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.sql.ISQLGest;

@Data
public abstract class ProvaSQLBase {
  private static final String   CSZ_SQLITE_PROPS    = "dati/SQLite/Banca_SQLite.properties";
  private static final String   CSZ_SQLSERVER_PROPS = "dati/SQLserver/Banca_SQLServer.properties";
  private static final String[] arrProps            = { CSZ_SQLITE_PROPS, CSZ_SQLSERVER_PROPS };

  protected DataModel model;
  protected ISQLGest  sqlgest;

  public abstract void eseguiTest();

  public void provaTutti() {
    for (String szFilProps : arrProps) {
      provaSingolo(szFilProps);
    }
  }

  public void provaSingolo(String szFilProps) {
    DataModel.resetInst();
    try {
      System.out.printf("\n%s - props:%s\n", getClass().getSimpleName(), szFilProps);
      model = new DataModel();
      model.setPropsFile(szFilProps);
      model.initApp(null);
      sqlgest = model.getSqlgest();

      System.out.printf("Data Base(%s):%s\n", model.getDbConn().getServerId(), model.getDbConn().getDbname());
      eseguiTest();
      model.closeApp(null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
