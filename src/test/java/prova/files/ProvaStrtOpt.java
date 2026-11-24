package prova.files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import sm.clagenna.banca.dati.strt.StrtOpt;
import sm.clagenna.stdcla.utils.AppProperties;

public class ProvaStrtOpt {

  @Test
  public void doTheJob() {
    System.out.println("ProvaStrtOpt.test()");
    String szOptName = "banca";
    AppProperties.setSingleton(false);
    // -----------------------------------------
    // nuova StrtOpt da file properties esistente  
    StrtOpt optBase = new StrtOpt(szOptName);
    assertFalse(optBase.isError(), "Errore costr " + szOptName + ": " + (null != optBase ? optBase.getMessage() : "opt == null"));
    System.out.printf("File trovato %s: %s\n", szOptName, optBase.getPropFile().toAbsolutePath().toString());
    // -----------------------------------------
    // duplicato properties quelle esistenti  
    szOptName = "banca New";
    StrtOpt optDupl = new StrtOpt(szOptName);
    optDupl.setOverwrite(false);
    // provo errore su duplicazione senza overwrite
    if (!optDupl.duplica(optBase, szOptName)) {
      System.out.printf("Errore duplicazione (1) %s : %s\n", szOptName, optDupl.getMessage());
      optDupl.setOverwrite(true);
      assertTrue(optDupl.duplica(optBase, szOptName),
          String.format("Errore duplicazione (2) %s : %s\n", szOptName, optDupl.getMessage()));
    }
    System.out.printf("File duplicato %s! %s\n", szOptName, optDupl.getPropFile().toAbsolutePath().toString());
    System.out.printf("\nverifica presenza del nuovo DB file: %s\n", optDupl.getDbFile().toAbsolutePath().toString());

    // -----------------------------------------
    // cancellazione del property file del nuovo StrOpt
    assertTrue(optDupl.delete(), String.format("Errore delete %s: %s\n", szOptName, optDupl.getMessage()));
    System.out.printf("File cancellato %s\n", szOptName);
  }
}
