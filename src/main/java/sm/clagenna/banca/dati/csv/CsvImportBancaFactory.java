package sm.clagenna.banca.dati.csv;

import sm.clagenna.banca.dati.ETipoBanca;

public class CsvImportBancaFactory {

  public CsvImportBancaFactory() {
    //
  }

  public static CsvImportBanca getCsvImportBanca(ETipoBanca p_tipoBanca) {
    CsvImportBanca ret = null;
    switch (p_tipoBanca) {
      case Amazon:
        ret = new CsvImportBancaAmazon();
        break;
      case Wise:
        ret = new CsvImportBancaWise();
        break;
      case Bsi:
        ret = new CsvImportBancaBsi();
        break;
      case BsiCredit:
        ret = new CsvImportBancaBsiCredit();
        break;
      case Carisp:
        ret = new CsvImportBancaCarisp();
        break;
      case CarispCredit:
        ret = new CsvImportBancaCarispCredit();
        break;
      case Contanti:
        ret = new CsvImportBancaContanti();
        break;
      case PayPal:
        ret = new CsvImportBancaPayPal();
        break;
      case Revolut:
        ret = new CsvImportBancaRevolut();
        break;
      case Smac:
        ret = new CsvImportBancaSmac();
        break;
      default:
        throw new IllegalArgumentException("Tipo banca_1 non valido: " + p_tipoBanca);
    }
    return ret;
  }

  public static CsvImportBanca getCsvImportBanca(String p_tipoBanca) {
    ETipoBanca tipo = ETipoBanca.parse(p_tipoBanca);
    if (null == tipo)
      throw new IllegalArgumentException("Tipo banca_2 non valido: " + p_tipoBanca);
    return CsvImportBancaFactory.getCsvImportBanca(tipo);
  }

}
