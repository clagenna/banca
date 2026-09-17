package sm.clagenna.banca.dati.csv;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.javafx.MessageDialog;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGest;
import sm.clagenna.stdcla.utils.AppProperties;

public class CsvFileContainer {
  private static final Logger s_log = LogManager.getLogger(CsvFileContainer.class);

  private List<CsvImpFile>         elenco;
  private Map<String, CsvImpFile>  mapStrToPath;
  private Map<Integer, CsvImpFile> mapIndxToPath;
  private DataModel                model;
  private ISQLGest                 sqlGest;

  public CsvFileContainer() {
    model = DataModel.getInst();
  }

  public ObservableList<CsvImpFile> loadListFiles() {
    elenco = new ArrayList<CsvImpFile>();
    final Path lastDir = model.getLastDir();
    if ( !Files.exists(lastDir, LinkOption.NOFOLLOW_LINKS)) {
      String szMsg = String.format("Non esiste il direttorio fatture:<br/>&nbsp;&nbsp; %s",
          null == lastDir ? "*null*" : lastDir.toString());
      MessageDialog.messageDialog(AlertType.WARNING, szMsg);
      return null;
    }
    AppProperties props = model.getProps();

    String fltrFiles = props.getProperty(Consts.PROP_FILTER_FILES);
    if (null == fltrFiles)
      fltrFiles = "wise,estra";

    String szGlobMatch = creaGlobMatch(fltrFiles);
    // String szGlobMatch = "glob:*:/**/{estra*,wise*}*.csv";
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(szGlobMatch);
    try (Stream<Path> walk = Files.walk(lastDir.toAbsolutePath())) {
      elenco = walk.filter(Files::isRegularFile) //
          .filter(f -> matcher.matches(f)) // check end with
          .map(pth -> convert(lastDir, pth)) //
          .collect(Collectors.toList()); // collect all matched to a List
    } catch (IOException e) {
      s_log.error("Errore scan dir\"{}\" msg={}", lastDir.toString(), e.getMessage(), e);
    }
    elenco = completaFilesDaDB(elenco);
    preparaMappa();
    ObservableList<CsvImpFile> liFilesCSV = FXCollections.observableArrayList(elenco);
    return liFilesCSV;
  }

  public CsvImpFile addFile(Path pth) {
    Path lastd = model.getLastDir();
    CsvImpFile imf = new CsvImpFile(lastd, pth);
    elenco.add(imf);
    //    mapStrToPath.put(imf.relativePath().toString(), imf);
    //    mapIndxToPath.put(imf.getId(), imf);
    updateMaps(imf);
    return imf;
  }

  private void updateMaps(CsvImpFile pimp) {
    if (null == mapIndxToPath)
      mapStrToPath = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (null == mapIndxToPath)
      mapIndxToPath = new TreeMap<>();
    mapStrToPath.put(pimp.relativePath().toString(), pimp);
    if (null != pimp.getId())
      mapIndxToPath.put(pimp.getId(), pimp);
  }

  private List<CsvImpFile> completaFilesDaDB(List<CsvImpFile> p_liFiles) {
    if (null == sqlGest)
      sqlGest = model.getSqlgest();
    if (null == p_liFiles || p_liFiles.isEmpty())
      return p_liFiles;
    List<CsvImpFile> liDb = ((SqlGest) sqlGest).getListCsvImpFiles();
    for (CsvImpFile imp : p_liFiles) {
      boolean bFound = false;
      for (CsvImpFile dbf : liDb) {
        if (imp.equals(dbf)) {
          bFound = true;
          imp.setInDb(true);
          imp.setId(dbf.getId());
          imp.setSize(dbf.getSize());
          imp.setQtarecs(dbf.getQtarecs());
          imp.setDtmin(dbf.getDtmin());
          imp.setDtmax(dbf.getDtmax());
          imp.setUltagg(dbf.getUltagg());
          break;
        }
      }
      if ( !bFound) {
        // la setInFileSystem e' stata fatta nella assignPath, quindi non serve farla qui
        imp.setInDb(false);
        imp.setId(null);
        imp.setDtmin(null);
        imp.setDtmax(null);
        imp.setUltagg(null);
      }
    }
    return p_liFiles;
  }

  /**
   * Legge dal DB le informazioni relative al file di importazione e le aggiunge
   * all'oggetto CsvImpFile passato come parametro
   *
   * @param fi
   *          oggetto CsvImpFile da completare con le info lette dal DB
   * @return numero di record letti dal DB (0 o 1)
   * @deprecated usa la SqlGest.getListCsvImpFiles poi cerca il Csv giusto per
   *             questa funzione
   */
  @Deprecated
  private int addInfoFromDB(CsvImpFile fi) {
    throw new UnsupportedOperationException(
        "Metodo deprecato, usare SqlGest.getListCsvImpFiles() per leggere le info dei files di importazione dal DB");
    //    int qtaRec = 0;
    //    int k = 1;
    //    try {
    //      stmtCsvImpFileSel.setString(k++, fi.getFileName());
    //      stmtCsvImpFileSel.setString(k++, fi.getRelDir());
    //      try (ResultSet res = stmtCsvImpFileSel.executeQuery()) {
    //        if (res.isClosed()) {
    //          s_log.warn("dataset closed on SEL info ImpFiles for {}", fi.getFileName().toString());
    //          return qtaRec;
    //        }
    //        while (res.next()) {
    //          fi.setId(res.getInt(CO_id));
    //          fi.setFileName(res.getString(CO_filename));
    //          fi.setRelDir(res.getString(CO_reldir));
    //          if ( !Utils.isValue(fi.getSize()))
    //            fi.setSize(res.getInt(CO_size));
    //          if ( !Utils.isValue(fi.getQtarecs()))
    //            fi.setQtarecs(res.getInt(CO_qtarecs));
    //          if ( !Utils.isValue(fi.getDtmin()))
    //            fi.setDtmin(ParseData.parseData(res.getString(CO_dtmin)));
    //          if ( !Utils.isValue(fi.getDtmax()))
    //            fi.setDtmax(ParseData.parseData(res.getString(CO_dtmax)));
    //          if ( !Utils.isValue(fi.getUltagg()))
    //            fi.setUltagg(ParseData.parseData(res.getString(CO_ultagg)));
    //          qtaRec++;
    //        }
    //      }
    //    } catch (SQLException e) {
    //      s_log.error("Errore get info ImpFiles on {} with err={}", fi.toString(), e.getMessage(), e);
    //    }
    //    return qtaRec;
  }

  /**
   * Salva le informazioni del file di importazione nel DB, se il file esiste
   * già aggiorna le informazioni, altrimenti inserisce un nuovo record
   *
   * @param impf
   *          oggetto CsvImpFile da salvare nel DB
   */
  public void saveDb(CsvImpFile impf) {
    if (null == sqlGest) {
      // openDB();
      sqlGest = model.getSqlgest();
    }
    int qta = 0;
    try {
      CsvImpFile tmp = (CsvImpFile) impf.clone();
      qta = addInfoFromDB(tmp);
      if (qta == 0)
        sqlGest.insertCsvImpFile(tmp);
      else
        sqlGest.updateCsvImpFile(tmp);
      updateMaps(tmp);
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
  }

  //  private void insertImpFile(CsvImpFile impf) {
  //    int k = 1;
  //    try {
  //      dbconn.setStmtString(stmtCsvImpFileIns, k++, impf.getFileName());
  //      dbconn.setStmtString(stmtCsvImpFileIns, k++, impf.getRelDir());
  //      dbconn.setStmtInt(stmtCsvImpFileIns, k++, impf.getSize());
  //      dbconn.setStmtInt(stmtCsvImpFileIns, k++, impf.getQtarecs());
  //      dbconn.setStmtDate(stmtCsvImpFileIns, k++, impf.getDtmin());
  //      dbconn.setStmtDate(stmtCsvImpFileIns, k++, impf.getDtmax());
  //      dbconn.setStmtDate(stmtCsvImpFileIns, k++, impf.getUltagg());
  //
  //      stmtCsvImpFileIns.executeUpdate();
  //      int ii = dbconn.getLastIdentity();
  //      impf.setId(ii);
  //      updateMaps(impf);
  //    } catch (SQLException e) {
  //      s_log.error("Errore get info ImpFiles with err={}", e.getMessage());
  //    }
  //  }

  //  private void updateImpFile(CsvImpFile impf) {
  //    int qtaRecsUpd = 0;
  //    int k = 1;
  //    try {
  //      dbconn.setStmtString(stmtCsvImpFileUpd, k++, impf.getFileName());
  //      dbconn.setStmtString(stmtCsvImpFileUpd, k++, impf.getRelDir());
  //      dbconn.setStmtInt(stmtCsvImpFileUpd, k++, impf.getSize());
  //      dbconn.setStmtInt(stmtCsvImpFileUpd, k++, impf.getQtarecs());
  //      dbconn.setStmtDate(stmtCsvImpFileUpd, k++, impf.getDtmin());
  //      dbconn.setStmtDate(stmtCsvImpFileUpd, k++, impf.getDtmax());
  //      dbconn.setStmtDate(stmtCsvImpFileUpd, k++, impf.getUltagg());
  //      // where id = ?
  //      dbconn.setStmtInt(stmtCsvImpFileUpd, k++, impf.getId());
  //      qtaRecsUpd = stmtCsvImpFileUpd.executeUpdate();
  //      if (qtaRecsUpd != 1) {
  //        s_log.warn("Non sono riuscito ad aggiornare il file {} su DB", impf.getFileName());
  //      }
  //      updateMaps(impf);
  //    } catch (SQLException e) {
  //      s_log.error("Errore get info ImpFiles with err={}", e.getMessage());
  //    }
  //
  //  }

  private void preparaMappa() {
    mapStrToPath = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    mapIndxToPath = new TreeMap<>();
    for (CsvImpFile pp : elenco) {
      Path pthRel = pp.relativePath();
      String szRel = pthRel.toString();
      mapStrToPath.put(szRel, pp);
      if (null != pp.getId())
        mapIndxToPath.put(pp.getId(), pp);
    }
  }

  public CsvImpFile getFromPath(Path pth) {
    if (null == mapStrToPath)
      return null;
    CsvImpFile imf = new CsvImpFile(model.getLastDir(), pth);
    Path rel = imf.relativePath();
    imf = mapStrToPath.get(rel.toString());
    return imf;
  }

  public CsvImpFile getFromIndex(Integer indx) {
    if (null == mapIndxToPath)
      return null;
    CsvImpFile imf = mapIndxToPath.get(indx);
    if (null == imf)
      s_log.error("Non trovo ImpFile No={}", indx);
    return imf;
  }

  private CsvImpFile convert(Path p_lastd, Path p_pth) {
    // System.out.printf("CsvFileContainer.convert(%s + %s)\n",p_lastd.toString(), p_pth.toString() );
    CsvImpFile imf = new CsvImpFile().assignPath(p_lastd, p_pth);
    return imf;
  }

  private String creaGlobMatch(String fltr) {
    String arr[] = fltr.split(",");
    StringBuilder fils = new StringBuilder();
    String vir = "";
    // String prefix = "estratt";
    for (String pat : arr) {
      fils.append(String.format("%s%s*", vir, pat));
      vir = ",";
    }
    return String.format("glob:*:/**/{%s}*.{csv,xls,xlsx}", fils.toString());
  }

  public List<Path> getListPaths() {
    Path lastd = model.getLastDir();
    List<Path> li = elenco //
        .stream() //
        .map(p -> p.fullPath(lastd)) //
        .collect(Collectors.toList());
    return li;
  }

  public List<CsvImpFile> controllaFilesAssenti() {
    List<CsvImpFile> lipth = new ArrayList<>();
    Path basep = model.getLastDir();
    for (CsvImpFile imp : getFilesFromDB()) {
      Path pth = imp.fullPath(basep);
      if ( !Files.exists(pth, LinkOption.NOFOLLOW_LINKS))
        lipth.add(imp);
    }
    return lipth;
  }

  /**
   * Legge dal DB la lista dei files di importazione
   *
   * @return lista dei files di importazione presenti nel DB
   * @deprecated usa la SqlGest.getListCsvImpFiles per questa funzione
   */
  @Deprecated
  private List<CsvImpFile> getFilesFromDB() {
    throw new UnsupportedOperationException(
        "Metodo deprecato, usare SqlGest.getListCsvImpFiles() per leggere le info dei files di importazione dal DB");
    //    List<CsvImpFile> liDbFiles = new ArrayList<>();
    //    String szQry = ConstsSQL.QRY_IMPFILES_SEL.substring(0, ConstsSQL.QRY_IMPFILES_SEL.indexOf("WHERE"));
    //    szQry += " order by id";
    //    PreparedStatement lstmt = null;
    //
    //    try {
    //      Connection conn = sqlGest.getDbconn().getConn();
    //      lstmt = conn.prepareStatement(szQry);
    //    } catch (SQLException e) {
    //      s_log.error("Errore prep statement {} on ImpFiles with err={}", szQry, e.getMessage());
    //    }
    //    try {
    //      try (ResultSet res = lstmt.executeQuery()) {
    //        if (res.isClosed()) {
    //          s_log.warn("dataset closed on SEL info ImpFiles");
    //          return liDbFiles;
    //        }
    //        while (res.next()) {
    //          CsvImpFile csvImpf = new CsvImpFile();
    //          csvImpf.setId(res.getInt(CO_id));
    //          csvImpf.setFileName(res.getString(CO_filename));
    //          csvImpf.setRelDir(res.getString(CO_reldir));
    //          if ( !Utils.isValue(csvImpf.getSize()))
    //            csvImpf.setSize(res.getInt(CO_size));
    //          if ( !Utils.isValue(csvImpf.getQtarecs()))
    //            csvImpf.setQtarecs(res.getInt(CO_qtarecs));
    //          if ( !Utils.isValue(csvImpf.getDtmin()))
    //            csvImpf.setDtmin(ParseData.parseData(res.getString(CO_dtmin)));
    //          if ( !Utils.isValue(csvImpf.getDtmax()))
    //            csvImpf.setDtmax(ParseData.parseData(res.getString(CO_dtmax)));
    //          if ( !Utils.isValue(csvImpf.getUltagg()))
    //            csvImpf.setUltagg(ParseData.parseData(res.getString(CO_ultagg)));
    //          liDbFiles.add(csvImpf);
    //        }
    //      }
    //    } catch (SQLException e) {
    //      s_log.error("Errore get info ImpFiles with err={}", e.getMessage(), e);
    //    }
    //    return liDbFiles;
  }

  /**
   * Cancella tutti i record dei files di importazione e dei movimenti collegati
   *
   * @param li
   *          lista dei files da cancellare
   * @deprecated usare il metodo di SqlGest.deleteCsvImpFiles() per cancellare i
   *             files e i movimenti collegati
   */
  @Deprecated
  public void cancellaRegsFiles(List<CsvImpFile> li) {
    String szWhe = li.stream().map(s -> String.valueOf(s.getId())).collect(Collectors.joining(","));
    final String szQryMas = "DELETE FROM %s WHERE %s IN (%s)";
    Connection conn = sqlGest.getDbconn().getConn();
    for (String szTb : SqlGest.allTables) {
      String szId = "id";
      if (szTb.startsWith("mov"))
        szId = "idfile";
      String szQry = String.format(szQryMas, szTb, szId, szWhe);
      try (Statement stmt = conn.createStatement()) {
        stmt.executeLargeUpdate(szQry);
        s_log.warn("Delete da tabella {} con ID files {}", szTb, szWhe);
      } catch (Exception e) {
        s_log.error("Errore SQL \"{}\"", szQry, e);
      }
    }
  }

  public List<CsvImpFile> getListSiblings(CsvImpFile imf) {
    String szCardh = imf.getCardHold();
    List<CsvImpFile> liFi = null;
    if (null != szCardh) {
      liFi = elenco //
          .stream() //
          .filter(s -> s.sameCardHold(szCardh)) //
          .filter(s -> s.hasPeriodo()) //
          .filter(s -> s.getRelDir().equals(imf.getRelDir())).collect(Collectors.toList());
    } else {
      liFi = elenco //
          .stream() //
          .filter(s -> s.hasPeriodo()) //
          .filter(s -> s.getRelDir().equals(imf.getRelDir())).collect(Collectors.toList());
    }
    return liFi;
  }

}
