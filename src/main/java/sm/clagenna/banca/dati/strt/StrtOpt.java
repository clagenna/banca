package sm.clagenna.banca.dati.strt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import sm.clagenna.stdcla.sql.EServerId;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;
import sm.clagenna.stdcla.utils.sys.ex.AppPropsException;

// FIXME replace underscores to blanks and vice versa in the name of the StrtOpt, so that the name is always a valid file name
@Data
public class StrtOpt {
  private static final Logger s_log = LogManager.getLogger(StrtOpt.class);

  private String        name;
  private Path          propFile;
  private Path          dbFile;
  private String        message;
  private boolean       changed;
  private boolean       error;
  private boolean       overwrite;
  private AppProperties props;
  private EServerId     dbtip;
  // private String propFileName;
  
  public StrtOpt() {
    changed = false;
    error = false;
  }

  public StrtOpt(String ptz) {
    setName(ptz);
    changed = false;
    error = false;
    lookupPropFile();
  }

  public boolean lookupPropFile() {
    try {
      Path currd = Paths.get("");
      String szPropFile = Utils.capitalize(name.replaceAll(" ", "_"));
      propFile = currd.resolve(szPropFile + ".properties");
      if ( !Files.exists(propFile)) {
        message = String.format("File properties X Banca app, \"%s\" non trovato!", propFile);
        s_log.warn(message);
        error = true;
      }
    } catch (Exception e) {
      s_log.error("Errore in lookupPropFile(): {}", e.getMessage(), e);
      error = true;
    }
    if ( !error)
      openPropertyFile();
    return error;
  }

  public void setName(String newName) {
    if ( !Utils.isValue(newName)) {
      throw new IllegalArgumentException("StrtOpt.setName() newName is null or empty");
    }
    // come nome tolgo gli eventuali underscore dal nome, 
    // perche' il nome e' anche il nome del file di properties e non puo' contenere caratteri speciali
    name = newName.replaceAll("_", " ");
  }

  /**
   * Copia la situazione attuale da una {@link StrtOpt} con un nuovo nome. Vige
   * la regola del <b>overwrite</b> per cui se esiste una qualsiasi file di
   * destinazione il processo si ferma con errore. Altrimenti vengono compiute
   * le seguenti azioni:
   * <ol>
   * <li>Copia il file di properties con un nuovo nome</li>
   * <li>Poi copia il file del database con il nuovo nome(le regole per
   * <b>overwrite</b> sono le stesse)</li>
   * <li>Salva il nome della copia di DataBase sul nuovo file di properties</li>
   * </ol
   *
   * @param newName
   * @return true se la duplicazione ha funzionato, false se c'e' stato un
   *         errore
   */
  public boolean duplica(StrtOpt from, String newName) {
    clearError();
    setName(newName);
    // non duplico se il nome e' uguale a quello esistente
    if (Utils.isValueEq(from.name, newName)) {
      message = String.format("Nome duplicato uguale e/o esiste: %s!", newName);
      s_log.warn(message);
      error = true;
    }
    // duplico il file di properties
    try {
      if ( !error) {
        Path currdir = Paths.get("");
        String szPropFileName = Utils.capitalize(name.replaceAll(" ", "_") + ".properties");
        propFile = currdir.resolve(szPropFileName);
        if (Files.exists(propFile) && !overwrite) {
          message = String.format("File properties %s gia' esistente!", propFile.toAbsolutePath().toString());
          s_log.warn(message);
          error = true;
        }
      }
      if ( !error) {
        StandardCopyOption[] options = //
            overwrite ? new StandardCopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new StandardCopyOption[] {};
        Files.copy(from.propFile, propFile, options);
        openPropertyFile();
        copyDataBase(from);
        cambiaStartDir(from);
      }
    } catch (Exception e) {
      error = true;
      message = e.getMessage();
      s_log.error("Errore in dupl(): {}", message, e);
    }
    return !error;
  }

  private void cambiaStartDir(StrtOpt from) {
    String szStrtDir = from.props.getProperty(AppProperties.CSZ_PROP_LASTDIR);
    if ( !Utils.isValue(szStrtDir))
      return;
    Path pthStrt = Paths.get(szStrtDir);
    Path pthPadre = pthStrt.getParent();
    Path pthNew = pthPadre.resolve(name.replaceAll(" ", "_"));
    if ( !Files.exists(pthNew)) {
      try {
        Files.createDirectory(pthNew);
      } catch (Exception e) {
        error = true;
        message = e.getMessage();
        s_log.error("Errore in cambiaStartDir(): {}", message, e);
        return;
      }
    }
    props.setProperty(AppProperties.CSZ_PROP_LASTDIR, pthNew.toAbsolutePath().toString());
    s_log.debug("cambiaStartDir() setProperty({})={}", AppProperties.CSZ_PROP_LASTDIR, pthNew.toAbsolutePath().toString());
    props.salvaSuProperties();

  }

  private void openPropertyFile() {
    props = null;
    try {
      props = new AppProperties();
      s_log.debug("openPropertyFile({}) propFile={}", name, propFile.toAbsolutePath().toString());
      props.leggiPropertyFile(propFile);
    } catch (AppPropsException e) {
      error = true;
      message = e.getMessage();
      s_log.error("Errore in openPropertyFile(): {}", message, e);
      return;
    }
    String szDbTyp = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    String szDbName = props.getProperty(AppProperties.CSZ_PROP_DB_name);
    dbtip = EServerId.parse(szDbTyp);
    dbFile = Paths.get(szDbName); // c'e' sempre il full path nel file di properties, quindi non serve currd.resolve()
    s_log.debug("openPropertyFile({}) dbTyp={} dbName={}", propFile, szDbTyp, szDbName);
  }

  /**
   * Copia il file del <b>database</b> con il nuovo nome, se esiste gia' il file
   * e <code>overwrite=false</code> ritorna <b>errore</b> altrimanti sovrascrive
   * il file esistente. <br>
   *
   * @param from
   * @return errore se il file esiste gia' e overwrite=false oppure se il file
   *         non esiste
   */
  private void copyDataBase(StrtOpt from) {
    String szDbTyp = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    String szDbFromName = props.getProperty(AppProperties.CSZ_PROP_DB_name);
    dbtip = EServerId.parse(szDbTyp);

    switch (dbtip) {

      case SQLite:
      case SQLite3:
        props.setProperty(AppProperties.CSZ_PROP_DB_Type, EServerId.SQLite3.toString());
        props.setProperty(AppProperties.CSZ_PROP_DB_Host, "localhost");
        props.setProperty(AppProperties.CSZ_PROP_DB_service, "-1");
        props.setProperty(AppProperties.CSZ_PROP_DB_user, "*none*");
        props.setProperty(AppProperties.CSZ_PROP_DB_passwd, "*none*");
        break;
      default:
        error = true;
        message = String.format("copyDataBase(\"%s\") non implementato per DB tipo %s", szDbFromName, dbtip);
        s_log.error(message);
        return;
    }
    Path dbFromFile = Paths.get(szDbFromName); // c'e' sempre il full path nel file di properties, quindi non serve currd.resolve()
    Path dbFromParent = dbFromFile.getParent();
    String szDbToFileName = Utils.capitalize(name.replaceAll(" ", "_") + ".db");
    dbFile = dbFromParent.resolve(szDbToFileName);
    message = String.format("copyDataBase(\"%s\") to new DB=%s", //
        dbFromFile.toAbsolutePath().toString(), //
        dbFile.toAbsolutePath().toString());
    s_log.debug(message);
    try {
      if (Files.exists(dbFile) && !overwrite) {
        error = true;
        message = String.format("copyDataBase() file di destinazione %s esiste!", dbFile.toAbsolutePath().toString());
        s_log.error(message);
        return;
      }
      StandardCopyOption[] options = //
          overwrite ? new StandardCopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new StandardCopyOption[] {};
      Files.copy(from.dbFile, dbFile, options);
      s_log.debug("copyDataBase() copiato {} -> {}", //
          dbFromFile.toAbsolutePath().toString(), dbFile.toAbsolutePath().toString());
    } catch (Exception e) {
      error = true;
      message = e.getMessage();
      s_log.error("Errore in copyDataBase(): {}", message, e);
      return;
    }
    props.setProperty(AppProperties.CSZ_PROP_DB_name, dbFile.toAbsolutePath().toString());
    s_log.debug("copyDataBase() setProperty({})={}", AppProperties.CSZ_PROP_DB_name, dbFile.toAbsolutePath().toString());
    props.salvaSuProperties();
  }

  /**
   * Cancella il file di properties, se non esiste ritorna <b>errore</b>
   *
   * @return true se il file e' stato cancellato, false se c'e' stato un errore
   */
  public boolean delete() {
    clearError();
    try {
      if ( !Files.exists(propFile)) {
        error = true;
        message = String.format("File da cancellare %s non esiste!", propFile);
        s_log.warn(message);
        return error;
      }
      Files.delete(propFile);
      propFile = null;
    } catch (Exception e) {
      error = true;
      message = e.getMessage();
      s_log.error("Errore in delete(): {}", message, e);
    }
    return !error;
  }

  private void clearError() {
    error = false;
    message = null;
  }

  @Override
  public String toString() {
    return name;
  }

  public String toStringEx() {
    return String.format("StrtOpt(%s) propFile=%s dbFile=%s", name, propFile, dbFile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other)
      return true;
    if (other instanceof StrtOpt quello)
      return Utils.isValueEqIC(name, quello.name) && Objects.equals(this.name, quello.name);
    return false;
  }
}
