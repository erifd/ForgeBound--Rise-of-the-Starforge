@echo off
cd /d "%~dp0"
jdk-21\jdk-21.0.8+9\bin\javac.exe App\Launcher.java
start jdk-21\jdk-21.0.8+9\bin\java.exe App.Launcher