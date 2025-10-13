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

    public PuzzleDoor(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    public boolean isSolved() {
        return solved;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    /** Show puzzle window without blocking the game */
    public void trigger(Stage parentStage, java.util.function.Consumer<Boolean> onComplete) {
        int timeLimit = puzzle.getTimeLimit();
        Label timerLabel = new Label("Time: " + timeLimit);

        // Create a dedicated non-modal stage instead of Alert
        Stage dialogStage = new Stage();
        dialogStage.initOwner(parentStage);
        dialogStage.initModality(Modality.NONE); // ✅ non-blocking
        dialogStage.setTitle("Puzzle");

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
                Platform.runLater(() -> dialogStage.close());
                solved = false;
                System.out.println("PuzzleDoor: time-up for puzzle " + puzzle.getId());
                onComplete.accept(false);
            }
        });
        countdown.getKeyFrames().add(kf);
        countdown.setCycleCount(timeLimit);
        countdown.play();

        VBox box = new VBox(10);
        Scene scene = new Scene(box, 300, 200);
        dialogStage.setScene(scene);

        // --- MCQ PUZZLE ---
        if (puzzle.getType() == Puzzle.Type.MCQ) {
            box.getChildren().add(new Label(puzzle.getQuestion()));
            ToggleGroup group = new ToggleGroup();

            for (int i = 0; i < puzzle.getOptions().length; i++) {
                RadioButton rb = new RadioButton(puzzle.getOptions()[i]);
                rb.setUserData(i);
                rb.setToggleGroup(group);
                box.getChildren().add(rb);
            }

            Button submit = new Button("Submit");
            box.getChildren().addAll(timerLabel, submit);

            submit.setOnAction(e -> {
                if (finished[0]) return;
                RadioButton selected = (RadioButton) group.getSelectedToggle();
                if (selected != null) {
                    int answer = (int) selected.getUserData();
                    solved = (answer == puzzle.getAnswerIndex());
                    finished[0] = true;
                    countdown.stop();

                    box.getChildren().clear();
                    Label result = new Label(solved ? "✅" : "❌");
                    result.setFont(javafx.scene.text.Font.font(60));
                    box.getChildren().add(result);
                    box.setAlignment(javafx.geometry.Pos.CENTER);

                    PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                    pause.setOnFinished(ev -> {
                        Platform.runLater(() -> dialogStage.close());
                        System.out.println("PuzzleDoor: MCQ puzzle " + puzzle.getId() + " solved=" + solved);
                        onComplete.accept(solved);
                    });
                    pause.play();
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
}
