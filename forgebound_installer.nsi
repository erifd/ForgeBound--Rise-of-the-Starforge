; Forgebound Installer Script (NSIS) - No Admin Required
; Requires NSIS (Nullsoft Scriptable Install System) to compile

!define APP_NAME "Forgebound"
!define APP_VERSION "1.0"
!define DEVELOPER_PASSWORD "DevAccess2025"

Name "${APP_NAME}"
OutFile "ForgeBound_Installer.exe"
InstallDir "$LOCALAPPDATA\${APP_NAME}"
RequestExecutionLevel user

!include "MUI2.nsh"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

Section "Install"
    SetOutPath "$INSTDIR"
    
    ; Copy all game files
    File /r "Platformer\*.*"
    
    ; Hide JDK and App folders using attrib
    nsExec::ExecToLog 'attrib +h +s "$INSTDIR\jdk-21"'
    nsExec::ExecToLog 'attrib +h +s "$INSTDIR\App"'
    
    ; Create the Forgebound launcher batch file
    FileOpen $0 "$INSTDIR\launcher.bat" w
    FileWrite $0 "@echo off$\r$\n"
    FileWrite $0 "cd /d `"%~dp0`"$\r$\n"
    FileWrite $0 "$\r$\n"
    FileWrite $0 "REM Compile the Java file$\r$\n"
    FileWrite $0 "`"jdk-21\jdk-21.0.8+9\bin\javac.exe`" App\Launcher.java$\r$\n"
    FileWrite $0 "$\r$\n"
    FileWrite $0 "REM Run the Java program$\r$\n"
    FileWrite $0 "start `"`" `"jdk-21\jdk-21.0.8+9\bin\java.exe`" App.Launcher$\r$\n"
    FileClose $0
    
    ; Create VBS wrapper to run batch silently (acts as Forgebound.exe)
    FileOpen $0 "$INSTDIR\Forgebound.vbs" w
    FileWrite $0 "Set WshShell = CreateObject(`"WScript.Shell`")$\r$\n"
    FileWrite $0 "WshShell.CurrentDirectory = CreateObject(`"Scripting.FileSystemObject`").GetParentFolderName(WScript.ScriptFullName)$\r$\n"
    FileWrite $0 "WshShell.Run `"launcher.bat`", 0, False$\r$\n"
    FileWrite $0 "Set WshShell = Nothing$\r$\n"
    FileClose $0
    
    ; Create Forgebound.bat that calls the VBS (user-friendly name)
    FileOpen $0 "$INSTDIR\Forgebound.bat" w
    FileWrite $0 "@echo off$\r$\n"
    FileWrite $0 "wscript.exe `"%~dp0Forgebound.vbs`"$\r$\n"
    FileClose $0
    
    ; Hide the internal files
    SetFileAttributes "$INSTDIR\launcher.bat" HIDDEN
    SetFileAttributes "$INSTDIR\Forgebound.vbs" HIDDEN
    
    ; Create developer backdoor
    FileOpen $0 "$INSTDIR\DevAccess.bat" w
    FileWrite $0 "@echo off$\r$\n"
    FileWrite $0 "title Developer Access - Forgebound$\r$\n"
    FileWrite $0 "color 0A$\r$\n"
    FileWrite $0 "echo ======================================$\r$\n"
    FileWrite $0 "echo    FORGEBOUND DEVELOPER ACCESS$\r$\n"
    FileWrite $0 "echo ======================================$\r$\n"
    FileWrite $0 "echo.$\r$\n"
    FileWrite $0 "set /p pass=Enter Developer Password: $\r$\n"
    FileWrite $0 "if NOT `"%pass%`"==`"${DEVELOPER_PASSWORD}`" ($\r$\n"
    FileWrite $0 "    color 0C$\r$\n"
    FileWrite $0 "    echo.$\r$\n"
    FileWrite $0 "    echo [ERROR] Access Denied!$\r$\n"
    FileWrite $0 "    timeout /t 2 >nul$\r$\n"
    FileWrite $0 "    exit$\r$\n"
    FileWrite $0 ")$\r$\n"
    FileWrite $0 "echo.$\r$\n"
    FileWrite $0 "echo [SUCCESS] Access Granted!$\r$\n"
    FileWrite $0 "echo.$\r$\n"
    FileWrite $0 "echo Unhiding protected files...$\r$\n"
    FileWrite $0 "attrib -h -s `"%~dp0jdk-21`" /s /d$\r$\n"
    FileWrite $0 "attrib -h -s `"%~dp0App`" /s /d$\r$\n"
    FileWrite $0 "attrib -h `"%~dp0launcher.bat`"$\r$\n"
    FileWrite $0 "attrib -h `"%~dp0Forgebound.vbs`"$\r$\n"
    FileWrite $0 "echo.$\r$\n"
    FileWrite $0 "echo Files are now visible for development.$\r$\n"
    FileWrite $0 "echo Opening installation folder...$\r$\n"
    FileWrite $0 "timeout /t 2 >nul$\r$\n"
    FileWrite $0 "explorer `"%~dp0`"$\r$\n"
    FileWrite $0 "echo.$\r$\n"
    FileWrite $0 "echo Press any key to re-hide files and exit...$\r$\n"
    FileWrite $0 "pause >nul$\r$\n"
    FileWrite $0 "attrib +h +s `"%~dp0jdk-21`" /s /d$\r$\n"
    FileWrite $0 "attrib +h +s `"%~dp0App`" /s /d$\r$\n"
    FileWrite $0 "attrib +h `"%~dp0launcher.bat`"$\r$\n"
    FileWrite $0 "attrib +h `"%~dp0Forgebound.vbs`"$\r$\n"
    FileWrite $0 "echo Files re-hidden. Goodbye!$\r$\n"
    FileWrite $0 "timeout /t 1 >nul$\r$\n"
    FileClose $0
    
    ; Hide developer backdoor
    SetFileAttributes "$INSTDIR\DevAccess.bat" HIDDEN|SYSTEM
    
    ; Create desktop shortcut
    CreateShortcut "$DESKTOP\${APP_NAME}.lnk" "$INSTDIR\Forgebound.bat" "" "$INSTDIR\Forgebound.bat" 0
    
    ; Create start menu shortcuts
    CreateDirectory "$SMPROGRAMS\${APP_NAME}"
    CreateShortcut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk" "$INSTDIR\Forgebound.bat"
    CreateShortcut "$SMPROGRAMS\${APP_NAME}\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
    
    ; Write uninstaller
    WriteUninstaller "$INSTDIR\Uninstall.exe"
    
    ; Write registry for current user only
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "DisplayName" "${APP_NAME}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "UninstallString" "$INSTDIR\Uninstall.exe"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "InstallLocation" "$INSTDIR"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "DisplayVersion" "${APP_VERSION}"
SectionEnd

Section "Uninstall"
    ; Unhide files before deletion
    nsExec::ExecToLog 'attrib -h -s "$INSTDIR\jdk-21" /s /d'
    nsExec::ExecToLog 'attrib -h -s "$INSTDIR\App" /s /d'
    
    ; Remove files and folders
    RMDir /r "$INSTDIR"
    
    ; Remove shortcuts
    Delete "$DESKTOP\${APP_NAME}.lnk"
    RMDir /r "$SMPROGRAMS\${APP_NAME}"
    
    ; Remove registry keys
    DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
SectionEnd