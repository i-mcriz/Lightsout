@echo off
REM === JavaFX run script ===

java --module-path "C:\Program Files\Java\javafx-sdk-25\lib" ^
     --add-modules javafx.controls,javafx.fxml,javafx.media ^
     --enable-native-access=javafx.graphics,javafx.media ^
     Start

pause
