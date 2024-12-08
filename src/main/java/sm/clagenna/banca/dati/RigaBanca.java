package sm.clagenna.banca.dati;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class RigaBanca {
  @Getter @Setter
  private Integer       rigaid;
  @Getter @Setter
  private Integer       idfile;
  @Getter @Setter
  private LocalDateTime dtmov;
  @Getter @Setter
  private LocalDateTime dtval;
  @Getter @Setter
  private Double        dare;
  @Getter
  private Double        avere;
  @Getter
  private String        descr;
  @Getter @Setter
  private String        caus;
  @Getter
  private String        cardid;
  @Getter
  private String        localCardIdent;

  public RigaBanca() {
    azzera();
  }

  public RigaBanca(LocalDateTime p_dtmov, LocalDateTime p_dtval, double p_dare, double p_avere, String p_descr, String p_caus,
      String p_cardid) {
    dtmov = p_dtmov;
    dtval = p_dtval;
    dare = p_dare;
    setAvere(p_avere);
    setDescr(p_descr);
    caus = p_caus;
    if (Utils.isValue(p_cardid))
      setCardid(p_cardid);
  }

  @Override
  public String toString() {
    String sz1 = null == dtmov ? "*null*" : ParseData.formatDate(dtmov);
    String sz2 = null == dtval ? "*null*" : ParseData.formatDate(dtval);
    return sz1 + "\t" + sz2 + "\t" + dare + "\t" + avere + "\t" + descr + "\t" + caus + "\t" + cardid + "\\n";
  }

  public void setAvere(double vv) {
    avere = vv;
    if (vv < 0)
      System.out.println("RigaBanca.setAvere()");
  }

  public void setDescr(String p_des) {
    descr = p_des;
    localCardIdent = DataController.getInst().getAssocid().findAssoc(descr);
    if (null == cardid && localCardIdent != null)
      setCardid(localCardIdent);
  }

  public void setCardid(String p_sz) {
    cardid = p_sz;
  }

  public void azzera() {
    rigaid = null;
    dtmov = null;
    dtval = null;
    dare = 0.;
    avere = .0;
    descr = null;
    caus = null;
    cardid = null;
  }

  public boolean isValido() {
    if ( !Utils.isValue(rigaid) || !Utils.isValue(dtmov) || !Utils.isValue(dtval))
      return false;
    if (null == dare || null == avere || dare == 0 && avere == 0)
      return false;
    if ( !Utils.isValue(descr) || !Utils.isValue(caus))
      return false;
    return true;
  }

  /**
   * Creo un id univoco per dtmov + dare + avere
   *
   * @return
   */
  public String getIdSet() {
    StringBuilder szRet = new StringBuilder().append(ParseData.formatDate(dtmov));
    szRet.append("_").append(Utils.formatDouble(dare));
    szRet.append("_").append(Utils.formatDouble(avere));
    return szRet.toString();
  }

  public void suply(int i) {
    LocalDateTime ldt = dtmov.plusSeconds(i);
    dtmov = ldt;
  }
}
