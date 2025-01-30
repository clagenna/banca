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
  private DataController   cntrl;

  public TableViewFillerBanca(TableView<List<Object>> tblview, DBConn p_dbc) {
    super(tblview, p_dbc);
    myExcludeCols = LoadBancaMainApp.getInst().getData().getExcludeCols();
    cntrl = DataController.getInst();
  }

  @Override
  public boolean isExcludedCol(String p_colNam) {
    boolean bRet = super.isExcludedCol(p_colNam);
    if (bRet || (null == myExcludeCols))
      return bRet;
    IRigaBanca rb = IRigaBanca.parse(p_colNam);
    if (null == rb)
      return false;
    bRet = myExcludeCols.contains(rb);
    return bRet;
  }

  @Override
  public void datasetReady() {
    cntrl.azzeraTotaliCodStat();
    String szQry = super.getSzQry();
    Dataset dts = super.getDataset();
    cntrl.firePropertyChange(DataController.EVT_NEW_QUERY_RESULT, null, szQry);
    cntrl.firePropertyChange(DataController.EVT_DATASET_CREATED, null, Integer.valueOf(dts.size()));
  }

  @Override
  public boolean scartaRiga(DtsRow riga) {
    if (super.scartaRiga(riga))
      return true;
    if ( !Utils.isValue(fltrParola))
      return false;
    if (fltrParolaRegEx)
      patt = Pattern.compile(fltrParola.toLowerCase());
    String desc = (String) riga.get(IRigaBanca.DESCR.getColNam());
    if ( !Utils.isValue(desc))
      return true;
    var lo = desc.toLowerCase();
    if ( !fltrParolaRegEx)
      return !lo.contains(fltrParola);
    if ( !patt.matcher(lo).find())
      return true;
    return false;
  }

  @Override
  public void addRiga(DtsRow riga) {
    super.addRiga(riga);
    String szCodice = (String) riga.get(EColsTableView.codstat.name());
    Number dareX = (Number) riga.get(EColsTableView.dare.name());
    Number avereX = (Number) riga.get(EColsTableView.avere.name());
    cntrl.aggiornaTotaliCodStat2(szCodice, dareX, avereX);
  }

  public void tableViewFilled() {
    cntrl.fineTotaliCodstat();
  }

}
