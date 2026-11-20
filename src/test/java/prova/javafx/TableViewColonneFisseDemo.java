package prova.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class TableViewColonneFisseDemo extends Application {

    private static final int N_COLONNE = 26;
    private static final int N_FISSE = 2;
    private static final double ALTEZZA_RIGA = 26;

    record Riga(String[] valori) {}

    @Override
    public void start(Stage stage) {
        // --- dati di prova ---
        ObservableList<Riga> obsArrList = FXCollections.observableArrayList();
        for (int r = 0; r < 200; r++) {
            String[] v = new String[N_COLONNE];
            for (int c = 0; c < N_COLONNE; c++) 
              v[c] = "R" + r + "C" + c;
            obsArrList.add(new Riga(v));
        }

        // --- le due tabelle ---
        TableView<Riga> tblvSinistra = new TableView<>();
        TableView<Riga> tblvDestra = new TableView<>();

        for (int c = 0; c < N_COLONNE; c++) {
            final int idx = c;
            TableColumn<Riga, String> col = new TableColumn<>("Colonna " + (c + 1));
            col.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().valori()[idx]));
            col.setPrefWidth(110);
            (c < N_FISSE ? tblvSinistra : tblvDestra).getColumns().add(col);
        }

        // stessa lista (ordinabile) per entrambe
        SortedList<Riga> ordinata = new SortedList<>(obsArrList);
        tblvSinistra.setItems(ordinata);
        tblvDestra.setItems(ordinata);

        // righe della stessa altezza, indispensabile per l'allineamento
        tblvSinistra.setFixedCellSize(ALTEZZA_RIGA);
        tblvDestra.setFixedCellSize(ALTEZZA_RIGA);

        // stessa selezione (modalità a righe)
        tblvSinistra.setSelectionModel(tblvDestra.getSelectionModel());

        // ordinamento gestito da noi tramite la SortedList condivisa
        tblvSinistra.setSortPolicy(_ -> true);
        tblvDestra.setSortPolicy(_ -> true);
        collegaOrdinamento(tblvSinistra, tblvDestra, ordinata);
        collegaOrdinamento(tblvDestra, tblvSinistra, ordinata);

        // larghezza della tabella sinistra = somma delle sue colonne (+ bordo)
        var larghezza = Bindings.createDoubleBinding(
                () -> tblvSinistra.getColumns().stream().mapToDouble(TableColumn::getWidth).sum() + 2,
                tblvSinistra.getColumns().stream().map(TableColumn::widthProperty).toArray(Observable[]::new));
        tblvSinistra.minWidthProperty().bind(larghezza);
        tblvSinistra.prefWidthProperty().bind(larghezza);
        tblvSinistra.maxWidthProperty().bind(larghezza);

        // --- layout ---
        HBox root = new HBox(tblvSinistra, tblvDestra);
        HBox.setHgrow(tblvDestra, Priority.ALWAYS);

        stage.setScene(new Scene(root, 900, 500));
        stage.setTitle("Colonne fisse");
        stage.show();

        // le scrollbar esistono solo dopo che la skin è stata creata
        Platform.runLater(() -> sincronizzaScroll(tblvSinistra, tblvDestra));
    }

    /** Quando ordini da una tabella, azzera l'ordinamento dell'altra e aggiorna la SortedList. */
    private static void collegaOrdinamento(TableView<Riga> a, TableView<Riga> b, SortedList<Riga> ordinata) {
        a.getSortOrder().addListener((ListChangeListener<TableColumn<Riga, ?>>) _ -> {
            if (!a.getSortOrder().isEmpty()) b.getSortOrder().clear();
        });
        a.comparatorProperty().addListener((_, _, nuovo) -> {
            if (nuovo != null) ordinata.setComparator(nuovo);
            else if (b.getSortOrder().isEmpty()) ordinata.setComparator(null);
        });
    }

    private static void sincronizzaScroll(TableView<Riga> sinistra, TableView<Riga> destra) {
        ScrollBar vSin = trovaScrollBar(sinistra, Orientation.VERTICAL);
        ScrollBar vDes = trovaScrollBar(destra, Orientation.VERTICAL);
        ScrollBar hDes = trovaScrollBar(destra, Orientation.HORIZONTAL);
        if (vSin == null || vDes == null || hDes == null) return;

        // scroll verticale sincronizzato nei due sensi
        vSin.valueProperty().bindBidirectional(vDes.valueProperty());

        // nascondo la scrollbar verticale della tabella di sinistra
        vSin.setStyle("-fx-min-width: 0; -fx-pref-width: 0; -fx-max-width: 0; -fx-opacity: 0;");
        vSin.setMouseTransparent(true);

        // la tabella di destra perde in altezza lo spazio della scrollbar orizzontale:
        // lo tolgo anche a sinistra, così le righe restano allineate fino in fondo
        sinistra.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(0, 0, hDes.isVisible() ? hDes.getHeight() : 0, 0),
                hDes.visibleProperty(), hDes.heightProperty()));
    }

    private static ScrollBar trovaScrollBar(Node tabella, Orientation orientamento) {
        for (Node n : tabella.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar sb && sb.getOrientation() == orientamento) return sb;
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}