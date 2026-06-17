:: set debug=1


@echo off
:: memorizzo il codice di ESCAPE per mioecho 
for /F %%a in ('echo prompt $E ^| cmd') do @set "ESC=%%a["

@echo.
call :mioecho backup di tutto

if "%DEBUG%" == "1" @echo on
cd /d "%~dp0\.."
if "%DEBUG%" == "1" cd
if "%DEBUG%" == "1" pause


call :mioecho StdCla backup di stdcla
start "Backup degli Standard" /d ..\stdcla /wait cmd.exe /c "savzip.cmd"
if "%DEBUG%" == "1" pause
call :mioecho StdCla backup progetto Banca
start "Backup progetto Banca" /d ..\banca /wait cmd.exe /c "savzip.cmd"
if "%DEBUG%" == "1" pause

goto fine

----------------------------------------------------
:mioecho %1 - reverse color, %2,%3,... Bold e Yellow
:: call mioecho <prefx> <sufix>
@echo off
@echo %ESC%7m%~1%ESC%0m %ESC%93m %2 %3 %4 %5 %ESC%0m
if "%DEBUG%" == 1 @echo on
goto :eof

:fine
@echo off
@echo.
call :mioecho "fine backup"
pause