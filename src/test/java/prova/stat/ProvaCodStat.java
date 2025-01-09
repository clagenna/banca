package prova.stat;

import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import sm.clagenna.banca.dati.CodStat;
import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;

public class ProvaCodStat {
  private static final String FILE_CODSTAT  = "dati/Ateco/AtecoProva.txt";
  private static final String FILE_CODSTAT2 = "AtecoValues.properties";
  
  @SuppressWarnings("unused")
  private PropertyChangeSupport supp;

  CodStat radice;

  public ProvaCodStat() {
    //
  }

  @Test
  public void doTheJob() throws AppPropsException {
    AppProperties codstats = new AppProperties();
    supp = new PropertyChangeSupport(this);
    codstats.leggiPropertyFile(FILE_CODSTAT2, true, false);

    radice = new CodStat();
    StringBuilder sb = new StringBuilder();

    for (Object szKey : codstats.getProperties().keySet()) {
      String szVal = codstats.getProperty(szKey.toString());
      System.out.println("Add:" + szKey);
      CodStat nuovo = CodStat.parse(szKey.toString());
      nuovo.setDescr(szVal);
      radice.add(nuovo);
    }
    radice.printAll(sb);
    System.out.println(sb.toString());
  }

  /* Test */
  public void doTheJob2() {

    String riga = null;
    Pattern s_pat = Pattern.compile("^([0-9]+[\\.0-9]+)[ \t]+(.*)");
    radice = new CodStat();
    StringBuilder sb = new StringBuilder();
    try (BufferedReader buf = new BufferedReader(new FileReader(FILE_CODSTAT))) {
      while ( (riga = buf.readLine()) != null) {
        Matcher mat = s_pat.matcher(riga);
        if (mat.find()) {
          String szNew = mat.group(1);
          String szDesc = mat.group(2);

          System.out.println("Add:" + szNew);
          CodStat nuovo = CodStat.parse(szNew);
          nuovo.setDescr(szDesc);
          radice.add(nuovo);
        }
      }
    } catch (IOException e) {
      System.out.printf("Errore lettura %s, err=%s", FILE_CODSTAT, e.getMessage());
    }
    radice.printAll(sb);
    System.out.println(sb.toString());
  }
}
