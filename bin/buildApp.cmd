@echo off
:: --------------------------------------------------------
:: (0) Init di alcune variabili [ESC, BaseDir, MvnCmd]
:: - - - - - 
:: (0,1) ESC

for /F %%a in ('echo prompt $E ^| cmd') do @set "ESC=%%a["
setlocal ENABLEEXTENSIONS
if "%1" == "-?" goto help
if "%1" == "-help" goto help
if "%DEBUG%" == "1" echo on

:: - - - - - 
:: (0,2) test parametri di lancio
set updVers=
set buildStd=
:testp
if "%DEBUG%" == "1" @echo 0=%0 1=%1 2=%2 3=%3
if /i "%1" EQU "-updVers" (
  	set updVers=1
	shift /1
	goto testp
)
if /i "%1" EQU "-buildStd" (
  	set buildStd=1
	shift /1
	goto testp
)
if "%DEBUG%" == "1" @echo updVers=%updVers%
if "%DEBUG%" == "1" @echo buildStd=%buildStd%
if "%DEBUG%" == "1" pause

:: - - - - - 
:: (0,3) BaseDir
@echo %~dpnx0
cd /d %~dp0
cd ..
set BaseDir=%CD%
call :mioecho "Base Dir" %BaseDir%

:: - - - - - 
:: (0,4) MvnCmd il comando batch di Maven
set MvnCmd=
for /F "usebackq tokens=*" %%i in (`where mvn.cmd` ) do set MvnCmd=%%i
if   "%MvnCmd%" == "" goto nomvn
call :mioecho MvnCmd %MvnCmd%
if "%DEBUG%" == "1" pause

:: --------------------------------------------------------
:: (1) rinfresco il deploy degli standard
if "%buildStd%" NEQ "1" goto seUpdV
:: pushd ..\stdcla
call :mioecho "Deploy degli STD" sotto %CD%"
start "Deploy degli Standard" /d ..\stdcla /wait cmd.exe /c "%MvnCmd%" clean install -Dmaven.test.skip=true
if "%DEBUG%" == "1" echo on
:: popd
if "%DEBUG%" == "1" cd
if "%DEBUG%" == "1" pause

:: --------------------------------------------------------
:: (2) Update della versione del progetto originale
:seUpdV
if "%updVers%" NEQ "1" goto build
call :mioecho "Update versione.java" sotto %BaseDir%
call ..\stdcla\bin\updVersione.cmd "%BaseDir%" 1
@echo off
if "%DEBUG%" == "1" echo on
if "%DEBUG%" == "1" pause

:: --------------------------------------------------------
:: (3) Build del progetto in questione
:build
call :mioecho "Build progetto in"  %CD%
rem  @echo Build of  %CD%
if "%DEBUG%" == "1" pause
pwsh -f "%BaseDir%\bin\buildApp.ps1"
call :mioecho "Fine Build" Banca
rem @echo %CD%

:: --------------------------------------------------------
:: (4) Copia del Banca_Inst.zip su Google Drive
:: echo on
setlocal ENABLEEXTENSIONS
setlocal ENABLEDELAYEDEXPANSION
set DSTDRV=%GOODRV%\zips\photon2
if exist Banca_inst.zip (
  if "%GOODRV%" neq "" (
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
:help
@echo Usage : %0 [-updVers] [-buildStd]
@echo dove:
@echo       %ESC%7m-updVer%ESC%0m 	incrementa la versione del prodotto (anche in Pom.xml)
@echo       %ESC%7m-buildStd%ESC%0m 	costruisce gli std prima di creare il build dell'applicazione
goto fine

----------------------------------------------------
:nomvn
@echo %ESC%7;31mnon trovo Maven mvn.cmd%ESC%0m
goto fine

:fine
pause



