package prova.banca;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;

public class ProvaPathMatcher {

  public ProvaPathMatcher() {
    //
  }

  @Test
  public void provalo() throws AppPropsException {
    final String CSZ_PATH = "F:\\Google Drive\\gennari\\Banche";
    AppProperties props = new AppProperties();
    props.leggiPropertyFile(new File("Banca.properties"), true, false);
    String szGlo = "glob:*:/**/{estra*,wise*}*.csv";
    String fltr = props.getProperty("filter_files");
    if (null != fltr)
      szGlo = creaGlobMatch(fltr);
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(szGlo);
    List<Path> res = null;
    try (Stream<Path> walk = Files.walk(Paths.get(CSZ_PATH), FileVisitOption.FOLLOW_LINKS)) {
      res = walk.filter(f -> !Files.isDirectory(f)) //
          .filter(f -> matcher.matches(f)) //
          .collect(Collectors.toList());
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    if (null == res) {
      System.out.println("** no files found ! **");
      return;
    }
    res.stream().forEach(System.out::println);
  }

  private String creaGlobMatch(String fltr) {
    String arr[] = fltr.split(",");
    String fils = "";
    String vir = "";
    for (String pat : arr) {
      fils += String.format("%s%s*", vir, pat);
      vir = ",";
    }
    return String.format("glob:*:/**/{%s}*.csv", fils);
  }
}
