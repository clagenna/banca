package prova.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ProvaJavaFXControlsBase extends Application {

  private static final String ENTER_NAME      = "Dammi il tuo nome.";
  private static final String ENTER_COGNOME   = "Dammi il tuo cognome.";
  private static final String SCRIVI_COMMENTO = "Scrivi un commento.";

  @SuppressWarnings("unused")
  private Stage    mainstage;
  private GridPane gridPane;
  private Scene    scene;

  @SuppressWarnings("unused")
  private int right_pad;
  @SuppressWarnings("unused")
  private int top_pad;
  @SuppressWarnings("unused")
  private int left_pad;
  @SuppressWarnings("unused")
  private int bottom_pad;

  @SuppressWarnings("unused")
  private int win_he;
  @SuppressWarnings("unused")
  private int win_wi;

  private TextField txCognome;
  private TextField txName;
  private Button    btSubmit;
  private Button    btClear;
  private Label     lbCommento;
  private TextField txComment;

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    mainstage = primaryStage;
    buildForm();
    scene = new Scene(gridPane, 500, 200);

    // scene.widthProperty().addListener(_ -> resized());
    // scene.heightProperty().addListener(_ -> resized());
    // primaryStage.setOnShown(_ -> buildForm());

    primaryStage.setTitle("Java FX Example");
    primaryStage.setScene(scene);
    primaryStage.show();

  }

  @SuppressWarnings("unused")
  private void buildForm() {
    //Creating a GridPane container
    gridPane = new GridPane();
    gridPane.setPadding(new Insets(10, 10, 10, 10));
    gridPane.setVgap(5);
    gridPane.setHgap(5);
    // definizione Nome
    txName = new TextField();
    txName.setPromptText(ENTER_NAME);
    txName.setPrefColumnCount(10);
    txName.getText();
    GridPane.setConstraints(txName, 0, 0);
    gridPane.getChildren().add(txName);
    txCognome = new TextField();
    txCognome.setPromptText(ENTER_COGNOME);
    GridPane.setConstraints(txCognome, 0, 1);
    gridPane.getChildren().add(txCognome);
    txComment = new TextField();
    txComment.setPrefColumnCount(15);
    txComment.setPromptText(SCRIVI_COMMENTO);
    GridPane.setConstraints(txComment, 0, 2);
    gridPane.getChildren().add(txComment);
    // definizione label Commento
    lbCommento = new Label("Commento");
    lbCommento.setPrefWidth(350);
    GridPane.setConstraints(lbCommento, 0, 3);
    gridPane.getChildren().add(lbCommento);

    // definizione bottone invia e 
    btSubmit = new Button("Invia");
    GridPane.setConstraints(btSubmit, 1, 0);
    gridPane.getChildren().add(btSubmit);
    btClear = new Button("Clear");
    GridPane.setConstraints(btClear, 1, 1);
    gridPane.getChildren().add(btClear);
    // definizione azioni dei bottoni
    btSubmit.setOnAction(e -> btSubmitClick());
  }

  private Object btSubmitClick() {
    String szCommento = "";
    if (txName.getText().length() > 0 && txName.getText().length() < 10)
      szCommento = "Ciao " + txName.getText();
    else {
      szCommento = "Nome non valido";
      lbCommento.setText(szCommento);
      return null;
    }
    if (txCognome.getText().length() > 0)
      szCommento += " " + txCognome.getText();
    else {
      szCommento = "Cognome non valido";
      lbCommento.setText(szCommento);
      return null;
    }
    if (txComment.getText().length() > 0)
      szCommento += " ; " + txComment.getText();
    lbCommento.setText(szCommento);
    return null;
  }

}
