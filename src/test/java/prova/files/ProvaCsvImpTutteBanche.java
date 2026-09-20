package prova.files;

import java.util.List;

import org.junit.jupiter.api.Test;

import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.ETipoBanca;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.dati.csv.CsvFileContainer;
import sm.clagenna.banca.dati.csv.CsvImpFile;
import sm.clagenna.banca.dati.csv.CsvImportBanca;
import sm.clagenna.banca.dati.csv.CsvImportBancaFactory;
import sm.clagenna.banca.sql.SqlGest;
import sm.clagenna.stdcla.utils.Utils;

public class ProvaCsvImpTutteBanche {
  private static final String PROP_FILE = "src/test/resources/prova/files/ProvaTutte.properties";
  private DataModel           model;

  @Test
  public void testCsvImpFileTutte() {
    DataModel.setJunit(true);
    System.out.println("Locale su UTILS:" + Utils.getLocale());
    model = new DataModel();
    model.setPropsFile(PROP_FILE);
    model.initApp(null);
    model.getSqlgest().setOverwrite(true);
    CsvFileContainer cont = new CsvFileContainer();
    List<CsvImpFile> liCsv = cont.loadListFiles();

    // ---- modifica questo !!!! ---------------------
    List<ETipoBanca> litpb = List.of( //
        ETipoBanca.Amazon // Ok
        , ETipoBanca.Wise // Ok
        , ETipoBanca.PayPal // Ok
        , ETipoBanca.Revolut //  +++
        , ETipoBanca.Bsi // +++
        , ETipoBanca.BsiCredit // +++
        , ETipoBanca.Carisp  // +++
        , ETipoBanca.CarispCredit  // +++
        , ETipoBanca.Smac // +++
        , ETipoBanca.Contanti // +++
    // , ETipoBanca.Contanti //
    // , ETipoBanca.CarispCredit
    );
    // -----------------------------------------------
    SqlGest gestdb = (SqlGest) model.getSqlgest();
    for (ETipoBanca tpb : litpb) {
      for (CsvImpFile csv : liCsv) {
        if (csv.getTipoBanca() == tpb) {
          System.out.printf("Import di \"%s\" per banca: %s\n", csv.getFileName(), csv.getTipoBanca());
          CsvImportBanca imp = CsvImportBancaFactory.getCsvImportBanca(tpb);
          imp.setModel(model);
          imp.setCsvImpFile(csv);
          imp.importCSV();
          List<RigaBanca> li = imp.analizzaRigheCsvBanca();
          System.out.printf("---- File: %s Righe importate: ", csv.getFileName(), li.size());
          li.forEach(r -> System.out.println(r.toStringShort()));
          li.forEach(r -> gestdb.writeMovimento(r));
          System.out.println("----------------------------------------");
          System.out.printf("SalvaDB: Ins=%d, Upd=%d, Scarti=%d\n", gestdb.getAdded(), gestdb.getQtaRecsUpd(), gestdb.getScarti());
        }
      }
    }
  }
}
