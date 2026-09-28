# per verificare la versione su GitHUB e se e' disponibile, si puo dare il comando:
#     iex "& { $(irm https://aka.ms/install-powershell.ps1) } -UseMSI"
# --------------------------------------------------------------------------------
Set-Location (Split-Path $PSCommandPath)
Set-Location '..'
Get-Location
# ---------------------------------------
$AppName = 'Banca'
$JdkVers = '25.0.2'
$JavaFXVers = '25.0.2'
# ---------------------------------------
$javaDir = 'C:\Program Files\Java'
$zipJdk = "${javaDir}\openjdk-${JdkVers}_windows-x64_bin.zip"
$zipJfx = "${javaDir}\openjfx-${JavaFXVers}_windows-x64_bin-sdk.zip"
# $prjDir = (get-item (get-location).path).fullName
$zipFile = "${AppName}_Inst.zip"
$appFile = "${AppName}_App.zip"

$Mvn = ${Env:\MAVEN_HOME}
if ( $null -eq $Mvn)
{
  $Mvn = ${Env:\MVN_HOME}
}
if ( $null -eq $Mvn)
{
  Write-host 'Manca la Var Ambiente MAVEN_HOME/MVN_HOME!' -ForegroundColor Red
  exit 1957
}
$mvnCmd = '{0}\bin\mvn.cmd' -f ${Mvn}

if ( Test-Path $zipFile )
{
  Remove-Item -Path $zipFile
}

Start-Process -Wait -FilePath $mvnCmd -ArgumentList 'clean', 'package', '-Dmaven.test.skip=true'
$arr = @()
$arr += "target\${AppName}.jar"
$arr += "${AppName}.properties"
$arr += ".\bin\${AppName}.cmd"
# $arr += '.\bin\installApp.cmd'
# $arr += ".\CodStat.properties"
# $arr += '.\bin\installApp.ps1'
$arr += '.\dati\SQLite\Banca_SQLite.properties'
$arr += '.\dati\SQLServer\Banca_SQLserver.properties'
$arr += '.\dati\SQLite\BancaNuovo.db' 
$arr += '.\dati\Manuale Utente.docx' 
$arr += '.\dati\Manuale Utente.pdf' 

# Get-ChildItem -path ".\bin\${AppName}.cmd", "${AppName}.properties", "target\${AppName}.jar", ".\bin\installApp.cmd", ".\bin\installApp.ps1", ".\dati\Estrattoconto_Contanti.xlsx", ".\dati\SQLite\BancaNuovo.db"   |
Get-ChildItem -path $arr |
  Compress-Archive -CompressionLevel Fastest -DestinationPath $zipFile
$arr = @()
$arr += "${zipFile}"
$arr += '.\bin\installApp.cmd'
$arr += '.\bin\installApp.ps1'
$arr += "${zipJdk}"
$arr += "${zipJfx}"
if ( Test-Path $appFile )
{
  Remove-Item -Path $appFile
}
Get-ChildItem -path $arr | Compress-Archive -CompressionLevel Fastest -DestinationPath $appFile
Remove-Item -Path $zipFile
write-host -ForegroundColor Green "Scritto archive d'istallazione ${appFile}"
