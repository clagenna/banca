package prova.files;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.opencsv.exceptions.CsvException;

import sm.clagenna.banca.dati.ConvertCsv2RigaBanca;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsCols;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.Utils;

public class ProvaConvCsv2RigaBanca {

  private List<RigaBanca> liRibanca;
  private ConvertCsv2RigaBanca          crb;

  public ProvaConvCsv2RigaBanca() {
    //
  }

  @Test
  public void provalo() {
    crb = new ConvertCsv2RigaBanca("amzn");
    crb.readConvProperties(Paths.get("src/test/resources/prova/files/amzn_data.properties"));

    Path pthFi = Paths.get("F:\\Google Drive\\gennari\\Banche\\Amazon\\estratto_amzn_2412_cla.csv");
    try (Dataset dts = new Dataset()) {
      dts.setCsvdelim(crb.getCsvDelim());
      dts.setCsvBlankOnZero(crb.isBlankOnZero());
      // lettura del file CSV
      int qta = dts.readcsv(pthFi).size();
      System.out.println("Rec letti:" + qta);
      System.out.printf("QtaCols:%d\n%s\n", dts.getColumns().size(), dts.getColumns());
      liRibanca = convertiDataSet(dts);
      liRibanca.stream().filter(s -> Utils.isValue(s.getDare())).forEach(s -> System.out.println(s.toString()));
    } catch (IOException | CsvException e) {
      e.printStackTrace();
    }

  }

  private List<RigaBanca> convertiDataSet(Dataset dts) {
    List<RigaBanca> retli = new ArrayList<>();
    @SuppressWarnings("unused")
    DtsCols cols = dts.getColumns();
    for (DtsRow riga : dts.getRighe()) {
      RigaBanca rb = new RigaBanca();
      crb.assign(rb, riga);
      retli.add(rb);
    }
    return retli;
  }

}
