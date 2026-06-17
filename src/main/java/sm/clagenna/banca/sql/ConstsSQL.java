package sm.clagenna.banca.sql;

public class ConstsSQL {
  /** */
  public static final String QRY_CODSTATS_KNOWN   = """
      SELECT  descr
           ,codstat
           FROM ListaMovimenti
           WHERE 1=1
             AND codstat IS NOT NULL
           ORDER BY descr""";
  /** query per i record da indovinare */
  public static final String QRY_CODSTATS_UNKNOWN = """
      SELECT id
            ,idFile
            ,tipo
            ,dtmov
            ,dare
            ,avere
            ,cardid
            ,descr
           FROM ListaMovimenti
            WHERE 1=1
             %s
             AND (dare <> 0 OR avere <> 0)
             AND codstat IS NULL
          ORDER BY descr""";


}
