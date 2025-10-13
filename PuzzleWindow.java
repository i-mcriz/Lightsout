import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.text.Font;

public class PuzzleWindow {

    private final Stage stage;
    private final Puzzle puzzle;
    private final DatabaseManager dbManager;
    private final PuzzleCompletionListener listener;

    public PuzzleWindow(Puzzle puzzle, DatabaseManager dbManager, PuzzleCompletionListener listener) {
        this.puzzle = puzzle;
        this.dbManager = dbManager;
        this.listener = listener;

        stage = new Stage();
        stage.setTitle("Puzzle Challenge");
        stage.initModality(Modality.APPLICATION_MODAL); // Pause main game
    }

    public void show() {
        Label questionLabel = new Label(puzzle.getQuestion());
        questionLabel.setWrapText(true);
        questionLabel.setFont(Font.font(16));

        ToggleGroup group = new ToggleGroup();
        VBox optionsBox = new VBox(10);

        String[] options = puzzle.getOptions();
        for (int i = 0; i < options.length; i++) {
            RadioButton rb = new RadioButton(options[i]);
            rb.setToggleGroup(group);
            rb.setUserData(i);
            optionsBox.getChildren().add(rb);
        }

        Button submit = new Button("Submit");
        submit.setOnAction(e -> {
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            if (selected != null) {
                int selectedIndex = (int) selected.getUserData();
                boolean correct = dbManager.validateAnswer(puzzle.getId(), selectedIndex);

                // Auto-close: Show result briefly then close automatically
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(null);
                alert.setContentText(correct ? "✅ Correct!" : "❌ Wrong Answer!");
                
                // Auto-close the result alert after 1 second
                alert.show();
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                pause.setOnFinished(ev -> {
                    alert.close();
                    // Close puzzle window immediately
                    stage.close();
                    // Notify GamePanel
                    listener.onPuzzleCompleted(correct);
                });
                pause.play();
            }
        });

        VBox root = new VBox(15, questionLabel, optionsBox, submit);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 400, 300));
        stage.show();  // Non-blocking - allows game to continue
    }
}
