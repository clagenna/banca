package prova.files;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class ProvaRelFile {

  @Test
  public void doTheJob() {
    // "F:\\java\\photon2\\banca\\dati\\Ateco\\AtecoProva.txt"
    Path filp = Paths.get("F:\\java\\photon2\\banca\\dati\\Ateco\\AtecoProva.txt");
    Path relp = Paths.get("F:\\java\\photon2\\banca\\dati");
    separaRel(relp, filp);
    relp = Paths.get("dati");
    separaRel(relp, filp);
  }

  private void separaRel(Path relp, Path filp) {
    String filename = filp.getFileName().toString();

    String ss = File.separator;
    String szRelRadice = String.format("%s%s%s", ss, relp.getFileName().toString(), ss);
    String szFullFile = filp.toAbsolutePath().toString();
    // Partenza del path Dati relativo
    int n1 = szFullFile.indexOf(szRelRadice);
    int n2 = n1 + szRelRadice.length();
    int n3 = szFullFile.length() - filename.length() - 1;

    String relDir = ".";
    if (n2 < n3)
      relDir = szFullFile.substring(n2, n3);

    System.out.printf("file=%s\tbase=%s\tRelDir=%s\t File=%s\n", //
        filp.toAbsolutePath().toString(), //
        relp.toString(), //
        relDir, //
        filename);
  }

}
