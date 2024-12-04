package prova.javafx;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ProvaProgressBar2 extends Application {
  int          litros;
  double       preciofinal;
  public Label precio = new Label();
  TextField    txtlitros;
  //tu tarea hacer
  Task<Object> copyWorker;

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Gasolinera");
    Group root = new Group();
    Scene scene = new Scene(root, 330, 120, Color.WHITE);

    BorderPane mainPane = new BorderPane();
    root.getChildren().add(mainPane);

    final Label label = new Label("Gasolina cargada");
    precio.setText("$");
    final ProgressBar progressBar = new ProgressBar(0);
    final HBox h1 = new HBox();
    h1.setSpacing(5);
    h1.setAlignment(Pos.CENTER);
    h1.getChildren().addAll(precio);
    mainPane.setTop(h1);

    final HBox hb = new HBox();
    hb.setSpacing(5);
    hb.setAlignment(Pos.CENTER);
    hb.getChildren().addAll(label, progressBar);
    mainPane.setCenter(hb);
    final Label litroslbl = new Label("Litros de gasolina:");
    txtlitros = new TextField("");
    final Button startButton = new Button("Start");
    final Button cancelButton = new Button("Cancel");
    final HBox hb2 = new HBox();
    hb2.setSpacing(5);
    hb2.setAlignment(Pos.CENTER);
    hb2.getChildren().addAll(litroslbl, txtlitros, startButton, cancelButton);
    mainPane.setBottom(hb2);

    startButton.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent event) {
        //limpias el diseño al empezar la acción del botón
        startButton.setDisable(true);
        progressBar.setProgress(0);
        cancelButton.setDisable(false);
        copyWorker = createWorker();

        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(copyWorker.progressProperty());

        copyWorker.messageProperty().addListener(new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            System.out.println(newValue);
          }
        });

        new Thread(copyWorker).start();
        preciofinal = Double.parseDouble(txtlitros.getText()) * 20;
        precio.setText("$" + preciofinal);
      }
    });
    cancelButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        startButton.setDisable(false);
        cancelButton.setDisable(true);
        copyWorker.cancel(true);
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        System.out.println("cancelado");
      }
    });
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public Task<Object> createWorker() {
    return new Task<Object>() {
      @Override
      protected Object call() throws Exception {
        litros = Integer.parseInt(txtlitros.getText());

        System.out.println("Litros:" + litros);
        for (int i = 0; i < litros / 2; i++) {
          Thread.sleep(1000);
          //                    updateMessage("2000 milliseconds");
          updateProgress(i + 1, litros / 2);
          System.out.println(i);

        }
        return true;
      }
    };
  }
}
