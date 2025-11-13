; Forgebound Installer Script (NSIS) - No Admin Required

!define APP_NAME "Forgebound"
!define APP_VERSION "1.0"
!define DEVELOPER_PASSWORD "DevAccess2025"

Name "${APP_NAME}"
OutFile "ForgeBound_Installer.exe"
InstallDir "$LOCALAPPDATA\${APP_NAME}"
RequestExecutionLevel user

!include "MUI2.nsh"

Var CreateDesktopShortcut

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

Section "Install"
    SetOutPath "$INSTDIR"
    File /r "Platformer\*.*"

    nsExec::ExecToLog 'attrib +h +s "$INSTDIR\jdk-21"'
    nsExec::ExecToLog 'attrib +h +s "$INSTDIR\App"'

    ; Create launcher.bat
    FileOpen $0 "$INSTDIR\launcher.bat" w
    FileWrite $0 "@echo off$\r$\ncd /d %~dp0$\r$\n"
    FileWrite $0 "jdk-21\jdk-21.0.8+9\bin\javac.exe App\Launcher.java$\r$\n"
    FileWrite $0 "start $\"$\" jdk-21\jdk-21.0.8+9\bin\java.exe App.Launcher$\r$\n"
    FileClose $0

    ; Create Forgebound.vbs
    FileOpen $0 "$INSTDIR\Forgebound.vbs" w
    FileWrite $0 "Set objShell=CreateObject($\"WScript.Shell$\")$\r$\n"
    FileWrite $0 "objShell.Run $\"launcher.bat$\", 0$\r$\n"
    FileClose $0

    ; Create Forgebound.bat
    FileOpen $0 "$INSTDIR\Forgebound.bat" w
    FileWrite $0 "@echo off$\r$\nwscript.exe $\"%~dp0Forgebound.vbs$\"$\r$\n"
    FileClose $0

    SetFileAttributes "$INSTDIR\launcher.bat" HIDDEN
    SetFileAttributes "$INSTDIR\Forgebound.vbs" HIDDEN

    ; Create DevAccess.bat
    FileOpen $0 "$INSTDIR\DevAccess.bat" w
    FileWrite $0 "@echo off$\r$\ntitle Developer Access$\r$\n"
    FileWrite $0 "color 0A$\r$\n"
    FileWrite $0 "echo ======================================$\r$\n"
    FileWrite $0 "echo FORGEBOUND DEVELOPER ACCESS$\r$\n"
    FileWrite $0 "echo ======================================$\r$\n"
    FileWrite $0 "set /p pass=Enter Password: $\r$\n"
    FileWrite $0 "if not $\"!pass!$\"==$\"${DEVELOPER_PASSWORD}$\" ($\r$\n"
    FileWrite $0 "  echo Access Denied$\r$\n"
    FileWrite $0 "  timeout /t 2 >nul$\r$\n"
    FileWrite $0 "  exit$\r$\n"
    FileWrite $0 ")$\r$\n"
    FileWrite $0 "echo Access Granted$\r$\n"
    FileWrite $0 "attrib -h -s $\"%~dp0jdk-21$\" /s /d$\r$\n"
    FileWrite $0 "attrib -h -s $\"%~dp0App$\" /s /d$\r$\n"
    FileWrite $0 "explorer $\"%~dp0$\"$\r$\n"
    FileWrite $0 "pause$\r$\n"
    FileWrite $0 "attrib +h +s $\"%~dp0jdk-21$\" /s /d$\r$\n"
    FileWrite $0 "attrib +h +s $\"%~dp0App$\" /s /d$\r$\n"
    FileClose $0

    SetFileAttributes "$INSTDIR\DevAccess.bat" HIDDEN|SYSTEM

    ${If} $CreateDesktopShortcut == 1
        CreateShortcut "$DESKTOP\${APP_NAME}.lnk" "$INSTDIR\Forgebound.bat"
    ${EndIf}

    CreateDirectory "$SMPROGRAMS\${APP_NAME}"
    CreateShortcut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk" "$INSTDIR\Forgebound.bat"
    CreateShortcut "$SMPROGRAMS\${APP_NAME}\Uninstall.lnk" "$INSTDIR\Uninstall.exe"

    WriteUninstaller "$INSTDIR\Uninstall.exe"

    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "DisplayName" "${APP_NAME}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "UninstallString" "$INSTDIR\Uninstall.exe"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "InstallLocation" "$INSTDIR"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" "DisplayVersion" "${APP_VERSION}"

    Exec "$INSTDIR\Forgebound.bat"
SectionEnd

Section "Uninstall"
    nsExec::ExecToLog 'attrib -h -s "$INSTDIR\jdk-21" /s /d'
    nsExec::ExecToLog 'attrib -h -s "$INSTDIR\App" /s /d'

    RMDir /r "$INSTDIR"
    Delete "$DESKTOP\${APP_NAME}.lnk"
    RMDir /r "$SMPROGRAMS\${APP_NAME}"
    DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
SectionEnd

Function .onInit
    StrCpy $CreateDesktopShortcut 1
FunctionEnd

Function LeaveDirectory
    MessageBox MB_YESNO "Create a desktop shortcut?" IDYES +2
    StrCpy $CreateDesktopShortcut 0
FunctionEnd