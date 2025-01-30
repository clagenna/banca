package sm.clagenna.banca.dati;

import lombok.Getter;

public class Simplyf {

  private String regex;
  private String conche;
  @Getter
  boolean        repeat;

  public Simplyf(String regx, String cc) {
    regex = regx;
    conche = cc;
    repeat = false;
  }

  public String replaceAll(String p_sz) {
    return p_sz.replaceAll(regex, conche);
  }
}
