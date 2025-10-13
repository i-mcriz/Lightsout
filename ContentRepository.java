import java.util.ArrayList;
import java.util.List;

public class ContentRepository {
    public static Puzzle getSamplePuzzle() {
        return new Puzzle(
            "p1",
            Subject.MATH,
            Puzzle.Type.MCQ,
            "2+2=?",
            new String[]{"3","4","5","6"},
            1,
            null,
            10,
            5, 5 // row, col on map
        );
    }
}
