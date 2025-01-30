package sm.clagenna.banca.dati;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.RealVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

public class PhraseComparator {
  @SuppressWarnings("unused")
  private static final Logger s_log = LogManager.getLogger(PhraseComparator.class);

  // Creazione di un vocabolario di parole uniche
  @Getter
  private Map<String, Integer> wordIndex;
  private List<Phrase>         knowns;
  private int                  index = 0;

  public record Similarity(double percent, Phrase phrase) {
  }

  public PhraseComparator() {
    //
  }

  public void addKnownPhrase(String p_sz, String key) {
    Phrase phr = new Phrase(p_sz);
    phr.setKey(key);
    phr = tratta(phr);
    if (null == knowns)
      knowns = new ArrayList<>();
    if ( !knowns.contains(phr))
      knowns.add(phr);
  }

  private Phrase tratta(Phrase phr) {
    phr.analyse();
    addToDictionary(phr.getToks());
    // phr.creaVector(wordIndex);
    return phr;
  }

  public int creaVectors() {
    for (Phrase phr : knowns)
      phr.creaVector(wordIndex);
    return wordIndex.size();
  }

  public void addToDictionary(String[] toks) {
    if (null == wordIndex) {
      wordIndex = new HashMap<>();
      index = 0;
    }
    for (String tok : toks) {
      if ( !wordIndex.containsKey(tok))
        wordIndex.put(tok, index++);
    }
  }

  public Similarity similarity(String p_sz) {
    Phrase phr = new Phrase(p_sz);
    phr.analyse();
    // aggiorno vocabolario con eventuali parole sconosciute
    int oldSize = wordIndex.size();
    for (String tok : phr.getToks()) {
      if ( !wordIndex.containsKey(tok))
        wordIndex.put(tok, index++);
    }
    boolean bAdded = oldSize != wordIndex.size();
    // creo il vector della frase entrante
    phr.creaVector(wordIndex);
    RealVector vec1 = phr.getVector();
    double ddbest = -2_000d;
    Phrase best = null;
    for (Phrase kno : knowns) {
      // aggiorno il vector delle frasi conosciute col nuovo vocabolario (se è cresciuto)
      if (bAdded)
        kno.creaVector(wordIndex);
      RealVector vec2 = kno.getVector();
      //           vec1 * vec2
      // prod = ----------------
      //         |vec1| * |vec2|
      double prod = vec1.dotProduct(vec2) / (vec1.getNorm() * vec2.getNorm());
      if (prod > ddbest) {
        ddbest = prod;
        best = kno;
      }
    }
    return new Similarity(ddbest, best);
  }
}
