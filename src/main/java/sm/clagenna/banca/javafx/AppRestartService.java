package sm.clagenna.banca.javafx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import sm.clagenna.stdcla.javafx.MessageDialog;

/**
 * Avvia una nuova istanza dell'app e chiude il processo corrente.
 */
public final class AppRestartService {
  private static final Logger s_log = LogManager.getLogger(AppRestartService.class);

  private AppRestartService() {
    // utility class
  }

  /**
   * Prova a rilanciare il processo corrente (exe o java) conservando gli
   * argomenti.
   *
   * @return true se il nuovo processo e' stato avviato
   */
  public static boolean restartCurrentApp() {
    List<String> cmd = buildCommandFromCurrentProcess();
    if (cmd == null || cmd.isEmpty()) {
      cmd = buildJavaFallbackCommand();
    }
    if (cmd == null || cmd.isEmpty()) {
      s_log.error("Impossibile costruire il comando di restart");
      return false;
    }

    try {
      ProcessBuilder pb = new ProcessBuilder(cmd);
      pb.directory(new File(System.getProperty("user.dir", ".")));
      pb.start();
      s_log.info("Restart avviato con comando: {}", String.join(" ", cmd));
    } catch (IOException ex) {
      s_log.error("Errore avvio restart", ex);
      return false;
    }

    Platform.runLater(Platform::exit);
    System.exit(0);
    return true;
  }

  private static List<String> buildCommandFromCurrentProcess() {
    ProcessHandle.Info info = ProcessHandle.current().info();
    Optional<String> command = info.command();
    if (command.isEmpty()) {
      return null;
    }

    List<String> cmd = new ArrayList<>();
    cmd.add(command.get());
    info.arguments().ifPresent(args -> cmd.addAll(Arrays.asList(args)));
    return cmd;
  }

  private static List<String> buildJavaFallbackCommand() {
    String javaHome = System.getProperty("java.home");
    String classPath = System.getProperty("java.class.path");
    if (javaHome == null || javaHome.isBlank() || classPath == null || classPath.isBlank()) {
      return null;
    }

    Path javaExe = Paths.get(javaHome, "bin", "java.exe");
    if ( !javaExe.toFile().exists()) {
      javaExe = Paths.get(javaHome, "bin", "java");
    }

    List<String> cmd = new ArrayList<>();
    cmd.add(javaExe.toString());
    cmd.add("-cp");
    cmd.add(classPath);
    cmd.add(LauncherBanca.class.getName());
    return cmd;
  }

  public static boolean restartApp() {
    boolean riavviata = false;
    String exe = null;
    try {
      exe = ProcessHandle.current().parent().flatMap(p -> p.info().command()).get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    s_log.info("RestartApp: parent process command: {}", exe);
    exe = "Banca.exe";
    MessageDialog.messageDialog(AlertType.WARNING,
        "Provo a rilanciare l'applicazione<br/>per assumere i nuovi parametri.<br/><b>Chiudi questa finestra</b>.");
    try {
      new ProcessBuilder("cmd", "/c", "timeout /t 2 /nobreak >nul && start \"\" " + exe + "\"")
          .directory(new File(exe).getParentFile()).start();
      riavviata = true;
      s_log.info("Restart avviato con comando: {}", exe);
    } catch (IOException e) {
      s_log.error("Errore avvio restart", e);
      e.printStackTrace();
    }
    return riavviata;
  }

  /**
   * Riavvia l'applicazione corrente con argomenti nuovi.
   *
   * @param nuoviArgs
   *          argomenti da passare alla nuova istanza
   */
  public static boolean riavvia(String... nuoviArgs) {
    boolean riavviata = false;
    File exe = null;
    //    Optional<String> cmd = null;
    //    try {
    //      cmd = ProcessHandle.current().info().command();
    //    } catch (Exception e) {
    //      s_log.error("Errore determinazione percorso eseguibile corrente", e);
    //    }
    //    if (null == cmd || cmd.isEmpty()) {
    //      s_log.error("Impossibile determinare il percorso dell'eseguibile corrente");
    //      return riavviata;
    //    }
    //    File exe = new File(cmd.get());
    exe = trovaEseguibile();
    if (null == exe || !exe.exists()) {
      String msg = "Impossibile determinare il percorso dell'eseguibile corrente";
      MessageDialog.messageDialog(AlertType.ERROR, msg //
          + "<br/>Controlla che il file <b>Banca.exe>/b> esista ..." //
          + "<br/>poi rilancia manualmente l'applicazione");
      s_log.error(msg);
      Platform.exit(); // chiude JavaFX in modo pulito
      System.exit(0); // garantisce la terminazione della JVM
      return riavviata;
    }
    s_log.info("Riavvio app corrente: {}", exe.getAbsolutePath());
    // cmd /c start "" /D <dir> "<exe>" args...
    List<String> comando = new ArrayList<>();
    Path currPath = Paths.get("");
    try {
      comando.add("cmd.exe");
      comando.add("/c");
      comando.add("start"); // DOS cmd
      comando.add("\"\""); // titolo vuoto
      comando.add("/D"); // stabilisce la directory di lavoro
      comando.add(currPath.toAbsolutePath().toString()); // directory di lavoro
      comando.add(exe.getAbsolutePath()); // eseguibile
      if (null != nuoviArgs && nuoviArgs.length > 0)
        comando.addAll(List.of(nuoviArgs)); // argomenti aggiuntivi
    } catch (Exception e) {
      s_log.error("Errore creazione comandi", e);
    }

    try {
      // Piccolo ritardo, così la vecchia istanza fa in tempo a chiudersi
      // (utile se usi un single-instance lock / file di lock)
      List<String> conAttesa = new ArrayList<>();
      conAttesa.add("cmd.exe");
      conAttesa.add("/c");
      conAttesa.add("ping 127.0.0.1 -n 2 >nul & " + String.join(" ", quote(comando.subList(2, comando.size()))));
      s_log.debug("Comando di restart: {}", String.join("\n\t", conAttesa));
      new ProcessBuilder(conAttesa).directory(exe.getParentFile()).start();
      s_log.info("Restart avviato: {}", exe);
      riavviata = true;
    } catch (Exception e) {
      s_log.error("Errore avvio restart", e);
      return riavviata;
    }

    Platform.exit(); // chiude JavaFX in modo pulito
    System.exit(0); // garantisce la terminazione della JVM
    return riavviata;
  }

  private static File trovaEseguibile() {
    Path currentRelativePath = Paths.get("");
    String s = currentRelativePath.toAbsolutePath().toString();
    String exeName = "target\\Banca.exe";
    File exe = new File(s, exeName);
    if (exe.exists())
      return exe;
    exeName = "Banca.exe";
    exe = new File(s, exeName);
    if (exe.exists())
      return exe;
    return null;
  }

  private static List<String> quote(List<String> parti) {
    List<String> out = new ArrayList<>();
    for (String p : parti) {
      out.add(p.equals("\"\"") || !p.contains(" ") ? p : "\"" + p + "\"");
    }
    return out;
  }

}
