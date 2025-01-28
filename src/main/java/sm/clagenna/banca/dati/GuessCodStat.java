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
import sm.clagenna.stdcla.utils.Utils;

public class GuessCodStat {
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
  private String codstatOrig;

  public GuessCodStat() {
    init();
  }

  private void init() {
    id = new SimpleIntegerProperty();
    tipo = new SimpleStringProperty();
    dtmov = new SimpleObjectProperty<LocalDateTime>();
    dare = new SimpleDoubleProperty();
    avere = new SimpleDoubleProperty();
    cardid = new SimpleStringProperty();
    descr = new SimpleStringProperty();
    codstat = new SimpleStringProperty();
    descrcds = new SimpleStringProperty();
    assigned = new SimpleBooleanProperty();
  }

  public GuessCodStat(Integer id, String tipo, LocalDateTime dtmov, Double dare, Double avere, String cardid, String descr,
      String codstat, String descrcds,  boolean assigned) {
    init();
    setId(id);
    setTipo(tipo);
    setDtmov(dtmov);
    setDare(dare);
    setAvere(avere);
    setCardid(cardid);
    setDescr(descr);
    setCodstat(codstat);
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
    codstat.set(ii);
    if ( null == codstatOrig)
      codstatOrig=ii;
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
  
}
