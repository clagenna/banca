package sm.clagenna.banca.dati;

import java.time.LocalDateTime;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class GuessCodStat {

  public static final String COL_ID       = "id";
  public static final String COL_TIPO     = "tipo";
  public static final String COL_DTMOV    = "dtmov";
  public static final String COL_DARE     = "dare";
  public static final String COL_AVERE    = "avere";
  public static final String COL_CARDID   = "cardid";
  public static final String COL_DESCR    = "descr";
  public static final String COL_CODSTAT  = "codstat";
  public static final String COL_CDSDESCR = "cdsdescr";
  public static final String COL_ASSIGNED = "assigned";

  private SimpleIntegerProperty               id;
  private SimpleStringProperty                tipo;
  private SimpleObjectProperty<LocalDateTime> dtmov;
  private SimpleDoubleProperty                dare;
  private SimpleDoubleProperty                avere;
  private SimpleStringProperty                cardid;
  private SimpleStringProperty                descr;
  private SimpleStringProperty                codstat;
  private SimpleStringProperty                descrcds;
  private SimpleBooleanProperty               assigned;
  private String                              codstatOrig;

  public GuessCodStat() {
    init();
  }

  private void init() {
    id = new SimpleIntegerProperty(null, COL_ID);
    tipo = new SimpleStringProperty(null, COL_TIPO);
    dtmov = new SimpleObjectProperty<LocalDateTime>(null, COL_DTMOV);
    dare = new SimpleDoubleProperty(null, COL_DARE);
    avere = new SimpleDoubleProperty(null, COL_AVERE);
    cardid = new SimpleStringProperty(null, COL_CARDID);
    descr = new SimpleStringProperty(null, COL_DESCR);
    codstat = new SimpleStringProperty(null, COL_CODSTAT);
    descrcds = new SimpleStringProperty(null, COL_CDSDESCR);
    assigned = new SimpleBooleanProperty(null, COL_ASSIGNED);
  }

  public GuessCodStat(Integer id, String tipo, LocalDateTime dtmov, Double dare, Double avere, String cardid, String descr,
      String codstat, String descrcds, boolean assigned) {
    init();
    setId(id);
    setTipo(tipo);
    setDtmov(dtmov);
    setDare(dare);
    setAvere(avere);
    setCardid(cardid);
    setDescr(descr);
    setCodstat(codstat);
    codstatOrig = codstat;
    setDescrCds(descrcds);
    setAssigned(assigned);
  }

  public SimpleIntegerProperty propertyId() {
    return id;
  }

  public StringProperty propertyTipo() {
    return tipo;
  }

  public ObjectProperty<LocalDateTime> propertyDtmov() {
    return dtmov;
  }

  public DoubleProperty propertyDare() {
    return dare;
  }

  public DoubleProperty propertyAvere() {
    return avere;
  }

  public StringProperty propertyCardid() {
    return cardid;
  }

  public StringProperty propertyDescr() {
    return descr;
  }

  public StringProperty propertyCodstat() {
    return codstat;
  }

  public StringProperty propertyDescrcds() {
    return descrcds;
  }

  public BooleanProperty propertyAssigned() {
    return assigned;
  }

  public Integer getId() {
    return id.get();
  }

  public void setId(Integer ii) {
    id.set(ii);
  }

  public String getTipo() {
    return tipo.get();
  }

  public void setTipo(String ii) {
    tipo.set(ii);
  }

  public LocalDateTime getDtmov() {
    return dtmov.get();
  }

  public void setDtmov(LocalDateTime ii) {
    dtmov.set(ii);
  }

  public Double getDare() {
    return dare.get();
  }

  public void setDare(Double ii) {
    dare.set(ii);
  }

  public Double getAvere() {
    return avere.get();
  }

  public void setAvere(Double ii) {
    avere.set(ii);
  }

  public String getCardid() {
    return cardid.get();
  }

  public void setCardid(String ii) {
    cardid.set(ii);
  }

  public String getDescr() {
    return descr.get();
  }

  public void setDescr(String ii) {
    descr.set(ii);
  }

  public String getCodstat() {
    return codstat.get();
  }

  public void setCodstat(String ii) {
    if ( null == ii) {
      codstat.set(null);
      return;
    }
    CodStat cds = CodStat.parse(ii);
    if ( null == cds)
      return;
    codstat.set(cds.getCodice());

    // aggiorno la nuova descr del codstat
    if ( !cds.getCodice().equals(codstatOrig)) {
      DataController cntrl = DataController.getInst();
      CodStatTreeData cdsCntrl = cntrl.getCodStatData();
      if (null != cdsCntrl) {
        cds = cdsCntrl.decodeCodStat(cds.getCodice());
        if (null != cds)
          setDescrCds(cds.getDescr());
      }
    }
  }

  public String getDescrcds() {
    return descrcds.get();
  }

  public void setDescrCds(String ii) {
    descrcds.set(ii);
  }

  public Boolean isAssigned() {
    return assigned.get();
  }

  public void setAssigned(boolean ii) {
    assigned.set(ii);
  }

  public boolean isModified() {
    boolean ret = Utils.isChanged(codstatOrig, codstat.get());
    return ret;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String vir = "";
    sb.append(String.format("%s%s=%s", vir, COL_ID, getId()));
    vir = ";\n";
    sb.append(String.format("%s%s=%s", vir, COL_TIPO, getTipo()));
    sb.append(String.format("%s%s=%s", vir, COL_DTMOV, ParseData.formatDate(getDtmov())));
    sb.append(String.format("%s%s=%s", vir, COL_DARE, getDare() != null ? Utils.s_fmtDbl.format(getDare()) : "-"));
    sb.append(String.format("%s%s=%s", vir, COL_AVERE, getAvere() != null ? Utils.s_fmtDbl.format(getAvere()) : "-"));
    sb.append(String.format("%s%s=%s", vir, COL_CARDID, getCardid()));
    sb.append(String.format("%s%s=%s", vir, COL_DESCR, getDescr()));
    sb.append(String.format("%s%s=%s", vir, COL_CODSTAT, getCodstat()));
    sb.append(String.format("%s%s=%s", vir, COL_CDSDESCR, getDescrcds()));
    sb.append(String.format("%s%s=%s", vir, COL_ASSIGNED, isAssigned()));
    
    return sb.toString();
  }
}
