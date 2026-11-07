package prova.junit;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sm.clagenna.banca.dati.DataModel;

public class ProvaResultViewTest extends ApplicationTest {
  // IMPORTANTE: mettere  --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
  private static final String CSZ_PROVA_RESVIEW_PROPS = "dati/ProvaResultView.properties";

  private DataModel      model;
  private Stage          myStage;
  private CountDownLatch latch;

  /**
   * Prima form di testFx, funziona ma:
   * <ol>
   * <li>non si riesce a chiudere la form con la crocetta perchè il focus è da
   * un'altra parte.<br/>
   * <li>infatti lo stage passato da testFx non contiene Scene per cui occupa
   * tutta la finestra.</li>
   * <li>Per questo motivo ho creato un nuovo <b>Scene</b> di dimensione 1x1 e
   * spostato fuori dallo schermo (-10_000, -10_000) l'ho assegnato allo Stage
   * passato proprio per neutralizzarlo</li>
   * <li>Per aprire la form di test ho creato un nuovo Stage e l'ho aperto con
   * il metodo {@link ProvaResultView#start(Stage)}</li>
   * <li>Ora, non si riesce a chiudere l'applicazione
   * <ol type="a">
   * <li>perchè lo Stage passato e' chissa dove (-10.000,-10.000) non si riesce
   * a chiudere</li>
   * <li>ecco perchè ho aggiunto un CountDownLatch per attendere la chiusura
   * della form di test</li>
   * </ol>
   * </ol>
   */
  @Override
  public void start(Stage stage) throws Exception {
    System.out.printf("start()\n%s - props:%s\n", getClass().getSimpleName(), CSZ_PROVA_RESVIEW_PROPS);
    Scene backScene = new Scene(new javafx.scene.layout.Pane(), 1, 1);
    // backScene.getRoot().setVisible(false);
    stage.setScene(backScene);
    stage.setX( -10_000);
    stage.setY( -10_000);
    openAll();
    creaNewStage();
    ProvaResultView app = new ProvaResultView();
    app.setModel(model);
    app.initApp(model.getProps());
    app.start(myStage);
    myStage.toFront();
    myStage.requestFocus();
    System.out.println("title=" + stage.getTitle() + " style=" + stage.getStyle());
    stage.show();
  }

  private void creaNewStage() {
    myStage = new Stage(StageStyle.DECORATED);
    // qui il latch non e' ancora stato inizializzato, lo faccio nel test che viene dopo
    //    if (null != latch) {
    //      myStage.setOnHidden(_ -> latch.countDown()); // chiusura con la crocetta
    //      System.out.println("(Start) latch is set");
    //    } else
    //      System.out.println("(Start) latch is null !!!");
  }

  private void openAll() {
    model = new DataModel();
    model.setPropsFile(CSZ_PROVA_RESVIEW_PROPS);
    model.initApp(null);
  }

  /**
   * Siccome lo Stage di TestFX passato e' chissa dove (-10.000,-10.000) non si
   * riesce a chiudere.<br/>
   * Siccome non voglio che il test finisca prima della chiusura della form di
   * test, ho aggiunto un CountDownLatch per attendere la chiusura della form di
   * test.<br/>
   * <b>IMPORTANTE</b>: va aggiunto nelle VM options di test:<br/>
   * <code>--add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED</code>
   */
  @Test
  public void testFormIsVisible() {
    System.out.printf("testFormIsVisible()\n%s - props:%s\n", getClass().getSimpleName(), CSZ_PROVA_RESVIEW_PROPS);
    latch = new CountDownLatch(1);
    if (null != myStage) {
      myStage.setOnHidden(_ -> latch.countDown()); // chiusura con la crocetta
      System.out.println("(Test) latch is set");
    } else
      System.out.println("(Test) myStage is null !!!");
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // metodo 2 di testfx per verificare la visibilità di un nodo con id "grid-pane"
    //    try {
    //      Thread.sleep(100_000);
    //    } catch (InterruptedException e) {
    //      e.printStackTrace();
    //    }
  }

}
