package sm.clagenna.banca.dati.csv;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.ETipoBanca;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class CsvImportBancaAmazon extends CsvImportBanca {
  private static final Logger  s_log = LogManager.getLogger(CsvImportBancaAmazon.class);

  public CsvImportBancaAmazon() {
    super();
  }

  public CsvImportBancaAmazon(Path p_fiCsv) {
    super(p_fiCsv);
  }

  @Override
  protected void init() {
    super.init();
    setTipoBanca(ETipoBanca.Amazon);
    //    setDelim(",");
    //    setBlankOnZero(true);
    //    setAbicaus("amazon");
    //    setCosto(0);
    //    setCodstat("10.01");
  }

  @Override
  public Logger getLogger() {
    return CsvImportBancaAmazon.s_log;
  }

  @Override
  public Dataset importCSV() {
    s_log.debug("Import CSV file {}", getCsvImpFile().getFileName().toString());
    setTipoFile("csv");
    firePropertyChange(Consts.EVT_PARSECSV, 0.);

    try (Dataset dts = new Dataset()) {
      dts.setIntToDouble(true);
      String szExt = Utils.getFileExtention(getCsvFile());
      dts.setCsvdelim(",");
      Utils.setLocale(Locale.ITALY);
      switch (szExt) {
        case ".csv":
          setDtsCsv(dts.readcsv(getCsvFile()));
          break;

        case ".xls":
        case ".xlsx":
          setTipoFile(szExt.toLowerCase().replace(".", ""));
          setDtsCsv(dts.readexcel(getCsvFile()));
      }
      setDblQtaRows(dts.size());
      firePropertyChange(Consts.EVT_SIZEDTS, getDblQtaRows());
      s_log.debug("Readed {} recs from {}", getDblQtaRows(), getCsvFile().toString());
    } catch (Exception e) {
      s_log.error("Errore read csv, err={}", e.getMessage(), e);
    }
    return getDtsCsv();
  }

  @Override
  public List<RigaBanca> analizzaRigheCsvBanca() {
    Dataset dts = getDtsCsv();
    if (null == dts || dts.getQtaCols() == 0)
      throw new UnsupportedOperationException("CSV dataset not opened !");
    // lo faccio nella funzione studiaRigaAmazon() perche'
    // le righe del CSV di Amazon non hanno tutte le stesse colonne
    // ------------------------------------------------------
    //    cnvRb = new ConvertCsv2RigaBanca(Consts.BANCA_AMAZON);
    //    // leggo la descrizione completa delle colonne nel file properties del CSV di Amazon
    //    String propCols = String.format(Consts.CSZ_FILE_PROPERTY_COLS, Consts.BANCA_AMAZON);
    //    Path pthCols = Paths.get(propCols);
    //    cnvRb.readConvProperties(pthCols);
    setRigheBanca(new ArrayList<RigaBanca>());
    Locale prevloc = Utils.getLocale();
    // firePropertyChange(Consts.EVT_FUNCTYPE, 0.);
    int nRow = 0;
    try {
      for (DtsRow row : dts.getRighe()) {
        firePropertyChange(Consts.EVT_DTSROW, (double) nRow++);
        studiaRigaAmazon(row);
      }
    } catch (Exception e) {
      s_log.error("Errore studia riga, err={}", e.getMessage(), e);
    } finally {
      Utils.setLocale(prevloc);
      firePropertyChange(Consts.EVT_ENDDTSROW, (double) dts.size());
      if ( !DataModel.isJunit())
        updateProgress(nRow, nRow);
      System.out.println("CsvImportBanca.analizzaBanca - " + Consts.EVT_ENDDTSROW);
    }
    return getRigheBanca();
  }

  /**
   * Le righe del CSV di Amazon sono convertite in oggetti RigaBanca e
   * memorizzate in una lista. Queste dovrebbero contenere le seguenti colonne:
   * <ul>
   * <li>order id</li>
   * <li>order url</li>
   * <li>items</li>
   * <li>to</li>
   * <li>date</li>
   * <li>total</li>
   * <li>shipping</li>
   * <li>shipping_refund</li>
   * <li>gift</li>
   * <li>VAT</li>
   * <li>refund</li>
   * <li>payments</li>
   * </ul>
   */
  private void studiaRigaAmazon(DtsRow row) {
    Object val = null;
    LocalDateTime dtmov = null;
    Double dare = null;
    Double avere = null;
    String descr = null;
    String caus = null;
    final String FINE_CSV = "order id";
    val = row.get(FINE_CSV);
    if (null != val && val.toString().equalsIgnoreCase(FINE_CSV)) {
      // Amazon ripete la riga di intestazione del CSV ogni 50 righe circa, quindi la scarto
      return;
    }

    val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      s_log.debug("Scarto dtmov=null riga Amazon: {}", row.toString());
      return;
    }
    if (val instanceof LocalDateTime ldt) {
      dtmov = ldt;
    } else if (val instanceof String lsz) {
      dtmov = ParseData.parseData(lsz);
    }

    val = getRowVal(EColsTableView.dare, row);
    if (null == val || val.toString().length() == 0)
      dare = 0.;
    else if (val instanceof Double dbl)
      dare = dbl;
    else
      dare = Utils.parseDouble(val.toString());

    val = getRowVal(EColsTableView.avere, row);
    if (null == val || val.toString().length() == 0)
      avere = 0.;
    else if (val instanceof Double dbl)
      avere = dbl;
    else
      avere = Utils.parseDouble(val.toString());
    if (dare == 0. && avere == 0.) {
      // in Amazon se non c' nulla nella colonna dare/avere, allora il valore potrebbe essere in "payments"
      // del tipo:  "2023-11-21: €3,99;" - provo estrarre quello
      try {
        val = row.get("payments");
        if (null != val && val.toString().length() > 0) {
          String[] arr = val.toString().split(":");
          String szVal2 = null;
          if (arr.length > 1) {
            szVal2 = arr[1].trim().replace(";", "").replace("€", "");
            Double dbl = Utils.parseDouble(szVal2);
            if (dbl > 0)
              dare = dbl;
          }
        }
      } catch (Exception e) {
        s_log.error("Errore estrazione payments, err={}", e.getMessage(), e);
      }
    }
    // se anche qui e' tutto zero allora provo a prendere il valore in "unit price"
    if (dare == 0. && avere == 0.) {
      val = row.get("unit price");
      if (null != val && val.toString().length() > 0) {
        String szVal2 = val.toString().trim().replace("€", "");
        Double dbl = Utils.parseDouble(szVal2);
        if (dbl > 0)
          dare = dbl;
      }
    }
    // se anche qui e' tutto zero allora mi arrendo !
    if (dare == 0. && avere == 0.) {
      s_log.debug("Scarto dare=avere=0 riga Amazon: {}", row.toString());
      return;
    }

    val = getRowVal(EColsTableView.descr, row);
    // se non c'è la descrizione, provo a prendere l'order url
    // questo succede su gli ordini di E-books
    if (null == val || val.toString().trim().length() == 0)
      val = row.get("order url");
    if (null == val || val.toString().trim().length() == 0) {
      s_log.debug("Scarto descr=null riga Amazon: {}", row.toString());
      return;
    }
    descr = val.toString().trim();
    caus = "AMZ";
    addRigaBanca(dtmov, dtmov, dare, avere, descr, caus);
  }
}
