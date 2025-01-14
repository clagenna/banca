:: pwsh -f installApp.ps1
for /F %%a in ('echo prompt $E ^| cmd') do @set "ESC=%%a["

cd /d "$~dp0"
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
@echo Fatto !!!