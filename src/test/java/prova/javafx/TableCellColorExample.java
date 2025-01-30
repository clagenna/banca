package prova.javafx;

import java.util.Random;
import java.util.function.Function;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
/**
 * preso da https://gist.github.com/james-d/0d4c1ce6b54394834816
 */
public class TableCellColorExample extends Application {

  private final Random RNG = new Random();

  @Override
  public void start(Stage primaryStage) {

    // Table. Item is defined below: just has three integer properties, x, y, z.
    TableView<Item> table = new TableView<>();
    table.setEditable(true);

    // Columns for x, y, and z repspectively:
    TableColumn<Item, Integer> xCol = column("X", item -> item.xProperty().asObject());
    TableColumn<Item, Integer> yCol = column("Y", item -> item.yProperty().asObject());
    TableColumn<Item, Integer> zCol = column("Z", item -> item.zProperty().asObject());

    // Default cell factory provides text field for editing and converts text in text field to int.
    // Note in real life you might want a custom implementation that restricts input to valid values.
    Callback<TableColumn<Item, Integer>, TableCell<Item, Integer>> defaultCellFactory = TextFieldTableCell.forTableColumn(new IntegerStringConverter());

    // Cell factory implementation that uses default cell factory above, and augments the implementation
    // by updating the value of the looked-up color cell-selection-color for the cell when the item changes:
    Callback<TableColumn<Item, Integer>, TableCell<Item, Integer>> cellFactory = col -> {
      TableCell<Item, Integer> cell = defaultCellFactory.call(col);
      cell.itemProperty().addListener((obs, oldValue, newValue) -> {
        if (newValue == null) {
          cell.setStyle("cell-selection-color: -fx-selection-bar ;");
        } else {
          Color color = createColor(newValue.intValue());
          String formattedColor = formatColor(color);
          cell.setStyle("cell-selection-color: " + formattedColor + " ;");
        }
      });
      return cell;
    };

    // attach cell factory defined above to each column:
    xCol.setCellFactory(cellFactory);
    yCol.setCellFactory(cellFactory);
    zCol.setCellFactory(cellFactory);

    // add each column to table:
    table.getColumns().add(xCol);
    table.getColumns().add(yCol);
    table.getColumns().add(zCol);

    // create 100 items for table with random values:
    for (int i = 0; i < 100; i++) {
      table.getItems().add(new Item(RNG.nextInt(360), RNG.nextInt(360), RNG.nextInt(360)));
    }

    // simple layout:
    BorderPane root = new BorderPane(table);
    Scene scene = new Scene(root, 600, 600);

    // add stylesheet that applies looked-up color cell-selection-color to cells in selected row:
    scene.getStylesheets().add("table-cell-color-example.css");

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  // Create color based on int value. Just use value as hue, full saturation and brightness:
  private Color createColor(int x) {
    return Color.hsb(x, 1.0, 1.0);
  }

  // Format color as string for CSS (#rrggbb format, values in hex).
  private String formatColor(Color c) {
    int r = (int) (255 * c.getRed());
    int g = (int) (255 * c.getGreen());
    int b = (int) (255 * c.getBlue());
    return String.format("#%02x%02x%02x", r, g, b);
  }

  // Utility method to create table columns for title and property
  private <S, T> TableColumn<S, T> column(String title, Function<S, ObservableValue<T>> property) {
    TableColumn<S, T> col = new TableColumn<>(title);
    col.setCellValueFactory(cellData -> property.apply(cellData.getValue()));
    col.setPrefWidth(150);
    return col;
  }

  // Simple model class
  public static class Item {
    private IntegerProperty x = new SimpleIntegerProperty();
    private IntegerProperty y = new SimpleIntegerProperty();
    private IntegerProperty z = new SimpleIntegerProperty();

    public Item(int x, int y, int z) {
      setX(x);
      setY(y);
      setZ(z);
    }

    public final IntegerProperty xProperty() {
      return this.x;
    }

    public final int getX() {
      return this.xProperty().get();
    }

    public final void setX(final int x) {
      this.xProperty().set(x);
    }

    public final IntegerProperty yProperty() {
      return this.y;
    }

    public final int getY() {
      return this.yProperty().get();
    }

    public final void setY(final int y) {
      this.yProperty().set(y);
    }

    public final IntegerProperty zProperty() {
      return this.z;
    }

    public final int getZ() {
      return this.zProperty().get();
    }

    public final void setZ(final int z) {
      this.zProperty().set(z);
    }

  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}
