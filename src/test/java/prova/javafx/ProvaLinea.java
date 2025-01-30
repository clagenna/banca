package prova.javafx;

//Java program to create a path
//and add VLineTo to it and display it
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.VLineTo;
import javafx.stage.Stage;

public class ProvaLinea extends Application {

  // launch the application
  @Override
  public void start(Stage stage) {
    try {
      stage.setTitle("VLineTo");
      // create a Group
      Group group = new Group();
      suaLinea(group);
      miaLinea(group);

      // create a scene
      Scene scene = new Scene(group, 400, 300);

      // set the scene
      stage.setScene(scene);
      stage.show();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private Path suaLinea(Group group) {
    // create VLineTo
    VLineTo vlineto = new VLineTo(200);
    // create moveto
    MoveTo moveto = new MoveTo(100, 100);
    // create a Path
    Path path = new Path(moveto, vlineto);
    // set fill for path
    path.setFill(Color.VIOLET);
    path.setStrokeDashOffset(5);
    // set stroke width
    path.setStrokeWidth(2);
    group.getChildren().add(path);
    return path;
  }

  private Path miaLinea(Group group) {
    MoveTo miaMvto = new MoveTo(150, 230);
    LineTo miaLinea = new LineTo(500., 130.);
    Path miapath = new Path(miaMvto, miaLinea);
    miapath.setFill(Color.BLUE);
    miapath.setStrokeWidth(1);
    group.getChildren().add(miapath);
    return miapath;
  }

  // Main Method
  public static void main(String args[]) {
    // launch the application
    Application.launch(args);
  }
}
