package prova.banca2.sql;

import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.sql.ESqlFiltri;
import sm.clagenna.banca.sql.ISQLGest;

public class ProvaExistMovimento extends ProvaSQLBase {

  public void main(String[] args) {
    ProvaExistMovimento prova = new ProvaExistMovimento();
    prova.provaTutti();
  }

  @Override
  public void eseguiTest() {
    System.out.printf("ProvaExistMovimento propFile:%s\n", model.getPropsFile());
    model.setFiltriQuery(ESqlFiltri.IdCodstat.getFlag() | ESqlFiltri.Cardid.getFlag());

    ISQLGest sqlgest = model.getSqlgest();
    RigaBanca rig = new RigaBanca();
    rig.setCodstat("01.02");
    // se si lascia il filtro cosi, esce la query: WHERE 1=1 AND tipo = ? AND dtval = ? AND dare = ? AND avere = ?
    model.setFiltriQuery(0);
    model.setFiltriQuery(ESqlFiltri.IdCodstat.getFlag());

    boolean bRet = sqlgest.existMovimento(rig);
    System.out.println("Riga Banca:" + rig.toString());
    if (bRet)
      System.out.println("Movimento esiste");
    else
      System.out.println("Movimento NON esiste");
  }

}
