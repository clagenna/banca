package prova.banca2.sql;

import java.util.List;

import sm.clagenna.banca.dati.RigaBanca;

public class ProvaDeleteMovimento extends ProvaSQLBase {

  public void main(String[] args) {
    ProvaDeleteMovimento prova = new ProvaDeleteMovimento();
    prova.provaTutti();
  }

  @Override
  public void eseguiTest() {
    System.out.printf("ProvaDeleteMovimento propFile:%s\n", model.getPropsFile());
    String szFiltri = """
            cardid is not null
        and idCodStat is not null
            """;
    List<RigaBanca> lst = sqlgest.getListMovimenti(55, 60, szFiltri);
    for (RigaBanca riga : lst) {
      System.out.printf("Riga:%s\n", riga);
      int qta = sqlgest.deleteMovimento(riga);
      System.out.printf("Delete id:%d -> qta=%d\n", riga.getRigaid(), qta);
    }
  }
}
