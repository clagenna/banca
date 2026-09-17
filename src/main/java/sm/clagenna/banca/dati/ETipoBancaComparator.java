package sm.clagenna.banca.dati;

import java.util.Comparator;

public class ETipoBancaComparator implements Comparator<String> {

  /**
   * Compare two strings, ignoring case considerations. If one string is a prefix of the other, the shorter string is considered 
   *
   * @param o1 the first string to be compared
   * @param o2 the second string to be compared
   * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
   */
  @Override
  public int compare(String o1, String o2) {
    if (null == o1 || null == o2)
      return -1;
    String primo = o1.trim().toLowerCase();
    String secon = o2.trim().toLowerCase();
    if (primo.equals(secon))
      return 0;
    String minor = primo.length() < secon.length() ? primo : secon;
    if ( primo.length()>secon.length() &&   primo.startsWith(minor))
      return -1;
    if ( secon.length()>primo.length() &&   secon.startsWith(minor))
      return 1;
    return o1.compareTo(o2);
  }

}
