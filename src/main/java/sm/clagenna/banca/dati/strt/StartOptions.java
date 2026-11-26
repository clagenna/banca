package sm.clagenna.banca.dati.strt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import lombok.Data;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;
import sm.clagenna.stdcla.utils.sys.ex.AppPropsException;

@Data
public class StartOptions implements IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(StartOptions.class);

  public static final String START_OPTZ_FILE    = "StartOpts.properties";
  public static final String PROP_STRTOPTZ_CURR = "curr.opt";
  public static final String PROP_STRTOPTZ_NEXT = "opt.%02d";

  private AppProperties        strtProps;
  private String               message;
  private boolean              overwrite;
  private boolean              changed;
  private StrtOpt              currStrtOptz;
  private Map<String, StrtOpt> mapStrtOptz;
  private List<String>         lsStrtOptz2Delete;

  public StartOptions() {
    //
  }

  @Override
  public void initApp(AppProperties p_props) {
    existStartOptzFile();
    try {
      strtProps = new AppProperties();
      strtProps.leggiPropertyFile(START_OPTZ_FILE);
    } catch (Exception e) {
      s_log.error("Errore in startOption initApp.openProperties(): {}", e.getMessage(), e);
      Platform.exit();
      System.exit(1957);
    }
    mapStrtOptz = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    lsStrtOptz2Delete = new ArrayList<>();
    int nErr = 0;
    for (int i = 0; i < 100 && nErr++ < 5; i++) {
      String sKey = String.format(PROP_STRTOPTZ_NEXT, i);
      String sOpt = strtProps.getProperty(sKey);
      if (sOpt == null)
        continue;
      sOpt = sOpt.trim().replaceAll("_", " ").replaceAll("  ", " ").trim();
      if (mapStrtOptz.containsKey(sOpt)) {
        s_log.warn("initApp() opzione di avvio {} duplicata!", sOpt);
        continue;
      }
      StrtOpt optz = new StrtOpt(sOpt);
      if ( !optz.isError()) {
        mapStrtOptz.put(sOpt, optz);
      } else {
        s_log.error("initApp() opzione di avvio {} non valida: {}", sOpt, optz.getMessage());
        lsStrtOptz2Delete.add(sOpt);
      }
    }
    String szCurr = strtProps.getProperty(PROP_STRTOPTZ_CURR).replaceAll("_", " ");
    if (mapStrtOptz.containsKey(szCurr)) {
      currStrtOptz = mapStrtOptz.get(szCurr);
    } else {
      message = String.format("L' opzione di avvio corrente %s non e' valida!", szCurr);
      s_log.warn(message);
    }
    if (lsStrtOptz2Delete.size() > 0) {
      s_log.warn("initApp() opzioni di avvio non valide da cancellare: {}", lsStrtOptz2Delete.toString());
      deleteInvalidOptz();
    }
    changed = false;
  }

  private void existStartOptzFile() {
    // 1) se esiste il file di properties, sono contento ed esco 
    if (Files.exists(Paths.get(START_OPTZ_FILE)))
      return;
    // 2) se non esiste, verifico presenza props di default (Banca)
    Path currd = Paths.get(".");
    Path propFile = currd.resolve("Banca.properties");
    // 3) se non esiste, sono nei GUAI!! 
    if ( !Files.exists(propFile)) {
      message = String.format("File properties X Banca app, \"%s\" non trovato!", propFile);
      s_log.error(message);
      return;
    }
    // 4) creo il file "StartOpts.properties" con l'opzione di default (Banca
    try {
      AppProperties lprops = new AppProperties();
      lprops.setProperty(PROP_STRTOPTZ_CURR, "Banca");
      lprops.setProperty(String.format(PROP_STRTOPTZ_NEXT, 1), "Banca");
      lprops.salvaSuProperties();
    } catch (Exception e) {
      s_log.error("Errore in creazione Start Optz File: {}", e.getMessage(), e);
    }
  }

  private void deleteInvalidOptz() {
    Properties prp = strtProps.getProperties();
    for (String szDel : lsStrtOptz2Delete) {
      prp.remove(szDel);
      s_log.info("deleteInvalidOptz() cancellata opzione di avvio non valida: {}", szDel);
    }
    lsStrtOptz2Delete.clear();
    saveAllOptz();
  }

  public StrtOpt get(String szOptz) {
    StrtOpt strt = null;
    if ( !Utils.isValue(szOptz))
      return strt;
    strt = mapStrtOptz.get(szOptz);
    return strt;
  }

  public boolean contains(StrtOpt strt) {
    if (null == strt || mapStrtOptz == null || !Utils.isValue(strt.getName()))
      return false;
    return mapStrtOptz.containsKey(strt.getName());
  }

  public void setCurrStrtOptz(StrtOpt ptz) {
    if (null == ptz || !Utils.isValue(ptz.getName()))
      return;
    if (mapStrtOptz.containsKey(ptz.getName())) {
      this.currStrtOptz = ptz;
      changed = true;
      strtProps.setProperty(PROP_STRTOPTZ_CURR, ptz.getName());
      strtProps.salvaSuProperties();
    } else {
      s_log.warn("Impostazione opzione di avvio {} non esistente!", ptz.getName());
    }
  }

  //  public void addCurrStrtOptz(StrtOpt ptz) {
  //    if ( !lsStrtOptz.contains(ptz)) {
  //      lsStrtOptz.add(ptz);
  //      changed = true;
  //    }
  //  }

  public StrtOpt duplica(String szNewOptz) {
    StrtOpt newStrt = null;
    // 1) controllo nome valido
    if ( !Utils.isValue(szNewOptz) || szNewOptz.length() < 4) {
      String szMsg = "duplicaOptz() nome opzione di avvio non valido (min 4 chars)!";
      s_log.warn(szMsg);
      return newStrt;
    }
    // 2) da dove copiare, se non c'e' la corrente non posso duplicare
    if (null == currStrtOptz || currStrtOptz.isError()) {
      s_log.warn("duplicaOptz() opzione di avvio corrente assente o in Errore!");
      return newStrt;
    }
    // 3) controllo che non esita gia' un opzione uguale 
    if (mapStrtOptz.containsKey(szNewOptz)) {
      s_log.warn("duplicaOptz() opzione di avvio {} e' gia esistente!", szNewOptz);
      return newStrt;
    }
    // 4) Ok! ho le condizioni giuste per duplicare
    newStrt = new StrtOpt();
    newStrt.setOverwrite(isOverwrite());
    if ( !newStrt.duplica(currStrtOptz, szNewOptz)) {
      s_log.warn("duplicaOptz() errore duplicazione {}: {}", szNewOptz, currStrtOptz.getMessage());
      return newStrt;
    }
    // 5) mi occupo del salvataggio in ultima posizione della nuova opzione di avvio
    mapStrtOptz.put(szNewOptz, newStrt);
    strtProps.setProperty(String.format(PROP_STRTOPTZ_NEXT, mapStrtOptz.size()-1), newStrt.getName());
    strtProps.salvaSuProperties();
    changed = true;
    return newStrt;
  }

  public boolean delete(StrtOpt opt) {
    // 1) controllo che l'opzione da cancellare esista
    if (null == opt || !mapStrtOptz.containsKey(opt.getName())) {
      message = String.format("delete() opzione di avvio %s non esistente!", null != opt ? opt.getName() : "null");
      s_log.warn(message);
      return false;
    }
    // 2) controllo che non sia l'opzione corrente
    if (opt.equals(currStrtOptz)) {
      message = String.format("delete() opzione di avvio %s corrente non puo' essere cancellata!", opt.getName());
      s_log.warn(message);
      return false;
    }
    // 3) cancello il file di properties e il file di DB
    if ( !opt.delete()) {
      s_log.warn("delete() errore cancellazione {}: {}", opt.getName(), opt.getMessage());
      return false;
    }
    // 4) rmuovo l'opzione dal registro e salvo le opzioni rimanenti
    mapStrtOptz.remove(opt.getName());
    saveAllOptz();
    changed = true;
    return true;
  }

  private void saveAllOptz() {
    deleteAllOptz();
    strtProps.setProperty(PROP_STRTOPTZ_CURR, currStrtOptz.getName());
    int k = 0;
    for (String sStr : mapStrtOptz.keySet()) {
      String sKey = String.format(PROP_STRTOPTZ_NEXT, k++);
      strtProps.setProperty(sKey, sStr);
    }
    strtProps.salvaSuProperties();
    changed = false;
    s_log.debug("Salvato opzioni di partenza su {}", strtProps.getPropertyFile().getAbsolutePath());
  }

  /**
   * cancella tutte le opzioni di avvio dal registro (file di properties), ma
   * non cancella i file di properties delle singole opzioni
   */
  private void deleteAllOptz() {
    // salvo il vero file di properties, perche' lo devo riaprire dopo aver cancellato tutte le opzioni
    File fiProps = strtProps.getPropertyFile();
    try {
      strtProps = new AppProperties();
      // nuovo registro vergine, senza opzioni di avvio
      strtProps.setPropertyFile(fiProps);
    } catch (AppPropsException e) {
      s_log.error("deleteAllOptz() errore reset properties: {}", e.getMessage());
      return;
    }
  }

  public int getLastIndex() {
    int nLast = mapStrtOptz != null ? mapStrtOptz.size() : -1;
    //    for (StrtOpt s : lsStrtOptz) {
    //      String szName = s.getName();
    //      if (szName.length() < 4)
    //        continue;
    //      String szIndex = szName.substring(szName.length() - 2);
    //      try {
    //        int nIndex = Integer.parseInt(szIndex);
    //        if (nIndex > nLast)
    //          nLast = nIndex;
    //      } catch (Exception e) {
    //        s_log.warn("getLastIndex() errore conversione {}: {}", szIndex, e.getMessage());
    //      }
    //    }
    return nLast;
  }

  @Override
  public void changeSkin() {
    // nothing to do

  }

  @Override
  public void closeApp(AppProperties p_props) {
    if ( !changed)
      return;
    saveAllOptz();
  }

  @Override
  public void forceClose() {
    closeApp(strtProps);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szEvtId = evt.getPropertyName();
    switch (szEvtId) {

    }
  }

  public List<StrtOpt> getListStrtOptz() {
    if (null == mapStrtOptz || mapStrtOptz.size() < 1)
      return null;
    return mapStrtOptz.values().stream().toList();
  }

}
