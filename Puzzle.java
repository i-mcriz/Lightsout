public class Puzzle {
    public enum Type { MCQ, TEXT }

    private final String id;
    private final Subject subject;
    private final Type type;
    private final String question;
    private final String[] options;
    private final int answerIndex;
    private final String contentText; // for TEXT type
    private final int timeLimit; // in seconds
    private final int row, col; // tile position in map

    public Puzzle(String id, Subject subject, Type type, String question,
                  String[] options, int answerIndex, String contentText,
                  int timeLimit, int row, int col) {
        this.id = id;
        this.subject = subject;
        this.type = type;
        this.question = question;
        this.options = options;
        this.answerIndex = answerIndex;
        this.contentText = contentText;
        this.timeLimit = timeLimit;
        this.row = row;
        this.col = col;
    }

    // Getters
    public String getId() { return id; }
    public Subject getSubject() { return subject; }
    public Type getType() { return type; }
    public String getQuestion() { return question; }
    public String[] getOptions() { return options; }
    public int getAnswerIndex() { return answerIndex; }
    public String getContentText() { return contentText; }
    public int getTimeLimit() { return timeLimit; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}
