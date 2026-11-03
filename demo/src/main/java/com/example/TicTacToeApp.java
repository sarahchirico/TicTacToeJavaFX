package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javafx.application.Application;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/** JavaFX application for playing Tic-Tac-Toe against the CPU. */
public class TicTacToeApp extends Application {
    private final Board board = new Board();
    private final HumanPlayer human = new HumanPlayer('X', new Scanner(""));
    private final CpuPlayer cpu = new CpuPlayer('O');
    private final Button[][] cells = new Button[3][3];
    private final Random random = new Random();
    private final List<ParallelTransition> confettiAnimations = new ArrayList<>();

    private Player currentPlayer = human;
    private Label status;
    private boolean gameOver;
    private Pane confettiLayer;
    private Timeline confettiSpawner;

    @Override
    public void start(Stage stage) {
        Label eyebrow = new Label("HUMAN VS CPU");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("Tic-Tac-Toe");
        title.getStyleClass().add("title");

        status = new Label();
        status.getStyleClass().add("status");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("board");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int cellRow = row;
                int cellColumn = column;
                Button cell = new Button();
                cell.getStyleClass().add("cell");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                cell.setOnAction(event -> playHumanMove(cellRow, cellColumn));
                cells[row][column] = cell;
                grid.add(cell, column, row);
                GridPane.setHgrow(cell, Priority.ALWAYS);
                GridPane.setVgrow(cell, Priority.ALWAYS);
            }
        }

        Button restart = new Button("New game");
        restart.getStyleClass().add("restart-button");
        restart.setOnAction(event -> resetGame());

        Label hint = new Label("Choose an empty square to make your move.");
        hint.getStyleClass().add("hint");

        VBox content = new VBox(18, eyebrow, title, status, grid, hint, restart);
        content.getStyleClass().add("content");
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

        confettiLayer = new Pane();
        confettiLayer.setMouseTransparent(true);
        StackPane root = new StackPane(content, confettiLayer);
        Scene scene = new Scene(root);
        confettiLayer.prefWidthProperty().bind(scene.widthProperty());
        confettiLayer.prefHeightProperty().bind(scene.heightProperty());
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setTitle("Tic-Tac-Toe");
        stage.setScene(scene);
        stage.setResizable(false);
        resetGame();
        stage.show();
    }

    private void playHumanMove(int row, int column) {
        if (gameOver || currentPlayer != human || !human.makeMove(board, row, column)) {
            return;
        }
        updateBoard();
        if (finishIfGameOver()) {
            return;
        }

        currentPlayer = cpu;
        cpu.makeMove(board);
        updateBoard();
        if (!finishIfGameOver()) {
            currentPlayer = human;
            updateStatus();
        }
    }

    private boolean finishIfGameOver() {
        if (board.checkWin()) {
            gameOver = true;
            status.setText(playerName(currentPlayer) + " wins! ٩(^ᗜ^ )و");
            updateBoard();
            startConfetti();
            return true;
        }
        if (board.isFull()) {
            gameOver = true;
            status.setText("It's a draw!");
            updateBoard();
            return true;
        }
        return false;
    }

    private void resetGame() {
        stopConfetti();
        board.initialize();
        currentPlayer = human;
        gameOver = false;
        updateBoard();
        updateStatus();
    }

    private void updateStatus() {
        status.setText(playerName(currentPlayer) + " (" + currentPlayer.getMarker() + "), your turn");
    }

    private String playerName(Player player) {
        return player == human ? human.getName() : cpu.getName();
    }

    private void updateBoard() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                char marker = board.getCell(row, column);
                Button cell = cells[row][column];
                cell.setText(marker == ' ' ? "" : Character.toString(marker));
                cell.setDisable(gameOver || marker != ' ');
                cell.getStyleClass().removeAll("x-cell", "o-cell");
                if (marker == 'X') {
                    cell.getStyleClass().add("x-cell");
                } else if (marker == 'O') {
                    cell.getStyleClass().add("o-cell");
                }
            }
        }
    }

    private void startConfetti() {
        confettiSpawner = new Timeline(new javafx.animation.KeyFrame(
                Duration.millis(90), event -> addConfetti()));
        confettiSpawner.setCycleCount(Animation.INDEFINITE);
        confettiSpawner.play();
    }

    private void addConfetti() {
        Color[] colors = {
            Color.DODGERBLUE, Color.MEDIUMPURPLE, Color.HOTPINK, Color.ORANGE
        };
        double size = 7 + random.nextDouble() * 8;
        double width = Math.max(confettiLayer.getWidth(), 460);
        double height = Math.max(confettiLayer.getHeight(), 650);
        Rectangle particle = new Rectangle(size, size, colors[random.nextInt(colors.length)]);
        particle.setArcWidth(2);
        particle.setArcHeight(2);
        particle.setLayoutX(random.nextDouble() * Math.max(0, width - size));
        particle.setLayoutY(-size);
        confettiLayer.getChildren().add(particle);

        TranslateTransition fall = new TranslateTransition(
                Duration.seconds(2.5 + random.nextDouble() * 2.5), particle);
        fall.setByY(height + size * 2);
        fall.setByX(random.nextDouble() * 120 - 60);
        RotateTransition spin = new RotateTransition(
                Duration.seconds(1.2 + random.nextDouble() * 1.8), particle);
        spin.setByAngle(360 + random.nextInt(360));

        ParallelTransition animation = new ParallelTransition(fall, spin);
        confettiAnimations.add(animation);
        animation.setOnFinished(event -> {
            confettiLayer.getChildren().remove(particle);
            confettiAnimations.remove(animation);
        });
        animation.play();
    }

    private void stopConfetti() {
        if (confettiSpawner != null) {
            confettiSpawner.stop();
            confettiSpawner = null;
        }
        for (ParallelTransition animation : confettiAnimations) {
            animation.stop();
        }
        confettiAnimations.clear();
        if (confettiLayer != null) {
            confettiLayer.getChildren().clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
