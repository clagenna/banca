package sm.clagenna.banca.dati;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class ImpFile implements Cloneable {
  @SuppressWarnings("unused")
  private static final Logger s_log = LogManager.getLogger(ImpFile.class);

  @Getter
  private Integer       id;
  @Getter
  private String        fileName;
  @Getter
  private String        relDir;
  @Getter
  private int           size;
  @Getter
  private int           qtarecs;
  @Getter
  private LocalDateTime dtmin;
  @Getter
  private LocalDateTime dtmax;
  @Getter
  private LocalDateTime ultagg;

  private SimpleStringProperty  oId;
  private SimpleStringProperty  oFileName;
  private SimpleStringProperty  oRelDir;
  private SimpleIntegerProperty oSize;
  private SimpleIntegerProperty oQtarecs;
  private SimpleStringProperty  oDtmin;
  private SimpleStringProperty  oDtmax;
  private SimpleStringProperty  oUltagg;

  public ImpFile() {
    init();
  }

  public ImpFile(Path pth) {
    init();
    assignPath(pth);
  }

  private void init() {
    oId = new SimpleStringProperty();
    oFileName = new SimpleStringProperty();
    oRelDir = new SimpleStringProperty();
    oSize = new SimpleIntegerProperty();
    oQtarecs = new SimpleIntegerProperty();
    oDtmin = new SimpleStringProperty();
    oDtmax = new SimpleStringProperty();
    oUltagg = new SimpleStringProperty();
  }

  public ImpFile assignPath(Path pth) {
    int n = pth.getNameCount();
    fileName = pth.getFileName().toString();
    relDir = pth.subpath(n - 2, n - 1).toString();
    try {
      size = (int) Files.size(pth);
    } catch (IOException e) {
      e.printStackTrace();
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

  @Override
  protected Object clone() throws CloneNotSupportedException {
    ImpFile lf = new ImpFile();
    lf.id = id;
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
    String szRet = String.format("(%d)\"%s/%s\"(%s) recs=%s", id, relDir, fileName, sz, sr);
    String szMin = ParseData.formatDate(dtmin);
    String szMax = ParseData.formatDate(dtmax);
    szRet += String.format(" [%s < %s]", szMin, szMax);
    return szRet;
  }

  public void setId(Integer ii) {
    id = ii;
    oId.set(String.valueOf(ii));
  }

  public void setFileName(String fi) {
    fileName = fi;
    oFileName.set(fi);
  }

  public void setRelDir(String rd) {
    relDir = rd;
    oRelDir.set(rd);
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

}
