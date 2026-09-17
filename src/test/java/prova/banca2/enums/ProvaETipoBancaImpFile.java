package prova.banca2.enums;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import sm.clagenna.banca.dati.Consts;
import sm.clagenna.banca.dati.ETipoBanca;
import sm.clagenna.banca.dati.csv.CsvImpFile;
import sm.clagenna.banca.sql.ISQLGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.utils.AppProperties;

public class ProvaETipoBancaImpFile {
  String           szDir = "F:\\Google Drive\\gennari\\Banche";
  Path             lastDir;
  PathMatcher      pthMatcher;
  List<CsvImpFile> elenco;
  AppProperties    props;
  private DBConn   dbConn;
  private ISQLGest sqlgest;

  @Before
  public void initApp() {
    openProperties();
    openDb();
    creaPathMatcher();
  }

  @Test
  public void elencoImpFile() {

    lastDir = Paths.get(szDir);
    elenco = new ArrayList<CsvImpFile>();
    try (Stream<Path> walk = Files.walk(lastDir.toAbsolutePath())) {
      elenco = walk.filter(Files::isRegularFile) //
          .filter(pth -> pthMatcher.matches(pth)) // filter by pattern
          .map(pth -> convert(lastDir, pth)) //
          .collect(Collectors.toList()); // collect all matched to a List
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (CsvImpFile imp : elenco) {
      if (null != imp.getTipoBanca())
        System.out.printf("File=\"%s\" = %s\n", imp.getFileName(), imp.getTipoBanca());
    }
    System.out.println("-------- *NONE*");
    for (CsvImpFile imp : elenco) {
      if (null == imp.getTipoBanca())
        System.out.printf("File=\"%s\" *none* \n", imp.getFileName());
    }
    System.out.println("--------");

  }

  private CsvImpFile convert(Path p_lastd, Path p_pth) {
    // System.out.printf("CsvFileContainer.convert(%s + %s)\n",p_lastd.toString(), p_pth.toString() );
    CsvImpFile imf = new CsvImpFile().assignPath(p_lastd, p_pth);
    imf.setTipoBanca(ETipoBanca.parse(p_pth));
    try {
      // questo lo setta gia la assigngPath()
      // imf.setSize((int) Files.size(p_pth));
      // 1. prima gli attributi del file
      BasicFileAttributes attrs = Files.readAttributes(p_pth, BasicFileAttributes.class);
      // 2. poi la data di creazione del file
      FileTime creationTime = attrs.creationTime();
      // 3. convertire FileTime in LocalDateTime
      LocalDateTime creationDateTime = creationTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      imf.setUltagg(creationDateTime);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imf;
  }

  private void openProperties() {
    AppProperties.setSingleton(false);
    DBConnFactory.setSingleton(false);
    try {
      if (props == null) {
        props = new AppProperties();
        props.leggiPropertyFile(new File(Consts.CSZ_MAIN_PROPS), false, false);
      }
    } catch (Exception e) {
      e.printStackTrace();

    }
  }

  private void openDb() {
    String szDbType;
    try {
      szDbType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
      // connSQL = new DBConnSQL();
      DBConnFactory conFact = new DBConnFactory();
      dbConn = conFact.get(szDbType);
      dbConn.readProperties(props);
      dbConn.doConn();
    } catch (Exception e) {
      System.out.println("ProvaETipoBancaImpFile.openDb()");
    }
    sqlgest = SqlGestFactory.get(dbConn.getServerId());
    sqlgest.setDbconn(dbConn);
  }

  private void creaPathMatcher() {
    String szPattern = "glob:**/*.csv";

    String fltrFiles = props.getProperty(Consts.PROP_FILTER_FILES);
    if (null == fltrFiles)
      fltrFiles = "payp,wise,estra";
    String arr[] = fltrFiles.split(",");
    StringBuilder fils = new StringBuilder();
    String vir = "";
    // String prefix = "estratt";
    for (String pat : arr) {
      fils.append(String.format("%s%s*", vir, pat));
      vir = ",";
    }
    szPattern = String.format("glob:*:/**/{%s}*.{csv,xls,xlsx}", fils.toString());
    pthMatcher = FileSystems.getDefault().getPathMatcher(szPattern);
  }
}
