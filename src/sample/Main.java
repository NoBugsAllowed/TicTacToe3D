package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends Application {
    private static final double STICK_RADIUS = 0.2;
    private static final double STICK_H = 6;
    private static final double STICK_DIST = 4;
    private static final double BALL_RADIUS = 1;
    private Stage window;
    public ComboBox cbFirstColor;
    private Rotate yCameraRotate;
    private Rotate xCameraRotate;
    private Translate cameraTranslate;
    private double lastMouseX;
    private double lastMouseY;
    private double yCameraAngle = 20;
    private double xCameraAngle = -30;

    private void addBall(Group group, int i, int j, int k, Color color) {
        Sphere ball = new Sphere(BALL_RADIUS);
        ball.setMaterial(new PhongMaterial(color));
        ball.setDrawMode(DrawMode.FILL);
        ball.getTransforms().add(new Translate((i - 1) * STICK_DIST, -(k * 2 + 1) * BALL_RADIUS, (j - 1) * STICK_DIST));
        group.getChildren().add(ball);
    }

    private Scene createGameArea() {
        // Sticks
        List<Cylinder> sticks = new ArrayList<>(9);
        for (int i = -1; i < 2; i++)
            for (int j = -1; j < 2; j++) {
                Cylinder stick = new Cylinder(STICK_RADIUS, STICK_H);
                stick.setMaterial(new PhongMaterial(Color.GRAY));
                stick.setDrawMode(DrawMode.FILL);
                stick.getTransforms().add(new Translate(i * STICK_DIST, -STICK_H / 2, j * STICK_DIST));
                sticks.add(stick);
            }
        Box surface = new Box(3 * STICK_DIST, 0.5, 3 * STICK_DIST);
        surface.setMaterial(new PhongMaterial(Color.BROWN));
        surface.setDrawMode(DrawMode.FILL);

        Translate pivot = new Translate();
        yCameraRotate = new Rotate(yCameraAngle, Rotate.Y_AXIS);
        xCameraRotate = new Rotate(xCameraAngle, Rotate.X_AXIS);
        cameraTranslate = new Translate(0, 0, -35);
        // Create and position camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
                pivot,
                yCameraRotate,
                xCameraRotate,
                cameraTranslate);

        // Build the Scene Graph
        Group root = new Group();
        root.getChildren().add(camera);
        root.getChildren().addAll(sticks);
        root.getChildren().addAll(surface);

        // Tymczasowe
        addBall(root, 1, 1, 0, Color.RED);
        addBall(root, 0, 1, 0, Color.BLUE);
        addBall(root, 0, 1, 1, Color.BLUE);

        // Use a SubScene
        SubScene subScene = new SubScene(root, 800, 500, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.ALICEBLUE);
        subScene.setCamera(camera);
        subScene.setOnMousePressed(event -> {
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
            yCameraAngle = yCameraRotate.getAngle();
            xCameraAngle = xCameraRotate.getAngle();
        });
        subScene.setOnMouseReleased(event -> {
            yCameraAngle = yCameraRotate.getAngle();
            xCameraAngle = xCameraRotate.getAngle();
        });
        subScene.setOnMouseDragged(event -> {
            double dx = (event.getSceneX() - lastMouseX) * 180 / subScene.getWidth();
            double dy = (event.getSceneY() - lastMouseY) * 70 / subScene.getHeight();
            yCameraRotate.setAngle(yCameraAngle + dx);
            xCameraRotate.setAngle(xCameraAngle - dy);
        });
        subScene.setOnScroll(event -> {
            if (event.getDeltaY() > 0)
                cameraTranslate.setZ(cameraTranslate.getZ() + 1);
            else
                cameraTranslate.setZ(cameraTranslate.getZ() - 1);
        });

        Group group = new Group();
        group.getChildren().add(subScene);
        return new Scene(group);
    }
    private Scene createStartMenu() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("start_menu.fxml"));

        // Prepare comboBoxes for chosing colors
        List<Color> colorList = Arrays.asList(Color.BLUE, Color.RED, Color.WHITE);

        ComboBox<Color> cbFirst = (ComboBox<Color>) root.lookup("#cbFirstColor");
        ComboBox<Color> cbSecond = (ComboBox<Color>) root.lookup("#cbSecondColor");
        Callback<ListView<Color>, ListCell<Color>> cellFactory = new Callback<>() {
            @Override
            public ListCell<Color> call(ListView<Color> l) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setStyle("-fx-background-color: #" + item.toString().substring(2, 8));
                        }
                    }
                };
            }
        };
        cbFirst.setItems(FXCollections.observableArrayList(colorList));
        cbFirst.setButtonCell(cellFactory.call(null));
        cbFirst.setCellFactory(cellFactory);
        cbFirst.valueProperty().addListener((ov, oldCol, newCol) -> {
            if (newCol == cbSecond.getSelectionModel().getSelectedItem()) {
                cbSecond.getSelectionModel().select(oldCol);
            }
        });
        cbSecond.setItems(FXCollections.observableArrayList(colorList));
        cbSecond.setButtonCell(cellFactory.call(null));
        cbSecond.setCellFactory(cellFactory);
        cbSecond.valueProperty().addListener((ov, oldCol, newCol) -> {
            if (newCol == cbFirst.getSelectionModel().getSelectedItem()) {
                cbFirst.getSelectionModel().select(oldCol);
            }
        });
        cbFirst.getSelectionModel().select(0);
        cbSecond.getSelectionModel().select(1);

        // Add handlers to buttons
        Button btnPvP = (Button) root.lookup("#btnPvP");
        btnPvP.setOnAction(e -> switchScene(createGameArea()));

        return new Scene(root,400,600);
    }

    public void switchScene(Scene scene) {
        window.setScene(scene);
        centerStage(window);
    }

    public void centerStage(Stage stage) {
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        window = primaryStage;

        primaryStage.setTitle("Tic Tac Toe");
        primaryStage.setMinWidth(320);
        primaryStage.setMinHeight(400);
        primaryStage.setResizable(false);
        primaryStage.setScene(createStartMenu());

        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
