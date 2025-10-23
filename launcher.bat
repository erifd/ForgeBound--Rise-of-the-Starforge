@echo off
cd /d "%~dp0"

REM Compile the Java files
jdk-21\jdk-21.0.8+9\bin\javac.exe App\Launcher.java App\UpdateChecker.java

REM Run without showing console window
start /B jdk-21\jdk-21.0.8+9\bin\javaw.exe App.Launcher

REM Exit this batch window immediately
exit