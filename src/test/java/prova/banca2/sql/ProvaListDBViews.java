package prova.banca2.sql;

public class ProvaListDBViews extends ProvaSQLBase {

  public void main(String[] args) {
    ProvaListDBViews prova = new ProvaListDBViews();
    prova.provaTutti();
  }

  @Override
  public void eseguiTest() {
    System.out.printf("ProvaListDBViews propFile:%s\n", model.getPropsFile());
    sqlgest.getListDBViews().forEach((k, v) -> {
      System.out.println("View: " + k + " - " + v);
    });
  }

}
