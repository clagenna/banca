package sm.clagenna.banca.javafx;

import java.util.List;
import java.util.regex.Pattern;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.stdcla.javafx.TableViewFiller;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.Utils;

public class TableViewFillerBanca extends TableViewFiller {

  private static String QRY_WHE_NOTRASF = "AND abicaus not in ('45','S3','S4') AND descr NOT LIKE '%wise%'";

  @Getter @Setter
  private boolean fltrParolaRegEx;
  @Getter @Setter
  private String  fltrParola;
  private Pattern patt;

  private List<EColsTableView> myExcludeCols;
  private DataController       cntrl;
  // private Double               precId, precDare, precAvere;

  private boolean m_bScartaImpTrasf;

  public TableViewFillerBanca(TableView<List<Object>> tblview, DBConn p_dbc) {
    super(tblview, p_dbc);
    myExcludeCols = LoadBancaMainApp.getInst().getData().getExcludeCols();
    cntrl = DataController.getInst();
  }

  @Override
  public String modifyQuery(String szQry) {
    StringBuilder szQry2 = new StringBuilder();
    if (m_bScartaImpTrasf) {
      int ndx = szQry.toLowerCase().indexOf("order");
      if (ndx > 0) {
        szQry2.append(szQry.substring(0, ndx)) //
            .append(QRY_WHE_NOTRASF) //
            .append(" ") //
            .append(szQry.substring(ndx));
      }
    } else
      szQry2.append(szQry);
    return szQry2.toString();
  }

  @Override
  public boolean isExcludedCol(String p_colNam) {
    boolean bRet = super.isExcludedCol(p_colNam);
    if (bRet || (null == myExcludeCols))
      return bRet;
    EColsTableView rb = EColsTableView.parse(p_colNam);
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
    //    DtsRow precRow = null;
    //    for (DtsRow row : dts.getRighe()) {
    //      if (null == precRow) {
    //        precRow = row;
    //        continue;
    //      }
    //      int vFlag = 0;
    //      Double precDare = (Double) precRow.get(EColsTableView.dare.name());
    //      Double precAvere = (Double) precRow.get(EColsTableView.avere.name());
    //      Double currDare = (Double) row.get(EColsTableView.dare.name());
    //      Double currAvere = (Double) row.get(EColsTableView.avere.name());
    //      if (precDare != 0 && currDare != 0)
    //        vFlag = Utils.isValueEq(precDare, currDare) ? 1 : 0;
    //      if (precAvere != 0 && currAvere != 0)
    //        vFlag += Utils.isValueEq(precAvere, currAvere) ? 2 : 0;
    //      if (vFlag > 0) {
    //        precRow.set(EColsTableView.flag.name(), vFlag);
    //        row.set(EColsTableView.flag.name(), vFlag);
    //      }
    //      precRow = row;
    //    }
  }

  @Override
  public boolean scartaRiga(DtsRow riga) {
    if (super.scartaRiga(riga))
      return true;
    if ( !Utils.isValue(fltrParola))
      return false;
    if (fltrParolaRegEx)
      patt = Pattern.compile(fltrParola.toLowerCase());
    String desc = (String) riga.get(EColsTableView.descr.toString());
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

  public void setScartaImpTrasf(boolean bv) {
    m_bScartaImpTrasf = bv;
  }

  @Override
  public void colBuilded(TableColumn<List<Object>, Object> pcol) {
    super.colBuilded(pcol);
    pcol.setCellFactory(_ -> new TableCell<List<Object>, Object>() {

      @Override
      protected void updateItem(Object item, boolean empty) {
        // super.updateItem(item, empty);
        // cell_updItm(this, item, empty);
        setStyle("");
        if (empty || null == item) {
          setText("");
          super.updateItem(item, empty);
          return;
        }
        String szVal = item.toString();
        String szCss = TableViewFiller.FX_ALIGNMENT_CENTER_LEFT;
        String szId = getId();
        // checkValueItem(this, item, empty);
        // super.updateItem(item, empty);
        int n = szId.indexOf("_");
        String szColName = "*";
        if (n >= 0)
          szColName = szId.substring(n + 1);
        if (szColName.equals("*"))
          return;
        EColsTableView ecol = EColsTableView.parse(szColName);
        switch (ecol) {
          case dtmov:
          case dtval:
            szCss = TableViewFiller.FX_ALIGNMENT_CENTER_RIGHT;
            break;
          case dare:
          case avere:
            szVal = cell_fmtMoney(szVal);
            szCss = TableViewFiller.FX_ALIGNMENT_CENTER_RIGHT;
            break;

          default:
            break;
        }
        // String szCls = pcol.getClass().getSimpleName();
        // System.out.printf("%s)[%s] %s --> %s\n", szColName, szCls, item.getClass().getSimpleName(), item.toString());

        /**
         * <pre>
         * if (szColName.equals(EColsTableView.dare.toString())) {
         *   // pr = pcol.
         *   Double locDare = Utils.parseDouble(szVal);
         *   if (null != locDare && null != precDare // 
         *       && locDare.doubleValue() != 0 // 
         *       && locDare.doubleValue() == precDare.doubleValue()) {
         *     szCss += String.format("-fx-Background-color: %s;", "gold");
         *     System.out.printf("%s) %s --> %s ( %.2f == %.2f)\n", //
         *         szColName, szCls, item.toString(), locDare.doubleValue(), precDare.doubleValue());
         *   }
         *   precDare = locDare;
         * }
         * 
         * if (szColName.equals(EColsTableView.avere.toString())) {
         *   Double locAvere = Utils.parseDouble(szVal);
         *   if (null != locAvere && locAvere.doubleValue() != 0 && null != precDare)
         *     if (locAvere.doubleValue() == precAvere.doubleValue()) {
         *       szCss += String.format("-fx-Background-color: %s;", "gold");
         *       System.out.printf("%s) %s --> %s\n", szColName, szCls, item.toString());
         *     }
         *   precAvere = locAvere;
         * }
         * </pre>
         */
        setText(szVal);
        setStyle(szCss);
      };
    });

  }

  private String cell_fmtMoney(String val) {
    String szVal = "";
    Double dbl = Utils.parseDouble(val);
    szVal = null != dbl ?  Utils.s_fmtDbl.format(dbl.doubleValue()) : szVal;
    return szVal;
  }

//  private void cell_updItm(TableCell<List<Object>, Object> cell, Object item, boolean empty) {
//    String szCls1 = cell.getClass().getSimpleName();
//    String szCls2 = null != item ? item.getClass().getSimpleName() : "*null*";
//    System.out.printf("cell=%s\titem=%s\n", szCls1, szCls2);
//  }

  public void tableViewFilled() {
    cntrl.fineTotaliCodstat();
  }

  //  private void checkValueItem(TableColumn pCell, Object item, boolean empty) {
  //    pCell.updateItem(item, empty);
  //    String szId = getId();
  //    int n = szId.indexOf("_");
  //    int nId = -1;
  //    if (n >= 0)
  //      nId = Integer.parseInt(szId.substring(n + 1));
  //    if ( nId < 0)
  //      return;
  //    if ( nId == EColsTableView.dare.getColNo()) {
  //      System.out.printf("TableViewFillerBanca.colBuilded(%d)\n", nId);
  //    }
  //  }

}
