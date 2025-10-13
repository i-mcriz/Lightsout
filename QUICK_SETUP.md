# 🚀 QUICK SETUP GUIDE - Lights Out Game with SQL Integration

## ✅ What's Been Implemented

### 1. **Marks System** ✓
- `MarksManager.java` - Tracks score out of 5
- +1 mark for correct answer
- -1 mark for wrong answer (minimum 0)
- Real-time score display in HUD
- Final grade screen (A+ to F)

### 2. **SQL Database Integration** ✓
- `DatabaseManager.java` - Connects to MySQL
- Loads 5 random questions from database
- Falls back to sample questions if database unavailable
- Supports MATH, ENGLISH, SCIENCE subjects

### 3. **Enhanced Visuals** ✓
- Beautiful gradient doors (orange to gold)
- Glow effects on doors
- Door emoji icons (🚪)
- Real-time marks display (green/orange/red based on score)
- Final score screen with grade animation

### 4. **Game Flow** ✓
- 5 puzzle doors in strategic maze positions
- Correct answer → continue game, +1 mark
- Wrong answer → end game, -1 mark, show final score
- Time up → end game, show final score
- All 5 correct → show victory screen with grade

---

## 📦 Files Added/Modified

### New Files Created:
1. ✅ `MarksManager.java` - Score tracking system
2. ✅ `DatabaseManager.java` - SQL integration
3. ✅ `database_setup.sql` - **YOUR SQL FILE LOCATION**
4. ✅ `README.md` - Complete documentation
5. ✅ `run.bat` - Easy run script
6. ✅ `QUICK_SETUP.md` - This file

### Modified Files:
1. ✅ `GameScene.java` - Added marks HUD, database loading, final score screen
2. ✅ `PuzzleDoor.java` - Updated callback system for marks

### Unchanged (Still Working):
- `Start.java`, `Player.java`, `GameMap.java`, `Level1.java`, `Puzzle.java`, `Subject.java`, `GameEngine.java`

---

## 🗄️ WHERE TO PUT YOUR SQL FILE

### **Option 1: Use the Provided SQL File (Recommended)**
The file `database_setup.sql` is already created with 20+ sample questions.

**To execute it:**
```bash
# Open MySQL Workbench or command line
mysql -u root -p < "C:\Users\User\Desktop\lights out\database_setup.sql"
```

### **Option 2: Add Your Own SQL File**
If you have your own SQL file with questions:

1. **Place your SQL file here:**
   ```
   C:\Users\User\Desktop\lights out\your_custom_questions.sql
   ```

2. **Make sure it has this table structure:**
   ```sql
   CREATE TABLE puzzles (
       id INT PRIMARY KEY AUTO_INCREMENT,
       subject VARCHAR(50) NOT NULL,           -- 'MATH', 'ENGLISH', or 'SCIENCE'
       question TEXT NOT NULL,
       option1 VARCHAR(255) NOT NULL,
       option2 VARCHAR(255) NOT NULL,
       option3 VARCHAR(255) NOT NULL,
       option4 VARCHAR(255) NOT NULL,
       correct_answer INT NOT NULL,            -- 1, 2, 3, or 4
       difficulty VARCHAR(20) DEFAULT 'MEDIUM',
       time_limit INT DEFAULT 30
   );
   ```

3. **Execute your SQL file:**
   ```bash
   mysql -u root -p lightsout_game < your_custom_questions.sql
   ```

---

## ⚙️ CONFIGURATION STEPS

### Step 1: Install MySQL (if not already installed)
1. Download: https://dev.mysql.com/downloads/mysql/
2. Install and remember your root password
3. Start MySQL service

### Step 2: Set Up Database
```bash
# Option A: Using command line
cd "C:\Users\User\Desktop\lights out"
mysql -u root -p < database_setup.sql

# Option B: Using MySQL Workbench
# - Open MySQL Workbench
# - File → Open SQL Script → Select database_setup.sql
# - Click Execute (⚡)
```

### Step 3: Download MySQL JDBC Driver
1. Download from: https://dev.mysql.com/downloads/connector/j/
2. Extract `mysql-connector-java-8.0.33.jar` (or latest version)
3. Create folder: `C:\Users\User\Desktop\lights out\lib\`
4. Put the JAR file inside `lib\`

### Step 4: Configure Database Connection
Edit `DatabaseManager.java` (lines 44-46):
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/lightsout_game";
private static final String DB_USER = "root";         // Your MySQL username
private static final String DB_PASSWORD = "yourpass"; // Your MySQL password
```

---

## 🎮 RUNNING THE GAME

### Option 1: Double-click `run.bat`
The easiest way! Just double-click `run.bat` in the project folder.

### Option 2: Manual Commands
```powershell
# Compile
javac --module-path "C:\Program Files\Java\javafx-sdk-25\lib" ^
      --add-modules javafx.controls,javafx.media ^
      --class-path "lib\*" *.java

# Run
java --module-path "C:\Program Files\Java\javafx-sdk-25\lib" ^
     --add-modules javafx.controls,javafx.media ^
     --class-path ".;lib\*" Start
```

---

## 🎯 GAME FEATURES

### Marks System
- **Starting Score**: 0/5
- **Correct Answer**: +1 mark
- **Wrong Answer**: -1 mark (minimum 0)
- **Time Up**: No change, game ends
- **Final Grades**:
  - 5/5 = A+ (100%)
  - 4/5 = A (80%)
  - 3/5 = B (60%)
  - 2/5 = C (40%)
  - 1/5 = D (20%)
  - 0/5 = F (0%)

### Visual Enhancements
- 🚪 Gradient doors (orange → gold) with glow effect
- 📊 Real-time score display (changes color based on performance)
- 🎨 Final score screen with grade and statistics
- ⏱️ Countdown timer in each puzzle
- ✨ Smooth fade animations

### Maze Layout (5 Doors)
Doors are strategically placed at:
1. Position (3, 3) - Early door
2. Position (5, 5) - Mid-left
3. Position (7, 8) - Mid-right
4. Position (9, 6) - Late-left
5. Position (11, 10) - Near exit

---

## 🐛 TROUBLESHOOTING

### "Database connection failed"
**Fix**: 
- Check MySQL is running
- Verify database name is `lightsout_game`
- Check username/password in `DatabaseManager.java`

### "MySQL JDBC Driver not found"
**Fix**:
- Download `mysql-connector-java-x.x.x.jar`
- Place in `lib/` folder
- Use `run.bat` or include `--class-path "lib\*"` in commands

### "Using fallback sample puzzles"
**Meaning**: Database not connected, using hardcoded questions
**Fix**: 
- Run `database_setup.sql`
- Check database credentials
- Verify table exists: `SELECT COUNT(*) FROM puzzles;`

### Game shows only 2-3 doors instead of 5
**Fix**: 
- Add more questions to database (need at least 5)
- Run the sample inserts in `database_setup.sql`

---

## 📊 TESTING CHECKLIST

### Before Playing:
- [ ] MySQL server is running
- [ ] Database `lightsout_game` exists
- [ ] Table `puzzles` has at least 5 questions
- [ ] JDBC driver is in `lib/` folder
- [ ] Database credentials are correct in `DatabaseManager.java`

### During Play:
- [ ] Start menu shows
- [ ] Can start game
- [ ] Maze renders with 5 orange doors
- [ ] Player can move with WASD/arrows
- [ ] Walking into door opens puzzle
- [ ] Timer counts down
- [ ] Correct answer: door disappears, score increases, game continues
- [ ] Wrong answer: game ends, final score shown
- [ ] Final score screen shows grade

---

## 📁 ADDING YOUR OWN QUESTIONS

### Method 1: Direct SQL Insert
```sql
INSERT INTO puzzles (subject, question, option1, option2, option3, option4, correct_answer, time_limit)
VALUES ('MATH', 'What is 10 × 5?', '40', '45', '50', '55', 3, 25);
```

### Method 2: Bulk CSV Import
1. Create CSV file:
```csv
subject,question,option1,option2,option3,option4,correct_answer,difficulty,time_limit
MATH,"What is 3+3?","4","5","6","7",3,EASY,20
ENGLISH,"Synonym of 'big'?","small","large","tiny","short",2,EASY,20
```

2. Import in MySQL Workbench:
   - Right-click `puzzles` table
   - Table Data Import Wizard
   - Select your CSV
   - Map columns
   - Import

---

## 🎓 DEMO QUESTIONS INCLUDED

The `database_setup.sql` file includes:
- **8 MATH questions** (addition, multiplication, powers, etc.)
- **7 ENGLISH questions** (spelling, grammar, synonyms, etc.)
- **8 SCIENCE questions** (chemistry, biology, astronomy, etc.)

Total: **23 questions** (game randomly selects 5 each time)

---

## 🎨 CUSTOMIZATION OPTIONS

### Change Door Appearance
Edit `GameScene.java` line ~135-170 to modify:
- Door colors (DARKORANGE, GOLD)
- Glow radius and color
- Border thickness
- Door emoji

### Change Scoring Rules
Edit `MarksManager.java`:
- MAX_MARKS constant (line 10)
- Grade thresholds (getGrade() method)

### Change Door Positions
Edit `DatabaseManager.java` line 115:
```java
int[][] doorPositions = {{3, 3}, {5, 5}, {7, 8}, {9, 6}, {11, 10}};
```

---

## ✅ SUMMARY

**Everything is ready to run!** 

1. Set up MySQL database (run `database_setup.sql`)
2. Download JDBC driver (put in `lib/` folder)
3. Update database credentials in `DatabaseManager.java`
4. Double-click `run.bat`

**Your game now:**
- ✅ Loads questions from SQL database
- ✅ Tracks marks out of 5
- ✅ Shows real-time score
- ✅ Has 5 beautiful glowing doors
- ✅ Displays final grade screen
- ✅ Looks professional and runs smoothly

---

**Need help?** Check the full `README.md` for detailed documentation!
