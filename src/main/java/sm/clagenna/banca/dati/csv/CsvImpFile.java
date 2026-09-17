package sm.clagenna.banca.dati.csv;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.ETipoBanca;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

/**
 * Classe che rappresenta un file di importazione CSV o anche Excel (xls,xlsx)
 * di una banca. Contiene le informazioni principali del file e anche le
 * informazioni relative ai record contenuti nel file stesso.<br/>
 * Viene accumulato nella classe {@link CsvFileContainer}.
 *
 * @author clagenna
 *
 */
public class CsvImpFile implements Cloneable {
  private static final Logger  s_log      = LogManager.getLogger(CsvImpFile.class);
  private static final Pattern s_cardHold = Pattern.compile(".*_([a-z]+)\\.[a-z]+", Pattern.CASE_INSENSITIVE);

  /** id del file nel DB */
  @Getter
  private Integer       id;
  /** tipo di banca a cui appartiene il file */
  @Getter
  private ETipoBanca    tipoBanca;
  /** nome del file semplice */
  @Getter
  private String        fileName;
  /** directory relativa al file rispetto alla radice */
  @Getter
  private String        relDir;
  /**
   * eventuale cardholder (eug,cla,and,ale...) a cui appartiene il file
   * (estratto dal nome del file)
   */
  @Getter
  private String        cardHold;
  /** dimensione del file in byte */
  @Getter
  private int           size;
  /** numero di record contenuti nel file (da calcolare leggendo il file) */
  @Getter
  private int           qtarecs;
  /**
   * data transazione minima dei record contenuti nel file (da calcolare
   * leggendo il file)
   */
  @Getter
  private LocalDateTime dtmin;
  /** data transazione massima dei record contenuti nel file */
  @Getter
  private LocalDateTime dtmax;
  /** data ultima modifica del file */
  @Getter
  private LocalDateTime ultagg;

  @Getter @Setter
  private boolean inDb;
  @Getter @Setter
  private boolean inFileSystem;

  private SimpleStringProperty  oId;
  private SimpleStringProperty  oTipoBanca;
  private SimpleStringProperty  oFileName;
  private SimpleStringProperty  oRelDir;
  private SimpleStringProperty  oCardHold;
  private SimpleIntegerProperty oSize;
  private SimpleIntegerProperty oQtarecs;
  private SimpleStringProperty  oDtmin;
  private SimpleStringProperty  oDtmax;
  private SimpleStringProperty  oUltagg;

  public CsvImpFile() {
    init();
  }

  public CsvImpFile(Path lastd, Path pth) {
    init();
    assignPath(lastd, pth);
  }

  public CsvImpFile(Integer p_id, ETipoBanca p_tipob, String p_fileName, String p_relDir, String p_cardHold, int p_size,
      int p_qtarecs, LocalDateTime p_dtmin, LocalDateTime p_dtmax, LocalDateTime p_ultagg) {
    init();
    setId(p_id);
    setTipoBanca(p_tipob);
    setRelDir(p_relDir);
    setFileName(p_fileName);
    setCardHold(p_cardHold);
    setSize(p_size);
    setQtarecs(p_qtarecs);
    setDtmin(p_dtmin);
    setDtmax(p_dtmax);
    setUltagg(p_ultagg);
    setInDb(null != p_id);
  }

  private void init() {
    oId = new SimpleStringProperty();
    oTipoBanca = new SimpleStringProperty();
    oFileName = new SimpleStringProperty();
    oRelDir = new SimpleStringProperty();
    oCardHold = new SimpleStringProperty();
    oSize = new SimpleIntegerProperty();
    oQtarecs = new SimpleIntegerProperty();
    oDtmin = new SimpleStringProperty();
    oDtmax = new SimpleStringProperty();
    oUltagg = new SimpleStringProperty();
  }

  public CsvImpFile assignPath(String rad, String pth) {
    return assignPath(Paths.get(rad), Paths.get(pth));
  }

  public CsvImpFile assignPath(Path rad, Path pth) {
    fileName = pth.getFileName().toString();

    //    int n1 = rad.toString().length() + 1;
    //    int n2 = pth.toString().indexOf(fileName.toString());
    //    if (n2 - n1 <= 0)
    //      relDir = ".";
    //    else
    //      relDir = pth.toString().substring(n1, n2 - 1);

    String ss = File.separator;
    String szRelRadice = String.format("%s%s%s", ss, rad.getFileName().toString(), ss);
    String szFullFile = pth.toAbsolutePath().toString();
    int n1 = szFullFile.indexOf(szRelRadice);
    int n2 = n1 + szRelRadice.length();
    int n3 = szFullFile.length() - fileName.length() - 1;

    relDir = ".";
    if (n2 < n3)
      relDir = szFullFile.substring(n2, n3);
    setInFileSystem(Files.exists(pth));
    cardHold = null;
    Matcher mat = s_cardHold.matcher(fileName);
    if (mat.find())
      cardHold = mat.group(1);
    try {
      size = (int) Files.size(pth);
    } catch (IOException e) {
      s_log.error("Errore estrazione nome file, err={}", e.getMessage(), e);
    }
    qtarecs = 0;
    dtmin = null;
    dtmax = null;
    ultagg = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault());
    return this;
  }

  public Path fullPath(Path basePath) {
    return Paths.get(basePath.toString(), getRelDir(), getFileName());
  }

  public Path relativePath() {
    Path pthRel = Paths.get(getRelDir(), getFileName());
    return pthRel;
  }

  public SimpleStringProperty getOid() {
    if (null != id)
      oId.set(String.valueOf(id));
    else
      oId.set("");
    return oId;
  }

  public SimpleStringProperty getOFileName() {
    oFileName.set(fileName);
    return oFileName;
  }

  public SimpleStringProperty getORelDir() {
    oRelDir.set(relDir);
    return oRelDir;
  }

  public SimpleStringProperty getOCardHold() {
    oCardHold.set(cardHold);
    return oCardHold;
  }

  public SimpleIntegerProperty getOSize() {
    oSize.set(size);
    return oSize;
  }

  public SimpleIntegerProperty getOQtarecs() {
    oQtarecs.set(qtarecs);
    return oQtarecs;
  }

  public SimpleStringProperty getODtmin() {
    oDtmin.set(ParseData.formatDate(dtmin));
    return oDtmin;
  }

  public SimpleStringProperty getODtmax() {
    oDtmax.set(ParseData.formatDate(dtmax));
    return oDtmax;
  }

  public SimpleStringProperty getOUltagg() {
    oUltagg.set(ParseData.formatDate(ultagg));
    return oUltagg;
  }

  public void completaInfo(List<RigaBanca> righeBanca) {
    LocalDateTime ldtMin = LocalDateTime.MAX;
    LocalDateTime ldtMax = LocalDateTime.MIN;
    for (RigaBanca rb : righeBanca) {
      ldtMin = Utils.min(ldtMin, rb.getDtmov());
      ldtMax = Utils.max(ldtMax, rb.getDtmov());
    }
    setQtarecs(righeBanca.size());
    setDtmin(ldtMin);
    setDtmax(ldtMax);
    setUltagg(LocalDateTime.now());
  }

  public boolean hasTipoBanca() {
    return null != tipoBanca;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    CsvImpFile lf = new CsvImpFile();
    lf.id = id;
    lf.tipoBanca = tipoBanca;
    lf.fileName = fileName;
    lf.relDir = relDir;
    lf.size = size;
    lf.qtarecs = qtarecs;
    lf.dtmin = dtmin;
    lf.dtmax = dtmax;
    lf.ultagg = ultagg;
    return lf;
  }

  @Override
  public String toString() {
    DecimalFormat fmt = (DecimalFormat) NumberFormat.getInstance(Locale.ITALIAN);
    String sz = fmt.format(size);
    String sr = fmt.format(qtarecs);
    String tpb = hasTipoBanca() ? tipoBanca.name() : "*nul*";
    String szCrea = ParseData.formatDate(ultagg);
    String szRet = String.format("(%s,%d)\"%s/%s\"(%s:%s) recs=%s", tpb, id, relDir, fileName, sz, szCrea, sr);
    String szMin = ParseData.formatDate(dtmin);
    String szMax = ParseData.formatDate(dtmax);
    szRet += String.format(" [%s < %s]", szMin, szMax);
    return szRet;
  }

  @Override
  public boolean equals(Object obj) {
    if ( (null == obj) || ! (obj instanceof CsvImpFile))
      return false;
    CsvImpFile other = (CsvImpFile) obj;
    //    if (null == id || null == other.id)
    //      return false;
    //    if ( !id.equals(other.id))
    //      return false;
    if ( !Utils.isValue(fileName) || !Utils.isValue(other.fileName))
      return false;
    if ( !fileName.equals(other.fileName))
      return false;
    //    if ( !Utils.isValue(relDir) || !Utils.isValue(other.relDir))
    //      return false; 
    //    if ( !relDir.equals(other.relDir))
    //      return false;
    //    if ( !Utils.isValue(cardHold) || !Utils.isValue(other.cardHold))
    //      return false;
    if (Utils.isValue(cardHold) && Utils.isValue(other.cardHold))
      if ( !cardHold.equals(other.cardHold))
        return false;
    //    if ( !Utils.isValue(tipoBanca) || !Utils.isValue(other.tipoBanca))
    //      return false;
    if (Utils.isValue(tipoBanca) && Utils.isValue(other.tipoBanca))
      if ( !tipoBanca.equals(other.tipoBanca))
        return false;
    //    if (size != other.size)
    //      return false;
    //    if (qtarecs != other.qtarecs)
    //      return false;
    return true;
  }

  public void setId(Integer ii) {
    id = ii;
    oId.set(String.valueOf(ii));
  }

  public void setFileName(String fi) {
    fileName = fi;
    oFileName.set(fi);
  }

  public void setTipoBanca(ETipoBanca rd) {
    tipoBanca = rd;
    oTipoBanca.set(null != rd ? rd.toString() : (String) null);
  }

  public void setRelDir(String rd) {
    relDir = rd;
    oRelDir.set(rd);
  }

  public void setCardHold(String rd) {
    cardHold = rd;
    oCardHold.set(rd);
  }

  public void setSize(int sze) {
    size = sze;
    oSize.set(sze);
  }

  public void setQtarecs(int qt) {
    qtarecs = qt;
    oQtarecs.set(qt);
  }

  public void setDtmin(LocalDateTime dmi) {
    dtmin = dmi;
    oDtmin.set(ParseData.formatDate(dmi));
  }

  public void setDtmax(LocalDateTime dma) {
    dtmax = dma;
    oDtmax.set(ParseData.formatDate(dma));
  }

  public void setUltagg(LocalDateTime ul) {
    ultagg = ul;
    oUltagg.set(ParseData.formatDate(ul));
  }

  public boolean hasPeriodo() {
    return null != dtmin && null != dtmax;
  }

  public boolean sameCardHold(String szCardh) {
    if (null == szCardh)
      return true;
    if (null == cardHold)
      return false;
    return cardHold.equals(szCardh);
  }

  public void garbleName(Path p_based) {
    String szOld = String.format("%s\\%s\\%s", p_based.toString(), relDir, fileName);
    String szNew = String.format("%s\\%s\\XEliminato_%s", p_based.toString(), relDir, fileName);
    try {
      Files.move(Paths.get(szOld), Paths.get(szNew), StandardCopyOption.REPLACE_EXISTING);
      s_log.info("Rinominato \"{}\" in \"{}\"", fileName, szNew);
    } catch (IOException e) {
      s_log.error("Non riesco a rinominare \"{}\" in \"{}\"", fileName, szNew);
    }
  }

}
