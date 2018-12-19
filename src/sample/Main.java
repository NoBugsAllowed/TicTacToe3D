package sample;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
    private static final double STICK_RADIUS = 0.2;
    private static final double STICK_H = 6;
    private static final double STICK_DIST = 4;
    private static final double BALL_RADIUS = 1;
    private static final Color COLOR_1 = Color.RED;
    private static final Color COLOR_2 = Color.AQUA;
    private Color currentColor = COLOR_1;
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
        Group root = new Group();
        Cylinder[] sticks = new Cylinder[9];
        int[] count = new int[9];
        Sphere[] balls = new Sphere[27];
        for (int i = -1; i < 2; i++)
            for (int j=-1;j<2;j++)
            {
                Cylinder stick = new Cylinder(STICK_RADIUS,STICK_H);
                stick.setMaterial(new PhongMaterial(Color.GRAY));
                stick.setDrawMode(DrawMode.FILL);
                //stick.getTransforms().add(new Translate(i*STICK_DIST,-STICK_H/2,j*STICK_DIST));
                stick.setTranslateX(i*STICK_DIST);
                stick.setTranslateY(-STICK_H/2);
                stick.setTranslateZ(j*STICK_DIST);
                stick.setOnMouseClicked(event->{
                    double I = stick.getTranslateX()/STICK_DIST;
                    double J = stick.getTranslateZ()/STICK_DIST;
                    int k = (int)((I+1)+(J+1)*3);
                    double y=0;
                    if(count[k]==3) return;
                    if(count[k]==0) y=-BALL_RADIUS;
                    if(count[k]==1) y=-3*BALL_RADIUS;
                    if(count[k]==2) y=-5*BALL_RADIUS;

                    Sphere ball = new Sphere(BALL_RADIUS);
                    ball.setMaterial(new PhongMaterial(currentColor));
                    ball.setDrawMode(DrawMode.FILL);
                    ball.setTranslateX(stick.getTranslateX());
                    ball.setTranslateZ(stick.getTranslateZ());
                    ball.setTranslateY(y);
                    balls[count[k]*9 + k] = ball;
                    root.getChildren().add(ball);
                    count[k]++;
                    currentColor = currentColor==COLOR_1?COLOR_2:COLOR_1;
                });
                sticks[(i+1)+(j+1)*3] = stick;

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
        root.getChildren().add(camera);
        root.getChildren().addAll(sticks);
        root.getChildren().addAll(surface);

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
