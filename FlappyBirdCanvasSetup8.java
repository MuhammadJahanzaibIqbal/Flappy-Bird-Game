import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlappyBirdCanvasSetup8 extends Application {

    private static final int BOARD_WIDTH = 360;
    private static final int BOARD_HEIGHT = 640;

    // Bird properties
    private Image birdImg;
    private double birdX = BOARD_WIDTH / 8.0;
    private double birdY = BOARD_HEIGHT / 2.0;
    private static final int BIRD_WIDTH = 51;
    private static final int BIRD_HEIGHT = 36;

    // Movement speed
    private static final double MOVE_SPEED = 20;

    // Pipe properties
    private Image leftPipeImg;
    private Image rightPipeImg;
    private static final int PIPE_HEIGHT = 40;
    private static final int PIPE_WIDTH = 80;
    private static final int ROW_SPACING = 130;
    private double pipeSpeed = 2;

    private List<HorizontalPipeRow> pipes = new ArrayList<>();
    private Random random = new Random();

    // Score and level properties
    private int score = 0;
    private int highScore = 0;
    private boolean gameOver = false;
    private int level = 1;

    // Background properties
    private Image backgroundDayImg;
    private Image backgroundNightImg;
    private boolean isDay = true;

    @Override
    public void start(Stage stage) {
        // Load images
        birdImg = new Image("flappybird.png");
        leftPipeImg = new Image("left.png");
        rightPipeImg = new Image("right.png");
        backgroundDayImg = new Image("flappybirdbg.png");
        backgroundNightImg = new Image("flappybirdbg_stage3.png");

        // Create canvas and GraphicsContext
        Canvas canvas = new Canvas(BOARD_WIDTH, BOARD_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Set up input actions for movement
        setupInput(canvas);

        // Add initial pipes
        addInitialPipes();

        // Animation loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameOver) {
                    updatePipes();
                    checkCollisions();
                    updateLevel();
                    draw(gc);
                } else {
                    drawGameOver(gc);
                }
            }
        }.start();

        // Set up the scene and stage
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, BOARD_WIDTH, BOARD_HEIGHT);

        stage.setScene(scene);
        stage.setTitle("Enhanced Flappy Bird");
        stage.show();
        canvas.requestFocus();
    }

    private void setupInput(Canvas canvas) {
        canvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) birdY -= MOVE_SPEED;
            if (event.getCode() == KeyCode.DOWN) birdY += MOVE_SPEED;
            if (event.getCode() == KeyCode.LEFT) birdX -= MOVE_SPEED;
            if (event.getCode() == KeyCode.RIGHT) birdX += MOVE_SPEED;
            if (event.getCode() == KeyCode.SPACE && gameOver) resetGame();
            constrainBirdWithinBounds();
        });
    }

    private void addInitialPipes() {
        for (int i = 0; i < 5; i++) {
            addPipeRow(-i * ROW_SPACING);
        }
    }

    private void addPipeRow(double y) {
        int leftGapSize = random.nextInt(101) + 50;
        int rightGapSize = random.nextInt(101) + 50;
        pipes.add(new HorizontalPipeRow(y, leftGapSize, rightGapSize));
    }

    private void constrainBirdWithinBounds() {
        if (birdX < 0) birdX = 0;
        if (birdX > BOARD_WIDTH - BIRD_WIDTH) birdX = BOARD_WIDTH - BIRD_WIDTH;
        if (birdY < 0) birdY = 0;
        if (birdY > BOARD_HEIGHT - BIRD_HEIGHT) birdY = BOARD_HEIGHT - BIRD_HEIGHT;
    }

    private void updatePipes() {
        List<HorizontalPipeRow> crossedPipes = new ArrayList<>();

        // Iterate through pipes
        for (HorizontalPipeRow pipe : new ArrayList<>(pipes)) {
            pipe.setY(pipe.getY() + pipeSpeed);

            if (pipe.getY() > BOARD_HEIGHT / 2 && !pipe.isCrossed()) {
                pipe.setCrossed(true);
                score++;
                if (score > highScore) highScore = score;
                addPipeRow(-ROW_SPACING);
            }

            if (pipe.getY() > BOARD_HEIGHT) {
                crossedPipes.add(pipe); // Collect pipes to be removed
            }
        }

        // Remove crossed pipes
        pipes.removeAll(crossedPipes);
    }


    private void checkCollisions() {
        for (HorizontalPipeRow pipe : pipes) {
            if (birdY < pipe.getY() + PIPE_HEIGHT && birdY + BIRD_HEIGHT > pipe.getY()) {
                if (birdX < pipe.getLeftGapStartX() || birdX + BIRD_WIDTH > pipe.getRightGapEndX()) {
                    gameOver = true;
                }
            }
        }
    }

    private void updateLevel() {
        if (score % 10 == 0 && score != 0) {
            level = score / 10 + 1;
            pipeSpeed += 0.5;
        }
    }

    private void draw(GraphicsContext gc) {
        // Draw background
        isDay = (score / 10) % 2 == 0;
        Image backgroundImg = isDay ? backgroundDayImg : backgroundNightImg;
        gc.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // Draw bird
        gc.drawImage(birdImg, birdX, birdY, BIRD_WIDTH, BIRD_HEIGHT);

        // Draw pipes
        for (HorizontalPipeRow pipe : pipes) {
            gc.drawImage(leftPipeImg, 0, pipe.getY(), pipe.getLeftGapStartX(), PIPE_HEIGHT);
            gc.drawImage(rightPipeImg, pipe.getRightGapEndX(), pipe.getY(), BOARD_WIDTH - pipe.getRightGapEndX(), PIPE_HEIGHT);
        }

        // Draw score and level
        gc.fillText("Score: " + score, 10, 20);
        gc.fillText("High Score: " + highScore, 10, 40);
        gc.fillText("Level: " + level, 10, 60);
    }

    private void drawGameOver(GraphicsContext gc) {
        gc.fillText("Game Over! Press SPACE to Restart", BOARD_WIDTH / 4.0, BOARD_HEIGHT / 2.0);
        gc.fillText("Final Score: " + score, BOARD_WIDTH / 4.0, BOARD_HEIGHT / 2.0 + 20);
    }

    private void resetGame() {
        score = 0;
        gameOver = false;
        birdX = BOARD_WIDTH / 8.0;
        birdY = BOARD_HEIGHT / 2.0;
        pipeSpeed = 2;
        pipes.clear();
        addInitialPipes();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class HorizontalPipeRow {
        private double y;
        private double leftGapSize;
        private double rightGapSize;
        private boolean crossed;

        public HorizontalPipeRow(double y, double leftGapSize, double rightGapSize) {
            this.y = y;
            this.leftGapSize = leftGapSize;
            this.rightGapSize = rightGapSize;
            this.crossed = false;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getLeftGapStartX() {
            return leftGapSize;
        }

        public double getRightGapEndX() {
            return BOARD_WIDTH - rightGapSize;
        }

        public boolean isCrossed() {
            return crossed;
        }

        public void setCrossed(boolean crossed) {
            this.crossed = crossed;
        }
    }
}
