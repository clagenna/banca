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

public class CsvImportBancaSmac extends CsvImportBanca {
  private static final Logger s_log = LogManager.getLogger(CsvImportBancaSmac.class);

  public CsvImportBancaSmac() {
    super();
  }

  public CsvImportBancaSmac(Path p_fiCsv) {
    super(p_fiCsv);
  }

  @Override
  protected void init() {
    super.init();
    setTipoBanca(ETipoBanca.Smac);
    //    setDelim(",");
    //    setBlankOnZero(true);
    //    setAbicaus("amazon");
    //    setCosto(0);
    //    setCodstat("10.01");
  }

  @Override
  public Logger getLogger() {
    return CsvImportBancaSmac.s_log;
  }

  @Override
  public Dataset importCSV() {
    s_log.debug("Import CSV file {}", getCsvFile().toString());
    setTipoFile("csv");
    firePropertyChange(Consts.EVT_PARSECSV, 0.);

    try (Dataset dts = new Dataset()) {
      dts.setIntToDouble(true);
      String szExt = Utils.getFileExtention(getCsvFile());
      // in base al tipo Banca imposto il delim di default, ma poi lo leggo dal file di properties
      dts.setCsvdelim(",");
      Utils.setLocale(Locale.US);
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
        studiaRigaSmac(row);
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

  private void studiaRigaSmac(DtsRow row) {
    LocalDateTime dtmov;
    LocalDateTime dtval;
    Double dare = null;
    Double avere = null;
    String descr = null;
    String caus = null;

    final String OPER_Borsellino = "borsellino"; // S5
    final String OPER_Fiscale = "fiscale"; // S3
    final String OPER_Ricarica = "ricarica"; // S4
    final String OPER_Spesa = "spesa"; // s2
    final String OPER_Spesa_fiscale = "spesa fiscale"; // S1

    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      getLogger().debug("Scarto riga SMAC: {}", row.toString());
      return;
    }
    dtmov = ParseData.parseData(val.toString());
    dtval = dtmov;

    val = row.get("Importo");
    dare = 0.;
    if (val instanceof Double dbl)
      dare = dbl;

    val = row.get("Sconto");
    if (null == val || val.toString().length() == 0)
      avere = 0.;
    else if (val instanceof Double dbl)
      avere = dbl;
    else
      avere = Utils.parseDouble(val.toString());

    val = row.get("Operazione");
    String op = "*";
    if (null != val)
      op = val.toString();
    switch (op.toLowerCase()) {
      case OPER_Borsellino:
        descr = "Ricarica";
        caus = "S5"; // !
        dare = 0.;
        break;
      case OPER_Fiscale:
        caus = "S3";
        break;
      case OPER_Ricarica:
        caus = "S4";
        break;
      case OPER_Spesa:
        caus = "S2";
        break;
      case OPER_Spesa_fiscale:
        caus = "S1";
        break;
      default:
        caus = "S6";
        break;
    }

    val = row.get("Esercente");
    if (null != val)
      descr = val.toString().replace("\"", "");
    if (null == descr) {
      getLogger().debug("Scarto riga SMAC: {}", row.toString());
      return;
    }
    // 25/03/2026 Aggiungo indirizzo nella descrizione (ovviare omonimi)
    val = row.get("Indirizzo");
    if (null != val)
      descr += ", " + val.toString().trim();
    addRigaBanca(dtmov, dtval, dare, avere, descr, caus);
  }

}
