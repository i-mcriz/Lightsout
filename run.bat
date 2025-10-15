@echo off
echo ================================================
echo    LIGHTS OUT - Maze Puzzle Game
echo    Compilation and Execution Script
echo ================================================
echo.

REM Set JavaFX path
set JAVAFX_PATH=C:\Program Files\Java\javafx-sdk-25\lib
set JDBC_LIB=lib\*

echo [1/3] Compiling Java files...
javac --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.media,javafx.base --class-path "%JDBC_LIB%" *.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Compilation FAILED! Check errors above.
    echo.
    pause
    exit /b 1
)

echo ✓ Compilation successful!
echo.
echo [2/3] Checking database connection...
echo      Make sure MySQL is running and database is set up!
echo.
echo [3/3] Starting game...
echo.

java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.media,javafx.base --class-path ".;%JDBC_LIB%" Start

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Game encountered an error!
    echo.
)

echo.
echo Game closed.
pause
