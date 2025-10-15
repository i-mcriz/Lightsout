import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlickeringLight {
    private double x, y;
    private double prevX, prevY;
    private double cutoutRadius;
    private Random random = new Random();
    
    // Sparkle image
    private Image sparkleImage;
    
    // Map overlay image (replaces black overlay)
    private Image mapOverlayImage;
    
    // Sparkle trail particles
    private List<Sparkle> sparkles = new ArrayList<>();
    private static final int MAX_SPARKLES = 40;
    
    // Inner class for sparkle particles
    private static class Sparkle {
        double x, y;
        double opacity;
        double size;
        int lifetime;
        double rotation;
        
        Sparkle(double x, double y) {
            this.x = x;
            this.y = y;
            this.opacity = 1.0;
            this.size = 15 + Math.random() * 10;
            this.lifetime = 20 + (int)(Math.random() * 15);
            this.rotation = Math.random() * 360;
        }
        
        void update() {
            lifetime--;
            opacity = (double)lifetime / 35.0;
            rotation += 5; // Rotate sparkle
        }
        
        boolean isAlive() {
            return lifetime > 0;
        }
    }

    public FlickeringLight(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
        this.cutoutRadius = radius;
        
        // Load sparkle image
        try {
            sparkleImage = new Image(getClass().getResourceAsStream("/player/sparkle.png"));
            if (sparkleImage.isError()) {
                throw new Exception("Image has error");
            }
        } catch (Exception e) {
            System.out.println("Could not load sparkle.png from resources, trying filesystem...");
            try {
                sparkleImage = new Image("file:src/main/resources/player/sparkle.png");
            } catch (Exception e2) {
                System.out.println("Could not load sparkle image: " + e2.getMessage());
                sparkleImage = null;
            }
        }
        
        // Load map overlay image
        try {
            mapOverlayImage = new Image(getClass().getResourceAsStream("/player/map.png"));
            if (mapOverlayImage.isError()) {
                throw new Exception("Image has error");
            }
        } catch (Exception e) {
            System.out.println("Could not load map.png from resources, trying filesystem...");
            try {
                mapOverlayImage = new Image("file:src/main/resources/player/map.png");
            } catch (Exception e2) {
                System.out.println("Could not load map overlay image: " + e2.getMessage());
                mapOverlayImage = null;
            }
        }
    }

    public void updatePosition(double x, double y) {
        // Detect movement and create sparkle trail
        double dx = x - this.x;
        double dy = y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 1) {
            // Create sparkles in the opposite direction of movement
            double angle = Math.atan2(-dy, -dx); // Opposite direction
            int numSparkles = (int)(distance / 3); // More sparkles for faster movement
            
            for (int i = 0; i < Math.min(numSparkles, 6); i++) {
                double sparkleX = x + Math.cos(angle) * (random.nextDouble() * 15);
                double sparkleY = y + Math.sin(angle) * (random.nextDouble() * 15);
                
                // Add some randomness to sparkle position
                sparkleX += (random.nextDouble() - 0.5) * 20;
                sparkleY += (random.nextDouble() - 0.5) * 20;
                
                sparkles.add(new Sparkle(sparkleX, sparkleY));
            }
            
            // Limit sparkle count
            while (sparkles.size() > MAX_SPARKLES) {
                sparkles.remove(0);
            }
        }
        
        this.prevX = this.x;
        this.prevY = this.y;
        this.x = x;
        this.y = y;
    }

    public void draw(GraphicsContext gc, double screenWidth, double screenHeight) {
        // Draw semi-transparent black overlay
        gc.setFill(Color.rgb(0, 0, 0, 0.70)); // Black with 70% opacity
        gc.fillRect(0, 0, screenWidth, screenHeight);
        
        // Clear a circular area around the player (60px radius)
        double radius = 65; // Changed to 60px as requested
        
        // Clear the circular cutout area - using clearRect for each pixel in the circle
        // This creates a sharp, non-blurred circular cutout
        for (int i = -((int)radius); i <= radius; i++) {
            for (int j = -((int)radius); j <= radius; j++) {
                double distance = Math.sqrt(i*i + j*j);
                if (distance <= radius) {
                    gc.clearRect(x + i, y + j, 1, 1);
                }
            }
        }
        
        // Update and draw sparkles
        gc.save();
        for (int i = sparkles.size() - 1; i >= 0; i--) {
            Sparkle s = sparkles.get(i);
            s.update();
            
            if (!s.isAlive()) {
                sparkles.remove(i);
            } else {
                // Draw sparkle image with rotation and fading
                if (sparkleImage != null && !sparkleImage.isError()) {
                    gc.setGlobalAlpha(s.opacity);
                    
                    // Save context for rotation
                    gc.save();
                    gc.translate(s.x, s.y);
                    gc.rotate(s.rotation);
                    gc.drawImage(sparkleImage, -s.size/2, -s.size/2, s.size, s.size);
                    gc.restore();
                    
                    gc.setGlobalAlpha(1.0);
                } else {
                    // Fallback: draw golden star shape if image not available
                    gc.setFill(Color.rgb(255, 215, 0, s.opacity * 0.8));
                    drawStar(gc, s.x, s.y, s.size/2, s.size/4, 5, s.rotation);
                }
            }
        }
        gc.restore();
    }
    
    // Helper method to draw a star shape (fallback)
    private void drawStar(GraphicsContext gc, double centerX, double centerY, 
                         double outerRadius, double innerRadius, int points, double rotation) {
        double[] xPoints = new double[points * 2];
        double[] yPoints = new double[points * 2];
        
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.toRadians(rotation + (i * 180.0 / points));
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY + radius * Math.sin(angle);
        }
        
        gc.fillPolygon(xPoints, yPoints, points * 2);
    }

    public void stop() { }
    
    public void resume() { }

    public void setBaseRadius(double radius) {
        this.cutoutRadius = radius;
    }

    public double getCurrentRadius() {
        return cutoutRadius;
    }
}
