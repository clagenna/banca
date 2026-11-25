package sm.clagenna.banca.dati.strt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import lombok.Data;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class StartOptions implements IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(StartOptions.class);

  public static final String START_OPTZ_FILE    = "StartOpts.properties";
  public static final String PROP_STRTOPTZ_CURR = "curr.opt";
  public static final String PROP_STRTOPTZ_NEXT = "opt.%02d";

  private AppProperties strtProps;
  private boolean       overwrite;
  private boolean       changed;
  private StrtOpt       currStrtOptz;
  private List<StrtOpt> lsStrtOptz;

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
    lsStrtOptz = new ArrayList<>();
    String szCurr = strtProps.getProperty(PROP_STRTOPTZ_CURR);
    currStrtOptz = new StrtOpt(szCurr);
    int nErr = 0;
    for (int i = 0; i < 100; i++) {
      String sKey = String.format(PROP_STRTOPTZ_NEXT, i);
      String sOpt = strtProps.getProperty(sKey);
      if (sOpt == null) {
        nErr++;
        if (nErr > 5)
          break;
      } else {
        StrtOpt optz = new StrtOpt(sOpt);
        if ( !optz.isError())
          lsStrtOptz.add(new StrtOpt(sOpt));
        else
          s_log.error("initApp() opzione di avvio {} non valida: {}", sOpt, optz.getMessage());
      }
    }
    changed = false;

  }

  private void existStartOptzFile() {
    if (Files.exists(Paths.get(START_OPTZ_FILE)))
      return;
    try {
      AppProperties lprops = new AppProperties();
      lprops.setProperty(PROP_STRTOPTZ_CURR, "Banca");
      lprops.setProperty(String.format(PROP_STRTOPTZ_NEXT, 1), "Banca");
      lprops.salvaSuProperties();
    } catch (Exception e) {
      s_log.error("Errore in creazione Start Optz File: {}", e.getMessage(), e);
    }
  }

  public StrtOpt get(String szOptz) {
    StrtOpt strt = null;
    if ( !Utils.isValue(szOptz))
      return strt;
    for (StrtOpt s : lsStrtOptz) {
      if (s.getName().equals(szOptz)) {
        strt = s;
        break;
      }
    }
    return strt;
  }

  public boolean contains(StrtOpt strt) {
    if (null == strt)
      return false;
    for (StrtOpt s : lsStrtOptz) {
      if (s.equals(strt))
        return true;
    }
    return false;
  }

  public void setCurrStrtOptz(StrtOpt ptz) {
    if (null != ptz && lsStrtOptz.contains(ptz)) {
      this.currStrtOptz = ptz;
      changed = true;
      strtProps.setProperty(PROP_STRTOPTZ_CURR, ptz.getName());
      strtProps.salvaSuProperties();
    }
  }

  public void addCurrStrtOptz(StrtOpt ptz) {
    if ( !lsStrtOptz.contains(ptz)) {
      lsStrtOptz.add(ptz);
      changed = true;
    }
  }

  public StrtOpt duplica(String szNewOptz) {
    StrtOpt newStrt = null;
    if ( !Utils.isValue(szNewOptz) || szNewOptz.length() < 4) {
      String szMsg = "duplicaOptz() nome opzione di avvio non valido (min 4 chars)!";
      s_log.warn(szMsg);
      return newStrt;
    }
    StrtOpt curr = getCurrStrtOptz();
    if (null == curr || curr.isError()) {
      s_log.warn("duplicaOptz() opzione di avvio corrente non valida!");
      return newStrt;
    }
    newStrt = new StrtOpt(szNewOptz);
    newStrt.setOverwrite(isOverwrite());
    if (contains(newStrt)) {
      s_log.warn("duplicaOptz() opzione di avvio {} esistente!", szNewOptz);
      return newStrt;
    }
    if ( !newStrt.duplica(curr, szNewOptz)) {
      s_log.warn("duplicaOptz() errore duplicazione {}: {}", szNewOptz, curr.getMessage());
      return newStrt;
    }
    // dopo la copia dei files
    lsStrtOptz.add(newStrt);
    strtProps.setProperty(String.format(PROP_STRTOPTZ_NEXT, lsStrtOptz.size()), newStrt.getName());
    strtProps.salvaSuProperties();
    changed = true;
    return newStrt;
  }

  public boolean delete(StrtOpt opt) {
    if (null == opt || !lsStrtOptz.contains(opt)) {
      s_log.warn("delete() opzione di avvio {} non esistente!", null != opt ? opt.getName() : "null");
      return false;
    }
    if (opt.equals(currStrtOptz)) {
      s_log.warn("delete() opzione di avvio {} corrente non puo' essere cancellata! devi cambiarla prima", opt.getName());
      return false;
    }
    if ( !opt.delete()) {
      s_log.warn("delete() errore cancellazione {}: {}", opt.getName(), opt.getMessage());
      return false;
    }
    lsStrtOptz.remove(opt);
    saveAllOptz();

    changed = true;
    return true;
  }

  private void saveAllOptz() {
    deleteAllOptz();
    for (int i = 0; i < lsStrtOptz.size(); i++) {
      StrtOpt strt = lsStrtOptz.get(i);
      String sKey = String.format(PROP_STRTOPTZ_NEXT, i);
      strtProps.setProperty(sKey, strt.getName());
    }
    strtProps.salvaSuProperties();
  }

  private void deleteAllOptz() {
    Properties props = strtProps.getProperties();
    int nErr = 0;
    for (int i = 0; i < lsStrtOptz.size() && nErr < 5; i++) {
      String sKey = String.format(PROP_STRTOPTZ_NEXT, i);
      if (props.containsKey(sKey))
        props.remove(sKey);
      else
        nErr++;
    }
  }

  public int getLastIndex() {
    int nLast = lsStrtOptz != null ? lsStrtOptz.size() : -1;
    for (StrtOpt s : lsStrtOptz) {
      String szName = s.getName();
      if (szName.length() < 4)
        continue;
      String szIndex = szName.substring(szName.length() - 2);
      try {
        int nIndex = Integer.parseInt(szIndex);
        if (nIndex > nLast)
          nLast = nIndex;
      } catch (Exception e) {
        s_log.warn("getLastIndex() errore conversione {}: {}", szIndex, e.getMessage());
      }
    }
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
    strtProps.setProperty(PROP_STRTOPTZ_CURR, currStrtOptz.getName());
    int k = 0;
    for (StrtOpt s : lsStrtOptz) {
      String sKey = String.format(PROP_STRTOPTZ_NEXT, k++);
      strtProps.setProperty(sKey, s.getName());
    }
    strtProps.salvaSuProperties();
    s_log.debug("Salvato opzioni di partenza su {}", START_OPTZ_FILE);
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

}
