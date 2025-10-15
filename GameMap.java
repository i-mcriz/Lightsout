import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import java.util.Random;

public class GameMap extends Pane {

    private final int[][] layout;
    private final int tileSize;
    private Random random = new Random(42); // Fixed seed for consistent torch placement

    public GameMap(int[][] layout, int tileSize) {
        this.layout = layout;
        this.tileSize = tileSize;
        drawMap();
    }

    /** Draw the grid based on layout array, matching map.png colors */
    private void drawMap() {
        getChildren().clear();
        for (int r = 0; r < layout.length; r++) {
            for (int c = 0; c < layout[r].length; c++) {
                Rectangle tile = new Rectangle(c * tileSize, r * tileSize, tileSize, tileSize);
                
                // Use cartoonish bright colors
                switch (layout[r][c]) {
                    case 1 -> {
                        // Walls - stone gray with cartoon outline
                        tile.setFill(Color.rgb(80, 80, 90));
                        tile.setStroke(Color.rgb(50, 50, 60));
                        tile.setStrokeWidth(2);
                    }
                    case 2 -> {
                        // Exit - bright gold/yellow
                        tile.setFill(Color.rgb(255, 215, 0));
                        tile.setStroke(Color.rgb(200, 160, 0));
                        tile.setStrokeWidth(2);
                    }
                    default -> {
                        // Path - light sandy color
                        tile.setFill(Color.rgb(245, 222, 179));
                        tile.setStroke(Color.rgb(210, 180, 140));
                        tile.setStrokeWidth(1.5);
                        
                        // Add torches to some path tiles (about 8% of paths)
                        if (random.nextDouble() < 0.08) {
                            addTorch(c * tileSize + tileSize/2, r * tileSize + tileSize/2);
                        }
                    }
                }
                
                getChildren().add(tile);
            }
        }
    }
    
    /** Add a decorative torch at the given position */
    private void addTorch(double x, double y) {
        // Torch base (small brown rectangle)
        Rectangle torchBase = new Rectangle(x - 2, y - 8, 4, 12);
        torchBase.setFill(Color.rgb(101, 67, 33));
        torchBase.setStroke(Color.rgb(70, 40, 20));
        torchBase.setStrokeWidth(0.5);
        
        // Flame (orange circle with gradient)
        Circle flame = new Circle(x, y - 10, 6);
        RadialGradient gradient = new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 255, 150)),
            new Stop(0.5, Color.rgb(255, 150, 0)),
            new Stop(1, Color.rgb(255, 100, 0))
        );
        flame.setFill(gradient);
        
        getChildren().addAll(torchBase, flame);
    }

    // ---- Getters ----
    public int[][] getLayout() { return layout; }
    public int getRows() { return layout.length; }
    public int getCols() { return layout[0].length; }
    public int getTileSize() { return tileSize; }

    /** Check if a point (world coordinates) is on the exit tile */
    public boolean isOnExit(double worldX, double worldY) {
        int col = (int) ((worldX - getLayoutX()) / tileSize);
        int row = (int) ((worldY - getLayoutY()) / tileSize);
        if (row >= 0 && row < getRows() && col >= 0 && col < getCols()) {
            return layout[row][col] == 2;
        }
        return false;
    }

    /** Circle vs wall collision detection */
    public boolean collidesWithCircle(double worldX, double worldY, double radius, boolean exitUnlocked) {
        int centerCol = (int) ((worldX - getLayoutX()) / tileSize);
        int centerRow = (int) ((worldY - getLayoutY()) / tileSize);

        for (int r = Math.max(0, centerRow - 1); r <= Math.min(getRows() - 1, centerRow + 1); r++) {
            for (int c = Math.max(0, centerCol - 1); c <= Math.min(getCols() - 1, centerCol + 1); c++) {
                // Check if this tile should be treated as solid
                int tileValue = layout[r][c];
                boolean isSolid = (tileValue == 1) || (tileValue == 2 && !exitUnlocked);
                
                if (isSolid) {
                    double rectX = getLayoutX() + c * tileSize;
                    double rectY = getLayoutY() + r * tileSize;

                    double closestX = clamp(worldX, rectX, rectX + tileSize);
                    double closestY = clamp(worldY, rectY, rectY + tileSize);

                    double dx = worldX - closestX;
                    double dy = worldY - closestY;
                    if (dx * dx + dy * dy < radius * radius) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Check if player is on a specific tile (used for puzzle doors) */
    public boolean isOnTile(double worldX, double worldY, int tileRow, int tileCol) {
        int col = (int) ((worldX - getLayoutX()) / tileSize);
        int row = (int) ((worldY - getLayoutY()) / tileSize);
        return row == tileRow && col == tileCol;
    }

    /** Utility: clamp a value between min and max */
    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
