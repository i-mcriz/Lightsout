import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Manages player marks/score throughout the game.
 * Tracks correct and incorrect answers, maintains score out of 5.
 */
public class MarksManager {
    private static final int MAX_MARKS = 5;
    private static final int TOTAL_QUESTIONS = 5;
    
    private final IntegerProperty marks;
    private final IntegerProperty correctAnswers;
    private final IntegerProperty wrongAnswers;
    private final IntegerProperty questionsAnswered;
    
    public MarksManager() {
        this.marks = new SimpleIntegerProperty(0);
        this.correctAnswers = new SimpleIntegerProperty(0);
        this.wrongAnswers = new SimpleIntegerProperty(0);
        this.questionsAnswered = new SimpleIntegerProperty(0);
    }
    
    /**
     * Add a mark for a correct answer
     */
    public void addMark() {
        if (marks.get() < MAX_MARKS) {
            marks.set(marks.get() + 1);
            correctAnswers.set(correctAnswers.get() + 1);
            questionsAnswered.set(questionsAnswered.get() + 1);
            System.out.println("✓ Correct! Marks: " + marks.get() + "/" + MAX_MARKS);
        }
    }
    
    /**
     * Reduce a mark for a wrong answer (minimum 0)
     */
    public void reduceMark() {
        if (marks.get() > 0) {
            marks.set(marks.get() - 1);
        }
        wrongAnswers.set(wrongAnswers.get() + 1);
        questionsAnswered.set(questionsAnswered.get() + 1);
        System.out.println("✗ Wrong! Marks: " + marks.get() + "/" + MAX_MARKS);
    }
    
    /**
     * Record a wrong answer without reducing marks (time-up scenario)
     */
    public void recordWrongAnswer() {
        wrongAnswers.set(wrongAnswers.get() + 1);
        questionsAnswered.set(questionsAnswered.get() + 1);
        System.out.println("✗ Time's up! Marks: " + marks.get() + "/" + MAX_MARKS);
    }
    
    // Getters
    public int getMarks() {
        return marks.get();
    }
    
    public int getCorrectAnswers() {
        return correctAnswers.get();
    }
    
    public int getWrongAnswers() {
        return wrongAnswers.get();
    }
    
    public int getQuestionsAnswered() {
        return questionsAnswered.get();
    }
    
    public int getTotalQuestions() {
        return TOTAL_QUESTIONS;
    }
    
    public boolean isGameComplete() {
        return questionsAnswered.get() >= TOTAL_QUESTIONS;
    }
    
    public double getPercentage() {
        return (marks.get() * 100.0) / MAX_MARKS;
    }
    
    public String getGrade() {
        double percentage = getPercentage();
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }
    
    // Observable properties for UI binding
    public IntegerProperty marksProperty() {
        return marks;
    }
    
    public IntegerProperty correctAnswersProperty() {
        return correctAnswers;
    }
    
    public IntegerProperty wrongAnswersProperty() {
        return wrongAnswers;
    }
    
    public IntegerProperty questionsAnsweredProperty() {
        return questionsAnswered;
    }
    
    /**
     * Reset all marks and counters
     */
    public void reset() {
        marks.set(0);
        correctAnswers.set(0);
        wrongAnswers.set(0);
        questionsAnswered.set(0);
        System.out.println("MarksManager reset.");
    }
    
    @Override
    public String toString() {
        return String.format("Score: %d/%d | Correct: %d | Wrong: %d | Grade: %s",
                marks.get(), MAX_MARKS, correctAnswers.get(), wrongAnswers.get(), getGrade());
    }
}
