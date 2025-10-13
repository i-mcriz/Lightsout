// GameScene.java
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GameScene {

    private final Stage stage;
    private final MediaPlayer musicPlayer;
    private double musicVolume;
    private double soundVolume;
    private boolean musicOn;
    private boolean soundOn;

    private Player player;
    private GameMap map;
    private int currentLevel = 1;

    private AnimationTimer timer;
    private boolean paused = false;

    private List<PuzzleDoor> doors = new ArrayList<>();
    private GameEngine gameEngine = new GameEngine();
    // Visual mapping for puzzle doors so we can show/remove them on solve (store Group node)
    private java.util.Map<PuzzleDoor, javafx.scene.Node> doorVisualMap = new java.util.HashMap<>();
    // Layer that contains door visuals (positioned to map layout)
    private Pane doorsLayer;
    // Prevent multiple dialogs / reentrancy while a puzzle is active
    private boolean dialogOpen = false;
    
    // NEW: Marks system and database integration
    private MarksManager marksManager;
    private DatabaseManager databaseManager;
    private Text marksText;  // HUD display for marks

    private static final double BUTTON_WIDTH = Start.BUTTON_WIDTH;
    private static final double BUTTON_HEIGHT = Start.BUTTON_HEIGHT;
    private static final double GAP_PX = Start.GAP_PX;
    private static final Duration CINEMATIC = Start.CINEMATIC;

    public GameScene(Stage stage, MediaPlayer musicPlayer,
                     double musicVolume, double soundVolume,
                     boolean musicOn, boolean soundOn) {
        this.stage = stage;
        this.musicPlayer = musicPlayer;
        this.musicVolume = musicVolume;
        this.soundVolume = soundVolume;
        this.musicOn = musicOn;
        this.soundOn = soundOn;
        
        // Initialize marks manager and database
        this.marksManager = new MarksManager();
        this.databaseManager = new DatabaseManager();
        this.databaseManager.connect();
        
        System.out.println("=== Lights Out Game Started ===");
        System.out.println("Marks System: Ready (0/" + marksManager.getTotalQuestions() + ")");
    }

    public void showWithCinematicFadeIn() {
        loadLevel(currentLevel);
    }

    private void loadLevel(int levelNum) {
        Pane gameLayer = new Pane();
        gameLayer.setPrefSize(800, 600);
        gameLayer.setStyle("-fx-background-color: white;");

        // Load level map
        if (levelNum == 1) {
            map = new Level1(45);  // Make sure Level1 extends GameMap
        } else {
            Start.showStartMenu(stage, musicPlayer, musicVolume, soundVolume, musicOn, soundOn);
            return;
        }

        double mapWidth = map.getCols() * map.getTileSize();
        double mapHeight = map.getRows() * map.getTileSize();
        map.setLayoutX((800 - mapWidth) / 2);
        map.setLayoutY((600 - mapHeight) / 2);
        gameLayer.getChildren().add(map);

        // Player spawn point
        double startX = 0, startY = 0;
        int[][] layout = map.getLayout();
        outer:
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                if (layout[r][c] == 0) {
                    startX = map.getLayoutX() + c * map.getTileSize() + map.getTileSize() / 2.0;
                    startY = map.getLayoutY() + r * map.getTileSize() + map.getTileSize() / 2.0;
                    break outer;
                }
            }
        }

    player = new Player(startX, startY, 30);
    // Create a dedicated layer for doors so they render above the map but below the player
    // assign to field so other methods can access it
    doorsLayer = new Pane();
    // position doorsLayer at the same offset as the map so children can use local tile coords
    doorsLayer.setLayoutX(map.getLayoutX());
    doorsLayer.setLayoutY(map.getLayoutY());
    gameLayer.getChildren().addAll(doorsLayer, player);

    // Create puzzle doors after map is initialized
    doors.clear();
    doors.addAll(createPuzzleDoors());

    // Populate doorsLayer with visuals
    doorVisualMap.clear();
    for (PuzzleDoor door : doors) {
        Puzzle p = door.getPuzzle();
        int tr = p.getRow();
        int tc = p.getCol();
        double x = tc * map.getTileSize();
        double y = tr * map.getTileSize();
        double pad = 4;
        
        // Create attractive door rectangle with gradient and glow
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(
            x + pad, y + pad,
            map.getTileSize() - pad*2, map.getTileSize() - pad*2);
        
        // Gradient fill for door
        javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
            0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.DARKORANGE),
            new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.GOLD)
        );
        rect.setFill(gradient);
        rect.setStroke(javafx.scene.paint.Color.DARKGOLDENROD);
        rect.setStrokeWidth(3);
        rect.setOpacity(0.95);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        
        // Add glow effect
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(javafx.scene.paint.Color.GOLD);
        glow.setRadius(15);
        glow.setSpread(0.4);
        rect.setEffect(glow);
        
        // Enhanced label with icon
        javafx.scene.text.Text label = new javafx.scene.text.Text("🚪 DOOR");
        label.setFill(javafx.scene.paint.Color.WHITE);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        label.setX(x + map.getTileSize() * 0.1);
        label.setY(y + map.getTileSize() * 0.55);
        
        // Add drop shadow to label for readability
        javafx.scene.effect.DropShadow labelShadow = new javafx.scene.effect.DropShadow();
        labelShadow.setColor(javafx.scene.paint.Color.BLACK);
        labelShadow.setRadius(3);
        label.setEffect(labelShadow);
        // Group them so we can remove both at once
        javafx.scene.Group g = new javafx.scene.Group(rect, label);
        doorsLayer.getChildren().add(g);
        doorVisualMap.put(door, g);
        System.out.println("Placed door visual at row=" + tr + " col=" + tc + " -> x=" + (map.getLayoutX()+x) + " y=" + (map.getLayoutY()+y));
    }

        // HUD
        Text levelText = new Text("Level " + levelNum);
        levelText.setFill(Color.BLACK);
        levelText.setFont(Font.font("Consolas", 20));
        StackPane.setAlignment(levelText, Pos.TOP_LEFT);
        StackPane.setMargin(levelText, new Insets(10));
        
        // NEW: Marks display HUD
        marksText = new Text("Score: 0/5 | Questions: 0/5");
        marksText.setFill(Color.DARKGREEN);
        marksText.setFont(Font.font("Consolas", 18));
        marksText.setStyle("-fx-font-weight: bold;");
        StackPane.setAlignment(marksText, Pos.TOP_CENTER);
        StackPane.setMargin(marksText, new Insets(10));
        
        // Bind marks display to MarksManager properties
        marksManager.marksProperty().addListener((obs, oldVal, newVal) -> updateMarksDisplay());
        marksManager.questionsAnsweredProperty().addListener((obs, oldVal, newVal) -> updateMarksDisplay());
        updateMarksDisplay();

        // Pause button
        Button pauseBtn = new Button("II");
        pauseBtn.setPrefSize(40, 40);
        pauseBtn.setFont(Font.font(18));
        StackPane.setAlignment(pauseBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(pauseBtn, new Insets(10));

        VBox pauseMenu = new VBox(GAP_PX);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 20; -fx-background-radius: 15;");
        pauseMenu.setVisible(false);

        Button resumeBtn = new Button("Resume");
        resumeBtn.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        Button restartBtn = new Button("Restart Level");
        restartBtn.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        Button exitBtn = new Button("Exit to Menu");
        exitBtn.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);

        pauseMenu.getChildren().addAll(resumeBtn, restartBtn, exitBtn);

        StackPane gameStack = new StackPane(gameLayer, levelText, marksText, pauseBtn, pauseMenu);
        Scene finalScene = new Scene(gameStack, 800, 600);

        pauseBtn.setOnAction(e -> togglePause(pauseMenu));
        resumeBtn.setOnAction(e -> togglePause(pauseMenu));
        restartBtn.setOnAction(e -> {
            pauseMenu.setVisible(false);
            paused = false;
            timer.stop();
            loadLevel(currentLevel);
        });
        exitBtn.setOnAction(e -> Start.showStartMenu(stage, musicPlayer, musicVolume, soundVolume, musicOn, soundOn));

        // Key movement
        final boolean[] up = {false}, down = {false}, left = {false}, right = {false};
        final boolean[] playerFrozen = {false};  // Flag to freeze player at door
        final boolean[] waitingForInput = {false};  // Flag to wait for user input after puzzle
        
        finalScene.setOnKeyPressed(e -> {
            // If waiting for input after puzzle, any movement key unfreezes the player
            if (waitingForInput[0]) {
                if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP ||
                    e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN ||
                    e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT ||
                    e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) {
                    waitingForInput[0] = false;
                    playerFrozen[0] = false;
                    System.out.println("Player unfrozen - movement key pressed");
                }
            }
            
            if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) up[0] = true;
            if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) down[0] = true;
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) left[0] = true;
            if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right[0] = true;
        });
        finalScene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP) up[0] = false;
            if (e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN) down[0] = false;
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT) left[0] = false;
            if (e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) right[0] = false;
        });

        // Game loop
        timer = new AnimationTimer() {
            final double speed = 3.5;

            @Override
            public void handle(long now) {
                if (paused) return;

                double dx = 0, dy = 0;
                
                // Only process movement if player is not frozen
                if (!playerFrozen[0]) {
                    if (up[0]) dy -= speed;
                    if (down[0]) dy += speed;
                    if (left[0]) dx -= speed;
                    if (right[0]) dx += speed;
                    
                    player.move(dx, dy, map);
                }

                // Check puzzle doors (only if no dialog is currently open)
                if (!dialogOpen) {
                    for (PuzzleDoor door : doors) {
                        if (!door.isSolved() && map.isOnTile(player.getCenterX(), player.getCenterY(),
                                door.getPuzzle().getRow(), door.getPuzzle().getCol())) {
                            dialogOpen = true;
                            playerFrozen[0] = true;  // Freeze player at door
                            System.out.println("Player stopped at door - puzzle triggered");
                            
                            // Show puzzle dialog - game loop continues running
                            door.trigger(stage, (Boolean solved) -> {
                                System.out.println("GameScene: puzzle callback for " + door.getPuzzle().getId() + " solved=" + solved);
                                // If solved, remove visual door from doorsLayer and continue
                                if (solved) {
                                    // Add mark for correct answer
                                    marksManager.addMark();
                                    
                                    javafx.application.Platform.runLater(() -> {
                                        javafx.scene.Node n = doorVisualMap.remove(door);
                                        if (n != null && doorsLayer != null) {
                                            doorsLayer.getChildren().remove(n);
                                        }
                                    });
                                    gameEngine.puzzleSolved(
                                            door.getPuzzle().getSubject(),
                                            door.isSolved(),
                                            door.getPuzzle().getTimeLimit() * 1000
                                    );
                                    
                                    // Check if game is complete (all 5 questions answered)
                                    if (marksManager.isGameComplete()) {
                                        showFinalScoreScreen();
                                    } else {
                                        // Wait for user input to continue
                                        waitingForInput[0] = true;
                                        dialogOpen = false;
                                        System.out.println("Puzzle solved - waiting for movement input to continue");
                                    }
                                } else {
                                    // Reduce mark for wrong answer
                                    marksManager.reduceMark();
                                    System.out.println("GameScene: puzzle failed — Final Score: " + marksManager);
                                    
                                    // Wait for user input before showing final score
                                    waitingForInput[0] = true;
                                    dialogOpen = false;
                                    System.out.println("Puzzle failed - waiting for movement input to continue");
                                    
                                    // Add a small delay then show final score
                                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
                                    pause.setOnFinished(ev -> {
                                        if (!waitingForInput[0]) {  // User pressed a key
                                            showFinalScoreScreen();
                                        } else {
                                            // Wait for key press, then show final score
                                            final boolean[] keyPressed = {false};
                                            finalScene.setOnKeyPressed(keyEvent -> {
                                                if (!keyPressed[0] && 
                                                    (keyEvent.getCode() == KeyCode.W || keyEvent.getCode() == KeyCode.UP ||
                                                     keyEvent.getCode() == KeyCode.S || keyEvent.getCode() == KeyCode.DOWN ||
                                                     keyEvent.getCode() == KeyCode.A || keyEvent.getCode() == KeyCode.LEFT ||
                                                     keyEvent.getCode() == KeyCode.D || keyEvent.getCode() == KeyCode.RIGHT)) {
                                                    keyPressed[0] = true;
                                                    showFinalScoreScreen();
                                                }
                                            });
                                        }
                                    });
                                    pause.play();
                                }
                            });
                            // only handle one dialog per frame
                            break;
                        }
                    }
                }

                // Check exit
                if (map.isOnExit(player.getCenterX(), player.getCenterY())) {
                    stop();
                    currentLevel++;
                    loadLevel(currentLevel);
                }
            }
        };
        timer.start();

        stage.setScene(finalScene);
    // Ensure the gameStack receives key focus so movement keys are processed
    gameStack.requestFocus();

        // Fade-in
        gameStack.setOpacity(0);
        FadeTransition fadeInGame = new FadeTransition(CINEMATIC, gameStack);
        fadeInGame.setFromValue(0.0);
        fadeInGame.setToValue(1.0);
        fadeInGame.play();
    }

    private void togglePause(VBox pauseMenu) {
        paused = !paused;
        pauseMenu.setVisible(paused);
    }

    // Load puzzle doors from database (5 questions total)
    private List<PuzzleDoor> createPuzzleDoors() {
        List<PuzzleDoor> list = new ArrayList<>();
        
        // Load puzzles from database
        List<Puzzle> puzzles = databaseManager.loadPuzzlesForGame();
        
        if (puzzles.size() < 5) {
            System.out.println("⚠ Warning: Only " + puzzles.size() + " puzzles loaded (expected 5)");
        }
        
        for (Puzzle puzzle : puzzles) {
            list.add(new PuzzleDoor(puzzle));
            System.out.println("  📋 Door at (" + puzzle.getRow() + "," + puzzle.getCol() + "): " + 
                             puzzle.getSubject() + " - " + puzzle.getQuestion().substring(0, Math.min(40, puzzle.getQuestion().length())) + "...");
        }
        
        System.out.println("✓ Loaded " + list.size() + " puzzle doors");
        return list;
    }
    
    /**
     * Update the marks display in HUD
     */
    private void updateMarksDisplay() {
        if (marksText != null) {
            int marks = marksManager.getMarks();
            int answered = marksManager.getQuestionsAnswered();
            int total = marksManager.getTotalQuestions();
            
            String displayText = String.format("Score: %d/5 | Questions: %d/5", marks, answered);
            marksText.setText(displayText);
            
            // Change color based on performance
            if (marks >= 4) {
                marksText.setFill(Color.DARKGREEN);
            } else if (marks >= 3) {
                marksText.setFill(Color.ORANGE);
            } else {
                marksText.setFill(Color.DARKRED);
            }
        }
    }
    
    /**
     * Show final score screen with grade and return to menu
     */
    private void showFinalScoreScreen() {
        timer.stop();
        
        // FIX: Safely disconnect database with null check
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        
        // Create final score display
        VBox scoreBox = new VBox(20);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-padding: 40; -fx-background-radius: 15;");
        
        Text titleText = new Text("🎯 Game Complete!");
        titleText.setFont(Font.font("Consolas", 32));
        titleText.setFill(Color.DARKBLUE);
        titleText.setStyle("-fx-font-weight: bold;");
        
        Text scoreText = new Text(String.format("Final Score: %d / %d", 
                                   marksManager.getMarks(), marksManager.getTotalQuestions()));
        scoreText.setFont(Font.font("Consolas", 28));
        scoreText.setFill(Color.BLACK);
        
        Text gradeText = new Text("Grade: " + marksManager.getGrade());
        gradeText.setFont(Font.font("Consolas", 36));
        gradeText.setStyle("-fx-font-weight: bold;");
        // Color based on grade
        String grade = marksManager.getGrade();
        if (grade.equals("A+") || grade.equals("A")) {
            gradeText.setFill(Color.DARKGREEN);
        } else if (grade.equals("B") || grade.equals("C")) {
            gradeText.setFill(Color.ORANGE);
        } else {
            gradeText.setFill(Color.DARKRED);
        }
        
        Text detailsText = new Text(String.format("✓ Correct: %d | ✗ Wrong: %d | Percentage: %.0f%%",
                                    marksManager.getCorrectAnswers(),
                                    marksManager.getWrongAnswers(),
                                    marksManager.getPercentage()));
        detailsText.setFont(Font.font("Consolas", 18));
        detailsText.setFill(Color.DARKGRAY);
        
        Button returnBtn = Start.createAgentButton("Return to Menu");
        returnBtn.setOnAction(e -> {
            marksManager.reset();
            Start.showStartMenu(stage, musicPlayer, musicVolume, soundVolume, musicOn, soundOn);
        });
        
        scoreBox.getChildren().addAll(titleText, scoreText, gradeText, detailsText, returnBtn);
        
        StackPane finalPane = new StackPane(scoreBox);
        finalPane.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        Scene scoreScene = new Scene(finalPane, 800, 600);
        
        stage.setScene(scoreScene);
        
        // Fade in
        finalPane.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), finalPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("FINAL SCORE: " + marksManager);
        System.out.println("=".repeat(50));
    }
    
    /**
     * FIX: Cleanup method to properly release all resources
     */
    public void cleanup() {
        if (timer != null) {
            timer.stop();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        System.out.println("✓ GameScene resources cleaned up");
    }
}

