# 🎮 Lights Out - Maze Puzzle Game

A JavaFX-based educational maze game where players solve puzzles to progress through doors. Score marks out of 5 based on your answers!

## 📋 Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Database Setup](#database-setup)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Game](#running-the-game)
- [How to Play](#how-to-play)
- [Adding Custom Questions](#adding-custom-questions)

---

## ✨ Features

- **Interactive Maze Navigation** - WASD/Arrow keys to move through the maze
- **5 Puzzle Doors** - Each door contains a question from the database
- **Marks System** - Score out of 5 (1 mark per correct answer)
- **Mark Reduction** - Wrong answers reduce your score
- **Real-time Score Display** - See your marks as you play
- **SQL Database Integration** - Questions loaded from MySQL database
- **Three Subjects** - Math, English, and Science questions
- **Timed Challenges** - Answer before time runs out!
- **Final Grade Screen** - See your performance at the end

---

## 🔧 Requirements

### Software Required:
1. **Java Development Kit (JDK) 17 or higher**
   - Download: https://www.oracle.com/java/technologies/downloads/

2. **JavaFX SDK 25**
   - Already present at: `C:\Program Files\Java\javafx-sdk-25\lib`

3. **MySQL Server 8.0 or higher**
   - Download: https://dev.mysql.com/downloads/mysql/

4. **MySQL Connector/J (JDBC Driver)**
   - Download: https://dev.mysql.com/downloads/connector/j/
   - Or use Maven: `mysql-connector-java-8.0.33.jar`

---

## 🗄️ Database Setup

### Step 1: Install MySQL
1. Download and install MySQL Server
2. Remember your root password during installation
3. Start MySQL Server (usually starts automatically)

### Step 2: Run the SQL Script

**Option A: Using MySQL Workbench (Recommended)**
1. Open MySQL Workbench
2. Connect to your MySQL server (localhost)
3. Open `database_setup.sql` file
4. Click the lightning bolt icon (⚡) to execute
5. Verify in the Output tab that tables were created

**Option B: Using Command Line**
```bash
# Navigate to the project folder
cd "C:\Users\User\Desktop\lights out"

# Run the SQL script
mysql -u root -p < database_setup.sql

# Enter your MySQL password when prompted
```

**Option C: Using phpMyAdmin**
1. Open phpMyAdmin in your browser
2. Click "Import" tab
3. Choose `database_setup.sql` file
4. Click "Go"

### Step 3: Verify Database
```sql
-- Open MySQL command line or Workbench and run:
USE lightsout_game;
SELECT COUNT(*) FROM puzzles;
-- Should show at least 20 questions

SELECT subject, COUNT(*) as count FROM puzzles GROUP BY subject;
-- Should show questions distributed across MATH, ENGLISH, SCIENCE
```

---

## 💾 Installation

### Step 1: Download MySQL Connector

**Download the JDBC Driver:**
1. Go to: https://dev.mysql.com/downloads/connector/j/
2. Download "Platform Independent" ZIP
3. Extract `mysql-connector-java-x.x.x.jar`
4. Place it in: `C:\Users\User\Desktop\lights out\lib\`

**Or download directly:**
```powershell
# Create lib directory
mkdir "C:\Users\User\Desktop\lights out\lib"

# Download connector (example - use actual link)
# Place mysql-connector-java-8.0.33.jar in the lib folder
```

### Step 2: Media Files (Optional)
Place these files in the project folder if you have them:
- `music.mp3` - Background music
- `background.mp4` - Start menu video background

If you don't have these files, the game will run without them.

---

## ⚙️ Configuration

### Update Database Connection Details

Edit `DatabaseManager.java` (lines 44-46):

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/lightsout_game";
private static final String DB_USER = "root";        // Your MySQL username
private static final String DB_PASSWORD = "yourpass"; // Your MySQL password
```

### Common Configurations:

**If MySQL is on a different port:**
```java
private static final String DB_URL = "jdbc:mysql://localhost:3307/lightsout_game";
```

**If using a different database name:**
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/my_custom_db";
```

---

## 🚀 Running the Game

### Option 1: Using PowerShell (Recommended)

```powershell
# Navigate to project directory
cd "C:\Users\User\Desktop\lights out"

# Compile all files
javac --module-path "C:\Program Files\Java\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.media --class-path "lib\*" *.java

# Run the game
java --module-path "C:\Program Files\Java\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.media --class-path ".;lib\*" Start
```

### Option 2: Create a Run Script

Create `run.bat` file:
```batch
@echo off
echo Compiling Lights Out Game...
javac --module-path "C:\Program Files\Java\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.media --class-path "lib\*" *.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful! Starting game...
    java --module-path "C:\Program Files\Java\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.media --class-path ".;lib\*" Start
) else (
    echo Compilation failed!
    pause
)
```

Then just double-click `run.bat` to start the game!

---

## 🎯 How to Play

### Game Controls:
- **W / ↑** - Move Up
- **S / ↓** - Move Down
- **A / ←** - Move Left
- **D / →** - Move Right
- **P** - Pause (click pause button)

### Gameplay:
1. **Start the Game** - Click "Start Game" from the main menu
2. **Navigate the Maze** - Use WASD or arrow keys to move your player (white circle)
3. **Find Puzzle Doors** - Orange rectangles marked "DOOR" are puzzle locations
4. **Answer Questions** - Walk into a door to trigger a puzzle
   - Read the question carefully
   - Select your answer
   - Watch the timer!
5. **Earn Marks** - 
   - ✅ Correct answer = +1 mark (continue playing)
   - ❌ Wrong answer = -1 mark (game ends)
   - ⏰ Time up = No mark change (game ends)
6. **Complete All 5 Doors** - Answer all questions to finish
7. **View Your Score** - See your final marks and grade

### Scoring System:
- **Total Marks**: 5 (one per question)
- **Correct Answer**: +1 mark
- **Wrong Answer**: -1 mark (minimum 0)
- **Final Grade**:
  - 5/5 = A+
  - 4/5 = A
  - 3/5 = B
  - 2/5 = C
  - 1/5 = D
  - 0/5 = F

---

## ➕ Adding Custom Questions

### Method 1: Direct Database Insert

```sql
INSERT INTO puzzles (subject, question, option1, option2, option3, option4, correct_answer, difficulty, time_limit) 
VALUES (
    'MATH',                          -- Subject: MATH, ENGLISH, or SCIENCE
    'What is 5 + 5?',               -- Your question
    '8',                             -- Option 1
    '9',                             -- Option 2
    '10',                            -- Option 3
    '11',                            -- Option 4
    3,                               -- Correct answer (1-4)
    'EASY',                          -- Difficulty: EASY, MEDIUM, HARD
    20                               -- Time limit in seconds
);
```

### Method 2: Using MySQL Workbench
1. Open MySQL Workbench
2. Connect to `lightsout_game` database
3. Right-click on `puzzles` table → "Select Rows - Limit 1000"
4. Click the edit icon
5. Add new rows
6. Click "Apply"

### Method 3: Bulk Import from Excel/CSV
1. Export your questions to CSV format:
   ```
   subject,question,option1,option2,option3,option4,correct_answer,difficulty,time_limit
   MATH,"What is 2+2?","2","3","4","5",3,EASY,20
   ```
2. In MySQL Workbench: Table → Import → Import from CSV
3. Map columns and import

---

## 🎨 Visual Enhancements

The game includes:
- ✨ Smooth fade transitions
- 🚪 Attractive orange doors with labels
- 📊 Real-time marks display in HUD
- 🎯 Exit tile highlighted in blue
- ⏱️ Countdown timer in puzzle dialogs
- 🎨 Clean, modern UI design

---

## 🐛 Troubleshooting

### "Database connection failed"
**Solution**: 
- Check MySQL is running (Services → MySQL80)
- Verify database name is `lightsout_game`
- Check username/password in `DatabaseManager.java`
- Ensure MySQL is on port 3306

### "MySQL JDBC Driver not found"
**Solution**:
- Download MySQL Connector/J
- Place `mysql-connector-java-x.x.x.jar` in `lib/` folder
- Add `--class-path "lib\*"` to compile/run commands

### "No puzzles loaded" or "Using fallback sample puzzles"
**Solution**:
- Database connection issue - check credentials
- Run `database_setup.sql` to create tables
- Verify puzzles exist: `SELECT COUNT(*) FROM puzzles;`

### "JavaFX cannot be resolved"
**Solution**:
- Ensure JavaFX SDK is at `C:\Program Files\Java\javafx-sdk-25\lib`
- Use the full compile/run commands with `--module-path`

### Game window doesn't open
**Solution**:
- Check Java version: `java --version` (must be 17+)
- Verify JavaFX modules are loaded
- Check console for error messages

---

## 📁 File Structure

```
lights out/
├── database_setup.sql          # SQL script to create database
├── README.md                   # This file
├── lib/                        # JDBC driver folder
│   └── mysql-connector-java-x.x.x.jar
├── Start.java                  # Main entry point
├── GameScene.java              # Game controller (UPDATED with marks)
├── GameMap.java                # Maze rendering
├── Level1.java                 # Maze layout
├── Player.java                 # Player movement
├── Puzzle.java                 # Puzzle data model
├── PuzzleDoor.java             # Door logic (UPDATED for marks)
├── MarksManager.java           # NEW: Score tracking
├── DatabaseManager.java        # NEW: SQL integration
├── Subject.java                # Enum for subjects
├── GameEngine.java             # Stats tracking
├── music.mp3                   # Optional: background music
└── background.mp4              # Optional: menu video
```

---

## 🔐 Database Security Notes

**For Production/Deployment:**
1. Never hardcode database passwords
2. Use environment variables:
   ```java
   String dbUser = System.getenv("DB_USER");
   String dbPass = System.getenv("DB_PASSWORD");
   ```
3. Create a separate database user (not root):
   ```sql
   CREATE USER 'gameuser'@'localhost' IDENTIFIED BY 'securepassword';
   GRANT SELECT ON lightsout_game.* TO 'gameuser'@'localhost';
   ```

---

## 📞 Support

If you encounter issues:
1. Check the console output for error messages
2. Verify all prerequisites are installed
3. Review the troubleshooting section
4. Check that database tables exist and have data

---

## 📝 License

This is an educational project. Feel free to modify and enhance it for learning purposes.

---

## 🎓 Educational Use

This game is designed for:
- Learning Java and JavaFX
- Understanding database integration
- Game development basics
- Quiz/assessment systems

---

**Enjoy the game! 🎮**
