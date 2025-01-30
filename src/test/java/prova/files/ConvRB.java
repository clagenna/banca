package prova.files;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.RigaBanca;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.sql.SqlTypes;
import sm.clagenna.stdcla.sys.ex.AppPropsException;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class ConvRB {
  private static final Logger s_log = LogManager.getLogger(ConvRB.class);

  class ConvCol {

    @Getter @Setter
    private RigaBanca      rbRef;
    @Getter
    private String         name;
    private EColsTableView rbCol;
    @Getter @Setter
    private String         defVal;
    @Getter @Setter
    private SqlTypes       type;
    @Getter @Setter
    private String         colFrom;
    @Getter @Setter
    private String         envval;

    public ConvCol() {
      //
    }

    public ConvCol(RigaBanca p_rb, String p_nam) {
      rbRef = p_rb;
      setName(p_nam);
      if (null != p_nam) {
        rbCol = EColsTableView.valueOf(p_nam);
      }
    }

    public ConvCol(RigaBanca p_rb, String p_nam, SqlTypes ty) {
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
      if (null == vv)
        vv = defVal;
      if (null == vv && null != envval) {
        vv = env.get(envval);
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
          rb.setDtmov((LocalDateTime) vv);
          break;
        case dtval:
          rb.setDtval((LocalDateTime) vv);
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
        s_log.error(szMsg);
        throw new UnsupportedOperationException(szMsg);
      }
    }

  }

  @Getter @Setter
  private String              tipo;
  @Getter @Setter
  private String              csvDelim;
  @Getter @Setter
  private RigaBanca           rigb;
  @Getter @Setter
  private boolean             blankOnZero;
  private AppProperties       convProps;
  private List<ConvCol>       convCols;
  @Getter
  private Map<String, String> env;

  public ConvRB(String pTipo) {
    //
  }

  public void readConvProperties(Path p_pth) {
    try {
      convProps = new AppProperties();
      convProps.leggiPropertyFile(p_pth.toFile(), true, false);
      parseProps();
    } catch (AppPropsException e) {
      e.printStackTrace();
    }

  }

  private void parseProps() {
    String tp = convProps.getProperty("tipo");
    setTipo(tp);
    String key = String.format("%s.csvDelim", tipo);
    setCsvDelim(convProps.getProperty(key, ";"));
    key = String.format("%s.blankOnZero", tipo);
    setBlankOnZero(convProps.getBooleanProperty(key, false));
    parseEnv();
    parseCols();
  }

  private void parseEnv() {
    final String fmt_env = "%s.env.";
    env = new HashMap<>();
    String prefix = String.format(fmt_env, getTipo());
    for (Object okey : convProps.getProperties().keySet()) {
      if (okey instanceof String key) {
        if (key.startsWith(prefix)) {
          String szVal = convProps.getProperty(key);
          env.put(key.substring(prefix.length()), szVal);
        }
      }
    }
  }

  private void parseCols() {
    final String fmt_col = "%s.col_%02d.";
    convCols = new ArrayList<>();
    int fails = 0;
    for (int k = 0; k < 100 && fails < 5; k++) {
      ConvCol ccol = new ConvCol();
      String szKey = String.format(fmt_col, getTipo(), k);
      if ( !ccol.parseProps(convProps, szKey)) {
        fails++;
        continue;
      }
      convCols.add(ccol);
    }
  }

  public RigaBanca assign(RigaBanca p_rb, DtsRow P_row) {
    for (ConvCol cc : convCols) {
      cc.assign(p_rb, P_row);
    }
    return p_rb;
  }
}
