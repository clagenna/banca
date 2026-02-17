package sm.clagenna.banca.dati;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class CsvFileContainer {
  private static final Logger s_log = LogManager.getLogger(CsvFileContainer.class);

  private static final String QRY_SEL = //
      "SELECT id," //
          + "       filename," //
          + "       reldir," //
          + "       size," //
          + "       qtarecs," //
          + "       dtmin," //
          + "       dtmax," //
          + "       ultagg" //
          + " FROM impFiles" //
          + " WHERE filename = ?" //
          + " AND relDir = ?";

  private static final String QRY_UPD = //
      "UPDATE impFiles SET " //
          + "       filename=?," //
          + "       reldir=?," //
          + "       size=?," //
          + "       qtarecs=?," //
          + "       dtmin=?," //
          + "       dtmax=?," //
          + "       ultagg=?" //
          + "  WHERE id = ?";

  private static final String QRY_INS = //
      "INSERT INTO impFiles (" //
          + "       filename," //
          + "       reldir," //
          + "       size," //
          + "       qtarecs," //
          + "       dtmin," //
          + "       dtmax," //
          + "       ultagg) " //
          + " VALUES ( ?, ?, ?, ?, ?, ?, ? )";

  private final int CO_id       = 1;
  private final int CO_filename = 2;
  private final int CO_reldir   = 3;
  private final int CO_size     = 4;
  private final int CO_qtarecs  = 5;
  private final int CO_dtmin    = 6;
  private final int CO_dtmax    = 7;
  private final int CO_ultagg   = 8;

  private List<ImpFile>         elenco;
  private Map<String, ImpFile>  mapStrToPath;
  private Map<Integer, ImpFile> mapIndxToPath;
  private DataModel             model;
  private DBConn                dbconn;
  private ISQLGest              m_db;
  private PreparedStatement     stmtSel;
  private PreparedStatement     stmtUpd;
  private PreparedStatement     stmtIns;

  public CsvFileContainer() {
    model = DataModel.getInst();
  }

  public ObservableList<ImpFile> loadListFiles() {
    elenco = new ArrayList<ImpFile>();
    AppProperties props = model.getProps();

    String fltrFiles = props.getProperty(DataModel.CSZ_FILTER_FILES);
    if (null == fltrFiles)
      fltrFiles = "wise,estra";

    String szGlobMatch = creaGlobMatch(fltrFiles);
    // String szGlobMatch = "glob:*:/**/{estra*,wise*}*.csv";
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(szGlobMatch);
    final Path lastDir = model.getLastDir();
    try (Stream<Path> walk = Files.walk(lastDir.toAbsolutePath())) {
      elenco = walk.filter(p -> !Files.isDirectory(p)) //
          // not a directory
          // .map(p -> p.toString().toLowerCase()) // convert path to string
          .filter(f -> matcher.matches(f)) // check end with
          .map(pth -> convert(lastDir, pth)) //
          .collect(Collectors.toList()); // collect all matched to a List
    } catch (IOException e) {
      s_log.error("Errore scan dir\"{}\" msg={}", lastDir.toString(), e.getMessage(), e);
    }
    elenco = completaFilesDaDB(elenco);
    preparaMappa();
    ObservableList<ImpFile> liFilesCSV = FXCollections.observableArrayList(elenco);
    return liFilesCSV;
  }

  public ImpFile addFile(Path pth) {
    Path lastd = model.getLastDir();
    ImpFile imf = new ImpFile(lastd, pth);
    elenco.add(imf);
    //    mapStrToPath.put(imf.relativePath().toString(), imf);
    //    mapIndxToPath.put(imf.getId(), imf);
    updateMaps(imf);
    return imf;
  }

  private void updateMaps(ImpFile pimp) {
    if (null == mapIndxToPath)
      mapStrToPath = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (null == mapIndxToPath)
      mapIndxToPath = new TreeMap<>();
    mapStrToPath.put(pimp.relativePath().toString(), pimp);
    if (null != pimp.getId())
      mapIndxToPath.put(pimp.getId(), pimp);
  }

  private List<ImpFile> completaFilesDaDB(List<ImpFile> p_li) {
    if (null == m_db)
      openDB();
    for (ImpFile fi : p_li) {
      addInfoFromDB(fi);
    }
    return p_li;
  }

  private void openDB() {
    dbconn = model.getDbConn();
    String szDbType = model.getDBType();
    m_db = SqlGestFactory.get(szDbType);
    m_db.setDbconn(dbconn);
    Connection conn = m_db.getDbconn().getConn();
    try {
      stmtSel = conn.prepareStatement(QRY_SEL);
      stmtUpd = conn.prepareStatement(QRY_UPD);
      stmtIns = conn.prepareStatement(QRY_INS);
    } catch (SQLException e) {
      s_log.error("Errore prep statement SELECT on ImpFiles with err={}", e.getMessage());
    }
  }

  private int addInfoFromDB(ImpFile fi) {
    int qtaRec = 0;
    int k = 1;
    try {
      stmtSel.setString(k++, fi.getFileName());
      stmtSel.setString(k++, fi.getRelDir());
      try (ResultSet res = stmtSel.executeQuery()) {
        if (res.isClosed()) {
          s_log.warn("dataset closed on SEL info ImpFiles for {}", fi.getFileName().toString());
          return qtaRec;
        }
        while (res.next()) {
          fi.setId(res.getInt(CO_id));
          fi.setFileName(res.getString(CO_filename));
          fi.setRelDir(res.getString(CO_reldir));
          if ( !Utils.isValue(fi.getSize()))
            fi.setSize(res.getInt(CO_size));
          if ( !Utils.isValue(fi.getQtarecs()))
            fi.setQtarecs(res.getInt(CO_qtarecs));
          if ( !Utils.isValue(fi.getDtmin()))
            fi.setDtmin(ParseData.parseData(res.getString(CO_dtmin)));
          if ( !Utils.isValue(fi.getDtmax()))
            fi.setDtmax(ParseData.parseData(res.getString(CO_dtmax)));
          if ( !Utils.isValue(fi.getUltagg()))
            fi.setUltagg(ParseData.parseData(res.getString(CO_ultagg)));
          qtaRec++;
        }
      }
    } catch (SQLException e) {
      s_log.error("Errore get info ImpFiles on {} with err={}", fi.toString(), e.getMessage(), e);
    }
    return qtaRec;
  }

  public void saveDb(ImpFile impf) {
    if (null == m_db)
      openDB();
    int qta = 0;
    try {
      ImpFile tmp = (ImpFile) impf.clone();
      qta = addInfoFromDB(tmp);
      if (qta == 0)
        insertImpFile(impf);
      else
        updateImpFile(impf);
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
  }

  private void insertImpFile(ImpFile impf) {
    int k = 1;
    try {
      dbconn.setStmtString(stmtIns, k++, impf.getFileName());
      dbconn.setStmtString(stmtIns, k++, impf.getRelDir());
      dbconn.setStmtInt(stmtIns, k++, impf.getSize());
      dbconn.setStmtInt(stmtIns, k++, impf.getQtarecs());
      dbconn.setStmtDate(stmtIns, k++, impf.getDtmin());
      dbconn.setStmtDate(stmtIns, k++, impf.getDtmax());
      dbconn.setStmtDate(stmtIns, k++, impf.getUltagg());

      stmtIns.executeUpdate();
      int ii = dbconn.getLastIdentity();
      impf.setId(ii);
      updateMaps(impf);
    } catch (SQLException e) {
      s_log.error("Errore get info ImpFiles with err={}", e.getMessage());
    }
  }

  private void updateImpFile(ImpFile impf) {
    int qtaRecsUpd = 0;
    int k = 1;
    try {
      dbconn.setStmtString(stmtUpd, k++, impf.getFileName());
      dbconn.setStmtString(stmtUpd, k++, impf.getRelDir());
      dbconn.setStmtInt(stmtUpd, k++, impf.getSize());
      dbconn.setStmtInt(stmtUpd, k++, impf.getQtarecs());
      dbconn.setStmtDate(stmtUpd, k++, impf.getDtmin());
      dbconn.setStmtDate(stmtUpd, k++, impf.getDtmax());
      dbconn.setStmtDate(stmtUpd, k++, impf.getUltagg());
      // where id = ?
      dbconn.setStmtInt(stmtUpd, k++, impf.getId());
      qtaRecsUpd = stmtUpd.executeUpdate();
      if (qtaRecsUpd != 1) {
        s_log.warn("Non sono riuscito ad aggiornare il file {} su DB", impf.getFileName());
      }
      updateMaps(impf);
    } catch (SQLException e) {
      s_log.error("Errore get info ImpFiles with err={}", e.getMessage());
    }

  }

  private void preparaMappa() {
    mapStrToPath = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    mapIndxToPath = new TreeMap<>();
    for (ImpFile pp : elenco) {
      Path pthRel = pp.relativePath();
      String szRel = pthRel.toString();
      mapStrToPath.put(szRel, pp);
      if (null != pp.getId())
        mapIndxToPath.put(pp.getId(), pp);
    }
  }

  public ImpFile getFromPath(Path pth) {
    if (null == mapStrToPath)
      return null;
    ImpFile imf = new ImpFile(model.getLastDir(), pth);
    Path rel = imf.relativePath();
    imf = mapStrToPath.get(rel.toString());
    return imf;
  }

  public ImpFile getFromIndex(Integer indx) {
    if (null == mapIndxToPath)
      return null;
    ImpFile imf = mapIndxToPath.get(indx);
    if (null == imf)
      s_log.error("Non trovo ImpFile No={}", indx);
    return imf;
  }

  private ImpFile convert(Path p_lastd, Path p_pth) {
    // System.out.printf("CsvFileContainer.convert(%s + %s)\n",p_lastd.toString(), p_pth.toString() );
    ImpFile imf = new ImpFile().assignPath(p_lastd, p_pth);
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

  public List<ImpFile> controllaFilesAssenti() {
    List<ImpFile> lipth = new ArrayList<>();
    Path basep = model.getLastDir();
    for (ImpFile imp : getFilesFromDB()) {
      Path pth = imp.fullPath(basep);
      if ( !Files.exists(pth, LinkOption.NOFOLLOW_LINKS))
        lipth.add(imp);
    }
    return lipth;
  }

  private List<ImpFile> getFilesFromDB() {
    List<ImpFile> liDbFiles = new ArrayList<ImpFile>();
    String szQry = QRY_SEL.substring(0, QRY_SEL.indexOf("WHERE"));
    szQry += " order by id";
    PreparedStatement lstmt = null;

    try {
      Connection conn = m_db.getDbconn().getConn();
      lstmt = conn.prepareStatement(szQry);
    } catch (SQLException e) {
      s_log.error("Errore prep statement {} on ImpFiles with err={}", szQry, e.getMessage());
    }
    try {
      try (ResultSet res = lstmt.executeQuery()) {
        if (res.isClosed()) {
          s_log.warn("dataset closed on SEL info ImpFiles");
          return liDbFiles;
        }
        while (res.next()) {
          ImpFile fi = new ImpFile();
          fi.setId(res.getInt(CO_id));
          fi.setFileName(res.getString(CO_filename));
          fi.setRelDir(res.getString(CO_reldir));
          if ( !Utils.isValue(fi.getSize()))
            fi.setSize(res.getInt(CO_size));
          if ( !Utils.isValue(fi.getQtarecs()))
            fi.setQtarecs(res.getInt(CO_qtarecs));
          if ( !Utils.isValue(fi.getDtmin()))
            fi.setDtmin(ParseData.parseData(res.getString(CO_dtmin)));
          if ( !Utils.isValue(fi.getDtmax()))
            fi.setDtmax(ParseData.parseData(res.getString(CO_dtmax)));
          if ( !Utils.isValue(fi.getUltagg()))
            fi.setUltagg(ParseData.parseData(res.getString(CO_ultagg)));
          liDbFiles.add(fi);
        }
      }
    } catch (SQLException e) {
      s_log.error("Errore get info ImpFiles with err={}", e.getMessage(), e);
    }
    return liDbFiles;
  }

  public void cancellaRegsFiles(List<ImpFile> li) {
    String szWhe = li.stream().map(s -> String.valueOf(s.getId())).collect(Collectors.joining(","));
    final String szQryMas = "DELETE FROM %s WHERE %s IN (%s)";
    Connection conn = m_db.getDbconn().getConn();
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

  public List<ImpFile> getListSiblings(ImpFile imf) {
    String szCardh = imf.getCardHold();
    List<ImpFile> liFi = null;
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
