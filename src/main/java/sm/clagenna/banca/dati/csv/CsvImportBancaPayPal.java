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

public class CsvImportBancaPayPal extends CsvImportBanca {
  private static final Logger s_log = LogManager.getLogger(CsvImportBancaPayPal.class);

  public CsvImportBancaPayPal() {
    super();
  }

  public CsvImportBancaPayPal(Path p_fiCsv) {
    super(p_fiCsv);
  }

  @Override
  protected void init() {
    super.init();
    setTipoBanca(ETipoBanca.PayPal);
    //    setDelim(",");
    //    setBlankOnZero(true);
    //    setAbicaus("amazon");
    //    setCosto(0);
    //    setCodstat("10.01");
  }

  @Override
  public Logger getLogger() {
    return CsvImportBancaPayPal.s_log;
  }

  @Override
  public Dataset importCSV() {
    s_log.debug("Import CSV file {}", getCsvFile().toString());
    setTipoFile("csv");
    firePropertyChange(Consts.EVT_PARSECSV, 0.);

    try (Dataset dts = new Dataset()) {
      dts.setIntToDouble(true);
      String szExt = Utils.getFileExtention(getCsvFile());
      dts.setCsvdelim(",");
      // i decimali da PayPal per Italy hanno le 'virgole'?!?
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
    setRigheBanca(new ArrayList<RigaBanca>());
    Locale prevloc = Utils.getLocale();
    // i decimali da PayPall hanno le 'virgole'?!?
    Utils.setLocale(Locale.ITALY);
    // firePropertyChange(Consts.EVT_FUNCTYPE, 0.);
    int nRow = 0;
    try {
      for (DtsRow row : dts.getRighe()) {
        firePropertyChange(Consts.EVT_DTSROW, (double) nRow++);
        studiaRigaPayPal(row);
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

  private void studiaRigaPayPal(DtsRow row) {
    String sz = null;
    LocalDateTime dtmov = null;
    Double dare = null;
    Double avere = null;
    String descr = null;
    String caus = null;

    Object val = getRowVal(EColsTableView.dtmov, row); // row.get("Date");
    // in PayPal hanno separato la data dalla ora, quindi dobbiamo fare un merge
    Object val2 = row.get("Time");
    if (null == val || null == val2) {
      s_log.warn("Scarto riga PayPal dt==null: {}", row.toString());
      return;
    }

    if (val instanceof LocalDateTime ldt) {
      sz = String.format("%s %s", ParseData.formatDate(ldt) , val2.toString());
      dtmov = ParseData.guessData(sz);
    } else if (val instanceof String lsz) {
      sz = String.format("%s %s", lsz.substring(0, 10), val.toString());
      dtmov = ParseData.parseData(sz);
    }
    // In PaylPal 2 descrizioni: "type" e "name", quindi dobbiamo fare un merge
    // es:
    // ,"Microsoft Payments","PreApproved Payment Bill User Payment"
    // "","General Card Deposit"
    val = getRowVal(EColsTableView.descr, row);
    val = null == val ? "" : val.toString().trim();
    val2 = row.get("type");
    val2 = null == val2 ? "" : val2.toString().trim();
    descr = String.format("%s %s", val, val2).trim();
    if (descr.length() < 2) {
      s_log.debug("Scarto riga PayPal Descr==null: {}", row.toString());
      return;
    }
    // in PayPal hanno un unico campo "Amount" che può essere positivo o negativo, quindi dobbiamo fare un check
    // val = row.get("Amount");
    val = getRowVal(EColsTableView.dare, row);
    double dbl = 0.;
    if (null == val || val.toString().length() == 0)
      dbl = 0.;
    else if (val instanceof Double dou)
      dbl = dou;
    else
      dbl = Utils.parseDouble(val.toString());
    if (dbl > 0) {
      avere = dbl;
      dare = 0.;
    } else {
      avere = 0.;
      dare = -dbl;
    }
    // causale PayPal = PP
    caus = "PP";
    addRigaBanca(dtmov, dtmov, dare, avere, descr, caus);
  }

}
