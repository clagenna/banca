package prova.junit5;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProvaControlsBaseTest extends ApplicationTest {

  // IMPORTANTE: mettere  --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED

  @Override
  public void start(Stage stage) throws Exception {
    ProvaControlsBase app = new ProvaControlsBase();
    Scene scene = app.buildForm(stage);
    stage.setScene(scene);
    stage.show();
  }

  @Test
  public void testFormIsVisible() {
    System.out.println("ProvaJavaFXControlsBaseTest.testFormIsVisible()");
    // Verifica che il form sia visibile
    // FxAssert.verifyThat("grid-pane", NodeMatchers.isVisible());
    // metodo 1 di testfx per verificare la visibilità di un nodo con id "grid-pane"
    CountDownLatch latch = new CountDownLatch(1);
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
