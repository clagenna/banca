Set-Location (Split-Path $PSCommandPath)
Set-Location ".."
Get-Location

$AppName = "Banca"
$zipFile = "${AppName}.zip"

$Mvn = ${Env:\MAVEN_HOME}
if ( $null -eq $Mvn) {
  $Mvn = ${Env:\MVN_HOME}
}
if ( $null -eq $Mvn) {
  Write-host  "Manca la Var Ambiente MAVEN_HOME/MVN_HOME!" -ForegroundColor Red
  exit 1957
}
$mvnCmd = "{0}\bin\mvn.cmd" -f ${Mvn}

if ( Test-Path $zipFile ) {
  Remove-Item -Path $zipFile
}

Start-Process -Wait -FilePath $mvnCmd -ArgumentList 'clean','package'

Get-ChildItem -path ".\bin\${AppName}.cmd", "${AppName}.properties", "target\${AppName}.jar", ".\bin\installApp.ps1", ".\dati\SQLite\BancaNuovo.db"   |
    Compress-Archive  -CompressionLevel Fastest -DestinationPath $zipFile

