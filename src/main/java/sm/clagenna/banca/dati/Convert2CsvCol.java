package sm.clagenna.banca.dati;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.sql.SqlTypes;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class Convert2CsvCol {

  private final ConvertCsv2RigaBanca convRB;
  @Getter @Setter
  private RigaBanca                  rbRef;
  @Getter
  private String                     name;
  private EColsTableView             rbCol;
  @Getter @Setter
  private String                     defVal;
  @Getter @Setter
  private SqlTypes                   type;
  @Getter @Setter
  private String                     colFrom;
  @Getter @Setter
  private String                     ifNull;
  @Getter @Setter
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
    if (null != sz) {
      setColFrom(sz);
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
    if (null != colFrom)
      vv = riga.get(colFrom);
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

}
