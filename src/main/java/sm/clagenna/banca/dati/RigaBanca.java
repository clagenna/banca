package sm.clagenna.banca.dati;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class RigaBanca {

  @Getter @Setter
  private Integer       rigaid;
  @Getter @Setter
  private String        tiporec;
  @Getter @Setter
  private Integer       idfile;
  @Getter
  private LocalDateTime dtmov;
  // Getter
  private LocalDateTime dtval;
  @Getter
  private String        movstr;
  @Getter @Setter
  private String        valstr;
  @Getter @Setter
  private Double        dare;
  @Getter
  private Double        avere;
  @Getter
  private String        descr;
  @Getter @Setter
  private String        abicaus;
  @Getter @Setter
  private String        descrcaus;
  @Getter @Setter
  private Integer       costo;
  @Getter
  private String        cardid;
  @Getter @Setter
  private String        codstat;
  @Getter @Setter
  private String        cdsdescr;
  @Getter
  private String        localCardIdent;

  public RigaBanca() {
    azzera();
  }

  public RigaBanca(String p_tipo) {
    azzera();
    setTiporec(p_tipo);
  }

  public RigaBanca(String p_tipo, LocalDateTime p_dtmov, LocalDateTime p_dtval, double p_dare, double p_avere, String p_descr,
      String p_caus, String p_cardid, String p_codstat) {
    tiporec = p_tipo;
    dtmov = p_dtmov;
    dtval = p_dtval;
    dare = p_dare;
    setAvere(p_avere);
    setDescr(p_descr);
    abicaus = p_caus;
    if (Utils.isValue(p_cardid))
      setCardid(p_cardid);
    codstat = p_codstat;
  }

  @Override
  public String toString() {
    String sz1 = null == dtmov ? "*null*" : ParseData.formatDate(dtmov);
    String sz2 = null == dtval ? "*null*" : ParseData.formatDate(dtval);
    return tiporec + "\t" + sz1 + "\t" + sz2 + "\t" + dare + "\t" + avere + "\t" + descr + "\t" + abicaus + "\t" + cardid + "\t"
        + codstat + "\\n";
  }

  public void setDtmov(LocalDateTime dt) {
    dtmov = dt;
    if (null != dt)
      movstr = ParseData.s_fmtPY4M.format(dt);
  }

  public void setDtval(LocalDateTime dt) {
    if (null != dt)
      dtval = dt;
    else
      dtval = dtmov;
    if (null != dt)
      valstr = ParseData.s_fmtPY4M.format(dt);
  }

  public LocalDateTime getDtval() {
    if (null != dtval)
      return dtval;
    return dtmov;

  }

  public void setAvere(double vv) {
    avere = vv;
    if (vv < 0)
      System.out.println("RigaBanca.setAvere()");
  }

  public void setDescr(String p_des) {
    descr = p_des;
    if (null != DataController.getInst())
      localCardIdent = DataController.getInst().getAssocid().findAssoc(descr);
    if (null == cardid && localCardIdent != null)
      setCardid(localCardIdent);
  }

  public void setCardid(String p_sz) {
    cardid = p_sz;
  }

  public void azzera() {
    tiporec = null;
    rigaid = null;
    idfile = null;
    dtmov = null;
    dtval = null;
    dare = 0.;
    avere = .0;
    descr = null;
    abicaus = null;
    cardid = null;
    codstat = null;
    cdsdescr = null;
  }

  public boolean isValido() {
    // FIXIT Perche' per validita testo il RigaId ?
    if ( !Utils.isValue(rigaid) || !Utils.isValue(dtmov) || !Utils.isValue(dtval))
      return false;
    if (null == dare || null == avere || dare == 0 && avere == 0)
      return false;
    if ( !Utils.isValue(descr) || !Utils.isValue(abicaus))
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

  public static RigaBanca parse(List<Object> elem) {
    if (null == elem)
      return null;
    int k = 0;
    RigaBanca rb = new RigaBanca();
    for (Object col : elem) {
      EColsTableView nome = EColsTableView.colName(k++);
      if ( !Utils.isValue(col))
        continue;
      switch (nome) {
        case id:
          rb.setRigaid(Integer.decode(col.toString()));
          break;
        case tipo:
          rb.setTiporec(col.toString());
          break;
        case idfile:
          rb.setIdfile(Integer.decode(col.toString()));
          break;
        case dtmov:
          rb.setDtmov(ParseData.parseData(col.toString()));
          break;
        case dtval:
          rb.setDtval(ParseData.parseData(col.toString()));
          break;
        case movstr:
          // settato da setDtmov()
          break;
        case valstr:
          // settato da setDtval()
          break;
        case dare:
          rb.setDare(Utils.parseDouble(col.toString()));
          break;
        case avere:
          rb.setAvere(Utils.parseDouble(col.toString()));
          break;
        case cardid:
          rb.setCardid(col.toString());
          break;
        case descr:
          rb.setDescr(col.toString());
          break;
        case abicaus:
          rb.setAbicaus(col.toString());
          break;
        case descrcaus:
          rb.setDescrcaus(col.toString());
          break;
        case costo:
          rb.setCosto(Integer.decode(col.toString()));
          break;
        case codstat:
          rb.setCodstat(col.toString());
          break;
        default:
          break;

      }
    }
    return rb;
  }
}
