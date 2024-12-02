package sm.clagenna.banca.dati;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.utils.ParseData;

@AllArgsConstructor
public class FileCSV {

  @Getter @Setter
  private SimpleStringProperty  name;
  @Getter @Setter
  private SimpleStringProperty  relPath;
  @Getter @Setter
  private SimpleIntegerProperty size;
  @Getter @Setter
  private SimpleIntegerProperty qtaRecs;
  @Getter @Setter
  private SimpleStringProperty  dtmin;
  @Getter @Setter
  private SimpleStringProperty  dtmax;
  @Getter @Setter
  private SimpleStringProperty  ultagg;

  public FileCSV() {
    name = new SimpleStringProperty();
    relPath = new SimpleStringProperty();
    size = new SimpleIntegerProperty();
    qtaRecs = new SimpleIntegerProperty();
    dtmin = new SimpleStringProperty();
    dtmax = new SimpleStringProperty();
    ultagg = new SimpleStringProperty();
  }

  public FileCSV assignPath(Path pth) {
    int n = pth.getNameCount();
    String szNam = pth.getFileName().toString();
    name.set(szNam);
    relPath.set(pth.subpath(n - 2, n - 1).toString());
    try {
      size.set((int) Files.size(pth));
    } catch (IOException e) {
      e.printStackTrace();
    }
    qtaRecs.set(0);
    dtmin.set("");
    dtmax.set("");
    ultagg.set(ParseData.s_fmtDtDate.format(new Date()));
    return this;
  }

}
