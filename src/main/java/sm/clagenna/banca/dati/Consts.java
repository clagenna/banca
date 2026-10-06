package sm.clagenna.banca.dati;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import sm.clagenna.banca.javafx.EColsTableView;

public class Consts {
  /**
   * costanti JavaFX
   */
  public static final String CSZ_MAIN_APP_CSS = "LoadBancaFX.css";
  public static final String CSZ_MAIN_ICON    = "sm/clagenna/banca/javafx/banca-100.png";
  public static final String CSZ_MAIN_PROPS   = "Banca.properties";

  /**
   * colonne dei movimenti
   */
  public static final String COL_MOV_ID        = "id";
  public static final String COL_MOV_Tipo      = "tipo";
  public static final String COL_MOV_Idfile    = "idfile";
  public static final String COL_MOV_DtMov     = "dtMov";
  public static final String COL_MOV_DtVal     = "dtVal";
  public static final String COL_MOV_Dare      = "dare";
  public static final String COL_MOV_Avere     = "avere";
  public static final String COL_MOV_Descr     = "descr";
  public static final String COL_MOV_Abicaus   = "abicaus";
  public static final String COL_MOV_DescrCaus = "descrcaus";
  public static final String COL_MOV_Costo     = "costo";
  public static final String COL_MOV_CardId    = "cardid";
  public static final String COL_MOV_CodStat   = "codstat";
  public static final String COL_MOV_IdCodStat = "idcodstat";

  // FIXTO questa fa lo stesso di ConvertCsv2RigaBanca analizzaBanca per AMAZON con cnvRb.readConvProperties(pthCols);
  // --> eliminato le classi Convert2CsvCol.java e ConvertCsv2RigaBanca.java
  /**
   * mappa delle colonne std con quali stringhe sono associate nelle
   * intestazioni dei vari files CSV
   */
  @Getter
  public static final Map<EColsTableView, List<String>> nomiCols;
  static {
    // mappa delle colonne std con quali stringhe sono associate nelle intestazioni dei vari files CSV
    // ATTENZIONE: Vanno messe in ordine di *LUNGHEZZA DISCENDENTE* !!
    // Vedi junit test ProvaCsvImpTutteBanche per il test di tutte le banche
    nomiCols = new HashMap<>();
    nomiCols.put(EColsTableView.tipo, //
        Arrays.asList(
            new String[] { EColsTableView.tipo.toString(), "Identificativo del trasferimento", "TransferWise ID", "tipo", "ID" }));
    nomiCols.put(EColsTableView.dtmov, //
        Arrays.asList(new String[] { EColsTableView.dtmov.toString(), "Data transazione", "Data di inizio", "Created on",
            "Order Date", "Creato il", "Ship Date", "data", "Date", }));
    nomiCols.put(EColsTableView.dtval, //
        Arrays.asList(new String[] { EColsTableView.dtval.toString(), "Data di completamento", "Data contabile", "Completato il",
            "Finished on", "valuta", "Value", "" }));
    nomiCols.put(EColsTableView.dare, //
        Arrays.asList(new String[] { EColsTableView.dare.toString(), "Importo di destinazione (dopo le commissioni)",
            "Source amount (after fees)", "Total Owed", "Addebiti", "importo", "Amount", "DEBIT", "total", }));
    nomiCols.put(EColsTableView.avere, //
        Arrays.asList(new String[] { EColsTableView.avere.toString(), "Accrediti", "refund", "CREDIT", "*no*", "*no*", }));
    nomiCols.put(EColsTableView.descr, //
        Arrays.asList(new String[] { EColsTableView.descr.toString(), "Nome del destinatario", "TRANSACTION CODE", "Product Name",
            "descrizione", "Target name", "Esercente", "Merchant", "causale", "items", "name", "type" }));
    nomiCols.put(EColsTableView.abicaus, //
        Arrays.asList(new String[] { "ABI REASON CODE", "causale abi", "categoria", "causabi", "ID" }));
    nomiCols.put(EColsTableView.cardid, //
        Arrays.asList(
            new String[] { EColsTableView.cardid.toString(), "Card Holder Full Name", "Nome d'origine", "Source name", "source" }));
  }

  /**
   * Proprieta nel Properties file
   */
  public static final String PROP_CHECK_CONV          = "check.convdb";
  public static final String PROP_LOG_LEVEL           = "logLevel";
  public static final String PROP_SPLITPOS            = "splitpos";
  public static final String PROP_COL_time            = "log_time";
  public static final String PROP_COL_leve            = "log_lev";
  public static final String PROP_COL_mesg            = "log_mesg";
  public static final String PROP_POSRESVIEW          = "resview";
  public static final String PROP_COLRESVIEW          = "resview.col";
  public static final String PROP_RESVIEWQRY          = "resview.qry";
  public static final String PROP_POSview_codstatView = "cdstt";
  public static final String PROP_POSVIEW_modcodstat  = "modcodstat";
  public static final String PROP_PROP_SCARTA         = "voci.scarta";
  public static final String PROP_EXCLUDEDCOLS        = "excludedcols";
  public static final String PROP_FLAG_FILTRI         = "FLAG_FILTRI";
  public static final String PROP_QTA_THREADS         = "QTA_THREADS";
  public static final String PROP_PERC_INDOV          = "PERC_INDOV";
  public static final String PROP_SCARTA_DESCR        = "scartaDescr";
  public static final String PROP_FILTER_FILES        = "filter_files";
  public static final String PROP_DEBUG_QRY           = "showStmtQry";
  public static final String PROP_CDST_DIM_COL1       = "cdstt.col1";
  public static final String PROP_CDST_DIM_COL2       = "cdstt.col2";
  public static final String PROP_CDST_DIM_DARE       = "cdstt.dare";
  public static final String PROP_CDST_DIM_AVERE      = "cdstt.avere";
  public static final String PROP_CDST_DIM_SALDO      = "cdstt.saldo";

  /**
   * Eventi
   */
  public static final String EVT_APP_CLOSE           = "closeApp";
  public static final String EVT_DBCHANGE            = "dbchange";
  public static final String EVT_SIZEDTS             = "sizedts";
  public static final String EVT_DTSROW              = "dtsrow";
  public static final String EVT_ENDDTSROW           = "Endrow";
  public static final String EVT_SAVEDB              = "savedb";
  public static final String EVT_SAVEDBROW           = "savedbrow";
  public static final String EVT_ENDSAVEDB           = "endsavedb";
  public static final String EVT_CHANGESKIN          = "changeskin";
  public static final String EVT_SELCODSTAT          = "selcodstat";
  public static final String EVT_CERCACODSTAT        = "cercacodstat";
  public static final String EVT_NEW_QUERY_RESULT    = "dtsresult";
  public static final String EVT_TOTCODSTAT          = "totcodstats";
  public static final String EVT_DBCODSTAT_CHANGED   = "DBCodstat";
  public static final String EVT_TREECODSTAT_CHANGED = "treeCodstat";
  public static final String EVT_FILTER_CODSTAT      = "filterCodstat";
  public static final String EVT_DATASET_CREATED     = "datasetCreated";
  public static final String EVT_PARSECSV            = "parsecsv";
  public static final String EVT_GUESSDATA_CREATED   = "guessdataCreated";
  public static final String EVT_OPTZ_FILTR_CHANGE   = "optzFiltrChange";

  /**
   * Costanti per i tipi di ABICAUS
   */

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
   * Costanti per i tipi banca
   */
  public static final String CSZ_FILE_PROPERTY_COLS = "%s_cols.properties";

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

  /**
   * Menu di Help
   */
  public static final String[][] main_shortcuts = { //
      { "ShortCuts sui Files", "" }, //
      { "Ctrl-Shft-R ", " Rescan Dirs" }, //
      { "Ctrl-Shft-M ", " Check Presenza Files" }, //
      { "Ctrl-Shft-I ", " Import Sels Files" }, //
      { "", "" }, //
      { "Ctrl-Shft-O ", " Opzioni" }, //
      { "Ctrl-Shft-S ", " Mosta Sovrapposizioni" }, //
      { "Ctrl-Shft-D ", " Mosta Dati" }, //
      { "Ctrl-Shft-T ", " Mostra Codici Stat." }, //
      { "Ctrl-Shft-C ", " Gestione Contanti" }, //
      { "Ctrl-Shft-I ", " Indovina Cod. Stat" }, //
      //
      { "ShortCuts sulla form dei risultati", "" }, //
      { "F5", "Ripeti la ricerca" }, //
      { "Ctrl+S", "Salva query" }, //
      { "Ctrl+Enter", "Esegui ricerca" }, //
      { "Num +", "Su riga di tabella dati - cerca il CodStat" }, //
      { "Esc", "Chiudi Help" }, //
      //
      { "ShortCuts sui Codici Statistici", "" }, //
      { "F5", "Ripeti la ricerca" }, //
      { "Enter", "Esegui la ricerca o conferma la selezione" }, //
      { "Shift", "Attiva la selezione multipla su piu righe consecutive" }, //
      { "Ctrl", "Attiva la selezione multipla non consecutive" }, //
      { "Dbl click", "Modifica il codice Stat. selezionato" }, //
      { "Ctrl + Dbl click", "Aggiunge un figlio al codice Stat. selezionato" }, //
      { "?", "Mostra questo aiuto" }, //
      { "Esc", "Chiudi Help" }, //
  };

}
