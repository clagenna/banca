package sm.clagenna.banca.dati;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.exceptions.CsvException;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.sql.SqlGest;
import sm.clagenna.banca.sql.SqlGestFactory;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.Utils;

public class TreeCodStat {
  private static final Logger s_log = LogManager.getLogger(TreeCodStat.class);
  // public static final String  FILE_CODSTAT = "CodStat.properties";
  private static final DecimalFormat fmt = new DecimalFormat("00,00");

  @Getter @Setter
  private Path                 fileCodStats;
  @Getter @Setter
  private CodStat              root;
  @Getter @Setter
  private String               codStat;
  private DataModel            model;
  @Getter
  private Map<String, CodStat> mapCodStat;
  private DBConn               dbConn;
  @Getter
  private SqlGest              sqlGest;

  public TreeCodStat() {
    init();
  }

  public TreeCodStat(CodStat p_no) {
    init();
    this.root = p_no;
  }

  private void init() {
    clear();
    model = DataModel.getInst();
    dbConn = model.getDbConn();
    sqlGest = (SqlGest) SqlGestFactory.get(dbConn.getServerId());
    sqlGest.setDbconn(dbConn);
  }

  public CodStat readTreeCodStats(Path pthCodStat) {
    clear();
    setFileCodStats(pthCodStat);
    try (Dataset dts = new Dataset()) {
      // dts.setCsvdelim(";");
      dts.setCsvBlankOnZero(true);

      int qta = -1;
      String szExt = Utils.getFileExtention(pthCodStat);
      switch (szExt) {
        case ".xls":
        case ".xlsx":
          dts.readexcel(pthCodStat);
          qta = dts.size();
          break;
        case ".csv":
          qta = dts.readcsv(pthCodStat).size();
          break;
      }
      s_log.debug("Rec da File CodStats \"{}\" letti {} recs", pthCodStat, qta);
      int qtaRecs = sqlGest.deleteAllCodStats();
      if (qtaRecs < 0) {
        s_log.error("Errore in cancellazione di tutti codici statistici!, esco dall'import!");
        return root;
      }
      // System.out.printf("QtaCols:%d\n%s\n", dts.getColumns().size(), dts.getColumns());
      // System.out.println(dts.toString());
      for (DtsRow row : dts.getRighe()) {
        CodStat cds = translate(row.get("cat1"), row.get("cat2"), row.get("cat3"));
        cds.setDescr((String) row.get("descrizione"));
        add(cds);
        // System.out.printf("%-10s\t%s\n", cds.getCodice(), cds.getDescr());
      }
      sqlGest.insAllCodStats(new ArrayList<CodStat>(mapCodStat.values()));
    } catch (IOException | CsvException e) {
      s_log.error("Errore lettura \"{}\", errore:{}", pthCodStat.toString(), e.getMessage(), e);
    }
    return root;
  }

  private CodStat translate(Object ob1, Object ob2, Object ob3) {
    CodStat cds = new CodStat();
    if (Utils.isValue(ob1)) {
      cds.assign((int) ob1, 0, 0);
      return cds;
    }
    if (Utils.isValue(ob2) && (Double) ob2 != 0) {
      Double dbl = (Double) ob2;
      String cat2 = fmt.format(dbl).replace('.', ',');
      String arr[] = cat2.split(",");
      cds.assign(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), 0);
      return cds;
    }
    String arr[] = ((String) ob3).split("\\.");
    cds.assign(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
    return cds;
  }

  public CodStat readTreeCodStats() {
    if (null != root)
      root.clear();
    mapCodStat = new TreeMap<String, CodStat>(String.CASE_INSENSITIVE_ORDER);
    List<CodStat> liCodStats = sqlGest.getListCodStat();
    for (CodStat cdst : liCodStats) {
      add(cdst);
    }
    s_log.debug("TreeCodStat: added {} nodes", liCodStats.size());
    return root;
  }

  public void add(CodStat p_cds) {
    // System.out.printf("\n---------- %s -----------\n", p_cds.getCodice());
    if (null == mapCodStat)
      mapCodStat = new TreeMap<String, CodStat>(String.CASE_INSENSITIVE_ORDER);
    CodStat start = root;
    for (int liv = 1; liv <= p_cds.getLivello() || liv <= 3; liv++) {
      CodStat trova = p_cds.getCodice(liv);
      CodStat trovato = start.find(trova);
      if (null == trovato) {
        start.add(trova);
        mapCodStat.put(trova.getCodice(), trova);
        if (liv == p_cds.getLivello())
          break;
      } else if (trovato.equals(p_cds)) {
        trovato.assign(p_cds);
        mapCodStat.put(trovato.getCodice(), trovato);
        trova = trovato;
        break;
      } else
        trova = trovato;
      start = trova;
    }
  }

  public void delete(CodStat cds) {
    CodStat lcd = find(cds.getCodice());
    if (null == lcd)
      lcd = find(cds.getIdCodStat());
    if (null != lcd) {
      CodStat padre = lcd.getFather();
      if (null != padre)
        padre.delete(lcd);
    }
  }

  public CodStat find(String string) {
    if (null == root || null == mapCodStat)
      return null;
    var cds = mapCodStat.get(string);
    return cds;
  }

  public CodStat find(int idCodStat) {
    if (null == root || null == mapCodStat)
      return null;
    List<CodStat> pv = mapCodStat.values().stream().filter(c -> c.getIdCodStat() == idCodStat).collect(Collectors.toList());
    if (null != pv && pv.size() > 0)
      return pv.get(0);
    return null;
  }

  public void clear() {
    if (null != mapCodStat)
      mapCodStat.clear();
    mapCodStat = null;
    root = new CodStat();
  }

  public void clearTotali() {
    if (null == root)
      return;
    root.clearTotali();
  }

  public List<CodStat> getList(String p_sz) {
    List<CodStat> li = null;
    if (null == root)
      return li;
    li = root.getList(p_sz);
    return li;
  }

  public void updateCodStat(CodStat cdsCurr, boolean bUpdDB) {
    if (null == cdsCurr)
      return;
    // codstats.setProperty(cdsCurr.getCodice(), cdsCurr.getDescr());
    if (sqlGest.existCodStat(cdsCurr))
      sqlGest.updadetCodStat(cdsCurr);
    else
      sqlGest.insertCodStat(cdsCurr);
  }

  @Deprecated
  public void saveAll() {
    //    codstats.salvaSuProperties();
    //    if (null != datac)
    //      datac.firePropertyChange(DataController.EVT_FILECODSTATS, "*null*", fileCodStats.toString());
    // s_log.info("Salvato il file Codici Statistici {}", codstats.getPropertyFile().toString());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (null == root)
      return "*null tree*";
    for (CodStat cds : root.toList()) {
      String tab = "   ".repeat(cds.getLivello());
      sb.append(tab).append(cds.toString()).append("\n");
    }
    return sb.toString();
  }

}
