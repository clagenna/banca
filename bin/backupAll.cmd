@echo off

for /F %%a in ('echo prompt $E ^| cmd') do @set "ESC=%%a["

@echo.
call :mioecho backup di tutto

if "%DEBUG%" == "1" @echo on
cd /d "%~dp0\.."
if "%DEBUG%" == "1" cd
if "%DEBUG%" == "1" pause


call :mioecho StdCla backup di stdcla
start "Backup degli Standard" /d ..\stdcla /wait cmd.exe /c "savzip.cmd"
call :mioecho StdCla backup progetto Banca
if "%DEBUG%" == "1" pause
start "Backup progetto Banca" /d . /wait cmd.exe /c "savzip.cmd"

goto fine

----------------------------------------------------
:mioecho
:: call mioecho <prefx> <sufix>
@echo off
@echo %ESC%7m%~1%ESC%0m %ESC%93m %2 %3 %4 %5 %ESC%0m
if "%DEBUG%" == 1 @echo on
goto :eof

:fine
call :mioecho "fine backup"
pause