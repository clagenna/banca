set SRCCMD=%~dp0
echo Src cmd = %SRCCMD%
pause
rem @ echo off
if "%1" == "" (
  @echo nome del DB senza estensione ".DB"
  set /P NEWDB=Nome del nuovo DB SQLite:
) else (
  set NEWDB=%1
)
set NEWDB=%NEWDB%.db
set CANCDB=A
if exist "%NEWDB%" (
  @echo il DB "%NEWDB%" esiste Gia' !
  @echo [93;101mlo vuoi Cancellare ?[0m
  set /P CANCDB="Cancellare (Y/N) ?:"
)
if /i "%CANCDB%" == "y" (
  del "%NEWDB%" 
  ) 
sqlite3.exe "%NEWDB%" < %SRCCMD%\Banca.sql
dir "%NEWDB%"

:fine
