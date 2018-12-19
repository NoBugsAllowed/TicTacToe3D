package sample;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

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
    private Rotate yCameraRotate;
    private Rotate xCameraRotate;
    private Translate cameraTranslate;
    private double lastMouseX;
    private double lastMouseY;
    private double yCameraAngle = 20;
    private double xCameraAngle = -30;

    private Parent createContent() {
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

        //Sphere ball = new Sphere(BALL_RADIUS);
        //ball.setMaterial(new PhongMaterial(Color.RED));
        //ball.setDrawMode(DrawMode.FILL);
        //ball.setTranslateY(-BALL_RADIUS);
        //Sphere ball2 = new Sphere(BALL_RADIUS);
        //ball2.setMaterial(new PhongMaterial(Color.BLUE));
        //ball2.setDrawMode(DrawMode.FILL);
        //ball2.setTranslateY(-3*BALL_RADIUS);

        Translate pivot = new Translate();
        yCameraRotate = new Rotate(yCameraAngle, Rotate.Y_AXIS);
        xCameraRotate = new Rotate(xCameraAngle, Rotate.X_AXIS);
        cameraTranslate = new Translate(0, 0, -35);
        // Create and position camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll (
                pivot,
                yCameraRotate,
                xCameraRotate,
                cameraTranslate);

        // Build the Scene Graph

        root.getChildren().add(camera);
        //root.getChildren().add(ball);
        //root.getChildren().add(ball2);
        root.getChildren().addAll(sticks);


        // Use a SubScene
        SubScene subScene = new SubScene(root, 800,500, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.ALICEBLUE);
        subScene.setCamera(camera);

        Group group = new Group();
        group.getChildren().add(subScene);
        return group;
    }

    @Override
    public void start(Stage primaryStage) {
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("Tic Tac Toe");

        scene.setOnMousePressed(event->{
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
            yCameraAngle = yCameraRotate.getAngle();
            xCameraAngle = xCameraRotate.getAngle();
        });
        scene.setOnMouseReleased(event->{
            yCameraAngle = yCameraRotate.getAngle();
            xCameraAngle = xCameraRotate.getAngle();
        });
        scene.setOnMouseDragged(event->{
            double dx = (event.getSceneX()-lastMouseX)*180/scene.getWidth();
            double dy = (event.getSceneY()-lastMouseY)*70/scene.getHeight();
            yCameraRotate.setAngle(yCameraAngle + dx);
            xCameraRotate.setAngle(xCameraAngle - dy);
        });
        scene.setOnScroll(event->{
            if(event.getDeltaY()>0)
                cameraTranslate.setZ(cameraTranslate.getZ()+1);
            else
                cameraTranslate.setZ(cameraTranslate.getZ()-1);
        });
        scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode()== KeyCode.S)
                for(int i=0; i<27; i++) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}