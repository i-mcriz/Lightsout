-- ====================================
-- Lights Out Game - Database Setup
-- ====================================
-- 
-- This script creates the database and table structure for the Lights Out maze game.
-- It also includes sample puzzle questions.

-- Step 1: Create the database
CREATE DATABASE IF NOT EXISTS lightsout_game;
USE lightsout_game;

-- Step 2: Create the puzzles table
CREATE TABLE IF NOT EXISTS puzzles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    subject VARCHAR(50) NOT NULL COMMENT 'MATH, ENGLISH, or SCIENCE',
    question TEXT NOT NULL COMMENT 'The question text',
    option1 VARCHAR(255) NOT NULL COMMENT 'First answer option',
    option2 VARCHAR(255) NOT NULL COMMENT 'Second answer option',
    option3 VARCHAR(255) NOT NULL COMMENT 'Third answer option',
    option4 VARCHAR(255) NOT NULL COMMENT 'Fourth answer option',
    correct_answer INT NOT NULL COMMENT '1, 2, 3, or 4 (1-indexed)',
    difficulty VARCHAR(20) DEFAULT 'MEDIUM' COMMENT 'EASY, MEDIUM, or HARD',
    time_limit INT DEFAULT 30 COMMENT 'Time limit in seconds',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 3: Insert sample puzzle questions
-- Feel free to add more questions or modify these!

-- MATH Questions
INSERT INTO puzzles (subject, question, option1, option2, option3, option4, correct_answer, difficulty, time_limit) VALUES
('MATH', 'What is 15 + 27?', '40', '42', '44', '46', 2, 'EASY', 20),
('MATH', 'What is 12 × 8?', '84', '96', '88', '92', 2, 'MEDIUM', 30),
('MATH', 'What is 7 × 9?', '56', '63', '72', '81', 2, 'MEDIUM', 25),
('MATH', 'What is the square root of 144?', '10', '11', '12', '13', 3, 'MEDIUM', 25),
('MATH', 'What is 25% of 80?', '15', '20', '25', '30', 2, 'MEDIUM', 30),
('MATH', 'What is 99 ÷ 9?', '9', '10', '11', '12', 3, 'EASY', 20),
('MATH', 'What is 2³ (2 to the power of 3)?', '6', '8', '9', '12', 2, 'MEDIUM', 25),
('MATH', 'If a triangle has angles 60°, 60°, and x°, what is x?', '30°', '45°', '60°', '90°', 3, 'MEDIUM', 30);

-- ENGLISH Questions
INSERT INTO puzzles (subject, question, option1, option2, option3, option4, correct_answer, difficulty, time_limit) VALUES
('ENGLISH', 'Choose the correct spelling:', 'Accomodate', 'Accommodate', 'Acomodate', 'Acommodate', 2, 'MEDIUM', 25),
('ENGLISH', 'What is the plural of "child"?', 'childs', 'childes', 'children', 'childrens', 3, 'EASY', 20),
('ENGLISH', 'Choose the synonym of "happy":', 'Sad', 'Joyful', 'Angry', 'Tired', 2, 'EASY', 20),
('ENGLISH', 'Which word is an adjective? "The quick brown fox"', 'The', 'quick', 'fox', 'brown', 2, 'MEDIUM', 25),
('ENGLISH', 'What is the past tense of "go"?', 'goed', 'went', 'gone', 'going', 2, 'EASY', 20),
('ENGLISH', 'Choose the correct: "She ___ to school every day."', 'go', 'goes', 'going', 'gone', 2, 'EASY', 20),
('ENGLISH', 'Which is a noun? "Run fast to win"', 'Run', 'fast', 'to', 'win', 4, 'MEDIUM', 25);

-- SCIENCE Questions
INSERT INTO puzzles (subject, question, option1, option2, option3, option4, correct_answer, difficulty, time_limit) VALUES
('SCIENCE', 'What is the chemical symbol for water?', 'H2O', 'CO2', 'O2', 'H2SO4', 1, 'EASY', 20),
('SCIENCE', 'What planet is known as the Red Planet?', 'Venus', 'Mars', 'Jupiter', 'Saturn', 2, 'EASY', 20),
('SCIENCE', 'How many bones are in the human body?', '196', '206', '216', '226', 2, 'MEDIUM', 30),
('SCIENCE', 'What gas do plants absorb from the atmosphere?', 'Oxygen', 'Nitrogen', 'Carbon Dioxide', 'Helium', 3, 'MEDIUM', 25),
('SCIENCE', 'What is the center of an atom called?', 'Electron', 'Proton', 'Nucleus', 'Neutron', 3, 'MEDIUM', 25),
('SCIENCE', 'At what temperature does water boil (Celsius)?', '90°C', '100°C', '110°C', '120°C', 2, 'EASY', 20),
('SCIENCE', 'What is the largest organ in the human body?', 'Heart', 'Brain', 'Skin', 'Liver', 3, 'MEDIUM', 25),
('SCIENCE', 'How many planets are in our solar system?', '7', '8', '9', '10', 2, 'EASY', 20);

-- Step 4: Verify the data
SELECT COUNT(*) as total_questions FROM puzzles;
SELECT subject, COUNT(*) as count FROM puzzles GROUP BY subject;

-- ====================================
-- Additional helpful queries:
-- ====================================

-- View all questions:
-- SELECT * FROM puzzles;

-- Add a new question:
-- INSERT INTO puzzles (subject, question, option1, option2, option3, option4, correct_answer, difficulty, time_limit) 
-- VALUES ('MATH', 'Your question here?', 'Option 1', 'Option 2', 'Option 3', 'Option 4', 2, 'MEDIUM', 30);

-- Delete a question:
-- DELETE FROM puzzles WHERE id = ?;

-- Update a question:
-- UPDATE puzzles SET question = 'New question text' WHERE id = ?;
