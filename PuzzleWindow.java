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
        stage.initStyle(StageStyle.UNDECORATED); // Remove window decorations for better styling
    }

    public void show() {
        Label questionLabel = new Label(puzzle.getQuestion());
        questionLabel.setWrapText(true);
        questionLabel.setFont(Font.font("Comic Sans MS", 20));
        questionLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        ToggleGroup group = new ToggleGroup();
        VBox optionsBox = new VBox(15);

        String[] options = puzzle.getOptions();
        for (int i = 0; i < options.length; i++) {
            RadioButton rb = new RadioButton(options[i]);
            rb.setToggleGroup(group);
            rb.setUserData(i);
            rb.setFont(Font.font("Comic Sans MS", 18));
            rb.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            optionsBox.getChildren().add(rb);
        }

        Button submit = new Button("Submit");
        submit.setPrefSize(200, 40);
        submit.setFont(Font.font("Comic Sans MS", 18));
        submit.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        );
        submit.setOnMouseEntered(e -> submit.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        submit.setOnMouseExited(e -> submit.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        submit.setOnAction(e -> {
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            if (selected != null) {
                int selectedIndex = (int) selected.getUserData();
                boolean correct = dbManager.validateAnswer(puzzle.getId(), selectedIndex);

                // Auto-close: Show result briefly then close automatically
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(null);
                alert.setContentText(correct ? "✅ Correct!" : "❌ Wrong Answer!");
                
                // Style the alert dialog to match
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setStyle("-fx-background-color: black;");
                dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-family: 'Comic Sans MS';");
                
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

        VBox root = new VBox(20, questionLabel, optionsBox, submit);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black; -fx-background-radius: 15;");

        Scene scene = new Scene(root, 550, 450);
        
        // Add CSS styling for radio buttons to make them visible on black background
        scene.getStylesheets().add("data:text/css," + 
            ".radio-button { -fx-text-fill: white; }" +
            ".radio-button .radio { -fx-background-color: white; -fx-background-insets: 0; }" +
            ".radio-button:selected .radio .dot { -fx-background-color: black; -fx-background-insets: 0; }"
        );
        
        stage.setScene(scene);
        stage.show();  // Non-blocking - allows game to continue
    }
}
