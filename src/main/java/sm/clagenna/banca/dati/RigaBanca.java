package sm.clagenna.banca.dati;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import sm.clagenna.banca.javafx.EColsTableView;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

/**
 * Immagine del record di tabella DB "movimenti"
 */
@Data
public class RigaBanca {
  private static final Logger s_log = LogManager.getLogger(RigaBanca.class);

  private Integer       rigaid;
  private String        tiporec;
  private Integer       idfile;
  private LocalDateTime dtmov;
  private LocalDateTime dtval;
  private String        movstr;
  private String        valstr;
  private Double        dare;
  private Double        avere;
  private String        descr;
  private String        abicaus;
  private String        descrcaus;
  private Integer       costo;
  private String        cardid;
  private String        codstat;
  private Integer       idcodstat;
  private String        cdsdescr;
  private String        localCardIdent;

  private TreeitemCodStat treeCodstat;

  public RigaBanca() {
    azzera();
    treeCodstat = DataModel.getInst().getCodStatData();
  }

  public RigaBanca(String p_tipo) {
    azzera();
    setTiporec(p_tipo);
    treeCodstat = DataModel.getInst().getCodStatData();
  }

  public RigaBanca( //
      String p_tipo, //
      LocalDateTime p_dtmov, //
      LocalDateTime p_dtval, //
      double p_dare, //
      double p_avere, //
      String p_descr, //
      String p_caus, //
      String p_cardid, //
      String p_codstat) {
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
    treeCodstat = DataModel.getInst().getCodStatData();
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
    DataModel model = DataModel.getInst();
    localCardIdent = model.getAssocid().findAssoc(descr);
    if (null == cardid && localCardIdent != null)
      setCardid(localCardIdent);
  }

  public void setCodstat(String cds) {
    codstat = cds;
    idcodstat = null;
    if (null != codstat) {
      CodStat rec = treeCodstat.find(cds);
      if (null != rec)
        idcodstat = rec.getIdCodStat();
      else
        s_log.warn("Non trovo CodStat per cod={}", cds);
    }
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
    idcodstat = null;
    cdsdescr = null;
  }

  public boolean isValido() {
    // FIXato Perche' per validita testo il RigaId ?
    if ( /* !Utils.isValue(rigaid) || */ !Utils.isValue(dtmov) || !Utils.isValue(dtval))
      return false;
    if (null == dare || null == avere || // 
        (dare == 0 && avere == 0))
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

  /**
   * Popola un record RigaBanca partendo da un ResultSet. Le colonne del
   * ResultSet devono avere lo stesso nome delle colonne della vista del DB
   * "listaMovimenti"
   *
   * @param rs
   *          il ResultSet da cui leggere i valori
   * @param dbconn
   *          la connessione al DB per leggere i valori
   * @return un record RigaBanca popolato
   */
  public static RigaBanca popolaDaResultSet(ResultSet rs, DBConn dbconn) {
    RigaBanca rb = new RigaBanca();
    try {
      rb.setRigaid(rs.getInt(Consts.COL_MOV_ID));
      rb.setTiporec(rs.getString(Consts.COL_MOV_Tipo));
      rb.setIdfile(rs.getInt(Consts.COL_MOV_Idfile));
      rb.setDtmov(dbconn.getStmtDatetime(rs, Consts.COL_MOV_DtMov));
      rb.setDtval(dbconn.getStmtDatetime(rs, Consts.COL_MOV_DtVal));
      rb.setDare(dbconn.getStmtDouble(rs, Consts.COL_MOV_Dare));
      rb.setAvere(dbconn.getStmtDouble(rs, Consts.COL_MOV_Avere));
      rb.setDescr(rs.getString(Consts.COL_MOV_Descr));
      rb.setAbicaus(rs.getString(Consts.COL_MOV_Abicaus));
      rb.setDescrcaus(rs.getString(Consts.COL_MOV_DescrCaus));
      rb.setCosto(rs.getInt(Consts.COL_MOV_Costo));
      rb.setCardid(rs.getString(Consts.COL_MOV_CardId));
      rb.setCodstat(rs.getString(Consts.COL_MOV_CodStat));
      rb.setIdcodstat(rs.getInt(Consts.COL_MOV_IdCodStat));
    } catch (Exception ex) {
      s_log.error("Errore popola RigaBanca, err={}", ex.getMessage(), ex);
    }
    return rb;
  }

  /**
   * Popola un record RigaBanca partendo da un List<Object>. L'ordine degli
   * Object deve corrispondere all'ordine delle colonne.
   *
   * @param elem
   *          i valori delle colonne sotto forma di List<Object>
   * @return un record RigaBanca popolato
   */
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
        case idcodstat:
          rb.setIdcodstat(Integer.decode(col.toString()));
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String sz1 = null == dtmov ? "*null*" : ParseData.formatDate(dtmov);
    String sz2 = null == dtval ? "*null*" : ParseData.formatDate(dtval);

    sb.append(String.format("RigaBanca - %s \trigaId=%s", tiporec, null != rigaid ? rigaid.toString() : "-"));
    sb.append(String.format("\n\tidFil  %s", idfile));
    sb.append(String.format("\n\tdtMov  %s", sz1));
    sb.append(String.format("\n\tdtVal  %s", sz2));
    sb.append(String.format("\n\tdare   %s", Utils.parseDouble(dare)));
    sb.append(String.format("\n\tavere  %s", Utils.parseDouble(avere)));
    sb.append(String.format("\n\tdescr  %s", descr));
    sb.append(String.format("\n\tAbicas %s - %s", abicaus, descrcaus));
    sb.append(String.format("\n\tcosto  %s", null != costo && costo.intValue() != 0 ? "Si" : ""));
    sb.append(String.format("\n\tCardid %s", cardid));
    sb.append(String.format("\n\tCodStt (%d)%s - %s", idcodstat, codstat, cdsdescr));

    //    return tiporec + "\t" + sz1 + "\t" + sz2 + "\t" + dare + "\t" + avere + "\t" + descr + "\t" + abicaus + "\t" + cardid + "\t"
    //        + codstat + "\\n";
    return sb.toString();
  }

  public String toStringShort() {
    StringBuilder sb = new StringBuilder();
    String sz1 = null == dtmov ? "*null*" : ParseData.formatDate(dtmov);
    // String sz2 = null == dtval ? "*null*" : ParseData.formatDate(dtval);

    sb.append(String.format("RigaBanca - %s \trigaId=%s", tiporec, null != rigaid ? rigaid.toString() : "-"));
    // sb.append(String.format("\n\tidFil  %s", idfile));
    sb.append(String.format("\n\tdtMov  %s", sz1));
    // sb.append(String.format("\n\tdtVal  %s", sz2));
    sb.append(String.format("\n\tdare   %s", Utils.parseDouble(dare)));
    sb.append(String.format("\n\tavere  %s", Utils.parseDouble(avere)));
    sb.append(String.format("\n\tdescr  %s", descr));
    // sb.append(String.format("\n\tAbicas %s - %s", abicaus, descrcaus));
    // sb.append(String.format("\n\tcosto  %s", null != costo && costo.intValue() != 0 ? "Si" : ""));
    sb.append(String.format("\n\tCardid %s", cardid));
    sb.append(String.format("\n\tCodStt (%d)%s - %s", idcodstat, codstat, cdsdescr));

    //    return tiporec + "\t" + sz1 + "\t" + sz2 + "\t" + dare + "\t" + avere + "\t" + descr + "\t" + abicaus + "\t" + cardid + "\t"
    //        + codstat + "\\n";
    return sb.toString();
  }
}
