package prova.files;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.sql.SqlGest;
import sm.clagenna.stdcla.sql.Dataset;

public class EsportaTutteTable {
  private static final SimpleDateFormat s_fmtDtFile    = new SimpleDateFormat("yyyyMMdd_HHmmss");
  private static final String           EXP_abicaus    = """
      SELECT abicaus
            ,descrcaus
            ,costo
        FROM causali
      ORDER BY abicaus
            """;
  private static final String           EXP_CodiciStat = """
      SELECT idCodStat
            ,codstat
            ,descrstat
        FROM CodiciStat
      ORDER BY codstat
            """;
  private static final String           EXP_impFiles   = """
      SELECT id
            ,filename
            ,reldir
            ,size
            ,qtarecs
            ,dtmin
            ,dtmax
            ,ultagg
        FROM impFiles
      ORDER BY filename
            """;
  private static final String           EXP_Movimenti  = """
      SELECT id
            ,tipo
            ,idfile
            ,dtmov
            ,dtval
            ,dare
            ,avere
            ,descr
            ,abicaus
            ,cardid
            ,idCodStat
        FROM movimenti
      ORDER BY dtmov,dtval,dare,avere
            """;

  private static final String              EXPORT_DIR = "dati/export";
  private static final Map<String, String> arrQry;
  static {
    arrQry = Map.of( //
        "causali", EXP_abicaus, //
        "codiciStat", EXP_CodiciStat, //
        "impFiles", EXP_impFiles, //
        "movimenti", EXP_Movimenti //
    );
  }
  private DataModel model;
  private SqlGest   gestdb;

  public static void main(String[] args) {
    DataModel.setJunit(true);
    EsportaTutteTable app = new EsportaTutteTable();
    app.doTheJob();
  }

  private void doTheJob() {
    model = new DataModel();
    model.setPropsFile("Banca.properties");
    model.initApp(null);
    gestdb = (SqlGest) model.getSqlgest();
    for (String szKQry : arrQry.keySet()) {
      salvaExportCsv(szKQry, arrQry.get(szKQry));
    }
    System.out.println("Esportazione completata");
  }

  private void salvaExportCsv(String szDbNam, String szQry) {
    try (Dataset dts = new Dataset()) {
      dts.setDb(gestdb.getDbconn());
      dts.setIntToDouble(true);
      dts.setCsvBlankOnZero(true);
      dts.executeQuery(szQry);
      String szData = s_fmtDtFile.format(new Date());
      String szFile = String.format("%s/exp_%s_%s.csv", //
          EXPORT_DIR, szDbNam, szData);
      Path pthFile = Paths.get(szFile).toAbsolutePath();
      if ( !Files.exists(pthFile.getParent()))
        Files.createDirectories(pthFile.getParent());
      Files.deleteIfExists(pthFile);
      dts.savecsv(pthFile);
      // System.out.printf("Esportato su CSV: %s\n", szFile);
    } catch (Exception e) {
      System.out.printf("Errore esportazione CSV: %s\n", e.getMessage());
      e.printStackTrace();
    }

  }

}
