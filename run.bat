@echo off
cd /d "C:\Users\family_2\Pictures\Real_Platformer"

REM Compile the Java file
"C:\Users\family_2\Pictures\Real_Platformer\jdk-21\jdk-21.0.8+9\bin\javac.exe" App\Launcher.java

REM Run the Java program
start "" "C:\Users\family_2\Pictures\Real_Platformer\jdk-21\jdk-21.0.8+9\bin\java.exe" App.Launcher

pause
