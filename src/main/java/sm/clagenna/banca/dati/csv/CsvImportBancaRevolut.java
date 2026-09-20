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

public class CsvImportBancaRevolut extends CsvImportBanca {
  private static final Logger s_log = LogManager.getLogger(CsvImportBancaRevolut.class);

  public CsvImportBancaRevolut() {
    super();
  }

  public CsvImportBancaRevolut(Path p_fiCsv) {
    super(p_fiCsv);
  }

  @Override
  protected void init() {
    super.init();
    setTipoBanca(ETipoBanca.Revolut);
    //    setDelim(",");
    //    setBlankOnZero(true);
    //    setAbicaus("amazon");
    //    setCosto(0);
    //    setCodstat("10.01");
  }

  @Override
  public Logger getLogger() {
    return CsvImportBancaRevolut.s_log;
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
        studiaRigaRevolut(row);
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
   * Analizza il CSV della Revolut
   *
   * <pre>
   * Tipo,Prodotto,Data di inizio,Data di completamento,Descrizione,Importo,Costo,Valuta,State,Saldo
   * Pagamento,Attuale,2026-02-09 18:42:51,2026-02-09 18:42:51,Balance migration to another region or legal entity,-1.30,0.00,EUR,COMPLETATO,0.00
   * Ricarica,Attuale,2026-02-26 19:52:01,2026-02-26 19:52:01,Pagamento da GENNARI ALESSANDRO,300.00,0.00,EUR,COMPLETATO,306.30
   * Pagamento con carta,Attuale,2026-02-28 20:35:53,2026-03-01 11:30:38,Necessaire,-8.00,0.00,EUR,COMPLETATO,298.30
   * Pagamento con carta,Attuale,2026-04-01 14:58:37,2026-04-02 17:08:32,Starbucks,-3.20,0.00,EUR,COMPLETATO,295.10
   * </pre>
   *
   * @param row
   */
  private void studiaRigaRevolut(DtsRow row) {
    LocalDateTime dtmov; // (1)
    LocalDateTime dtval; // (2)
    Double dare = null; // (3)
    Double avere = null; // (4)
    String descr = null; // (5)
    String caus = null; // (6)

    Object val = getRowVal(EColsTableView.dtmov, row);
    if (null == val) {
      getLogger().debug("Scarto riga Revolut: {}", row.toString());
      return;
    }
    // --> (1)
    dtmov = ParseData.parseData(val.toString());
    // --> (2)
    val = getRowVal(EColsTableView.dtval, row);
    if (null == val) {
      dtval = dtmov;
    } else
      dtval = ParseData.parseData(val.toString());

    // --> (3)
    val = getRowVal(EColsTableView.dare, row);
    if (null == val || val.toString().length() == 0)
      dare = 0.;
    else if (val instanceof Double dbl)
      dare = dbl;
    else
      dare = Utils.parseDouble(val.toString());
    // -> (4)
    if (dare >= 0) {
      avere = dare;
      dare = 0.;
    } else {
      dare = -dare;
      avere = 0.;
    }

    // -> (5)
    val = getRowVal(EColsTableView.descr, row);
    if (null == val) {
      getLogger().debug("Scarto riga : {}", row.toString());
      return;
    }
    descr = val.toString().replace("\"", "");

    // --> (6)
    /* causale = Pagamento/Ricarica/Pagamento con Carta, etc... */
    caus = Consts.ABICAUS_PAGARE;
    String source = (String) row.get("Tipo");
    if (null != source) {
      source = source.toLowerCase().replace("\"", "");
      if (source.toLowerCase().contains("pagamento con carta"))
        caus = Consts.ABICAUS_POS;
      else if (source.toLowerCase().contains("ricarica"))
        caus = Consts.ABICAUS_TRANSF;
      else if (source.toLowerCase().contains("rimborso su carta"))
        caus = Consts.ABICAUS_STORNO;
    }
    addRigaBanca(dtmov, dtval, dare, avere, descr, caus);
  }

}
