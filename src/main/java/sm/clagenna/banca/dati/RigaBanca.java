package sm.clagenna.banca.dati;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.Setter;

public class RigaBanca {
  @Getter @Setter
  private LocalDateTime dtmov;
  @Getter @Setter
  private LocalDateTime dtval;
  @Getter @Setter
  private double        dare;
  @Getter
  private double        avere;
  @Getter
  private String        descr;
  @Getter @Setter
  private String        caus;
  @Getter
  private String        cardid;
  @Getter
  private String        localCardIdent;

  public RigaBanca(LocalDateTime p_dtmov, LocalDateTime p_dtval, double p_dare, double p_avere, String p_descr, String p_caus,
      String p_cardid) {
    dtmov = p_dtmov;
    dtval = p_dtval;
    dare = p_dare;
    setAvere(p_avere);
    setDescr(p_descr);
    caus = p_caus;
    cardid = p_cardid;
  }

  @Override
  public String toString() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String sz1 = null == dtmov ? "*null*" : formatter.format(dtmov);
    String sz2 = null == dtval ? "*null*" : formatter.format(dtval);
    return sz1 + "\t" + sz2 + "\t" + dare + "\t" + avere + "\t" + descr + "\t" + caus + "\t" + cardid + "\\n";
  }

  public void setAvere(double vv) {
    avere = vv;
    if (vv < 0)
      System.out.println("RigaBanca.setAvere()");
  }

  public void setDescr(String p_des) {
    descr = p_des;
    localCardIdent = discerniCardId(p_des);
  }

  public void setCardid(String p_sz) {
    cardid = p_sz;
    if (null == p_sz && localCardIdent != null)
      cardid = localCardIdent;
  }

  public String discerniCardId(String descr) {
    String ret = null;
    if (null == descr)
      return ret;
    if (descr.matches(".*[0-9]+84806"))
      ret = "cla";
    else if (descr.matches(".*[0-9]+66542"))
      ret = "eug";
    else if (descr.matches(".*[0-9]+85928"))
      ret = "eug";
    return ret;
  }
}
