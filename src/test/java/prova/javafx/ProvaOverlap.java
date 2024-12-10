package prova.javafx;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import sm.clagenna.banca.dati.ImpFile;
import sm.clagenna.stdcla.utils.ParseData;

public class ProvaOverlap extends Application {
  private DateTimeFormatter s_fmt_ldt = DateTimeFormatter.ofPattern("dd/MM/YY");
  private Stage             mainstage;
  private Pane              pane;
  private List<ImpFile>     liFil;
  private LocalDateTime     dtMin;
  private LocalDateTime     dtMax;
  private int               posDtMin;
  private int               posDtMax;
  private int               win_he;
  private int               top_pad;
  private int               bottom_pad;
  private int               left_pad;
  private int               win_wi;
  private int               right_pad;
  private int               TIC_MIN;
  private int               TIC_MAX;
  private double            dlt_x;
  private double            pyRuller;

  @Override
  public void start(Stage primaryStage) throws Exception {
    mainstage = primaryStage;
    pane = new Pane();
    fillFiles();
    Scene scene = new Scene(pane, 500, 200);
    scene.widthProperty().addListener(s -> resized());
    scene.heightProperty().addListener(s -> resized());

    primaryStage.setOnShown(s -> drawImpFiles());

    primaryStage.setTitle("Ruler Example");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void drawImpFiles() {
    top_pad = 50;
    bottom_pad = 80;
    left_pad = 30;
    win_he = (int) mainstage.getHeight();
    win_wi = (int) mainstage.getWidth();
    right_pad = 50;
    TIC_MIN = 5;
    TIC_MAX = 10;

    drawRuller();
    int k = 0;
    for (ImpFile impf : liFil) {
      drawImpFiles(impf, k++);
    }
  }

  private void drawImpFiles(ImpFile pif, int progr) {
    int lPosMin = getDtPos(pif.getDtmin());
    int intlMin = lPosMin - posDtMin;
    int lPosMax = getDtPos(pif.getDtmax());
    int intlMax = lPosMax - posDtMin;
    double py = top_pad + progr * 40;
    double px1 = left_pad + dlt_x * intlMin;
    px1 -= (dlt_x / 31.) * ( 31 - pif.getDtmin().getDayOfMonth());
    double px2 = left_pad + dlt_x * intlMax;
    px2 -= (dlt_x / 31.) * ( 31 - pif.getDtmax().getDayOfMonth());
    Color strk = Color.GREEN;

    Line tickLine1 = new Line(px1, pyRuller, px1, py - TIC_MAX);
    tickLine1.setStroke(strk);
    tickLine1.getStrokeDashArray().addAll(5d, 20d);
    Line tickLine2 = new Line(px2, pyRuller, px2, py - TIC_MAX);
    tickLine2.setStroke(strk);
    tickLine2.getStrokeDashArray().addAll(5d, 20d);
    Line tickLine = new Line(px1, py, px2, py);
    pane.getChildren().addAll(tickLine1, tickLine2, tickLine);

    Text lab = new Text(px1, py + 15, pif.getFileName());
    Text labd1 = new Text(px1, py - 10, s_fmt_ldt.format(pif.getDtmin()));
    Text labd2 = new Text(px2, py - 10, s_fmt_ldt.format(pif.getDtmax()));
    pane.getChildren().addAll(lab, labd1, labd2);
  }

  private void drawRuller() {
    //    Text txDtMin = new Text(10, 15, ParseData.formatDate(dtMin));
    //    Text txDtMax = new Text(10, 30, ParseData.formatDate(dtMax));
    //    pane.getChildren().addAll(txDtMin, txDtMax);

    int qtaMesi = posDtMax - posDtMin;
    dlt_x = (win_wi - left_pad - right_pad) / qtaMesi;
    pyRuller = win_he - bottom_pad;
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
          szLab = i == 0 ? s_fmt_ldt.format(dtMin) : s_fmt_ldt.format(dtMax);
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
  }

  private Object resized() {
    win_he = (int) mainstage.getHeight();
    win_wi = (int) mainstage.getWidth();
    pane.getChildren().clear();
    drawImpFiles();
    return null;
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  private int getDtPos(LocalDateTime ldt) {
    return ldt.getYear() * 12 + ldt.getMonthValue() - 1;
  }

  private void fillFiles() {
    liFil = new ArrayList<ImpFile>();
    liFil.add(new ImpFile(4, "estrattoconto_BSI_2112.csv", "Banca BSI", 1104, 10, ParseData.parseData("2021-03-04 00:00:00"),
        ParseData.parseData("2021-12-31 00:00:00"), ParseData.parseData("2024-12-07 17:57:12")));
    liFil.add(new ImpFile(5, "estrattoconto_BSI_2212.csv", "Banca BSI", 875, 7, ParseData.parseData("2022-03-04 00:00:00"),
        ParseData.parseData("2022-12-31 00:00:00"), ParseData.parseData("2024-12-07 17:57:13")));
    liFil.add(new ImpFile(6, "estrattoconto_BSI_2312.csv", "Banca BSI", 28002, 202, ParseData.parseData("2023-03-07 00:00:00"),
        ParseData.parseData("2023-12-31 00:00:00"), ParseData.parseData("2024-12-07 17:57:15")));
    liFil.add(new ImpFile(1, "estrattoconto_BSI_2410.csv", "Banca BSI", 25866, 192, ParseData.parseData("2024-01-02 00:00:00"),
        ParseData.parseData("2024-11-02 00:00:00"), ParseData.parseData("2024-12-07 17:39:18")));
    liFil.add(new ImpFile(2, "estrattoconto_BSI_2411.csv", "Banca BSI", 29879, 220, ParseData.parseData("2023-11-27 00:00:00"),
        ParseData.parseData("2024-11-11 00:00:00"), ParseData.parseData("2024-12-08 14:51:20")));
    dtMin = liFil.stream().map(s -> s.getDtmin()).min(LocalDateTime::compareTo).orElseThrow(NoSuchElementException::new);
    dtMax = liFil.stream().map(s -> s.getDtmax()).max(LocalDateTime::compareTo).orElseThrow(NoSuchElementException::new);
    posDtMin = getDtPos(dtMin);
    posDtMax = getDtPos(dtMax);
  }
}
