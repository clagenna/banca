package prova.banca;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class ProvaDiscerniCardId {

  public ProvaDiscerniCardId() {
    //
  }

  @Test
  public void provalo() {
    Path pth = Paths.get("F:\\Google Drive\\gennari\\Banche\\Banca BSI\\estrattoconto_BSI_2312.csv");
    String szId = discerniCardId(pth);
    System.out.printf(" Id=%s su path=%s\n", szId, pth.toString());
    pth = Paths.get("F:\\Google Drive\\gennari\\Banche\\Banca BSI Credit\\estrattoconto_BSI_Credit 2024-11_cla.csv");
    szId = discerniCardId(pth);
    System.out.printf(" Id=%s su path=%s\n", szId, pth.toString());
  }

  private String discerniCardId(Path p_pth) {
    String szRet = null;
    String sz = p_pth.toString();
    int n = sz.lastIndexOf("_");
    if (n < 0)
      return szRet;
    String sz2 = sz.substring(n - 2);
    Pattern pat = Pattern.compile(".*_([a-z]+)\\.[a-z]+", Pattern.CASE_INSENSITIVE);
    Matcher mat = pat.matcher(sz2);
    if (mat.find()) {
      szRet = mat.group(1);
    }
    return szRet;
  }
}
