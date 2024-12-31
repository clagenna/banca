package sm.clagenna.banca.javafx;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.banca.dati.CsvFileContainer;
import sm.clagenna.banca.dati.DataController;
import sm.clagenna.banca.dati.ImpFile;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.utils.AppProperties;

public class SovrapposView implements Initializable, IStartApp {
  private static final Logger s_log = LogManager.getLogger(SovrapposView.class);

  private static DateTimeFormatter s_fmt_ldt             = DateTimeFormatter.ofPattern("dd/MM/YY");
  public static final String       CSZ_FXMLNAME          = "SovrapposView.fxml";
  private static final String      CSZ_PROP_POSRESVIEW_X = "sovrapp.x";
  private static final String      CSZ_PROP_POSRESVIEW_Y = "sovrapp.y";
  private static final String      CSZ_PROP_DIMRESVIEW_X = "sovrapp.lx";
  private static final String      CSZ_PROP_DIMRESVIEW_Y = "sovrapp.ly";

  @FXML
  private Pane pane;

  @Getter @Setter
  private Scene            myScene;
  private Stage            lstage;
  private LoadBancaMainApp m_appmain;
  private AppProperties    m_mainProps;

  private List<ImpFile> liFil;
  private LocalDateTime dtMin;
  private LocalDateTime dtMax;
  private int           posDtMin;
  private int           posDtMax;
  private int           win_he;
  private int           top_pad;
  private int           bottom_pad;
  private int           left_pad;
  private int           win_wi;
  private int           right_pad;
  private int           TIC_MIN;
  private int           TIC_MAX;
  private double        dlt_x;
  private double        pyRuller;

  public SovrapposView() {
    //
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // System.out.println("SovrapposView.initialize()");
  }

  @Override
  public void initApp(AppProperties p_props) {
    // System.out.println("SovrapposView.initApp()");
    m_appmain = LoadBancaMainApp.getInst();
    m_mainProps = m_appmain.getProps();
    impostaForma(m_mainProps);
    if (lstage != null)
      lstage.setOnCloseRequest(e -> {
        closeApp(m_mainProps);
      });
    drawImpFiles();
  }

  private void impostaForma(AppProperties p_props) {
    lstage = null;
    if (myScene == null)
      myScene = pane.getScene();
    myScene.widthProperty().addListener(s -> resized());
    myScene.heightProperty().addListener(s -> resized());

    if (lstage == null && myScene != null)
      lstage = (Stage) myScene.getWindow();
    if (lstage == null) {
      s_log.error("Non trovo lo stage per Sovrapp.View");
      return;
    }

    int px = p_props.getIntProperty(CSZ_PROP_POSRESVIEW_X);
    int py = p_props.getIntProperty(CSZ_PROP_POSRESVIEW_Y);
    int dx = p_props.getIntProperty(CSZ_PROP_DIMRESVIEW_X);
    int dy = p_props.getIntProperty(CSZ_PROP_DIMRESVIEW_Y);
    if (px != -1 && py != -1 && px * py != 0) {
      lstage.setX(px);
      lstage.setY(py);
      lstage.setWidth(dx);
      lstage.setHeight(dy);
    }
    URL url = m_appmain.getUrlCSS();
    if (null != url)
      myScene.getStylesheets().add(url.toExternalForm());
  }

  private Object resized() {
    win_he = (int) lstage.getHeight();
    win_wi = (int) lstage.getWidth();
    pane.getChildren().clear();
    drawImpFiles();
    return null;
  }

  private void drawImpFiles() {
    top_pad = 50;
    bottom_pad = 80;
    left_pad = 30;
    win_he = (int) lstage.getHeight();
    win_wi = (int) lstage.getWidth();
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
    if ( !pif.hasPeriodo()) {
      s_log.warn("Non considero {} perche' non ha periodo", pif.getFileName());
      return;
    }
    int lPosMin = getDtPos(pif.getDtmin()) + 1;
    int intlMin = lPosMin - posDtMin;
    int lPosMax = getDtPos(pif.getDtmax());
    int intlMax = lPosMax - posDtMin;
    double py = top_pad + progr * 40;
    double px1 = left_pad + dlt_x * intlMin;
    px1 -= dlt_x / 31. * (31 - pif.getDtmin().getDayOfMonth());
    double px2 = left_pad + dlt_x * intlMax;
    px2 -= dlt_x / 31. * (31 - pif.getDtmax().getDayOfMonth());
    Color strk = Color.GREEN;
    Color oriz = Color.BLUEVIOLET;

    Line tickLine1 = new Line(px1, pyRuller, px1, py - TIC_MAX);
    tickLine1.setStroke(strk);
    tickLine1.getStrokeDashArray().addAll(5d, 15d);
    
    Line tickLine2 = new Line(px2, pyRuller, px2, py - TIC_MAX);
    tickLine2.setStroke(strk);
    tickLine2.getStrokeDashArray().addAll(3d, 8d);
    
    Line tickLine = new Line(px1, py, px2, py);
    tickLine.setStroke(oriz);
    tickLine.setStrokeWidth(5.);
    
    pane.getChildren().addAll(tickLine1, tickLine2, tickLine);

    Text lab = new Text(px1, py + 15, pif.getFileName());
    Text labd1 = new Text(px1, py - 10, s_fmt_ldt.format(pif.getDtmin()));
    Text labd2 = new Text(px2, py - 10, s_fmt_ldt.format(pif.getDtmax()));
    pane.getChildren().addAll(lab, labd1, labd2);
  }

  private void drawRuller() {
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

  private int getDtPos(LocalDateTime ldt) {
    return ldt.getYear() * 12 + ldt.getMonthValue() - 1;
  }

  @Override
  public void changeSkin() {
    URL url = m_appmain.getUrlCSS();
    if (null == url || null == myScene)
      return;
    myScene.getStylesheets().clear();
    myScene.getStylesheets().add(url.toExternalForm());
  }

  @Override
  public void closeApp(AppProperties p_props) {

    double px = myScene.getWindow().getX();
    double py = myScene.getWindow().getY();
    double dx = myScene.getWindow().getWidth();
    double dy = myScene.getWindow().getHeight();

    p_props.setProperty(CSZ_PROP_POSRESVIEW_X, (int) px);
    p_props.setProperty(CSZ_PROP_POSRESVIEW_Y, (int) py);
    p_props.setProperty(CSZ_PROP_DIMRESVIEW_X, (int) dx);
    p_props.setProperty(CSZ_PROP_DIMRESVIEW_Y, (int) dy);
  }

  public int setImpFileStart(ImpFile imf) {
    DataController data = DataController.getInst();
    CsvFileContainer csvf = data.getContCsv();
    liFil = csvf.getListSiblings(imf);
    if (null == liFil || liFil.size() <= 1)
      return 1;
    dtMin = liFil.stream() //
        .filter(s -> s.hasPeriodo())//
        .map(s -> s.getDtmin())//
        .min(LocalDateTime::compareTo) //
        .orElseThrow(NoSuchElementException::new);
    dtMax = liFil.stream()//
        .filter(s -> s.hasPeriodo())//
        .map(s -> s.getDtmax())//
        .max(LocalDateTime::compareTo) //
        .orElseThrow(NoSuchElementException::new);
    posDtMin = getDtPos(dtMin);
    posDtMax = getDtPos(dtMax);
    return liFil.size();
  }

}
