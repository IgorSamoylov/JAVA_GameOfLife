package com.game;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class GameOfLife extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Upper control panel setup
        Button randomFillB = new Button("Random Fill");
        Button runB = new Button("Run");
        Button stopB = new Button("Stop");
        Button stepB = new Button("Step");
        Button clearB = new Button("Clear");

        HBox controlPanel1 = new HBox(20, randomFillB, runB, stopB, stepB, clearB);
        controlPanel1.setAlignment(Pos.BASELINE_CENTER);

        // Lower control panel setup
        Button fasterB = new Button("Faster");
        Button slowerB = new Button("Slower");
        Label aliveLabel = new Label("000000");
        Label epochLabel = new Label("000000");

        Button settingsB = new Button();
        Image settingsImage = new Image("settings-button.png");
        ImageView settingsImageView = new ImageView(settingsImage);
        settingsImageView.setFitHeight(25);
        settingsImageView.setPreserveRatio(true);
        settingsB.setGraphic(settingsImageView);

        Background labelBackground = new Background(
                new BackgroundFill(GameSettings.LABEL_BACKGROUND, null, null));
        aliveLabel.setBackground(labelBackground);
        epochLabel.setBackground(labelBackground);

        HBox controlPanel2 = new HBox(20, aliveLabel, fasterB, slowerB, epochLabel, settingsB);
        controlPanel2.setAlignment(Pos.BASELINE_CENTER);


        // Game Field setup
        final Canvas gameField = new Canvas(GameSettings.W_WIDTH, GameSettings.W_HEIGHT);

        // Main window setup
        VBox mainRoot = new VBox(20);
        Background mainRootBackground = new Background(
                new BackgroundFill(GameSettings.MAINWINDOW_BACKGROUND_COLOR, null, null));
        mainRoot.setBackground(mainRootBackground);

        Scene mainScene = new Scene(mainRoot, GameSettings.W_WIDTH, GameSettings.W_HEIGHT + 120);
        mainRoot.getChildren().addAll(gameField, controlPanel1, controlPanel2);
        primaryStage.setTitle("GAME OF LIFE");
        primaryStage.setResizable(false);
        primaryStage.setScene(mainScene);
        //primaryStage.setFullScreen(true);

        // Game engine init
        GraphicsContext gameFieldGraphics = gameField.getGraphicsContext2D();
        // Creating thread #1 for Game Engine
        GameEngine gameEngine = new GameEngine(gameFieldGraphics, aliveLabel, epochLabel);
        gameEngine.start();
        gameEngine.clearField();
        // Creating thread #2 for Animation Timer
        GameAnimationTimer animationTimer = new GameAnimationTimer(gameEngine);
        animationTimer.start();

        // Buttons bindings setup
        gameField.setOnMouseClicked(mouseEvent ->
                gameEngine.drawCell(
                        (int)(mouseEvent.getSceneX() / GameSettings.CELL_SIZE),
                        (int)(mouseEvent.getSceneY() / GameSettings.CELL_SIZE)
                ));
        randomFillB.setOnAction(keyEvent -> gameEngine.randomFill());
        runB.setOnAction(keyEvent -> animationTimer.animationEngine.start());
        stepB.setOnAction(keyEvent -> gameEngine.nextStep());
        stopB.setOnAction(keyEvent -> animationTimer.animationEngine.stop());
        clearB.setOnAction(keyEvent -> {
            gameEngine.clearField();
            animationTimer.animationEngine.stop();
        });
        fasterB.setOnAction(keyEvent -> GameSettings.GAME_REFRESH_DELAY -= 50);
        slowerB.setOnAction(keyEvent -> GameSettings.GAME_REFRESH_DELAY += 50);
        settingsB.setOnAction(event -> {
            animationTimer.animationEngine.stop();
            Stage settingsStage = new Stage();
            new SettingsWindow().start(settingsStage);
        });

        // Alert on close window setup
        Alert closeWindowAlert = new Alert(Alert.AlertType.CONFIRMATION);
        closeWindowAlert.setContentText("Really close?");
        primaryStage.setOnCloseRequest(event ->
                closeWindowAlert.showAndWait().ifPresent(btnType -> {
                    if (btnType == ButtonType.CANCEL) event.consume();
                }));

        // Create main window
        primaryStage.show();
    }
}
