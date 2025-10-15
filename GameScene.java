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
    
    // NEW: Exit barrier to block exit until all puzzles solved
    private javafx.scene.Node exitBarrier = null;
    private boolean exitUnlocked = false;
    private boolean exitMessageShown = false; // Prevent message spam
    
    // NEW: Marks system and database integration
    private MarksManager marksManager;
    private DatabaseManager databaseManager;
    private Text marksText;  // HUD display for marks
    
    // NEW: Flickering light effect
    private FlickeringLight lightEffect;

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
        // Reset game state for new level
        exitUnlocked = false;
        exitBarrier = null;
        dialogOpen = false;
        
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

        player = new Player(startX, startY, 20);
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
    
        // Create EXIT BARRIER - blocks exit until all puzzles solved
        // Reuse the layout array that was already defined
        int exitRow = -1, exitCol = -1;
        // Find exit tile (marked with 2)
        outerExit:
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                if (layout[r][c] == 2) {
                    exitRow = r;
                    exitCol = c;
                    break outerExit;
                }
            }
        }
        
        if (exitRow != -1 && exitCol != -1) {
            double exitX = exitCol * map.getTileSize();
            double exitY = exitRow * map.getTileSize();
            double pad = 2;
            
            // Create locked barrier with chains/lock visual
            javafx.scene.shape.Rectangle barrierRect = new javafx.scene.shape.Rectangle(
                exitX + pad, exitY + pad,
                map.getTileSize() - pad*2, map.getTileSize() - pad*2);
            
            // Red gradient for locked barrier
            javafx.scene.paint.LinearGradient lockGradient = new javafx.scene.paint.LinearGradient(
                0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.DARKRED),
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.RED)
            );
            barrierRect.setFill(lockGradient);
            barrierRect.setStroke(javafx.scene.paint.Color.DARKRED);
            barrierRect.setStrokeWidth(4);
            barrierRect.setOpacity(0.9);
            
            // Add lock icon
            javafx.scene.text.Text lockIcon = new javafx.scene.text.Text("🔒");
            lockIcon.setStyle("-fx-font-size: 24px;");
            lockIcon.setX(exitX + map.getTileSize() * 0.3);
            lockIcon.setY(exitY + map.getTileSize() * 0.6);
            
            javafx.scene.text.Text lockLabel = new javafx.scene.text.Text("LOCKED");
            lockLabel.setFill(javafx.scene.paint.Color.WHITE);
            lockLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
            lockLabel.setX(exitX + map.getTileSize() * 0.15);
            lockLabel.setY(exitY + map.getTileSize() * 0.8);
            
            exitBarrier = new javafx.scene.Group(barrierRect, lockIcon, lockLabel);
            doorsLayer.getChildren().add(exitBarrier);
            exitUnlocked = false;
            System.out.println("🔒 Exit barrier created at row=" + exitRow + " col=" + exitCol);
        }

        // HUD
        Text levelText = new Text("Level " + levelNum);
        levelText.setFill(Color.BLACK);
        levelText.setFont(Font.font("Consolas", 20));
        StackPane.setAlignment(levelText, Pos.TOP_LEFT);
        StackPane.setMargin(levelText, new Insets(10));
        
        // NEW: Marks display HUD - Initially hidden, only shown at exit
        marksText = new Text("Score: 0/5 | Questions: 0/5");
        marksText.setFill(Color.DARKGREEN);
        marksText.setFont(Font.font("Consolas", 18));
        marksText.setStyle("-fx-font-weight: bold;");
        marksText.setVisible(false);  // Hide marks during gameplay
        StackPane.setAlignment(marksText, Pos.TOP_CENTER);
        StackPane.setMargin(marksText, new Insets(10));
        
        // Bind marks display to MarksManager properties (for when it's shown)
        marksManager.marksProperty().addListener((obs, oldVal, newVal) -> updateMarksDisplay());
        marksManager.questionsAnsweredProperty().addListener((obs, oldVal, newVal) -> updateMarksDisplay());
        updateMarksDisplay();

        // Pause button - blend with overlay with visible white lines
        Button pauseBtn = new Button("II");
        pauseBtn.setPrefSize(40, 40);
        pauseBtn.setFont(Font.font(18));
        pauseBtn.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.7);" +  // Match overlay color
            "-fx-text-fill: white;" +
            "-fx-border-color: transparent;" +
            "-fx-background-radius: 5;"
        );
        StackPane.setAlignment(pauseBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(pauseBtn, new Insets(10));

        VBox pauseMenu = new VBox(GAP_PX);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: rgba(0,0,0,0.95); -fx-padding: 40; -fx-background-radius: 15;");
        pauseMenu.setVisible(false);

        Button resumeBtn = Start.createAgentButton("Resume");
        Button restartBtn = Start.createAgentButton("Restart Level");
        Button exitBtn = Start.createAgentButton("Exit to Menu");
        
        // Override button text color to white for dark background
        resumeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        );
        resumeBtn.setOnMouseEntered(e -> resumeBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        resumeBtn.setOnMouseExited(e -> resumeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        
        restartBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        );
        restartBtn.setOnMouseEntered(e -> restartBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        restartBtn.setOnMouseExited(e -> restartBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        
        exitBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        );
        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        exitBtn.setOnMouseExited(e -> exitBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));

    pauseMenu.getChildren().addAll(resumeBtn, restartBtn, exitBtn);

        // NEW: Create canvas for light effect overlay
        javafx.scene.canvas.Canvas lightCanvas = new javafx.scene.canvas.Canvas(800, 600);
        lightCanvas.setMouseTransparent(true); // Don't block mouse events
        lightEffect = new FlickeringLight(player.getTranslateX(), player.getTranslateY(), 50);  // 50px radius = 100x100 cutout
        
        StackPane gameStack = new StackPane(gameLayer, lightCanvas, levelText, marksText, pauseBtn, pauseMenu);
        Scene finalScene = new Scene(gameStack, 800, 600);

        pauseBtn.setOnAction(e -> togglePause(pauseMenu));
        resumeBtn.setOnAction(e -> togglePause(pauseMenu));
        restartBtn.setOnAction(e -> {
            pauseMenu.setVisible(false);
            paused = false;
            if (timer != null) {
                timer.stop();
            }
            // Reset marks and progress
            marksManager.reset();
            System.out.println("Game restarted - progress reset");
            loadLevel(currentLevel);
        });
        exitBtn.setOnAction(e -> Start.showStartMenu(stage, musicPlayer, musicVolume, soundVolume, musicOn, soundOn));

        final boolean[] playerFrozen = {false};  // Flag to freeze player at door
        final boolean[] waitingForInput = {false};  // Flag to wait for user input after puzzle
        final boolean[] up = {false};
        final boolean[] down = {false};
        final boolean[] left = {false};
        final boolean[] right = {false};
        
        finalScene.setOnKeyPressed(e -> {
            // Don't process keys if player is frozen or waiting for input
            if (playerFrozen[0] || waitingForInput[0]) {
                // If waiting for input after puzzle, any movement key unfreezes the player
                if (waitingForInput[0]) {
                    if (e.getCode() == KeyCode.W || e.getCode() == KeyCode.UP ||
                        e.getCode() == KeyCode.S || e.getCode() == KeyCode.DOWN ||
                        e.getCode() == KeyCode.A || e.getCode() == KeyCode.LEFT ||
                        e.getCode() == KeyCode.D || e.getCode() == KeyCode.RIGHT) {
                        waitingForInput[0] = false;
                        playerFrozen[0] = false;
                        player.unfreeze();
                        System.out.println("Player unfrozen - movement key pressed: " + e.getCode());
                        // Fall through to set key states below instead of returning
                    } else {
                        return; // Other keys don't unfreeze
                    }
                } else {
                    return; // Ignore all key inputs when frozen
                }
            }
            
            // Track key states for smooth movement only when not frozen
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
            
            // Stop animation when all keys are released
            if (!up[0] && !down[0] && !left[0] && !right[0]) {
                player.stopMoving();
            }
        });

        // Game loop
        final javafx.scene.canvas.Canvas finalLightCanvas = lightCanvas;
        timer = new AnimationTimer() {
            final double speed = 2.8;  // Smoother, slightly slower speed

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
                    
                    player.move(dx, dy, map, exitUnlocked);
                    
                    // Update light position to follow player
                    lightEffect.updatePosition(player.getTranslateX(), player.getTranslateY());
                    
                    // Explicitly stop player if no keys are pressed
                    if (!up[0] && !down[0] && !left[0] && !right[0]) {
                        player.stopMoving();
                    }
                } else {
                    // Ensure player stays frozen and shows idle frame
                    player.stopMoving();
                }
                
                // Draw the light effect on the canvas
                javafx.scene.canvas.GraphicsContext gc = finalLightCanvas.getGraphicsContext2D();
                lightEffect.draw(gc, 800, 600);

                // Check puzzle doors (only if no dialog is currently open)
                if (!dialogOpen) {
                    for (PuzzleDoor door : doors) {
                        if (!door.isSolved() && map.isOnTile(player.getCenterX(), player.getCenterY(),
                                door.getPuzzle().getRow(), door.getPuzzle().getCol())) {
                            dialogOpen = true;
                            playerFrozen[0] = true;  // Freeze player at door
                            player.freeze();  // Freeze animation and movement
                            
                            // Clear all key states to prevent automatic movement after puzzle
                            up[0] = false;
                            down[0] = false;
                            left[0] = false;
                            right[0] = false;
                            
                            System.out.println("🚪 Player at door (" + door.getPuzzle().getRow() + "," + door.getPuzzle().getCol() + ") - " + door.getPuzzle().getSubject());
                            
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
                                        // All questions answered - UNLOCK EXIT and guide player
                                        System.out.println("🎉 All questions answered! Unlocking exit...");
                                        
                                        // Remove exit barrier
                                        if (exitBarrier != null && doorsLayer != null && !exitUnlocked) {
                                            doorsLayer.getChildren().remove(exitBarrier);
                                            exitBarrier = null;
                                            exitUnlocked = true;
                                            System.out.println("🔓 Exit barrier removed - EXIT UNLOCKED");
                                        }
                                        
                                        waitingForInput[0] = true;
                                        dialogOpen = false;
                                        
                                        // Show notification that player should go to exit
                                        javafx.application.Platform.runLater(() -> {
                                            javafx.scene.control.Alert exitAlert = new javafx.scene.control.Alert(
                                                javafx.scene.control.Alert.AlertType.INFORMATION);
                                            exitAlert.setTitle("All Puzzles Complete!");
                                            exitAlert.setHeaderText("🎉 Congratulations! 🔓");
                                            exitAlert.setContentText(
                                                "You've answered all questions!\n\n" +
                                                "The EXIT has been UNLOCKED!\n\n" +
                                                "Make your way to the exit to finish the game.");
                                            
                                            // Apply black background with white text styling
                                            javafx.scene.control.DialogPane dialogPane = exitAlert.getDialogPane();
                                            dialogPane.setStyle(
                                                "-fx-background-color: black;" +
                                                "-fx-font-family: 'Comic Sans MS';" +
                                                "-fx-font-size: 16px;"
                                            );
                                            
                                            // Style header
                                            dialogPane.lookup(".header-panel").setStyle(
                                                "-fx-background-color: black;"
                                            );
                                            javafx.scene.control.Label headerLabel = (javafx.scene.control.Label) dialogPane.lookup(".header-panel .label");
                                            if (headerLabel != null) {
                                                headerLabel.setStyle(
                                                    "-fx-text-fill: white;" +
                                                    "-fx-font-family: 'Comic Sans MS';" +
                                                    "-fx-font-size: 20px;" +
                                                    "-fx-font-weight: bold;"
                                                );
                                            }
                                            
                                            // Style content
                                            dialogPane.lookup(".content").setStyle(
                                                "-fx-background-color: black;"
                                            );
                                            javafx.scene.control.Label contentLabel = (javafx.scene.control.Label) dialogPane.lookup(".content .label");
                                            if (contentLabel != null) {
                                                contentLabel.setStyle(
                                                    "-fx-text-fill: white;" +
                                                    "-fx-font-family: 'Comic Sans MS';" +
                                                    "-fx-font-size: 16px;"
                                                );
                                            }
                                            
                                            // Style button
                                            javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialogPane.lookupButton(javafx.scene.control.ButtonType.OK);
                                            if (okButton != null) {
                                                okButton.setStyle(
                                                    "-fx-background-color: transparent;" +
                                                    "-fx-text-fill: white;" +
                                                    "-fx-font-family: 'Comic Sans MS';" +
                                                    "-fx-font-size: 18px;" +
                                                    "-fx-font-weight: bold;" +
                                                    "-fx-border-color: white;" +
                                                    "-fx-border-width: 2;" +
                                                    "-fx-background-radius: 0;" +
                                                    "-fx-border-radius: 0;" +
                                                    "-fx-padding: 10 30 10 30;"
                                                );
                                                okButton.setOnMouseEntered(e -> okButton.setStyle(
                                                    "-fx-background-color: white;" +
                                                    "-fx-text-fill: black;" +
                                                    "-fx-font-family: 'Comic Sans MS';" +
                                                    "-fx-font-size: 18px;" +
                                                    "-fx-font-weight: bold;" +
                                                    "-fx-border-color: white;" +
                                                    "-fx-border-width: 2;" +
                                                    "-fx-background-radius: 0;" +
                                                    "-fx-border-radius: 0;" +
                                                    "-fx-padding: 10 30 10 30;"
                                                ));
                                                okButton.setOnMouseExited(e -> okButton.setStyle(
                                                    "-fx-background-color: transparent;" +
                                                    "-fx-text-fill: white;" +
                                                    "-fx-font-family: 'Comic Sans MS';" +
                                                    "-fx-font-size: 18px;" +
                                                    "-fx-font-weight: bold;" +
                                                    "-fx-border-color: white;" +
                                                    "-fx-border-width: 2;" +
                                                    "-fx-background-radius: 0;" +
                                                    "-fx-border-radius: 0;" +
                                                    "-fx-padding: 10 30 10 30;"
                                                ));
                                            }
                                            
                                            exitAlert.showAndWait();
                                        });
                                    } else {
                                        // Wait for user input to continue
                                        waitingForInput[0] = true;
                                        dialogOpen = false;
                                        System.out.println("Puzzle solved - waiting for movement input to continue");
                                    }
                                } else {
                                    // Reduce mark for wrong answer (only once per door)
                                    if (!door.isMarksDeducted()) {
                                        marksManager.reduceMark();
                                        door.setMarksDeducted(true);
                                        System.out.println("GameScene: puzzle failed — Score: " + marksManager.getMarks() + "/" + marksManager.getTotalQuestions());
                                    } else {
                                        System.out.println("GameScene: puzzle failed but marks already deducted for this door");
                                    }
                                    
                                    // Wait for user input to continue (don't show final score yet)
                                    waitingForInput[0] = true;
                                    dialogOpen = false;
                                    System.out.println("Puzzle failed - waiting for movement input to continue");
                                }
                            });
                            // only handle one dialog per frame
                            break;
                        }
                    }
                }

                // Check if player is near exit tile (to show message when touching locked exit)
                if (!exitUnlocked) {
                    // Find exit tile and check distance
                    int[][] layout = map.getLayout();
                    for (int r = 0; r < map.getRows(); r++) {
                        for (int c = 0; c < map.getCols(); c++) {
                            if (layout[r][c] == 2) { // Exit tile
                                double tileSize = map.getTileSize();
                                double exitCenterX = map.getLayoutX() + c * tileSize + tileSize / 2.0;
                                double exitCenterY = map.getLayoutY() + r * tileSize + tileSize / 2.0;
                                
                                double distX = player.getCenterX() - exitCenterX;
                                double distY = player.getCenterY() - exitCenterY;
                                double distance = Math.sqrt(distX * distX + distY * distY);
                                
                                // If player is very close to exit (touching it), show message once
                                if (distance < tileSize * 0.8 && !exitMessageShown) {
                                    exitMessageShown = true;
                                    System.out.println("EXIT BLOCKED - Quiz is not over");
                                    
                                    // Show non-intrusive text message without taking focus
                                    javafx.application.Platform.runLater(() -> {
                                        javafx.scene.text.Text messageText = new javafx.scene.text.Text("Quiz is not over!");
                                        messageText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 28));
                                        messageText.setFill(javafx.scene.paint.Color.RED);
                                        messageText.setStroke(javafx.scene.paint.Color.WHITE);
                                        messageText.setStrokeWidth(2);
                                        
                                        // Center it on screen
                                        messageText.setLayoutX(400 - 100);
                                        messageText.setLayoutY(100);
                                        
                                        gameStack.getChildren().add(messageText);
                                        
                                        // Auto-remove after 2 seconds
                                        javafx.animation.PauseTransition pause = 
                                            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                                        pause.setOnFinished(e -> gameStack.getChildren().remove(messageText));
                                        pause.play();
                                    });
                                }
                                
                                // Reset message flag when player moves away
                                if (distance > tileSize * 1.5) {
                                    exitMessageShown = false;
                                }
                                break;
                            }
                        }
                    }
                }
                
                // Check exit - only allow if all questions are answered
                if (map.isOnExit(player.getCenterX(), player.getCenterY())) {
                    if (marksManager.isGameComplete()) {
                        // All questions answered - show marks and allow exit
                        System.out.println("EXIT UNLOCKED - Showing final score");
                        stop();
                        timer.stop();
                        
                        // Show marks display at exit
                        if (marksText != null) {
                            marksText.setVisible(true);
                        }
                        
                        // Show final score screen
                        showFinalScoreScreen();
                    }
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
        
        // Pause/resume the light effect
        if (lightEffect != null) {
            if (paused) {
                lightEffect.stop();
            } else {
                lightEffect.resume();
            }
        }
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
        
        // Create final score display with black background and white text
        VBox scoreBox = new VBox(20);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setStyle("-fx-background-color: black; -fx-padding: 40; -fx-background-radius: 15;");
        
        Text titleText = new Text("🎯 Game Complete!");
        titleText.setFont(Font.font("Comic Sans MS", 32));
        titleText.setFill(Color.WHITE);
        titleText.setStyle("-fx-font-weight: bold;");
        
        Text scoreText = new Text(String.format("Final Score: %d / %d", 
                                   marksManager.getMarks(), marksManager.getTotalQuestions()));
        scoreText.setFont(Font.font("Comic Sans MS", 28));
        scoreText.setFill(Color.WHITE);
        
        Text gradeText = new Text("Grade: " + marksManager.getGrade());
        gradeText.setFont(Font.font("Comic Sans MS", 36));
        gradeText.setStyle("-fx-font-weight: bold;");
        gradeText.setFill(Color.WHITE);
        
        Text detailsText = new Text(String.format("✓ Correct: %d | ✗ Wrong: %d | Percentage: %.0f%%",
                                    marksManager.getCorrectAnswers(),
                                    marksManager.getWrongAnswers(),
                                    marksManager.getPercentage()));
        detailsText.setFont(Font.font("Comic Sans MS", 18));
        detailsText.setFill(Color.WHITE);
        
        Button returnBtn = Start.createAgentButton("Return to Menu");
        // Override button style for white text on black background
        returnBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        );
        returnBtn.setOnMouseEntered(e -> returnBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        returnBtn.setOnMouseExited(e -> returnBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
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

