$propFile = 'banca.properties'
$loc = Get-Location
$dbName = 'Banca.db'
$absPath = "${loc}\${dbName}"
Write-Host "Path DB : ${absPath}"
$text = Get-Content -Path $propFile
$text = $text -replace 'DB\.name=.*', "DB.name=${absPath}" 
Write-Host $text
