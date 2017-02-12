package icurves.guifx;

import icurves.CurvesApp;
import icurves.concrete.ConcreteDiagram;
import icurves.description.AbstractCurve;
import icurves.description.Description;
import icurves.diagram.Curve;
import icurves.diagram.DiagramCreator;
import icurves.graph.EulerDualNode;
import icurves.graph.MED;
import icurves.util.Examples;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class Controller {

    private SettingsController settings;

    @FXML
    private FXRenderer renderer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Slider sliderZoom;

    @FXML
    private Menu menuDiagrams;

    @FXML
    private TextField fieldInput;

//    @FXML
//    private TextArea areaInfo;

    private Alert progressDialog = new Alert(Alert.AlertType.INFORMATION);

    private List<Description> historyUndo = new ArrayList<>();
    private List<Description> historyRedo = new ArrayList<>();
    private Description currentDescription = Description.from("");

    public void initialize() {

        dialogSettings = new Dialog<>();
        dialogSettings.setTitle("Settings");
        dialogSettings.getDialogPane().getButtonTypes().add(ButtonType.OK);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ui_settings.fxml"));

            Parent root = fxmlLoader.load();
            settings = fxmlLoader.getController();

            // set global settings hack
            CurvesApp.getInstance().setSettings(settings);

            dialogSettings.getDialogPane().setContent(root);
        } catch (Exception e) {
            showError(e);
        }


        //areaInfo.setVisible(false);
        renderer.setTranslateX(-1000);
        renderer.setTranslateY(-1000);

        //renderer.prefWidthProperty().bind(sliderZoom.valueProperty().divide(100).multiply(4000));
        //renderer.prefHeightProperty().bind(sliderZoom.valueProperty().divide(100).multiply(4000));

        renderer.scaleXProperty().bind(sliderZoom.valueProperty().divide(100));
        renderer.scaleYProperty().bind(sliderZoom.valueProperty().divide(100));

        fieldInput.setOnAction(e -> {
            Description ad = Description.from(fieldInput.getText());
            historyUndo.add(ad);
            visualize(ad);
        });

        fieldInput.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Z) {
                undo();
            }
        });

        historyUndo.add(currentDescription);

        initMenuDiagrams();

        progressDialog.setTitle("Working...");
        progressDialog.setHeaderText("Generating...");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressDialog.getDialogPane().setContent(progressIndicator);
    }

    private void initMenuDiagrams() {
        Examples.INSTANCE.getList().forEach(pair -> {
            MenuItem item = new MenuItem(pair.getFirst());
            item.setOnAction(e -> visualize(pair.getSecond()));

            menuDiagrams.getItems().addAll(item);
        });
//
//        MenuItem itemExamples = new MenuItem("Examples");
//        itemExamples.setOnAction(e -> {
//            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
//
//            ListView<ExampleDiagram> list = new ListView<>(FXCollections.observableArrayList(ExampleData.exampleDiagrams));
//
//            ScrollPane scrollPane = new ScrollPane(list);
//
//            dialog.getDialogPane().setContent(scrollPane);
//
//            dialog.showAndWait().ifPresent(buttonType -> {
//                ExampleDiagram diagram = list.getSelectionModel().getSelectedItem();
//                if (diagram != null) {
//                    visualize(Description.from(diagram.description));
//                }
//            });
//        });
//
//        menuDiagrams.getItems().addAll(itemExamples);
    }

    @FXML
    private void new_() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("ui_main.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("iCirclesFX");
            stage.show();
        } catch (Exception e) {
            showError(e);
        }
    }

    @FXML
    private void open() {
        // TODO: load data in via a dialog

        Description ad = Description.from("a b c ab bc abd bcd");
        visualize(ad);
    }

    @FXML
    private void save() {
        // TODO: save whatever we are working on now
        showError(new UnsupportedOperationException("Not yet implemented!"));
    }

    @FXML
    private void saveAs() {
        ExtensionFilter filterPNG = new ExtensionFilter("PNG image", "*.png");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As...");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.setInitialFileName("diagram");
        fileChooser.getExtensionFilters().addAll(filterPNG);
        fileChooser.setSelectedExtensionFilter(filterPNG);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            WritableImage fxImage = renderer.snapshot(null, null);
            BufferedImage img = SwingFXUtils.fromFXImage(fxImage, null);

            try {
                ImageIO.write(img, "png", file);
            } catch (IOException e) {
                showError(e);
            }
        }
    }

    @FXML
    private void quit() {
        System.exit(0);
    }

    @FXML
    private void undo() {
        if (historyUndo.size() > 1) {
            historyRedo.add(historyUndo.remove(historyUndo.size() - 1));

            Description ad = historyUndo.get(historyUndo.size() - 1);
            fieldInput.setText(ad.getInformalDescription());
            visualize(ad);
        }
    }

    @FXML
    private void redo() {
        if (!historyRedo.isEmpty()) {
            Description ad = historyRedo.remove(historyRedo.size() - 1);
            historyUndo.add(ad);

            fieldInput.setText(ad.getInformalDescription());
            visualize(ad);
        }
    }

    @FXML
    private void about() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About iCirclesFX");
        alert.setHeaderText(null);
        alert.setContentText("iCirclesFX is a set visualization library.");
        alert.show();
    }

    private Dialog<ButtonType> dialogSettings;

    @FXML
    private void settings() {
        dialogSettings.showAndWait();
    }

    private void showError(Throwable e) {
        System.out.println("Caught error:\n");
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ooops");
        alert.setContentText(e.getMessage());
        alert.show();
    }

    private void visualize(Description description) {
        System.out.println("Visualizing: " + description.getInformalDescription());

        progressDialog.show();

        fieldInput.setText(description.getInformalDescription());
        currentDescription = description;
        int size = (int) Math.min(renderer.getWidth(), renderer.getHeight());

        Task<ConcreteDiagram> task = new CreateDiagramTask(description);

        Thread t = new Thread(task, "Diagram Creation Thread");
        t.start();
    }

    /**
     * A task of creating a diagram and subsequently drawing it on the screen.
     */
    private class CreateDiagramTask extends Task<ConcreteDiagram> {

        private Description description;

        private long generationTime;

        private DiagramCreator newCreator;

        private double fieldSize = 4000.0;

        public CreateDiagramTask(Description description) {
            this.description = description;

            renderer.setPrefSize(fieldSize, fieldSize);
            renderer.setCanvasSize(fieldSize, fieldSize);

            //renderer.setScaleX(0.15);
            //renderer.setScaleY(0.15);

            renderer.clearSceneGraph();
        }

        @Override
        protected ConcreteDiagram call() throws Exception {
            long startTime = System.nanoTime();

            Platform.runLater(renderer::clearRenderer);

            // else do Hamiltonian
            newCreator = new DiagramCreator(settings);

            newCreator.getCurveToContour().addListener((MapChangeListener<? super AbstractCurve, ? super Curve>) change -> {
                if (change.wasAdded()) {
                    Curve c = change.getValueAdded();

                    renderer.addContour(c);
                }
            });

            newCreator.createDiagram(description);

            generationTime = System.nanoTime() - startTime;

            System.out.printf("Diagram creation took: %.3f sec\n", generationTime / 1000000000.0);

            return null;
        }

        @Override
        protected void succeeded() {

            ConcreteDiagram diagram = getValue();

            if (diagram != null) {

                renderer.draw(diagram);

            } else {

                // draw any debug points
                newCreator.getDebugPoints().forEach(p -> {
                    Circle point = new Circle(p.getX(), p.getY(), 10, Color.LIGHTSKYBLUE);

                    Text coord = new Text((int) p.getX() + "," + (int) p.getY());
                    coord.setTranslateX(p.getX());
                    coord.setTranslateY(p.getY() - 10);

                    renderer.rootSceneGraph.getChildren().addAll(point, coord);
                });

                newCreator.getDebugShapes().forEach(shape -> {
                    System.out.println("adding debug info");
                    shape.setFill(Color.RED);
                    shape.setStrokeWidth(5);
                    shape.setStroke(Color.BLUE);

                    renderer.rootSceneGraph.getChildren().addAll(shape);
                });

                if (settings.showMED()) {

                    MED modifiedDual = newCreator.getModifiedDual();

                    // draw MED nodes
                    modifiedDual.getNodes().stream().map(EulerDualNode::getPoint).forEach(p -> {
                        Circle point = new Circle(p.getX(), p.getY(), 10, Color.RED);

                        Text coord = new Text((int) p.getX() + "," + (int) p.getY());
                        coord.setTranslateX(p.getX());
                        coord.setTranslateY(p.getY() - 10);

                        renderer.rootSceneGraph.getChildren().addAll(point, coord);
                    });

                    // draw MED edges
                    modifiedDual.getEdges().forEach(e -> {
                        e.getCurve().setStroke(Color.RED);
                        e.getCurve().setStrokeWidth(6);
                        renderer.rootSceneGraph.getChildren().addAll(e.getCurve());

//                    e.getCurve().setOnMouseClicked(event -> {
//                        e.getCurve().setStroke(Color.YELLOW);
//                    });
                    });
                }

                // add shaded zones
                newCreator.getConcreteShadedZones().forEach(zone -> {
                    Shape shape = zone.getShape();
                    shape.setFill(Color.GRAY);

                    renderer.rootShadedZones.getChildren().addAll(shape);
                });
            }

//            try {
//                long startTime = System.nanoTime();
//
//                renderer.draw(diagram, cbEulerDual.isSelected());
//
//                // highlighting
//                renderer.getShadedZones().forEach(zone -> {
//                    zone.setOnMouseEntered(e -> {
//                        ((Shape) zone).setFill(Color.YELLOW);
//                        areaInfo.setText(((ConcreteZone) zone.getUserData()).toDebugString());
//                    });
//
//                    zone.setOnMouseExited(e -> {
//                        ((Shape) zone).setFill(Color.TRANSPARENT);
//                    });
//                });
//
//                long estimatedTime = System.nanoTime() - startTime;
//
//                System.out.printf("Diagram creation took: %.3f sec\n", generationTime / 1000000000.0);
//                System.out.printf("Diagram drawing took:  %.3f sec\n", estimatedTime / 1000000000.0);
//            } catch (Exception e) {
//                showError(e);
//            }


            progressDialog.hide();
        }

        @Override
        protected void failed() {
            progressDialog.hide();

            Throwable error = getException();
            if (error != null) {
                showError(error);

                newCreator.getDebugShapes().forEach(shape -> {
                    System.out.println("adding debug info");
                    shape.setFill(Color.RED);
                    shape.setStrokeWidth(5);
                    shape.setStroke(Color.BLUE);

                    renderer.rootSceneGraph.getChildren().addAll(shape);
                });
            }
            else
                showError(new RuntimeException("Unresolved error. Exception returned null"));

            succeeded();
        }
    }
}
