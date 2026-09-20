/**
 * Package per la gestione dei file CSV di importazione dati bancari, e la loro
 * conversione in oggetti {@link sm.clagenna.banca.dati.RigaBanca} che poi
 * verranno salvato in un database.<br/>
 * Il database di destinazione è gestito da
 * {@link sm.clagenna.banca.dati.DataModel} e la tabella di destinazione è
 * "Movimenti". Esempio:
 * <pre
 *    CsvFileContainer cont = new CsvFileContainer();
 *    List<CsvImpFile> liCsv = cont.loadListFiles();
 *    for (CsvImpFile csv : liCsv) {
 *       System.out.println(csv.getFileName());
 *   }
 * </pre>
 *
 * @author Clagenna
 */
package sm.clagenna.banca.dati.csv;
