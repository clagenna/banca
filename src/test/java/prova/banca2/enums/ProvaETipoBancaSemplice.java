package prova.banca2.enums;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import sm.clagenna.banca.dati.ETipoBanca;
import sm.clagenna.banca.dati.csv.CsvImpFile;

public class ProvaETipoBancaSemplice {
  String        szDir = "F:\\Google Drive\\gennari\\Banche";
  Path          lastDir;
  List<CsvImpFile> elenco;

  @Test
  public void test() {
    lastDir = Paths.get(szDir);
    try (Stream<Path> stream = Files.walk(lastDir)) {
      stream //
          .filter(Files::isRegularFile) //
          .forEach(pth -> tratta(pth));
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  private void tratta(Path pth) {
    String s = pth.toAbsolutePath().toString();
    Path fileName = pth.getFileName();
    ETipoBanca t = ETipoBanca.parse(fileName.toString());
    if (null == t)
      System.out.printf("File=\"%s\" *none* \n", s);
    else
      System.out.printf("File=\"%s\" = %s\n", s, t);
  }

}
