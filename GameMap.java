import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameMap extends Pane {

    private final int[][] layout;
    private final int tileSize;

    public GameMap(int[][] layout, int tileSize) {
        this.layout = layout;
        this.tileSize = tileSize;
        drawMap();
    }

    /** Draw the grid based on layout array */
    private void drawMap() {
        getChildren().clear();
        for (int r = 0; r < layout.length; r++) {
            for (int c = 0; c < layout[r].length; c++) {
                Rectangle tile = new Rectangle(c * tileSize, r * tileSize, tileSize, tileSize);
                switch (layout[r][c]) {
                    case 1 -> tile.setFill(Color.GRAY);        // wall
                    case 2 -> tile.setFill(Color.DODGERBLUE);  // exit
                    default -> tile.setFill(Color.WHITE);      // path
                }
                tile.setStroke(Color.BLACK);
                tile.setStrokeWidth(0.5);
                getChildren().add(tile);
            }
        }
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
    public boolean collidesWithCircle(double worldX, double worldY, double radius) {
        int centerCol = (int) ((worldX - getLayoutX()) / tileSize);
        int centerRow = (int) ((worldY - getLayoutY()) / tileSize);

        for (int r = Math.max(0, centerRow - 1); r <= Math.min(getRows() - 1, centerRow + 1); r++) {
            for (int c = Math.max(0, centerCol - 1); c <= Math.min(getCols() - 1, centerCol + 1); c++) {
                if (layout[r][c] == 1) { // wall
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
