import java.util.HashMap;
import java.util.Map;

public class GameEngine {
    private final Map<Subject, Integer> solvedCount = new HashMap<>();

    public void puzzleSolved(Subject subject, boolean solved, long timeSpent) {
        if (solved) {
            solvedCount.put(subject, solvedCount.getOrDefault(subject, 0) + 1);
        }
        System.out.println("Puzzle solved? " + solved + " | Subject: " + subject + " | Time: " + timeSpent + "ms");
    }
}
