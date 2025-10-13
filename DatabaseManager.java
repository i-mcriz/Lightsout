import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages database connection and puzzle queries.
 * 
 * DATABASE SETUP INSTRUCTIONS:
 * ============================
 * 
 * 1. Create a database named "lightsout_game"
 * 
 * 2. Create a table with this structure:
 * 
 *    CREATE TABLE puzzles (
 *        id INT PRIMARY KEY AUTO_INCREMENT,
 *        subject VARCHAR(50) NOT NULL,
 *        question TEXT NOT NULL,
 *        option1 VARCHAR(255) NOT NULL,
 *        option2 VARCHAR(255) NOT NULL,
 *        option3 VARCHAR(255) NOT NULL,
 *        option4 VARCHAR(255) NOT NULL,
 *        correct_answer INT NOT NULL,
 *        difficulty VARCHAR(20),
 *        time_limit INT DEFAULT 30
 *    );
 * 
 * 3. Insert sample questions:
 * 
 *    INSERT INTO puzzles (subject, question, option1, option2, option3, option4, correct_answer, difficulty, time_limit) VALUES
 *    ('MATH', 'What is 2 + 2?', '3', '4', '5', '6', 2, 'EASY', 20),
 *    ('MATH', 'What is 12 × 8?', '84', '96', '88', '92', 2, 'MEDIUM', 30),
 *    ('ENGLISH', 'Which is the correct spelling?', 'Accomodate', 'Accommodate', 'Acomodate', 'Acommodate', 2, 'MEDIUM', 25),
 *    ('SCIENCE', 'What is the chemical symbol for water?', 'H2O', 'CO2', 'O2', 'H2SO4', 1, 'EASY', 20),
 *    ('MATH', 'What is the square root of 144?', '10', '11', '12', '13', 3, 'MEDIUM', 25);
 * 
 * 4. Update the connection details below with your MySQL credentials
 * 
 * FILE LOCATION: Place your SQL script in:
 * C:\Users\User\Desktop\lights out\database_setup.sql
 */
public class DatabaseManager {
    
    // ========== CHANGE THESE TO MATCH YOUR DATABASE SETUP ==========
    private static final String DB_URL = "jdbc:mysql://localhost:3306/lightsout_game";
    private static final String DB_USER = "root";  // Change to your MySQL username
    private static final String DB_PASSWORD = "";   // Change to your MySQL password
    // ================================================================
    
    private Connection connection;
    
    /**
     * Initialize database connection
     */
    public DatabaseManager() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL JDBC Driver loaded");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver not found!");
            System.err.println("  Download from: https://dev.mysql.com/downloads/connector/j/");
            System.err.println("  Place mysql-connector-java-x.x.x.jar in your classpath");
        }
    }
    
    /**
     * Connect to the database
     */
    public boolean connect() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✓ Database connected successfully");
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            System.err.println("  Make sure MySQL is running and database 'lightsout_game' exists");
            return false;
        }
    }
    
    /**
     * Close database connection
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database disconnected");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error closing database: " + e.getMessage());
        }
    }
    
    /**
     * Load exactly 5 random puzzles from the database
     */
    public List<Puzzle> loadPuzzlesForGame() {
        List<Puzzle> puzzles = new ArrayList<>();
        
        // If database connection fails, return sample puzzles
        if (connection == null && !connect()) {
            System.out.println("⚠ Using fallback sample puzzles (database unavailable)");
            return getSamplePuzzles();
        }
        
        String query = "SELECT id, subject, question, option1, option2, option3, option4, " +
                       "correct_answer, time_limit FROM puzzles ORDER BY RAND() LIMIT 5";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int doorIndex = 0;
            int[][] doorPositions = {{3, 3}, {5, 5}, {7, 8}, {9, 6}, {11, 10}};  // 5 strategic positions
            
            while (rs.next() && doorIndex < 5) {
                String id = "db_" + rs.getInt("id");
                String subjectStr = rs.getString("subject");
                Subject subject = Subject.valueOf(subjectStr.toUpperCase());
                String question = rs.getString("question");
                
                String[] options = {
                    rs.getString("option1"),
                    rs.getString("option2"),
                    rs.getString("option3"),
                    rs.getString("option4")
                };
                
                int correctAnswer = rs.getInt("correct_answer") - 1; // Convert to 0-indexed
                int timeLimit = rs.getInt("time_limit");
                
                int[] pos = doorPositions[doorIndex];
                Puzzle puzzle = new Puzzle(id, subject, Puzzle.Type.MCQ, question, 
                                          options, correctAnswer, null, timeLimit, 
                                          pos[0], pos[1]);
                puzzles.add(puzzle);
                doorIndex++;
                
                System.out.println("✓ Loaded puzzle: " + question.substring(0, Math.min(30, question.length())) + "...");
            }
            
            // If we got fewer than 5 puzzles, fill with samples
            if (puzzles.size() < 5) {
                System.out.println("⚠ Only " + puzzles.size() + " puzzles in database, adding samples");
                List<Puzzle> samples = getSamplePuzzles();
                while (puzzles.size() < 5 && puzzles.size() < samples.size()) {
                    puzzles.add(samples.get(puzzles.size()));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ Error loading puzzles: " + e.getMessage());
            return getSamplePuzzles();
        }
        
        return puzzles;
    }
    
    /**
     * Fallback sample puzzles if database is unavailable
     */
    private List<Puzzle> getSamplePuzzles() {
        List<Puzzle> puzzles = new ArrayList<>();
        int[][] positions = {{3, 3}, {5, 5}, {7, 8}, {9, 6}, {11, 10}};
        
        puzzles.add(new Puzzle("sample1", Subject.MATH, Puzzle.Type.MCQ,
                "What is 15 + 27?", 
                new String[]{"40", "42", "44", "46"}, 1, null, 20, positions[0][0], positions[0][1]));
        
        puzzles.add(new Puzzle("sample2", Subject.ENGLISH, Puzzle.Type.MCQ,
                "Choose the correct word: The weather is _____ today.",
                new String[]{"beautiful", "beautifull", "beutiful", "beatiful"}, 0, null, 25, positions[1][0], positions[1][1]));
        
        puzzles.add(new Puzzle("sample3", Subject.SCIENCE, Puzzle.Type.MCQ,
                "What planet is known as the Red Planet?",
                new String[]{"Venus", "Mars", "Jupiter", "Saturn"}, 1, null, 20, positions[2][0], positions[2][1]));
        
        puzzles.add(new Puzzle("sample4", Subject.MATH, Puzzle.Type.MCQ,
                "What is 7 × 9?",
                new String[]{"56", "63", "72", "81"}, 1, null, 25, positions[3][0], positions[3][1]));
        
        puzzles.add(new Puzzle("sample5", Subject.ENGLISH, Puzzle.Type.MCQ,
                "What is the plural of 'child'?",
                new String[]{"childs", "childes", "children", "childrens"}, 2, null, 20, positions[4][0], positions[4][1]));
        
        return puzzles;
    }
    
    /**
     * Validate answer against database
     */
    public boolean validateAnswer(String puzzleId, int selectedAnswer) {
        if (!puzzleId.startsWith("db_")) {
            // Sample puzzle, can't validate from DB
            return false;
        }
        
        try {
            int dbId = Integer.parseInt(puzzleId.substring(3));
            String query = "SELECT correct_answer FROM puzzles WHERE id = ?";
            
            // FIX: Use try-with-resources for both PreparedStatement AND ResultSet
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                
                // FIX: Set parameter BEFORE executing query!
                pstmt.setInt(1, dbId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int correctAnswer = rs.getInt("correct_answer") - 1; // Convert to 0-indexed
                        return selectedAnswer == correctAnswer;
                    }
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("✗ Error validating answer: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if database is connected
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
