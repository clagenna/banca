package sm.clagenna.banca.javafx;

import java.util.Optional;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.Setter;

public class MessageDialog {

  @Setter
  private static Stage stage = null;

  /**
   * per abilitare il display HTML ho messo un WebView embedded nel alert pero'
   * ho dovuto specificare <b>javafx.media,javafx.web</b>
   *
   * <pre>
   * --module-path "C:/Program Files/Java/javafx-sdk-20.0.2/lib"
   * --add-modules=javafx.swing,javafx.graphics,javafx.fxml,javafx.media,javafx.web
   * </pre>
   *
   * @param typ
   *          Il tipo di {@link AlertType}
   * @param p_msg
   *          Il messaggio (anche HTML) da emettere
   * @param bt
   *          Il tipo di {@link ButtonType}
   * @return
   */
  public static Optional<ButtonType> messageDialog(AlertType typ, String p_msg, ButtonType bt) {
    Alert alert = new Alert(typ);
    alert.setResizable(true);
    if ( null == stage)
      stage = LoadBancaMainApp.getInst().getPrimaryStage();
    if (null != stage)
      alert.initOwner(stage);

    Scene scene = null;
    if (null != stage)
      scene = stage.getScene();
    if (null != scene) {
      double posx = scene.getWindow().getX();
      double posy = scene.getWindow().getY();
      double widt = scene.getWidth();
      double px = posx + widt / 2 - 366;
      double py = posy + 50;
      alert.setX(px);
      alert.setY(py);
    }
    alert.setWidth(400);

    switch (typ) {
      case CONFIRMATION:
        alert.setTitle("Verifica cosa fare");
        alert.setHeaderText("Scegli cosa fare");
        break;
      case INFORMATION:
        alert.setTitle("Informazione");
        alert.setHeaderText("Comunicazione");
        break;

      case WARNING:
        alert.setTitle("Attenzione");
        alert.setHeaderText("Occhio !");
        break;

      case ERROR:
        alert.setTitle("Errore !");
        alert.setHeaderText("Ahi ! Ahi !");
        break;

      default:
        break;
    }
    // button type YES -> aggiungo il no
    if (null != bt && bt.equals(ButtonType.YES)) {
      alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
    }
    //    alert.setContentText(p_msg);
    WebView webView = new WebView();
    webView.getEngine().loadContent(p_msg);
    webView.setPrefSize(300, 60);
    alert.getDialogPane().setContent(webView);
    Optional<ButtonType> btret = alert.showAndWait();
    return btret;
  }

  public static Optional<ButtonType> messageDialog(AlertType typ, String szMsg) {
    return messageDialog(typ, szMsg, null);
  }

}
