package sm.clagenna.banca.javafx;

import java.util.List;
import java.util.regex.Pattern;

import javafx.scene.control.TableView;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.IRigaBanca;
import sm.clagenna.stdcla.javafx.TableViewFiller;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.Utils;

public class TableViewFillerBanca extends TableViewFiller {

  @Getter @Setter
  private boolean fltrParolaRegEx;
  @Getter @Setter
  private String  fltrParola;
  private Pattern patt;

  private List<IRigaBanca> myExcludeCols;

  public TableViewFillerBanca(TableView<List<Object>> tblview, DBConn p_dbc) {
    super(tblview, p_dbc);
    myExcludeCols = LoadBancaMainApp.getInst().getData().getExcludeCols();
  }

  @Override
  public boolean isExcludedCol(String p_colNam) {
    boolean bRet = super.isExcludedCol(p_colNam);
    if (bRet)
      return bRet;
    IRigaBanca rb = IRigaBanca.parse(p_colNam);
    if (null == rb)
      return false;
    bRet = myExcludeCols.contains(rb);
    return bRet;
  }

  @Override
  public void datasetReady() {
    // cambiare questo in datacontroller.firePropertyChange "resultview", szQry,szQry
    // LoadBancaMainApp mainApp = LoadBancaMainApp.getInst();
    // mainApp.aggiornaTotaliCodStat(szQry);
    DataController cntrl = DataController.getInst();
    String szQry = super.getSzQry();
    Dataset dts = super.getDataset();
    cntrl.firePropertyChange(DataController.EVT_NEW_QUERY_RESULT, null, szQry);
    cntrl.firePropertyChange(DataController.EVT_DATASET_CREATED, null, Integer.valueOf(dts.size()));
  }

  @Override
  public boolean scartaRiga(DtsRow riga) {
    if (fltrParolaRegEx)
      patt = Pattern.compile(fltrParola.toLowerCase());
    if (fltrParolaRegEx) {
      String desc = (String) riga.get(IRigaBanca.DESCR.getColNam());
      if ( !Utils.isValue(desc))
        return true;
      var lo = desc.toLowerCase();
      if ( !patt.matcher(lo).find())
        return true;
    }
    return false;
  }
}
