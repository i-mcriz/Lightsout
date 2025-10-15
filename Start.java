import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;

public class Start extends Application {

    // Shared UI metrics (same used by GameScene)
    static final double BUTTON_WIDTH = 200;
    static final double BUTTON_HEIGHT = 40;
    static final double SLIDER_WIDTH = 230;
    static final double GAP_PX = 14;
    static final Duration CINEMATIC = Duration.millis(1500);

    // App state
    private boolean musicOn = true;
    private boolean soundOn = true;
    private double prevMusicVolume = 50;
    private double prevSoundVolume = 70;

    // Persistent music player
    private MediaPlayer musicPlayer;

    @Override
    public void start(Stage primaryStage) {
        // Disable maximize button
        primaryStage.setResizable(false);
        
        // Create persistent music player once (first launch)
        if (musicPlayer == null) {
            Media musicMedia = new Media(getClass().getResource("music.mp3").toExternalForm());
            musicPlayer = new MediaPlayer(musicMedia);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setMute(!musicOn);
            musicPlayer.setVolume(prevMusicVolume / 100.0);
            musicPlayer.play();
        }

        // Show the menu
        showStartMenu(primaryStage, musicPlayer, prevMusicVolume, prevSoundVolume, musicOn, soundOn);
    }

    // Reusable builder so GameScene can return here without recreating music
    public static void showStartMenu(Stage stage,
                                     MediaPlayer musicPlayer,
                                     double musicVolume,
                                     double soundVolume,
                                     boolean musicOn,
                                     boolean soundOn) {

        // --- Background VIDEO (muted) ---
        MediaView mediaView = null;
        MediaPlayer videoPlayer = null;
        
        try {
            java.net.URL videoUrl = Start.class.getResource("background.mp4");
            if (videoUrl == null) {
                // Try loading from file system as fallback
                java.io.File videoFile = new java.io.File("background.mp4");
                if (videoFile.exists()) {
                    videoUrl = videoFile.toURI().toURL();
                }
            }
            
            if (videoUrl != null) {
                Media bgMedia = new Media(videoUrl.toExternalForm());
                videoPlayer = new MediaPlayer(bgMedia);
                videoPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                videoPlayer.setMute(true);
                videoPlayer.play();
                
                mediaView = new MediaView(videoPlayer);
                mediaView.setPreserveRatio(false);
                mediaView.setFitWidth(800);
                mediaView.setFitHeight(600);
                mediaView.setEffect(new GaussianBlur(2));
            } else {
                System.err.println("Warning: background.mp4 not found. Menu will display without video.");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load background video: " + e.getMessage());
        }

        // --- Buttons ---
        Button startBtn = createAgentButton("Start Game");
        Button musicBtn = createAgentButton(musicOn ? "Music" : "Music 🔇");
        Button soundBtn = createAgentButton(soundOn ? "Sound" : "Sound 🔇");
        Button exitBtn  = createAgentButton("Exit");

        // --- Sliders ---
        Slider musicSlider = createSmallSlider(musicOn ? musicVolume : 0);
        Slider soundSlider = createSmallSlider(soundVolume);

        // Keep musicPlayer in the requested state
        musicPlayer.setMute(!musicOn);
        if (!musicPlayer.isMute()) {
            musicPlayer.setVolume(musicSlider.getValue() / 100.0);
        }

        // Live volume only when not muted
        musicSlider.valueProperty().addListener((obs, ov, nv) -> {
            if (!musicPlayer.isMute()) {
                musicPlayer.setVolume(nv.doubleValue() / 100.0);
            }
        });

        // Toggle music (move slider to 0 on mute as requested)
        final double[] prevMusicVol = { musicVolume };
        musicBtn.setOnAction(e -> {
            if (musicPlayer.isMute()) {
                // Turn ON
                double to = prevMusicVol[0] <= 0 ? 50 : prevMusicVol[0];
                musicSlider.setValue(to);
                musicPlayer.setMute(false);
                musicPlayer.setVolume(musicSlider.getValue() / 100.0);
                musicBtn.setText("Music");
            } else {
                // Turn OFF
                prevMusicVol[0] = musicSlider.getValue();
                musicSlider.setValue(0);
                musicPlayer.setMute(true);
                musicBtn.setText("Music 🔇");
            }
        });

        // Toggle sound (slider to 0 when off)
        final double[] prevSoundVol = { soundVolume };
        soundBtn.setOnAction(e -> {
            if (soundBtn.getText().contains("🔇")) {
                // Turn ON
                double to = prevSoundVol[0] <= 0 ? 70 : prevSoundVol[0];
                soundSlider.setValue(to);
                soundBtn.setText("Sound");
            } else {
                // Turn OFF
                prevSoundVol[0] = soundSlider.getValue();
                soundSlider.setValue(0);
                soundBtn.setText("Sound 🔇");
            }
        });

        exitBtn.setOnAction(e -> stage.close());

        // --- Layout ---
        VBox menuBox = new VBox(GAP_PX,
                startBtn,
                musicBtn, musicSlider,
                soundBtn, soundSlider,
                exitBtn
        );
        menuBox.setAlignment(Pos.CENTER);

        StackPane root;
        if (mediaView != null) {
            root = new StackPane(mediaView, menuBox);
        } else {
            root = new StackPane(menuBox);
        }
        StackPane.setAlignment(menuBox, Pos.CENTER);
        StackPane.setMargin(menuBox, new Insets(0));
        root.setStyle("-fx-background-color: black;"); // safety: no white flash

        Scene menuScene = new Scene(root, 800, 600, Color.BLACK);
        stage.setScene(menuScene);
        stage.setTitle("Start Menu");
        stage.show();

        // Cinematic fade-in (also used when returning from game)
        root.setOpacity(0);
        FadeTransition in = new FadeTransition(CINEMATIC, root);
        in.setFromValue(0.0);
        in.setToValue(1.0);
        in.play();

        // Start -> Game
        final MediaPlayer finalVideoPlayer = videoPlayer;
        startBtn.setOnAction(e -> {
            startBtn.setDisable(true);
            musicBtn.setDisable(true);
            soundBtn.setDisable(true);
            exitBtn.setDisable(true);

            FadeTransition out = new FadeTransition(CINEMATIC, root);
            out.setFromValue(1.0);
            out.setToValue(0.0);
            out.setOnFinished(evt -> {
                if (finalVideoPlayer != null) {
                    finalVideoPlayer.stop();
                    finalVideoPlayer.dispose();
                }

                new GameScene(stage, musicPlayer,
                        musicSlider.getValue(), soundSlider.getValue(),
                        !musicPlayer.isMute(),            // current musicOn
                        !soundBtn.getText().contains("🔇") // current soundOn
                ).showWithCinematicFadeIn();
            });
            out.play();
        });
    }

    // --- Styling helpers (shared with GameScene) ---
    static Button createAgentButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Comic Sans MS", 18)); // cartoonish
        btn.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: black;" +
            "-fx-border-color: black;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-border-color: black;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: black;" +
            "-fx-border-color: black;" +
            "-fx-border-width: 2;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;"
        ));
        return btn;
    }

    static Slider createSmallSlider(double initialValue) {
        Slider slider = new Slider(0, 100, initialValue);
        slider.setPrefWidth(SLIDER_WIDTH);
        slider.setMaxWidth(SLIDER_WIDTH);
        slider.setMinWidth(SLIDER_WIDTH);
        slider.setStyle(
            "-fx-control-inner-background: black;" +
            "-fx-accent: black;"
        );
        return slider;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
