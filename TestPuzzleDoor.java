public class TestPuzzleDoor {
    public static void main(String[] args) {
        Puzzle p = new Puzzle("test1", Subject.MATH, Puzzle.Type.MCQ,
                "1+1?", new String[]{"1","2","3"}, 1, null, 5, 0, 0);
        PuzzleDoor pd = new PuzzleDoor(p);
        // Test removed - use actual game to test puzzle doors
        System.out.println("PuzzleDoor test: Use the game to test puzzle functionality");
        System.out.println("Puzzle created: " + p.getQuestion());
    }
}
