package sm.clagenna.banca.dati;

import java.io.Serializable;

/**
 * Questa classe ha l'unico scopo di mantenere la versione degli standard
 * corrente. Bisogna mettere il branch corrente e la versione corrente.
 */
public class Versione implements Serializable {
  private static final long serialVersionUID = 7739416949184823945L;

  /** Nome del branch a cui appartiene il progetto */
  public static final String BRANCH = "HEAD";

  /** Nome Applicativo */
  public static final String NOME_APPL = "Banca";

  /** Nome Applicativo */
  public static final String DESC_APPL = "Gestione movimenti bancari";

  /** logo grande, mi aspetto che sia sotto /newappl/images */
  public static final String LOGO_BIG_APPL   = "logo128.gif";
  /** logo piccolo, mi aspetto che sia sotto /newappl/images */
  public static final String LOGO_SMALL_APPL = "logo16.gif";

  /** Major Version */
  public static final int APP_MAX_VERSION = 1;
  /** Minor Version */
  public static final int APP_MIN_VERSION = 0;
  /** Build Version */
  public static final int    APP_BUILD = 31;

  // e oggi esteso ${dh:CSZ_DATEDEPLOY}
  public static final String CSZ_DATEDEPLOY = "27/02/2025 17:50:19";

  public static void main(String[] args) {
    System.out.println(DESC_APPL + " " + Versione.getVersion());
    System.out.println("Extended:" + Versione.getVersionEx());
  }

  /**
   * Costruttore vuoto che inizializza le variabili interne per avere un XML
   * corretto.
   */
  public Versione() {
    //
  }

  /**
   * Ritorna la versione corrente degli standard nella forma
   *
   * <pre>
   * maxver.minver.build
   * </pre>
   *
   * @return versione applicativo
   */
  public static String getVersion() {
    String szVer = String.format("%d.%d.%d", APP_MAX_VERSION, APP_MIN_VERSION, APP_BUILD);
    return szVer;
  }

  @Override
  public String toString() {
    return Versione.getVersion();
  }

  public static String getVersionEx() {
    String sz = String.format("%s: %s ver. %s pubbl.il %s", NOME_APPL, DESC_APPL, Versione.getVersion(), CSZ_DATEDEPLOY);
    return sz;
  }
}









































