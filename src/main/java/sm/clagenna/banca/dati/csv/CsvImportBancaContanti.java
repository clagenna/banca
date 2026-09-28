package sm.clagenna.banca.dati.csv;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
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

public class CsvImportBancaContanti extends CsvImportBanca {
  private static final Logger s_log = LogManager.getLogger(CsvImportBancaContanti.class);

  public CsvImportBancaContanti() {
    super();
  }

  public CsvImportBancaContanti(Path p_File) {
    super(p_File);
  }

  @Override
  protected void init() {
    super.init();
    setTipoBanca(ETipoBanca.Contanti);
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
    getLogger().debug("Import CSV file {}", getCsvFile().toString());
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
      getLogger().debug("Readed {} recs from {}", getDblQtaRows(), getCsvFile().toString());
    } catch (Exception e) {
      getLogger().error("Errore read csv, err={}", e.getMessage(), e);
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
        studiaRigaContanti(row);
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

  private void studiaRigaContanti(DtsRow row) {
    RigaBanca rb = new RigaBanca();
    rb.setTiporec(Consts.BANCA_CONTANTI);
    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      getLogger().warn("Scarto riga contante: {}", row.toString());
      return;
    }
    if (val instanceof Double dbl) {
      Date dt = new Date();
      dt.setTime(dbl.longValue());
      System.out.println("CsvImportBanca.studiaRigaContanti():" + dt.toString());
    }
    rb.setDtmov(ParseData.parseData(val.toString()));
    rb.setDtval(rb.getDtmov());

    val = getRowVal(EColsTableView.dare, row);
    double dbl = 0.;
    if (null == val || val.toString().length() == 0)
      dbl = 0.;
    else if (val instanceof Double dou)
      dbl = dou;
    else
      dbl = Utils.parseDouble(val.toString());
    rb.setDare(dbl);

    dbl = 0.;
    val = getRowVal(EColsTableView.avere, row);
    if (null == val || val.toString().length() == 0) {
      dbl = 0.;
    } else if (val instanceof Double dou)
      dbl = dou;
    else
      dbl = Utils.parseDouble(val.toString());
    rb.setAvere(dbl);

    val = getRowVal(EColsTableView.descr, row);
    if (null == val) {
      getLogger().warn("Scarto riga contante: {}", row.toString());
      return;
    }
    rb.setDescr(val.toString());
    rb.setAbicaus("CO");
    getRigheBanca().add(rb);
  }

}
