package prova.javafx;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import sm.clagenna.stdcla.utils.ParseData;

public class ProvaJavaFXGraphicBase extends Application {

  private Stage mainstage;
  private Pane  pane;

  private int right_pad;
  private int top_pad;
  private int left_pad;
  private int bottom_pad;

  private int win_he;
  private int win_wi;

  private int           TIC_MIN;
  private int           TIC_MAX;
  private LocalDateTime dtMin;
  private LocalDateTime dtMax;
  private int           posDtMin;
  private int           posDtMax;

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    mainstage = primaryStage;
    pane = new Pane();

    Scene scene = new Scene(pane, 500, 200);
    scene.widthProperty().addListener(_ -> resized());
    scene.heightProperty().addListener(_ -> resized());

    primaryStage.setOnShown(_ -> drawGraphic());

    primaryStage.setTitle("Ruler Example");
    primaryStage.setScene(scene);
    primaryStage.show();

  }

  private int getDtPos(LocalDateTime ldt) {
    return ldt.getYear() * 12 + ldt.getMonthValue() - 1;
  }

  private Object drawGraphic() {
    top_pad = 50;
    bottom_pad = 80;
    left_pad = 30;
    right_pad = 50;

    win_he = (int) mainstage.getHeight();
    win_wi = (int) mainstage.getWidth();
    TIC_MIN = 5;
    TIC_MAX = 10;
    dtMin = LocalDateTime.of(2025, 1, 1, 0, 0);
    dtMax = LocalDateTime.of(2025, 12, 31, 23, 59);
    posDtMin = getDtPos(dtMin);
    posDtMax = getDtPos(dtMax);

    int qtaMesi = posDtMax - posDtMin;
    int dlt_x = (win_wi - left_pad - right_pad) / qtaMesi;
    int pyRuller = win_he - bottom_pad;
    int meseIni = dtMin.getMonthValue();
    int currAA = dtMin.getYear();
    int currMM = dtMin.getMonthValue() - 1;

    for (int i = 0; i <= qtaMesi; i++) {
      Color strk = Color.BLACK;
      int ticHe = TIC_MIN;
      int currCurs = (i + meseIni) % 12;
      if (i == 0 || currCurs % 6 == 0 || i == qtaMesi) {
        ticHe = TIC_MAX;
        if (i == 0 || i == qtaMesi)
          strk = Color.PURPLE;
      }
      double px = left_pad + (int) (i * dlt_x);
      Line tickLine = new Line(px, pyRuller, px, pyRuller - ticHe);
      tickLine.setStroke(strk);
      pane.getChildren().add(tickLine);
      if (currMM >= 12) {
        currAA++;
        currMM = 0;
      }
      if (i == 0 || currCurs % 6 == 0 || currCurs % 12 == 0 || i == qtaMesi) {
        String szLab = String.format("%02d/%04d", currMM + 1, currAA);
        double lpx = px;
        double lpy = pyRuller;
        if (i == 0 || i == qtaMesi) {
          szLab = i == 0 ? ParseData.s_fmtY4MD.format(dtMin) : ParseData.s_fmtY4MD.format(dtMax);
          lpy += 10;
          if (i == qtaMesi)
            lpx -= 45;
        }
        Text lab = new Text(lpx, lpy + 15, szLab);
        pane.getChildren().add(lab);
      }
      currMM++;
    }

    Line baseLine = new Line(left_pad, pyRuller, win_wi - right_pad, pyRuller);
    baseLine.setStroke(Color.CHOCOLATE);
    pane.getChildren().add(baseLine);

    return null;
  }

  private Object resized() {
    win_he = (int) mainstage.getHeight();
    win_wi = (int) mainstage.getWidth();
    pane.getChildren().clear();
    drawGraphic();
    return null;
  }

}
