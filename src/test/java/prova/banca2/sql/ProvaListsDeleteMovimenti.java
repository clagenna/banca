package prova.banca2.sql;

import java.util.Date;
import java.util.List;
import java.util.Random;

import sm.clagenna.banca.dati.RigaBanca;

public class ProvaListsDeleteMovimenti extends ProvaSQLBase {

  public void main(String[] args) {
    ProvaListsDeleteMovimenti prova = new ProvaListsDeleteMovimenti();
    prova.provaTutti();
  }

  @Override
  public void eseguiTest() {
    System.out.printf("ProvaListsMovimenti propFile:%s\n", model.getPropsFile());
    Random rnd = new Random(new Date().getTime());
    int ini = rnd.nextInt(1000);
    int fin = ini + 100;
    List<RigaBanca> liMov = sqlgest.getListMovimenti(ini, fin, null);
    liMov.forEach(r -> eliminaMovs(r));
  }

  private void eliminaMovs(RigaBanca rig) {
    if ( !sqlgest.existMovimento(rig)) {
      System.out.printf("Mov %s non esiste\n", rig.toStringShort());
    } else {
      int qta = sqlgest.deleteMovimento(rig);
      System.out.printf("Eliminata/e %d Mov %s\n", qta, rig.toStringShort());
    }
  }

}
