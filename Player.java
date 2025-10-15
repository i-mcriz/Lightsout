import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Player extends Group {

    private final double radius; // keeps collision radius
    private final ImageView sprite;
    private final Image idleFrame;  // Single idle frame
    private final Image[] walkFrames; // Animation frames for walking
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private boolean isMoving = false;
    private boolean animationEnabled = true;

    public Player(double startX, double startY, double radius) {
        this.radius = radius;

        // Load frames: frame 1 is idle, frames 2-3 are walking animation
        Image frame1 = new Image(getClass().getResource("/player/player1.png").toExternalForm());
        Image frame2 = new Image(getClass().getResource("/player/player2.png").toExternalForm());
        Image frame3 = new Image(getClass().getResource("/player/player3.png").toExternalForm());
        
        this.idleFrame = frame1;  // First frame is always idle/still
        this.walkFrames = new Image[] { frame2, frame3 };  // Alternate between these when walking

        sprite = new ImageView(idleFrame);
        // Make sprite slightly smaller than collision radius for better visual centering
        sprite.setFitWidth(radius * 1.8);
        sprite.setFitHeight(radius * 1.8);
        sprite.setPreserveRatio(true);
        sprite.setSmooth(true);
        
        // Center the sprite on the player position
        sprite.setTranslateX(-radius * 0.9);
        sprite.setTranslateY(-radius * 0.9);

        getChildren().add(sprite);
        setTranslateX(startX);
        setTranslateY(startY);

        // Animation handler - only animates when moving AND enabled
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isMoving && animationEnabled) {
                    // Animate between walk frames
                    if (now - lastFrameTime > 200_000_000) { // every 0.2 sec (slower for smoother look)
                        currentFrame = (currentFrame + 1) % walkFrames.length;
                        sprite.setImage(walkFrames[currentFrame]);
                        lastFrameTime = now;
                    }
                } else {
                    // Show idle frame when not moving or animation disabled
                    sprite.setImage(idleFrame);
                    currentFrame = 0;
                }
            }
        }.start();
    }

    /** Player movement with wall collision and smooth centering */
    public void move(double dx, double dy, GameMap map, boolean exitUnlocked) {
        // Track if any movement actually occurred
        double oldX = getTranslateX();
        double oldY = getTranslateY();
        
        // Only process if there's input
        if (dx == 0 && dy == 0) {
            // No input - stop moving
            if (isMoving) {
                isMoving = false;
                sprite.setImage(idleFrame);
                currentFrame = 0;
            }
            return;
        }

        // Better collision radius - smaller for better centering in corridors
        double squishFactor = 0.5; // Much smaller for proper centering
        double effectiveRadius = radius * squishFactor;

        // Calculate next position
        double nextX = getTranslateX() + dx;
        double nextY = getTranslateY() + dy;
        
        // Try full diagonal movement first
        if (!map.collidesWithCircle(nextX, nextY, effectiveRadius, exitUnlocked)) {
            setTranslateX(nextX);
            setTranslateY(nextY);
        } else {
            // Diagonal blocked - try X only, then Y only
            if (dx != 0 && !map.collidesWithCircle(nextX, getTranslateY(), effectiveRadius, exitUnlocked)) {
                setTranslateX(nextX);
            }
            if (dy != 0 && !map.collidesWithCircle(getTranslateX(), nextY, effectiveRadius, exitUnlocked)) {
                setTranslateY(nextY);
            }
        }
        
        // Check if position actually changed
        boolean actuallyMoved = (getTranslateX() != oldX || getTranslateY() != oldY);
        
        // Update animation state based on actual movement
        if (actuallyMoved && !isMoving) {
            isMoving = true;
        } else if (!actuallyMoved && isMoving) {
            // Tried to move but blocked - stop animation
            isMoving = false;
            sprite.setImage(idleFrame);
            currentFrame = 0;
        }
    }

    /** Stop player movement and animation */
    public void stopMoving() {
        isMoving = false;
        currentFrame = 0;
        sprite.setImage(idleFrame);
    }
    
    /** Freeze player completely (for doors/dialogs) */
    public void freeze() {
        isMoving = false;
        animationEnabled = false;
        currentFrame = 0;
        sprite.setImage(idleFrame);
    }
    
    /** Unfreeze player (allow movement and animation) */
    public void unfreeze() {
        animationEnabled = true;
    }
    
    /** Check if player is currently moving */
    public boolean isMoving() {
        return isMoving;
    }

    // Utility getters
    public double getCenterX() { return getTranslateX(); }
    public double getCenterY() { return getTranslateY(); }
    public double getRadius() { return radius; }
}
