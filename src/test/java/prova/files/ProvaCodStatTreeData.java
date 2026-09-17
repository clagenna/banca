package prova.files;

import sm.clagenna.banca.dati.DataModel;
import sm.clagenna.banca.dati.TreeCodStat;
import sm.clagenna.stdcla.utils.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;

public class ProvaCodStatTreeData {

  private AppProperties props;
  DataModel             model;
  TreeCodStat           cdsdata;

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

    model = new DataModel();
    model.initApp(props);

    cdsdata = new TreeCodStat();

  }

}
