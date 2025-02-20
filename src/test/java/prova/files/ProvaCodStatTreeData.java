package prova.files;

import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.TreeCodStat2;
import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;

public class ProvaCodStatTreeData {

  private AppProperties props;
  DataController        datac;
  TreeCodStat2          cdsdata;

  public ProvaCodStatTreeData() {
    //
  }

  public void doTheJob() throws AppPropsException {
    init();
  }

  private void init() throws AppPropsException {
    AppProperties.setSingleton(false);
    props = new AppProperties();
    props.leggiPropertyFile("Banca.properties");

    datac = new DataController();
    datac.initApp(props);

    cdsdata = new TreeCodStat2();

  }

}
