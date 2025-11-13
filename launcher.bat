@echo off
cd /d "%~dp0"

REM Change to Platformer directory where everything is
cd Platformer

REM Compile the Java files
"jdk-21\jdk-21.0.8+9\bin\javac.exe" "App\Launcher.java" "App\UpdateChecker.java"

REM Run without showing console window
start "" "jdk-21\jdk-21.0.8+9\bin\javaw.exe" App.Launcher

REM Exit this batch window immediately
exit@echo off
cd /d "%~dp0"

REM Compile the Java files
"jdk-21\jdk-21.0.8+9\bin\javac.exe" "App\Launcher.java" "App\UpdateChecker.java"

REM Run without showing console window
start "" "jdk-21\jdk-21.0.8+9\bin\javaw.exe" App.Launcher

REM Exit this batch window immediately
exit@echo off
cd /d "%~dp0"

REM Compile the Java files (they're in the root directory)
"Platformer\jdk-21\jdk-21.0.8+9\bin\javac.exe" "App\Launcher.java" "App\UpdateChecker.java"

REM Run without showing console window
start "" "Platformer\jdk-21\jdk-21.0.8+9\bin\javaw.exe" App.Launcher

REM Exit this batch window immediately
exit@echo off
cd /d "%~dp0"

REM Compile the Java files
"Platformer\jdk-21\jdk-21.0.8+9\bin\javac.exe" App\Launcher.java App\UpdateChecker.java

REM Run without showing console window using relative path
start "" "Platformer\jdk-21\jdk-21.0.8+9\bin\javaw.exe" App.Launcher

REM Exit this batch window immediately
exit@echo off
cd /d "%~dp0"

REM Compile the Java files
Platformer\jdk-21\jdk-21.0.8+9\bin\javac.exe App\Launcher.java App\UpdateChecker.java

REM Run without showing console window using relative path
start Platformer\jdk-21\jdk-21.0.8+9\bin\javaw.exe App.Launcher

REM Exit this batch window immediately
exit@echo off
cd /d "%~dp0"

REM Compile the Java files
jdk-21\jdk-21.0.8+9\bin\javac.exe App\Launcher.java App\UpdateChecker.java

REM Run without showing console window using relative path
start jdk-21\jdk-21.0.8+9\bin\javaw.exe App.Launcher

REM Exit this batch window immediately
exit@echo off
cd /d "%~dp0"

REM Compile the Java files
jdk-21\jdk-21.0.8+9\bin\javac.exe App\Launcher.java App\UpdateChecker.java

@REM REM Run without showing console window
@REM start /B jdk-21\jdk-21.0.8+9\bin\javaw.exe App.Launcher

REM Exit this batch window immediately
exit