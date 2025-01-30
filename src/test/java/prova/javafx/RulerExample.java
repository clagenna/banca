package prova.javafx;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RulerExample extends Application {

  private static final int TIC_MIN = 5;
  private static final int TIC_MAX = 10;
  private Stage            mainstage;
  private Pane             pane;

  private int POS_Y  = 50;
  private int STRT_X = 10;
  private int END_X  = 500;
  private int WI_X   = 240;
  private int DLT_X  = 20;

  @Override
  public void start(Stage primaryStage) {
    mainstage = primaryStage;
    pane = new Pane();

    drawRuler(pane, STRT_X, END_X, WI_X, DLT_X);

    Scene scene = new Scene(pane, END_X, 100);
    scene.widthProperty().addListener(s -> resized());
    scene.heightProperty().addListener(s -> resized());
    primaryStage.setTitle("Ruler Example");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private ChangeListener<? super Number> resized() {
    POS_Y = (int) (mainstage.getHeight() - 70.);
    END_X = (int) (mainstage.getWidth() - 30.);
    // System.out.printf("resiz:X=%.0f Y=%.0f\n", mainstage.getWidth(), mainstage.getHeight());
    pane.getChildren().clear();
    drawRuler(pane, STRT_X, END_X, WI_X, DLT_X);
    return null;
  }

  private void drawRuler(Pane pane, int startX, int endX, int majorTickInterval, int minorTickInterval) {

    for (int i = 0; i <= endX; i += minorTickInterval) {
      int height = i % majorTickInterval == 0 ? TIC_MAX : TIC_MIN;
      int px = startX + i;
      Line tick = new Line(px, POS_Y - height, px, POS_Y);
      tick.setStroke(Color.BLACK);
      pane.getChildren().add(tick);

      if (i % majorTickInterval == 0) {
        Text label = new Text(px, POS_Y + 15, String.valueOf(i));
        pane.getChildren().add(label);
      }
    }
    Line baseLine = new Line(startX, POS_Y, endX, POS_Y);
    baseLine.setStroke(Color.CHOCOLATE);
    pane.getChildren().add(baseLine);
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}
