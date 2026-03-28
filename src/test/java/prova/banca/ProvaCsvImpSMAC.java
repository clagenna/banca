package prova.banca;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import sm.clagenna.banca.dati.CsvImportBanca;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SQLiteGest;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.sys.TimerMeter;
import sm.clagenna.stdcla.utils.sys.ex.AppPropsException;

public class ProvaCsvImpSMAC {
  private CsvImportBanca csvi;
  private AppProperties  props;
  private DataModel      model;

  public ProvaCsvImpSMAC() {
    //
  }

  @Test
  public void provalo() throws AppPropsException {
    // openProperties();
    DataModel.setJUnit(true);
    model = new DataModel();
    model.initApp(props);
    model.setOverwrite(true);
    // openDb();
    Path pth = Paths.get("F:\\Google Drive\\gennari\\Banche\\SMAC\\estrattoconto_SMAC_2026-02-28_cla.xls");
    CsvImportBanca cs = importFile(pth);
    writeDb(cs);
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
    ISQLGest sqlg = model.getSqlgest();
    sqlg.setOverwrite(model.isOverwrite());
    for (RigaBanca ri : csvi.getRigheBanca()) {
      TimerMeter tm1 = new TimerMeter("Write row");
      sqlg.writeMovimento(ri);
      System.out.printf("ProvaCsvImport.writeDb(time=%s)\n", tm1.stop());
    }

    System.out.printf("Write DB %s , del:%d, add:%d, scarti:%d\n", //
        csvi.getSqlTableName(), //
        ((SQLiteGest) sqlg).getDeleted(), //
        ((SQLiteGest) sqlg).getAdded(), //
        ((SQLiteGest) sqlg).getScarti());
  }

}
