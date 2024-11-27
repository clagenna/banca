@echo off
if "%1" == "" (
  @echo nome del DB senza estensione
  set /P NEWDB=Nome del nuovo DB SQLite:
) else (
  set NEWDB=%1
)
set CANCDB=A
if exist "%NEWDB%.db" (
  @echo il DB "%NEWDB%.db" esiste Gia' !
  @echo [93;101mlo vuoi Cancellare ?[0m
  set /P CANCDB="Cancellare (Y/N) ?:"
)
if /i "%CANCDB%" == "y" del "%NEWDB%.db"
if /i "%CANCDB%" == "n" goto fine
sqlite3.exe "%NEWDB%.db" < Banca.sql
dir "%NEWDB%.db"

:fine
