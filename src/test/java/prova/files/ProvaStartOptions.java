package prova.files;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import sm.clagenna.banca.dati.strt.StartOptions;
import sm.clagenna.banca.dati.strt.StrtOpt;
import sm.clagenna.stdcla.utils.AppProperties;

public class ProvaStartOptions {

  private StartOptions startOptions;

  @Test
  public void doTheJob() {
    AppProperties.setSingleton(false);
    System.out.println("ProvaStartOptions.test()");
    startOptions = new StartOptions();
    startOptions.initApp(null);
    System.out.printf("\nStartOptions: currStrtOptz=%s\n", startOptions.getCurrStrtOptz().toStringEx());
    startOptions.getListStrtOptz().forEach(s -> System.out.printf("StartOptions: %s\n", s.toStringEx()));
    startOptions.setOverwrite(true);
    creaLeggi();
    startOptions.closeApp(null);
  }

  public void creaLeggi() {
    String szOptName = "banca";
    // -----------------------------------------
    // StrtOpt da nome esistente
    StrtOpt opzOrig = startOptions.get(szOptName);
    if (null == opzOrig || opzOrig.isError()) {
      System.out.printf("Errore costr %s: %s\n", szOptName, null != opzOrig ? opzOrig.getMessage() : "opz == null");
      return;
    }
    System.out.printf("File trovato %s: %s\n", szOptName, opzOrig.getPropFile().toAbsolutePath().toString());

    // -----------------------------------------
    // duplicato properties quelle esistenti  
    szOptName = "banca Prova";
    startOptions.setOverwrite(false);
    // se true sono in errore
    StrtOpt optNew = startOptions.duplica(szOptName);
    if (null == optNew || optNew.isError()) {
      System.out.printf("Errore duplicazione (1) %s\n", szOptName);
      startOptions.setOverwrite(true);
      optNew = startOptions.duplica(szOptName);
      assertFalse(null == optNew || optNew.isError(), String.format("Errore duplicazione (2) %s\n", szOptName));
    }
    System.out.printf("File duplicato %s: %s\n", optNew.getName(), optNew.getPropFile().toAbsolutePath().toString());
    System.out.printf("\nverifica presenza del nuovo DB file: %s\n", optNew.getDbFile().toAbsolutePath().toString());

    // -----------------------------------------
    // cancellazione del property file del nuovo StrOpt
    assertTrue(startOptions.delete(optNew), String.format("Errore delete %s: %s\n", optNew.getName(), optNew.getMessage()));
    System.out.printf("File cancellato %s: %s\n", optNew.getName(), optNew.getPropFile().toAbsolutePath().toString());
  }

}
