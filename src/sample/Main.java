package sample;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class Main extends Application {
    private static final int N = 4;
    private static final int Big = 10000;
    private static final double STICK_RADIUS = 0.2;
    private static final double STICK_DIST = 6;
    private static final double BALL_RADIUS = 1;
    private static final double STICK_H = N*2*BALL_RADIUS;
    private static final double MIN_CAMERA_DIST = 30;
    private static final double MAX_CAMERA_DIST = 60;
    private Color COLOR_1;
    private Color COLOR_2;
    private Color currentColor;
    private Stage window;
    private Label gameFinishedInfo;
    private Rotate yCameraRotate;
    private Rotate xCameraRotate;
    private Translate cameraTranslate;
    private double lastMouseX;
    private double lastMouseY;
    private double yCameraAngle = 20;
    private double xCameraAngle = -30;
    private int start;
    private int player = 1;
    private boolean isGameFinished = false;

    private boolean EndOfGame2(int[][][] isPresent, Coords c1){
        for(int i=-1; i<2; i++)
            for(int j=-1; j<2; j++)
                for(int k=-1; k<2; k++){
                    if(i==0 && j==0 && k==0) continue;
                    Coords c2 = new Coords(c1.getX()+i,c1.getY()+j,c1.getZ()+k);
                    Coords c3 = new Coords(c1.getX()+2*i,c1.getY()+2*j,c1.getZ()+2*k);
                    Coords c4 = new Coords(c1.getX()+3*i,c1.getY()+3*j,c1.getZ()+3*k);
                    if(c2.isCorrect && c3.isCorrect && c4.isCorrect){
                        if(isPresent[c1.getX()][c1.getY()][c1.getZ()]==
                                isPresent[c2.getX()][c2.getY()][c2.getZ()]&&isPresent[c2.getX()][c2.getY()][c2.getZ()]
                                ==isPresent[c3.getX()][c3.getY()][c3.getZ()]&&isPresent[c4.getX()][c4.getY()][c4.getZ()]
                                ==isPresent[c3.getX()][c3.getY()][c3.getZ()]) return true;}
                    c4 = new Coords(c1.getX()-i,c1.getY()-j,c1.getZ()-k);
                    if(c2.isCorrect && c3.isCorrect && c4.isCorrect){
                        if(isPresent[c1.getX()][c1.getY()][c1.getZ()]==
                                isPresent[c2.getX()][c2.getY()][c2.getZ()]&&isPresent[c2.getX()][c2.getY()][c2.getZ()]
                                ==isPresent[c3.getX()][c3.getY()][c3.getZ()]&&isPresent[c4.getX()][c4.getY()][c4.getZ()]
                                ==isPresent[c3.getX()][c3.getY()][c3.getZ()]) return true;}
                }
        return false;
    }

    private void alfabeta(GameState gs, boolean isMaximizer, int currentLevel, int terminalLevel, int player, int alfa, int beta){
        if(currentLevel==terminalLevel) {gs.ComputeValue(); return;}
        gs.ComputeValue();
        if(gs.getValue()==Big||gs.getValue()==-Big) return;

        gs.InitMoves(player);
        for (GameState g:gs.moves
        ) {
            alfabeta(g,!isMaximizer,currentLevel+1,terminalLevel,player==1?2:1,alfa,beta);
            if(isMaximizer) {
                if(g.getValue()>alfa) {alfa = g.getValue(); gs.setValue(alfa);}
                if(alfa>=beta) break;
            }
            else {
                if(g.getValue()<beta) {beta = g.getValue(); gs.setValue(beta);}
                if(alfa>=beta) break;
            }
        }
        //gs.ComputeValue(isMaximizer);
    }

    private Scene createGameArea() {
        // Sticks
        AnchorPane anchorPane = new AnchorPane();
        Scene scene = new Scene(anchorPane);
        Group root = new Group();
        Cylinder[] sticks = new Cylinder[N*N];
        int[] count = new int[N*N];
        int[][][] isPresent = new int[N][N][N];
        for(int i=0; i<N; i++)
            for(int j=0; j<N; j++) Arrays.fill(isPresent[i][j], 0);

        for (int i = -N/2; i < N/2; i++)
            for (int j=-N/2; j< N/2; j++)
            {
                Cylinder stick = new Cylinder(STICK_RADIUS,STICK_H);
                stick.setMaterial(new PhongMaterial(Color.GRAY));
                stick.setDrawMode(DrawMode.FILL);
                //stick.getTransforms().add(new Translate(i*STICK_DIST,-STICK_H/2,j*STICK_DIST));
                stick.setTranslateX(i*STICK_DIST + STICK_DIST/2);
                stick.setTranslateY(-STICK_H/2);
                stick.setTranslateZ(j*STICK_DIST + STICK_DIST/2);
                stick.setOnMouseClicked(event->{
                    if(isGameFinished) {
                        return;
                    }
                    double I = (stick.getTranslateX()-STICK_DIST/2)/STICK_DIST+N/2;
                    double J = (stick.getTranslateZ()-STICK_DIST/2)/STICK_DIST+N/2;
                    int k = (int)I+(int)J*N;
                    double y=0;
                    if(count[k]==N) return;
                    /*if(count[k]==0) y=-BALL_RADIUS;
                    if(count[k]==1) y=-3*BALL_RADIUS;
                    if(count[k]==2) y=-5*BALL_RADIUS;*/
                    y = -count[k]*2-1;

                    Sphere ball = new Sphere(BALL_RADIUS);
                    ball.setMaterial(new PhongMaterial(currentColor));
                    ball.setDrawMode(DrawMode.FILL);
                    ball.setTranslateX(stick.getTranslateX());
                    ball.setTranslateZ(stick.getTranslateZ());
                    ball.setTranslateY(y);
                    //balls[count[k]*9 + k] = ball;
                    isPresent[(int)I][(int)J][count[k]] = player;
                    root.getChildren().add(ball);

                    if(EndOfGame2(isPresent, new Coords((int)I,(int)J, count[k]))){
                        isGameFinished = true;
                        if(start==0) {
                            gameFinishedInfo.setText(player==1?"Player 1. wins!":"Player 2. wins!");
                            gameFinishedInfo.setTextFill(player==1?COLOR_1:COLOR_2);
                        }
                        else {
                            gameFinishedInfo.setText("You win!");
                            //gameFinishedInfo.setTextFill(COLOR_1);
                        }
                        return;
                    }
                    count[k]++;
                    if(start==0) {currentColor = currentColor==COLOR_1?COLOR_2:COLOR_1; player=player==1?2:1;}
                    else {GameState gs = new GameState(isPresent, new Coords(-1,-1,-1),N);
                        alfabeta(gs,true,0,start==1?1:4,2,-100000,100000);
                        Coords move = new Coords(-1,-1,-1);
                        for (GameState g:gs.moves
                        ) {
                            if(g.getValue()==gs.getValue()) {move = new Coords(g.getLastMove().getX(),g.getLastMove().getY(),g.getLastMove().getZ());break;}
                        }
                        Sphere ball1 = new Sphere(BALL_RADIUS);
                        ball1.setMaterial(new PhongMaterial(COLOR_2));
                        ball1.setDrawMode(DrawMode.FILL);
                        ball1.setTranslateX((move.getX()-N/2)*STICK_DIST+STICK_DIST/2);
                        ball1.setTranslateZ((move.getY()-N/2)*STICK_DIST+STICK_DIST/2);
                        ball1.setTranslateY(move.getZ()*(-2)-1);
                        root.getChildren().add(ball1);
                        isPresent[move.getX()][move.getY()][move.getZ()] = 2;
                        if(EndOfGame2(isPresent, move)){
                            isGameFinished = true;
                            gameFinishedInfo.setText("AI wins!");
                            //gameFinishedInfo.setTextFill(COLOR_2);
                            return;
                        }
                        count[move.getX()+N*move.getY()]++;
                    }
                });
                sticks[(i+N/2)+(j+N/2)*N] = stick;

            }
        Box surface = new Box(N * STICK_DIST, 0.5, N * STICK_DIST);
        surface.setMaterial(new PhongMaterial(Color.BROWN));
        surface.setDrawMode(DrawMode.FILL);

        Translate pivot = new Translate();
        yCameraRotate = new Rotate(yCameraAngle, Rotate.Y_AXIS);
        xCameraRotate = new Rotate(xCameraAngle, Rotate.X_AXIS);
        cameraTranslate = new Translate(0, 0, -48);
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
        subScene.setFill(Color.LIGHTYELLOW);
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
            if (xCameraAngle - dy < 0 && xCameraAngle - dy > -90)
                xCameraRotate.setAngle(xCameraAngle - dy);
        });
        subScene.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                if (cameraTranslate.getZ() < -MIN_CAMERA_DIST)
                    cameraTranslate.setZ(cameraTranslate.getZ() + 1);
            } else if (cameraTranslate.getZ() > -MAX_CAMERA_DIST)
                cameraTranslate.setZ(cameraTranslate.getZ() - 1);
        });
        window.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                try {
                    displayMenu(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        anchorPane.getChildren().add(subScene);

        gameFinishedInfo = new Label();
        gameFinishedInfo.setFont(Font.font ("Verdana", 20));
        gameFinishedInfo.setMinWidth(100);
//        Text gameFinishedInfo = new Text();
//        gameFinishedInfo.setText("This is a text sample");
//        gameFinishedInfo.setFont(Font.font ("Verdana", 20));
//        gameFinishedInfo.setFill(Color.RED);
        AnchorPane.setTopAnchor(gameFinishedInfo,10.0);
        AnchorPane.setLeftAnchor(gameFinishedInfo,20.0);
        anchorPane.getChildren().add(gameFinishedInfo);
        //Group group = new Group();
        //group.getChildren().add(subScene);
        //return new Scene(group);
        return scene;
    }

    private Scene createStartMenu() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("start_menu.fxml"));

        // Prepare comboBoxes for choosing colors
        List<Color> colorList = Arrays.asList(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.PINK, Color.WHITE);

        ComboBox<Color> cbFirst = (ComboBox<Color>) root.lookup("#cbFirstColor");
        ComboBox<Color> cbSecond = (ComboBox<Color>) root.lookup("#cbSecondColor");
        Callback<ListView<Color>, ListCell<Color>> cellFactory = new Callback<ListView<Color>, ListCell<Color>>() {
            @Override
            public ListCell<Color> call(ListView<Color> l) {
                return new ListCell<Color>() {
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
        btnPvP.setOnAction(e -> {
            COLOR_1 = cbFirst.getSelectionModel().getSelectedItem();
            COLOR_2 = cbSecond.getSelectionModel().getSelectedItem();
            currentColor = COLOR_1;
            switchScene(createGameArea());
            start = 0;
        });
        Button btnPvAIEasy = (Button) root.lookup("#btnPvAIEasy");
        btnPvAIEasy.setOnAction(e -> {
            COLOR_1 = cbFirst.getSelectionModel().getSelectedItem();
            COLOR_2 = cbSecond.getSelectionModel().getSelectedItem();
            currentColor = COLOR_1;
            switchScene(createGameArea());
            start = 1;
        });
        Button btnPvAIHard = (Button) root.lookup("#btnPvAIHard");
        btnPvAIHard.setOnAction(e -> {
            COLOR_1 = cbFirst.getSelectionModel().getSelectedItem();
            COLOR_2 = cbSecond.getSelectionModel().getSelectedItem();
            currentColor = COLOR_1;
            switchScene(createGameArea());
            start = 2;
        });
        return new Scene(root, 400, 600);
    }

    private void displayMenu(int winner) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("game_menu.fxml"));
        double centerXPosition = window.getX() + window.getWidth() / 2d;
        double centerYPosition = window.getY() + window.getHeight() / 2d;

        Button btnPlayAgain = (Button) root.lookup("#btnPlayAgain");
        Button btnStartMenu = (Button) root.lookup("#btnStartMenu");
        Button btnExit = (Button) root.lookup("#btnExit");

        btnPlayAgain.setOnAction(e -> {
            currentColor = COLOR_1;
            isGameFinished = false;
            switchScene(createGameArea());
            ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
        });
        btnStartMenu.setOnAction(e -> {
            try {
                isGameFinished = false;
                switchScene(createStartMenu());
                ((Stage) ((Button) e.getSource()).getScene().getWindow()).close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        btnExit.setOnAction(e -> {
            window.close();
        });

        Stage stage = new Stage();
        stage.setScene(new Scene(root, 300, 400));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(window);
        stage.setOnShown(ev -> {
            stage.setX(centerXPosition - stage.getWidth() / 2d);
            stage.setY(centerYPosition - stage.getHeight() / 2d);
            stage.show();
        });
        stage.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                stage.close();
            }
        });
        stage.show();
    }

    private void switchScene(Scene scene) {
        window.setScene(scene);
        centerStage(window);
    }

    private void centerStage(Stage stage) {
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
