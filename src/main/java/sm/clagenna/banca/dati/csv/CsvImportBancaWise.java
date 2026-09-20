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

public class CsvImportBancaWise extends CsvImportBanca {
  private static final Logger s_log = LogManager.getLogger(CsvImportBancaWise.class);

  public CsvImportBancaWise() {
    super();
  }

  public CsvImportBancaWise(Path p_fiCsv) {
    super(p_fiCsv);
  }

  @Override
  protected void init() {
    super.init();
    setTipoBanca(ETipoBanca.Wise);
    //    setDelim(",");
    //    setBlankOnZero(true);
    //    setAbicaus("wise");
    //    setCosto(0);
    //    setCodstat("10.01");
  }

  @Override
  public Logger getLogger() {
    return CsvImportBancaWise.s_log;
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
      throw new UnsupportedOperationException("CSV dataset WISE not opened !");
    setRigheBanca(new ArrayList<RigaBanca>());
    Locale prevloc = Utils.getLocale();
    int nRow = 0;
    try {
      for (DtsRow row : dts.getRighe()) {
        firePropertyChange(Consts.EVT_DTSROW, (double) nRow++);
        studiaRigaWise(row);
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

  private void studiaRigaWise(DtsRow row) {
    LocalDateTime dtmov;
    LocalDateTime dtval;
    Double dare = null;
    Double avere = null;
    String descr = null;
    String caus = null;
    String cardid = null;

    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      getLogger().debug("Scarto riga Wise: {}", row.toString());
      return;
    }
    dtmov = ParseData.parseData(val.toString());

    val = getRowVal(EColsTableView.dtval, row);
    if (null == val) {
      dtval = dtmov;
    } else
      dtval = ParseData.parseData(val.toString());
    /* source = Claudio Gennari/ TransferWise / "" */
    String source = (String) row.get("Source name");
    if ( !Utils.isValue(source))
      source = (String) row.get("Card Holder Full Name");
    if ( !Utils.isValue(source))
      source = getCsvImpFile().getCardHold();
    if ( !Utils.isValue(source))
      source = "";
    else
      source = source.toLowerCase().replace("\"", "");
    cardid = source.length() >= 3 ? source.substring(0, 3).toLowerCase() : null;

    val = getRowVal(EColsTableView.dare, row);
    if (null == val || val.toString().length() == 0)
      dare = 0.;
    else if (val instanceof Double dbl)
      dare = dbl;
    else
      dare = Utils.parseDouble(val.toString());
    caus = Consts.ABICAUS_POS;
    String idTran = (String) row.get("ID");
    if ( !Utils.isValue(idTran))
      idTran = (String) row.get("TransferWise ID");
    if ( !Utils.isValue(idTran))
      idTran = "*";
    avere = 0.;
    if (source.toLowerCase().contains("wise") //
        || idTran.toLowerCase().contains("transf") //
        || idTran.toLowerCase().contains("cashback")) {
      // Balance cash back or Transfer
      avere = dare;
      dare = 0.;
      caus = Consts.ABICAUS_TRANSF;
      if (null != idTran && idTran.toLowerCase().contains("cashback")) {
        caus = Consts.ABICAUS_CASH;
        if ( !Utils.isValue(descr))
          descr = "cash back";
      }
    }
    if (dare < 0) {
      dare = -dare;
    } else if (source.length() == 0) {
      // solo se source è valorizzato posso invertire
      avere = dare;
      dare = 0.;
    }
    if (dare == 0. && avere == 0.) {
      getLogger().debug("Scarto riga Wise dare/avere=0: {}", row.toString());
      return;
    }

    if (! Utils.isValue(descr)) {
      val = getRowVal(EColsTableView.descr, row);
      if (null == val) {
        getLogger().debug("Scarto riga WISE, descr *null* : {}", row.toString());
        return;
      }
      descr = val.toString().replace("\"", "");
    }
    addRigaBanca(dtmov, dtval, dare, avere, descr, caus, cardid);
  }

}
