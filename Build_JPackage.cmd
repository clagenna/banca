goto init
-----------------------------------------------------------------------------

jpackage --name Banca --runtime-image target --input target --main-jar Banca.jar --main-class sm.clagenna.banca.javafx.LoadBancaMainApp --type exe


It is a common headache when working with jpackage: the tool officially documentation still 
often references WiX 3.0, but that version is increasingly difficult to source or get running 
on modern systems.

The good news is that WiX 4.0 and 5.0 are the current standards. While jpackage in JDK 25 
is backward compatible, you need to ensure your environment variables are mapped correctly 
because the file structure changed between WiX 3 and 5.

Here is a complete command-line walkthrough to build your .exe using WiX 5.x.

1. Environment Setup (The "No WiX 3" Fix)

	Since WiX 3 is legacy, install the modern version via .NET tool or download the 
	binaries. jpackage looks for the WiX toolset in your PATH.
        Install WiX 5: Open PowerShell/CMD and run:
		dotnet tool install --global wix

2. Verify: Run wix --version.

The Secret Sauce: jpackage specifically looks for candle.exe and light.exe (from WiX 3). 
In WiX 4/5, these are bundled into a single wix.exe. To fix this without old tools, 
ensure you have the WiX Toolset path added to your System Environment Variables.

3. But wix.exe error WIX0144 The extension 'WixToolset.Util.wixext' could not be found. Checked paths: WixToolset.Util.wixext
	wix extension add -g WixToolst.Util.wixext/6.0.2
  	wix extension add -g WixToolst.UI.wixext/6.0.2
        wix extension add    WixToolset.Util.wixext/6.0.2

Update the jpackage Command
Use the --resource-dir flag to point to the folder containing your main.wxs. 
This forces jpackage to use your modern WiX 6-compatible definitions.

PowerShell
jpackage `
  --type exe `
  --dest . `
  --name "BancaExe" `
  --resource-dir d:\temp\bancaExe `
  --input target `
  --main-jar Banca.jar `
  --main-class sm.clagenna.banca.javafx.LoadBancaMainApp `
  --module-path "%JAVAFX_HOME%\lib" `
  --add-modules javafx.controls,javafx.fxml `
  --win-dir-chooser `
  --verbose



-----------------------------------------------------------------------------
:init
@echo on
set TEMP_DEST=d:\temp\bancaExe
if "%1" == "no" goto build
set MAVEN_OPTS="--enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow"
call mvn clean package
:build
if exist "%TEMP_DEST%" rmdir /s /q "%TEMP_DEST%"
rem   --resource-dir "%TEMP_DEST%" 
jpackage ^
  --type exe ^
  --dest . ^
  --name "BancaExe" ^
  --input target ^
  --main-jar Banca.jar ^
  --main-class sm.clagenna.banca.javafx.LoadBancaMainApp ^
  --module-path "%JAVAFX_HOME%\lib;mods" ^
  --add-modules javafx.controls,javafx.fxml ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-menu ^
  --vendor "Claudio Gennari" ^
  --description "Gestione Finanziaria dei movimenti Bancari" ^
  --temp %TEMP_DEST% ^
  --verbose



