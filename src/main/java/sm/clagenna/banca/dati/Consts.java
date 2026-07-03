package sm.clagenna.banca.dati;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import sm.clagenna.banca.javafx.EColsTableView;

public class Consts {
  public static final String EVT_PARSECSV = "parsecsv";
  public static final String EVT_SIZEDTS  = "sizedts";
  // public static final String EVT_FUNCTYPE  = "functype";
  public static final String EVT_DTSROW    = "dtsrow";
  public static final String EVT_ENDDTSROW = "Endrow";
  public static final String EVT_SAVEDB    = "savedb";
  public static final String EVT_SAVEDBROW = "savedbrow";
  public static final String EVT_ENDSAVEDB = "endsavedb";

  public static final String BANCA_AMAZON        = "amzn";
  public static final String BANCA_AMAZONL       = "amazon";
  public static final String BANCA_BSI           = "bsi";
  public static final String BANCA_BSICREDIT     = "bsicredit";
  public static final String BANCA_BSICREDIT_UND = "bsi_credit";
  public static final String BANCA_CARISP        = "carisp";
  public static final String BANCA_CARCREDIT     = "carispcredit";
  public static final String BANCA_CARCREDIT_UND = "carisp_credit";
  public static final String BANCA_CONTANTI      = "contanti";
  public static final String BANCA_PAYPAL        = "paypal";
  public static final String BANCA_REVOLUT       = "revolut";
  public static final String BANCA_SMAC          = "smac";
  public static final String BANCA_WISE          = "wise";

  /** Pagamento tramite POS */
  public static final String ABICAUS_POS    = "43";
  /** Accredito con RID */
  public static final String ABICAUS_TRANSF = "Z7";
  /** Accredito interessi bancari */
  public static final String ABICAUS_CASH   = "18";
  /** Storni Vari */
  public static final String ABICAUS_STORNO = "68";
  /** Pagamento/incasso bollettino bancario */
  public static final String ABICAUS_PAGARE = "ZT";

  /**
   * mappa delle colonne std con quali stringhe sono associate nelle
   * intestazioni dei vari files CSV
   */
  @Getter
  public static final Map<EColsTableView, List<String>> nomiCols;
  static {
    nomiCols = new HashMap<>();
    nomiCols.put(EColsTableView.tipo, // 
        Arrays.asList(new String[] { EColsTableView.dtmov.toString(), "tipo", "" }));
    nomiCols.put(EColsTableView.dtmov, //
        Arrays.asList(new String[] { EColsTableView.dtmov.toString(), "Data di inizio", "Data transazione", "Created on", "data",
            "Date", "" }));
    nomiCols.put(EColsTableView.dtval, //
        Arrays.asList(new String[] { EColsTableView.dtval.toString(), "Data di completamento", "Data contabile", "Finished on",
            "valuta", "Value", "" }));
    nomiCols.put(EColsTableView.dare, //
        Arrays.asList(new String[] { EColsTableView.dare.toString(), "importo", "DEBIT", "Amount", "Source amount (after fees)" }));
    nomiCols.put(EColsTableView.avere, //
        Arrays.asList(new String[] { EColsTableView.avere.toString(), "*no*", "*no*", "CREDIT" }));
    nomiCols.put(EColsTableView.descr, // 
        Arrays.asList(new String[] { EColsTableView.descr.toString(), "causale", "descrizione", "TRANSACTION CODE", "Target name",
            "Esercente", "Merchant" }));
    nomiCols.put(EColsTableView.abicaus, //
        Arrays.asList(new String[] { "causabi", "ABI REASON CODE", "causale abi", "categoria", "ID" }));

  }
}
