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

public class CsvImportBancaBsiCredit extends CsvImportBanca {

  private static final Logger s_log = LogManager.getLogger(CsvImportBancaBsiCredit.class);

  public CsvImportBancaBsiCredit() {
    super();
  }

  public CsvImportBancaBsiCredit(Path p_fiCsv) {
    super(p_fiCsv);

  }

  @Override
  protected void init() {
    super.init();
    setTipoBanca(ETipoBanca.BsiCredit);
    //    setDelim(",");
    //    setBlankOnZero(true);
    //    setAbicaus("amazon");
    //    setCosto(0);
    //    setCodstat("10.01");
  }

  @Override
  public Logger getLogger() {
    return s_log;
  }

  @Override
  public Dataset importCSV() {
    s_log.debug("Import CSV file {}", getCsvFile().toString());
    setTipoFile("csv");
    firePropertyChange(Consts.EVT_PARSECSV, 0.);
    try (Dataset dts = new Dataset()) {
      dts.setIntToDouble(true);
      String szExt = Utils.getFileExtention(getCsvFile());

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
    if (null == getDtsCsv() || getDtsCsv().getQtaCols() == 0)
      throw new UnsupportedOperationException("CSV dataset not opened !");
    setRigheBanca(new ArrayList<RigaBanca>());
    Locale prevloc = Utils.getLocale();
    int nRow = 0;
    try {
      for (DtsRow row : getDtsCsv().getRighe()) {
        firePropertyChange(Consts.EVT_DTSROW, (double) nRow++);
        studiaRigaBSICredit(row);
      }
    } catch (Exception e) {
      s_log.error("Errore studia riga, err={}", e.getMessage(), e);
    } finally {
      Utils.setLocale(prevloc);
      firePropertyChange(Consts.EVT_ENDDTSROW, (double) getDtsCsv().size());
      if ( !DataModel.isJunit())
        updateProgress(nRow, nRow);
      // System.out.println("CsvImportBanca.analizzaBanca - " + Consts.EVT_ENDDTSROW);
    }
    return getRigheBanca();
  }
  

  /**
   * Analizza una riga <b>generica</b> del CSV su un DtsRow e la converte in un
   * oggetto {@link RigaBanca} che viene aggiunto alla lista {@link #righeBanca}
   *
   * @param row
   */
  protected void studiaRigaBSICredit(DtsRow row) {
    LocalDateTime dtmov;
    LocalDateTime dtval;
    Double dare = null;
    Double avere = null;
    String descr;
    String caus = null;
    // -----------------  DT MOV ------------------------
    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      getLogger().debug("Scarto riga (no dtmov): {}", row.toString());
      return;
    }
    dtmov = ParseData.parseData(val.toString());
    // -----------------  DT VAL ------------------------
    val = getRowVal(EColsTableView.dtval, row);
    if (null == val) {
      getLogger().debug("Scarto riga (no dtVal): {}", row.toString());
      return;
    }
    dtval = ParseData.parseData(val.toString());
    // -----------------  DARE ------------------------ 
    val = getRowVal(EColsTableView.dare, row);
    if ( !Utils.isValue(val))
      dare = 0.;
    else if (val instanceof Double dbl)
      dare = dbl;
    else
      dare = Utils.parseDouble(val.toString());
    dare = Math.abs(dare);
    // -----------------  AVERE ------------------------
    val = getRowVal(EColsTableView.avere, row);
    if ( !Utils.isValue(val)) {
      if (dare < 0) {
        avere = -dare;
        dare = 0.;
      } else
        avere = 0.;
    } else if (val instanceof Double dbl)
      avere = dbl;
    else
      avere = Utils.parseDouble(val.toString());
    avere = Math.abs(avere);
    // -----------------  DARE AVERE == ZERO ------------
    if (dare == 0 && avere == 0) {
      getLogger().debug("Scarto perche dare == 0 avere == 0, riga : {}", row.toString());
      return;
    }

    // -----------------  DESCR ------------------------
    val = getRowVal(EColsTableView.descr, row);
    if (null == val) {
      getLogger().debug("Scarto riga (no descr): {}", row.toString());
      return;
    }
    descr = val.toString();
    if (getModel().scartaVoce(descr)) {
      getLogger().debug("Scarto voce riga : {}", row.toString());
      return;
    }

    val = getRowVal(EColsTableView.abicaus, row);
    if (null != val)
      caus = val.toString();
    addRigaBanca(dtmov, dtval, dare, avere, descr, caus);
  }

}
