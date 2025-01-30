package prova.banca;

import java.util.List;

import org.junit.Test;

import sm.clagenna.banca.dati.AtecoContainer;
import sm.clagenna.banca.dati.CodeAteco;

public class ProvaAteco {
  public ProvaAteco() {
    //
  }

  @Test
  public void provalo() {
    AtecoContainer cont = new AtecoContainer();
    cerca(cont, "manuten");
    cerca(cont, "ristor");
    cerca(cont, "casa");
  }

  private void cerca(AtecoContainer cont, String szSearch) {
    List<CodeAteco> li = cont.contains(szSearch);
    if (null != li && li.size() > 0) {
      System.out.printf("\n--- %s ---\n", szSearch);
      li.stream().forEach(System.out::println);
    } else {
      System.out.printf("\n--- %s ---  not found\n", szSearch);
    }
  }

}
