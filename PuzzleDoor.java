import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PuzzleDoor {

    private final Puzzle puzzle;
    private boolean solved = false;
    private boolean marksDeducted = false;  // Track if marks already deducted for this door

    public PuzzleDoor(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    public boolean isSolved() {
        return solved;
    }
    
    public boolean isMarksDeducted() {
        return marksDeducted;
    }
    
    public void setMarksDeducted(boolean deducted) {
        this.marksDeducted = deducted;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    /** Show puzzle window without blocking the game */
    public void trigger(Stage parentStage, java.util.function.Consumer<Boolean> onComplete) {
        int timeLimit = puzzle.getTimeLimit();
        Label timerLabel = new Label("Time: " + timeLimit);
        timerLabel.setFont(javafx.scene.text.Font.font("Comic Sans MS", 18));
        timerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Create a dedicated non-modal stage instead of Alert
        Stage dialogStage = new Stage();
        dialogStage.initOwner(parentStage);
        dialogStage.initModality(Modality.NONE); // ✅ non-blocking
        dialogStage.setTitle("Puzzle");
        dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Remove window decorations

        final boolean[] finished = {false};

        // Timer countdown
        Timeline countdown = new Timeline();
        KeyFrame kf = new KeyFrame(Duration.seconds(1), e -> {
            int t = Integer.parseInt(timerLabel.getText().split(": ")[1]);
            t--;
            timerLabel.setText("Time: " + t);
            if (t <= 0 && !finished[0]) {
                finished[0] = true;
                countdown.stop();
                Platform.runLater(() -> {
                    dialogStage.close();
                    showTryAgainWindow(parentStage, "Time's Up!\nTry Again");
                });
                solved = false;
                System.out.println("PuzzleDoor: time-up for puzzle " + puzzle.getId());
                onComplete.accept(false);
            }
        });
        countdown.getKeyFrames().add(kf);
        countdown.setCycleCount(timeLimit);
        countdown.play();

        VBox box = new VBox(20);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPadding(new javafx.geometry.Insets(40));
        box.setStyle("-fx-background-color: black; -fx-background-radius: 15;");
        
        Scene scene = new Scene(box, 550, 450);
        
        // Add CSS styling for radio buttons - completely disable all transitions and effects
        scene.getStylesheets().add("data:text/css," + 
            ".radio-button { -fx-text-fill: white; }" +
            ".radio-button .radio { -fx-background-color: white; -fx-background-insets: 0; }" +
            ".radio-button:selected .radio .dot { -fx-background-color: black; -fx-background-insets: 0; }" +
            "* { -fx-focus-color: transparent; -fx-faint-focus-color: transparent; }" +
            ".radio-button:hover .radio { -fx-background-color: white; }" +
            ".radio-button:armed .radio { -fx-background-color: white; }" +
            ".radio-button:pressed .radio { -fx-background-color: white; }" +
            ".radio-button * { -fx-effect: null; }"
        );
        
        dialogStage.setScene(scene);

        // --- MCQ PUZZLE ---
        if (puzzle.getType() == Puzzle.Type.MCQ) {
            Label questionLabel = new Label(puzzle.getQuestion());
            questionLabel.setWrapText(true);
            questionLabel.setFont(javafx.scene.text.Font.font("Comic Sans MS", 20));
            questionLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            box.getChildren().add(questionLabel);
            
            ToggleGroup group = new ToggleGroup();
            java.util.List<RadioButton> radioButtons = new java.util.ArrayList<>();

            for (int i = 0; i < puzzle.getOptions().length; i++) {
                RadioButton rb = new RadioButton(puzzle.getOptions()[i]);
                rb.setUserData(i);
                rb.setToggleGroup(group);
                rb.setFont(javafx.scene.text.Font.font("Comic Sans MS", 18));
                rb.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                box.getChildren().add(rb);
                radioButtons.add(rb);
            }
            
            // Select first option by default
            if (!radioButtons.isEmpty()) {
                radioButtons.get(0).setSelected(true);
            }

            Button submit = new Button("Submit");
            submit.setPrefSize(200, 40);
            submit.setFont(javafx.scene.text.Font.font("Comic Sans MS", 18));
            submit.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: black;" +
                "-fx-border-color: white;" +
                "-fx-border-width: 2;" +
                "-fx-background-radius: 0;" +
                "-fx-border-radius: 0;"
            );
            
            box.getChildren().addAll(timerLabel, submit);
            
            // Add keyboard navigation
            final int[] currentIndex = {0};
            scene.setOnKeyPressed(keyEvent -> {
                if (finished[0]) return;
                
                javafx.scene.input.KeyCode code = keyEvent.getCode();
                
                // Arrow keys or WASD to navigate options
                if (code == javafx.scene.input.KeyCode.DOWN || code == javafx.scene.input.KeyCode.S) {
                    currentIndex[0] = (currentIndex[0] + 1) % radioButtons.size();
                    radioButtons.get(currentIndex[0]).setSelected(true);
                    keyEvent.consume();
                } else if (code == javafx.scene.input.KeyCode.UP || code == javafx.scene.input.KeyCode.W) {
                    currentIndex[0] = (currentIndex[0] - 1 + radioButtons.size()) % radioButtons.size();
                    radioButtons.get(currentIndex[0]).setSelected(true);
                    keyEvent.consume();
                } else if (code == javafx.scene.input.KeyCode.SPACE || code == javafx.scene.input.KeyCode.ENTER) {
                    // Spacebar or Enter to submit
                    submit.fire();
                    keyEvent.consume();
                }
            });

            submit.setOnAction(e -> {
                if (finished[0]) return;
                RadioButton selected = (RadioButton) group.getSelectedToggle();
                if (selected != null) {
                    int answer = (int) selected.getUserData();
                    solved = (answer == puzzle.getAnswerIndex());
                    finished[0] = true;
                    countdown.stop();

                    if (solved) {
                        // Correct answer - close immediately without animation
                        Platform.runLater(() -> dialogStage.close());
                        System.out.println("PuzzleDoor: MCQ puzzle " + puzzle.getId() + " solved=true");
                        onComplete.accept(true);
                    } else {
                        // Wrong answer - show white X for 2 seconds
                        box.getChildren().clear();
                        Label result = new Label("✗");
                        result.setFont(javafx.scene.text.Font.font("Comic Sans MS", 80));
                        result.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        box.getChildren().add(result);
                        box.setAlignment(javafx.geometry.Pos.CENTER);

                        PauseTransition pause = new PauseTransition(Duration.seconds(2));
                        pause.setOnFinished(ev -> {
                            Platform.runLater(() -> dialogStage.close());
                            System.out.println("PuzzleDoor: MCQ puzzle " + puzzle.getId() + " solved=false");
                            onComplete.accept(false);
                        });
                        pause.play();
                    }
                }
            });

        // --- TEXT PUZZLE ---
        } else if (puzzle.getType() == Puzzle.Type.TEXT) {
            TextArea content = new TextArea(puzzle.getContentText());
            content.setEditable(false);
            box.getChildren().addAll(content, timerLabel);

            // Auto close after 0.5 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(ev -> {
                if (!finished[0]) {
                    finished[0] = true;
                    solved = true;
                    countdown.stop();
                    Platform.runLater(() -> dialogStage.close());
                    System.out.println("PuzzleDoor: text puzzle " + puzzle.getId() + " auto-complete");
                    onComplete.accept(true);
                }
            });
            pause.play();
        }

        // Handle manual close
        dialogStage.setOnCloseRequest(e -> {
            if (!finished[0]) {
                finished[0] = true;
                solved = false;
                countdown.stop();
                onComplete.accept(false);
            }
        });

        dialogStage.show(); // ✅ Non-blocking window
    }
    
    /**
     * Show a "Try Again" window for wrong answers or timeout
     * Auto-closes after 1 second
     */
    private void showTryAgainWindow(Stage parentStage, String message) {
        Stage tryAgainStage = new Stage();
        tryAgainStage.initOwner(parentStage);
        tryAgainStage.initModality(Modality.APPLICATION_MODAL); // Block until closed
        tryAgainStage.setTitle("Puzzle Failed");
        tryAgainStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Remove decorations
        
        VBox box = new VBox(15);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPadding(new javafx.geometry.Insets(30));
        box.setStyle("-fx-background-color: black; -fx-background-radius: 15;");
        
        // Show the specific message (either "Wrong Answer! Try Again" or "Time's Up! Try Again")
        Label messageLabel = new Label(message);
        messageLabel.setFont(javafx.scene.text.Font.font("Comic Sans MS", javafx.scene.text.FontWeight.BOLD, 24));
        messageLabel.setStyle("-fx-text-fill: white;");
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        box.getChildren().add(messageLabel);
        
        Scene scene = new Scene(box, 350, 200);
        tryAgainStage.setScene(scene);
        tryAgainStage.show(); // Non-blocking
        
        // Auto-close after 2 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> tryAgainStage.close());
        pause.play();
    }
}
