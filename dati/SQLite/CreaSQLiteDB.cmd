if "%1" == "" (
  @echo nome del DB senza estensione
  set /P NEWDB=Nome del nuovo DB SQLite:
) else (
  set NEWDB=%1
)
sqlite3.exe "%NEWDB%.db" < Banca.sql
dir "%NEWDB%.db"
