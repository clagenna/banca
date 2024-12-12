Set-Location (Split-Path $PSCommandPath)
Set-Location ".."
Get-Location

$AppName = "Banca"
$zipFile = "${AppName}_Inst.zip"

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
$arr = @()
$arr += "target\${AppName}.jar"
$arr += "${AppName}.properties"
$arr += ".\bin\${AppName}.cmd"
$arr += ".\bin\installApp.cmd"
$arr += ".\bin\installApp.ps1"
$arr += ".\dati\Banca_SQLite.properties"
$arr += ".\dati\Banca_SQLserver.properties"
$arr += ".\dati\SQLite\BancaNuovo.db" 
# Get-ChildItem -path ".\bin\${AppName}.cmd", "${AppName}.properties", "target\${AppName}.jar", ".\bin\installApp.cmd", ".\bin\installApp.ps1", ".\dati\Estrattoconto_Contanti.xlsx", ".\dati\SQLite\BancaNuovo.db"   |
Get-ChildItem -path $arr |
    Compress-Archive  -CompressionLevel Fastest -DestinationPath $zipFile

