// Player.java
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.util.Duration;
import java.util.Random;

public class Player extends Group {

    private final Ellipse leftEye;
    private final Ellipse rightEye;
    private final double eyeHeight = 6;
    private final Random random = new Random();
    private final double radius; // visual + collision radius

    public Player(double startX, double startY, double radius) {
        this.radius = radius;

        // Head (white)
        Circle head = new Circle(radius, Color.WHITE);

        // Eyes (black, blinking)
        leftEye = new Ellipse(-radius / 3, -radius / 4, 4, eyeHeight);
        leftEye.setFill(Color.BLACK);

        rightEye = new Ellipse(radius / 3, -radius / 4, 4, eyeHeight);
        rightEye.setFill(Color.BLACK);

        getChildren().addAll(head, leftEye, rightEye);
        setTranslateX(startX);
        setTranslateY(startY);

        startBlinking();
    }

    /** Makes the eyes blink periodically */
    private void startBlinking() {
        Timeline blinkLoop = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> blinkOnce()),
                new KeyFrame(Duration.seconds(3), e -> blinkOnce())
        );
        blinkLoop.setCycleCount(Timeline.INDEFINITE);
        blinkLoop.play();
    }

    /** A single blink animation */
    private void blinkOnce() {
        double delay = 1.5 + random.nextDouble() * 3.0;

        Timeline blink = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(leftEye.radiusYProperty(), eyeHeight),
                        new KeyValue(rightEye.radiusYProperty(), eyeHeight)
                ),
                new KeyFrame(Duration.millis(100),
                        new KeyValue(leftEye.radiusYProperty(), 0),
                        new KeyValue(rightEye.radiusYProperty(), 0)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(leftEye.radiusYProperty(), eyeHeight),
                        new KeyValue(rightEye.radiusYProperty(), eyeHeight)
                )
        );
        blink.setDelay(Duration.seconds(delay));
        blink.play();
    }

    /** Player movement with wall collision */
    public void move(double dx, double dy, GameMap map) {
        double squishFactor = 0.7; // smaller collision radius
        double effectiveRadius = radius * squishFactor;

        // Horizontal
        double nextX = getTranslateX() + dx;
        if (!map.collidesWithCircle(nextX, getTranslateY(), effectiveRadius)) {
            setTranslateX(nextX);
        }

        // Vertical
        double nextY = getTranslateY() + dy;
        if (!map.collidesWithCircle(getTranslateX(), nextY, effectiveRadius)) {
            setTranslateY(nextY);
        }
    }

    // Utility getters
    public double getCenterX() { return getTranslateX(); }
    public double getCenterY() { return getTranslateY(); }
    public double getRadius() { return radius; }

}
