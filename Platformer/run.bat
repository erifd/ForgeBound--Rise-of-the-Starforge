@echo off
cd /d "%~dp0"

REM Compile the Java file
"jdk-21\jdk-21.0.8+9\bin\javac.exe" App\Launcher.java

REM Run the Java program
start "" "jdk-21\jdk-21.0.8+9\bin\java.exe" App.Launcher

pause