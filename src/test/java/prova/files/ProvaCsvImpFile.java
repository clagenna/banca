package prova.files;

import java.util.List;

import org.junit.jupiter.api.Test;

import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.csv.CsvFileContainer;
import sm.clagenna.banca.dati.csv.CsvImpFile;

public class ProvaCsvImpFile {

  public ProvaCsvImpFile() {
    //
  }

  @Test
  public void testCsvImpFile() {
    DataModel model = new DataModel();
    model.initApp(null);
    CsvFileContainer cont = new CsvFileContainer();
    List<CsvImpFile> liCsv = cont.loadListFiles();
    for (CsvImpFile csv : liCsv) {
      System.out.println(csv.toString());
    }
  }
}
