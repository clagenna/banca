package sm.clagenna.banca.dati;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import opennlp.tools.tokenize.SimpleTokenizer;

public class Phrase implements Comparable<Phrase> {
  @SuppressWarnings("unused")
  private static final Logger  s_log = LogManager.getLogger(Phrase.class);
  private static List<Simplyf> allRegex;

  @Getter @Setter
  private String     key;
  @Getter @Setter
  private String     phrase;
  @Getter
  private String[]   toks;
  @Getter
  private RealVector vector;

  static {
    allRegex = new ArrayList<>();
    allRegex.add(new Simplyf("(dal|al|il|del)+[ \\t]+[0-9]+[\\./]*[0-9]+[\\./]*[0-9]+[\\./]*", " "));
    allRegex.add(new Simplyf("(ore)+[ \\t]+[0-9]+[\\ \\./:]*[0-9]+", " "));
    allRegex.add(new Simplyf("nr.pan\\  *[0-9]+", " "));
    allRegex.add(new Simplyf("nr.pan\\  *[0-9]+", " "));
    allRegex.add(new Simplyf("d   +[0-9\\-\\/]+", " "));
    allRegex.add(new Simplyf("[0-9/]+", " "));
    allRegex.add(new Simplyf("\\* *[a-z]+", " "));
    allRegex.add(new Simplyf("pag.pos circuito inter .+a", "pos "));
    allRegex.add(new Simplyf("pag.to cartazzurra +", "pos "));
    allRegex.add(new Simplyf("pagamento tramite pos +", "pos "));
    allRegex.add(new Simplyf("pag.to pos +", "pos "));
    allRegex.add(new Simplyf("pos +", "pos "));
    allRegex.add(new Simplyf("[\"\\-`'\\.\\:,#]", " "));
    allRegex.add(new Simplyf("[ \\t][ \\t]+", " "));
    allRegex.add(new Simplyf("nr pan", " "));
    allRegex.add(new Simplyf("svcs", " "));
    allRegex.add(new Simplyf("[ \\t][ \\t]+", " "));
  }

  public Phrase() {
    //
  }

  public Phrase(String p_sz) {
    setPhrase(p_sz);
  }

  public void analyse() {
    semplify();
    creaTokens();
  }

  private void semplify() {
    phrase = semplify(phrase);
  }

  private String semplify(String p_sz) {
    String sz = p_sz.toLowerCase();
    for (Simplyf ss : allRegex)
      sz = ss.replaceAll(sz);
    return sz;
  }

  private void creaTokens() {
    // Tokenizzazione delle frasi
    SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
    toks = tokenizer.tokenize(phrase);
  }

  public void creaVector(Map<String, Integer> wordIndex) {
    if (null != vector && wordIndex.size() == vector.getDimension())
      return;
    vector = new ArrayRealVector(wordIndex.size());
    for (String token : toks) {
      vector.addToEntry(wordIndex.get(token), 1);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (null == o || null == phrase)
      return false;
    if (o instanceof Phrase phr) {
      if (null != phr.phrase)
        return phrase.equals(phr.phrase);
    }
    return false;
  }

  @Override
  public int compareTo(Phrase o) {
    if (null == o || null == phrase)
      return -1;
    if (o instanceof Phrase ophr)
      return phrase.compareTo(ophr.phrase);
    return -1;
  }

  @Override
  public String toString() {
    String sz = String.format("%-40s %s ", phrase, key);
    return sz;
  }
}
