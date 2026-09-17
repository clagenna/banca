@echo off
if "%DEBUG%" == "1" echo on
cd /d "%~dp0"
goto iniz

=======================================================================
Installare PWSH se non è presente.


C:\>winget search --id Microsoft.PowerShell

The `msstore` source requires that you view the following agreements before using.
Terms of Transaction: https://aka.ms/microsoft-store-terms-of-transaction
The source requires the current machine's 2-letter geographic region to be sent to the backend service to function properly (ex. "US").

Do you agree to all the source agreements terms?
[Y] Yes  [N] No: -->  Y  <--
Name               Id                           Version   Source
-----------------------------------------------------------------
PowerShell         Microsoft.PowerShell         7.5.4.0   winget
PowerShell Preview Microsoft.PowerShell.Preview 7.6.0.101 winget

a questo punto posso digitare:
winget install --id Microsoft.PowerShell --source winget

=======================================================================

:iniz
where pwsh.exe
if %ERRORLEVEL% EQU 0 goto cont
@echo .
@echo Installo POWERSHELL perche' non presente!
@echo .
winget install --id Microsoft.PowerShell --source winget

:cont
cd /d "%~dp0"
pwsh -f installApp.ps1
goto fine

=======================================================================
for /F %%a in ('echo prompt $E ^| cmd') do @set "ESC=%%a["

cd /d "%~dp0"
@echo Scegli quale tipo di DataBase vuoi utilizzare
@echo  "1 - %ESC%7mSQLite%ESC%0m (default) nessun bisogno di per-installazione di un server DB"
@echo  "2 - %ESC%7mSQL Server%ESC%0m  : il server Microsoft (Vers >= 19) deve essere pre-installata"
set /P cosa=Scegli il DB :
if "%cosa%" == "" set cosa=1
if  "%cosa%" == "1" (
   copy /Y BancaNuovo.db Banca.db
   copy /Y Banca_SQLite.properties Banca.properties 
)
if  "%cosa%" == "2" (
   
   copy /Y Banca_SQLServer.properties Banca.properties 
)
=======================================================================

:fine
@echo Fatto !!!
pause