package prova.stat2;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.opencsv.exceptions.CsvException;

import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.TreeCodStat;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.Utils;
import sm.clagenna.stdcla.utils.sys.ex.DatasetException;

public class ImportCodStat {

  DecimalFormat fmt = new DecimalFormat("00,00");
  DataModel model;

  public ImportCodStat() {
    //
  }

  @Test
  public void leggiCodStats() throws DatasetException {

    model = new DataModel();
    model.initApp(null);
    
    Path pthFi = Paths.get("CodStat_Estratto_2026_2026-03-25_17-49-03.csv");
    List<CodStat> li = new ArrayList<CodStat>();
    
    TreeCodStat tree = new TreeCodStat();

    try (Dataset dts = new Dataset()) {
      // dts.setCsvdelim(";");
      dts.setCsvBlankOnZero(true);
      // lettura del file CSV
      int qta = dts.readcsv(pthFi).size();
      System.out.println("Rec letti:" + qta);
      System.out.printf("QtaCols:%d\n%s\n", dts.getColumns().size(), dts.getColumns());
      System.out.println();
      // System.out.println(dts.toString());
      for (DtsRow row : dts.getRighe()) {
        CodStat cds = translate(row.get("cat1"), row.get("cat2"), row.get("cat3"));
        cds.setDescr((String) row.get("descrizione"));
        li.add(cds);
        tree.add(cds);
        System.out.printf("%-10s\t%s\n", cds.getCodice(), cds.getDescr());
      }
    } catch (IOException | CsvException e) {
      e.printStackTrace();
    }
    System.out.println("----------------------------------------\nTreeCodstat:");
    System.out.println(tree.toString());
  }

  private CodStat translate(Object ob1, Object ob2, Object ob3) {
    CodStat cds = new CodStat();
    if (Utils.isValue(ob1)) {
      cds.assign((int) ob1, 0, 0);
      return cds;
    }
    if (Utils.isValue(ob2) && (Double) ob2 != 0) {
      Double dbl = (Double) ob2;
      String cat2 = fmt.format(dbl).replaceAll("\\.", ",");
      String arr[] = cat2.split(",");
      cds.assign(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), 0);
      return cds;
    }
    String arr[] = ((String) ob3).split("\\.");
    cds.assign(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
    return cds;
  }

}
