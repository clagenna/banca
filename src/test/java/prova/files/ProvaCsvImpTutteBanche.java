package prova.files;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.utils.Utils;

public class ProvaCsvImpTutteBanche {
  private static final String           PROP_FILE   = "src/test/resources/prova/files/ProvaTutte.properties";
  private static final String           EXPORT_DIR  = "dati/export";
  private static final SimpleDateFormat s_fmtDtFile = new SimpleDateFormat("yyyyMMdd_HHmmss");
  private static final String           SEL_MOV     = """
            SELECT tipo, idFile, dtmov, dtval, dare, avere, descr, abicaus, cardid, idcodstat
      FROM Movimenti
      WHERE 1 = 1 AND
           idFile = %d
      ORDER BY tipo,
              dtmov,
              dtval,
              dare,
              avere;
            """;

  private DataModel        model;
  private List<CsvImpFile> liCsv;
  private CsvFileContainer cont;
  private SqlGest          gestdb;

  @Test
  public void testCsvImpFileTutte() {
    DataModel.setJunit(true);
    System.out.println("Locale su UTILS:" + Utils.getLocale());
    model = new DataModel();
    model.setPropsFile(PROP_FILE);
    model.initApp(null);
    model.setOverwrite(true);
    cont = new CsvFileContainer();
    liCsv = cont.loadListFiles();
    gestdb = (SqlGest) model.getSqlgest();
    // ---- modifica questo !!!! ---------------------
    //      SINGOLO FILE DI TEST
    Path pthFile = Paths.get("F:\\Google Drive\\gennari\\Banche\\wise\\estrattoconto_wise-2025-01-31_cla.csv");
    // ------- Intera famiglia -----------------------
    List<ETipoBanca> litpb = List.of( //
        //   ETipoBanca.Amazon // Ok
        //   , ETipoBanca.Wise // Ok
        //   , ETipoBanca.PayPal // Ok
        //   , ETipoBanca.Revolut //  Ok
        ETipoBanca.Bsi // Ok
        , ETipoBanca.BsiCredit // Ok
        , ETipoBanca.Carisp // +++
        , ETipoBanca.CarispCredit // +++
    //   , ETipoBanca.Smac // +++
    //   , ETipoBanca.Contanti // +++
    //   , ETipoBanca.Contanti //
    );
    // -----------------------------------------------
    if ( !litpb.isEmpty()) {
      importCsv(litpb);
    } else {
      CsvImpFile csvFile = cont.getFromPath(pthFile);
      importCsv(csvFile);
    }
  }

  /**
   * Scandisco tutti i file CSV ed importo tutti i file con tipo banca presente
   * nella lista litpb
   */
  private void importCsv(List<ETipoBanca> litpb) {
    for (ETipoBanca tpb : litpb) {
      for (CsvImpFile filecsv : liCsv) {
        if (filecsv.getTipoBanca() == tpb) {
          importCsv(tpb, filecsv);
        }
      }
    }
  }

  /**
   * Importa il singolo file file CSV
   */
  private void importCsv(CsvImpFile filecsv) {
    importCsv(filecsv.getTipoBanca(), filecsv);
  }

  private void importCsv(ETipoBanca tpb, CsvImpFile filecsv) {
    System.out.printf("Import di \"%s\" per banca: %s\n", filecsv.getFileName(), filecsv.getTipoBanca());
    CsvImportBanca impcsv = CsvImportBancaFactory.getCsvImportBanca(tpb);
    impcsv.setModel(model);
    filecsv.salvaFileSuDb(gestdb);
    impcsv.importCSV(filecsv);
    List<RigaBanca> li = impcsv.analizzaRigheCsvBanca();
    System.out.printf("---- File: %s Righe importate: ", filecsv.getFileName(), li.size());
    // li.forEach(r -> System.out.println(r.toStringShort()));
    System.out.printf("---- Salvataggio su DB: %s\n", filecsv.getFileName());
    li.forEach(r -> gestdb.writeMovimento(r));
    System.out.println("----------------------------------------");
    System.out.printf("SalvaDB: Ins=%d, Upd=%d, Scarti=%d\n", gestdb.getAdded(), gestdb.getQtaRecsUpd(), gestdb.getScarti());
    salvaExportCsv(gestdb, filecsv);
  }

  private void salvaExportCsv(SqlGest gestdb, CsvImpFile filecsv) {
    try (Dataset dts = new Dataset()) {
      dts.setDb(gestdb.getDbconn());
      dts.setIntToDouble(true);
      dts.setCsvBlankOnZero(true);
      String szQry = String.format(SEL_MOV, filecsv.getId());

      dts.executeQuery(szQry);
      String szData = s_fmtDtFile.format(new Date());
      int idFile = filecsv.getId();
      String szFile = String.format("%s/exp_%s_%03d_%s.csv", //
          EXPORT_DIR, filecsv.getTipoBanca().name(), idFile, szData);
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
