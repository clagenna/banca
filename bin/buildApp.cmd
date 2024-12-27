@echo off
:: --------------------------------------------------------
:: (0) Init di alcune variabili [ESC, BaseDir, MvnCmd]
:: (0,1) ESC
for /F %%a in ('echo prompt $E ^| cmd') do @set "ESC=%%a["
if "%DEBUG%" == "1" echo on
:: (0,2) BaseDir
cd /d %~dp0
cd ..
set BaseDir=%CD%
call :mioecho "Base Dir" %BaseDir%

:: (0,4) MvnCmd il comando batch di Maven
set MvnCmd=
for /F "usebackq tokens=*" %%i in (`where mvn.cmd` ) do set MvnCmd=%%i
if   "%MvnCmd%" == "" goto nomvn
call :mioecho MvnCmd %MvnCmd%
if "%DEBUG%" == "1" pause

:: --------------------------------------------------------
:: (1) rinfresco il deploy degli standard
:: pushd ..\stdcla
call :mioecho "Deploy degli STD" sotto %CD%"
start "Deploy degli Standard" /d ..\stdcla /wait cmd.exe /c "%MvnCmd%" clean install
:: popd
if "%DEBUG%" == "1" cd
if "%DEBUG%" == "1" pause

:: --------------------------------------------------------
:: (2) Update della versione del progetto originale
call :mioecho "Update versione.java" sotto %BaseDir%
call ..\stdcla\bin\updVersione.cmd "%BaseDir%" 1
@echo off
if "%DEBUG%" == "1" echo on
if "%DEBUG%" == "1" pause

:: (3) Build del progetto in questione
call :mioecho "Build progetto in"  %CD%
rem  @echo Build of  %CD%
if "%DEBUG%" == "1" pause
pwsh -f "%BaseDir%\bin\buildApp.ps1"
call :mioecho "Fine Build" Banca
rem @echo %CD%

:: (4) Copia del Banca_Inst.zip su Google Drive
if exist Banca_inst.zip (
  if "%GOODRV%" neq "" (
    set DSTDRV=%GOODRV%\zips\photon2
    call :mioecho "Copio Banca_inst.zip" "%DSTDRV%"
    copy /Y Banca_inst.zip "%DSTDRV%"
    call :mioecho Copiato Banca_inst.zip
  )
)
@echo %ESC%92mFine del build di %CD%%ESC%0m
goto fine

----------------------------------------------------
:mioecho
:: call mioecho <prefx> <sufix>
@echo off
@echo %ESC%7m%~1%ESC%0m %ESC%93m %2 %3 %4 %5 %ESC%0m
if "%DEBUG%" == 1 @echo on
goto :eof

----------------------------------------------------
:nomvn
@echo %ESC%7;31mnon trovo Maven mvn.cmd%ESC%0m
goto fine

:fine
pause



