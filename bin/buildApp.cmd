set LUOGO=%~dp0
cd /d "%LUOGO%"
cd

pwsh -f buildApp.ps1
pause 