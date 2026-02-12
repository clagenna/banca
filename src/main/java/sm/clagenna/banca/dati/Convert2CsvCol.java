package sm.clagenna.banca.dati;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import lombok.Data;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.sql.SqlTypes;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class Convert2CsvCol {

  private final ConvertCsv2RigaBanca convRB;
  private RigaBanca                  rbRef;
  private String                     name;
  private EColsTableView             rbCol;
  private String                     defVal;
  private SqlTypes                   type;
  private ArrayList<String>          colFrom;
  private String                     ifNull;
  private String                     envval;

  public Convert2CsvCol(ConvertCsv2RigaBanca convRB) {
    this.convRB = convRB;
    //
  }

  public Convert2CsvCol(ConvertCsv2RigaBanca convRB, RigaBanca p_rb, String p_nam) {
    this.convRB = convRB;
    rbRef = p_rb;
    setName(p_nam);
    if (null != p_nam) {
      rbCol = EColsTableView.valueOf(p_nam);
    }
  }

  public Convert2CsvCol(ConvertCsv2RigaBanca convRB, RigaBanca p_rb, String p_nam, SqlTypes ty) {
    this.convRB = convRB;
    rbRef = p_rb;
    setName(p_nam);
    setType(ty);
  }

  public boolean parseProps(AppProperties p_prop, String szKey) {
    String sz = p_prop.getProperty(szKey + "name", null);
    if (sz == null)
      return false;
    setName(sz);

    sz = p_prop.getProperty(szKey + "value", null);
    if (null != sz) {
      setDefVal(sz);
      return true;
    }

    sz = p_prop.getProperty(szKey + "type", null);
    if (null != sz) {
      SqlTypes ty = SqlTypes.parse(sz);
      if (null != ty)
        setType(ty);
    }

    sz = p_prop.getProperty(szKey + "colfrom", null);
    colFrom = new ArrayList<String>();
    if (null != sz) {
      if (sz.contains(";")) {
        String[] arr = sz.split(";");
        colFrom.addAll(Arrays.asList(arr));
      } else
        colFrom.add(sz);
    }

    sz = p_prop.getProperty(szKey + "ifnull", null);
    if (null != sz) {
      setIfNull(sz);
    }

    sz = p_prop.getProperty(szKey + "envval", null);
    if (null != sz) {
      setEnvval(sz);
    }
    return true;
  }

  public RigaBanca assign(RigaBanca rb, DtsRow riga) {
    Object vv = null;
    // scandisco i vari nomi di colonna da cui prelevare
    if (null != colFrom) {
      //  vv = riga.get(colFrom);
      for (String szCol : colFrom) {
        vv = riga.get(szCol);
        if (Utils.isValue(vv))
          break;
      }
    }
    if ( !Utils.isValue(vv) && null != ifNull)
      vv = riga.get(ifNull);
    if ( !Utils.isValue(vv))
      vv = defVal;
    if ( !Utils.isValue(vv) && null != envval) {
      vv = this.convRB.env.get(envval);
    }

    switch (rbCol) {
      case id:
        break;
      case tipo:
        rb.setTiporec((String) vv);
        break;
      case idfile:
        break;
      case dtmov:
        if (vv instanceof String szdt) {
          LocalDateTime ldt = ParseData.guessData(szdt);
          rb.setDtmov(ldt);
        } else {
          rb.setDtmov((LocalDateTime) vv);
        }
        break;
      case dtval:
        if (vv instanceof String szdt) {
          LocalDateTime ldt = ParseData.guessData(szdt);
          rb.setDtval(ldt);
        } else {
          rb.setDtval((LocalDateTime) vv);
        }
        break;
      case dare:
        rb.setDare((Double) vv);
        break;
      case avere:
        rb.setAvere(Utils.parseDouble(vv));
        break;
      case descr:
        rb.setDescr((String) vv);
        break;
      case abicaus:
        rb.setAbicaus((String) vv);
        break;
      case costo:
        rb.setCosto(Utils.parseInt(vv));
        break;
      case cardid:
        if (Utils.isValue(vv))
          rb.setCardid((String) vv);
        break;
      case codstat:
        rb.setCodstat((String) vv);
        break;
      case descrcaus:
        break;
      case movstr:
        break;
      case valstr:
        break;
      default:
        break;

    }
    return rb;
  }

  public void setName(String p_nam) {
    name = p_nam;
    rbCol = EColsTableView.valueOf(name);
    if (null == rbCol) {
      String szMsg = String.format("Il nome %s non corrisponde a nessun campo in RigaBanca", p_nam);
      ConvertCsv2RigaBanca.s_log.error(szMsg);
      throw new UnsupportedOperationException(szMsg);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Name=").append(name);
    sb.append("\n\t").append("Tipo=").append(null == rbCol ? "-" : rbCol.toString());
    sb.append("\n\t").append("Defv=").append(defVal);
    sb.append("\n\t").append("SqlT=").append(null == type ? "-" : type.toString());
    sb.append("\n\t").append("ColF=").append(colFrom);
    sb.append("\n\t").append("Ifnl=").append(ifNull);
    sb.append("\n\t").append("EnvV=").append(envval);
    return sb.toString();
  }

}
