param(  [Parameter()][string] $fromDir )
Set-StrictMode -Version 3.0

if ( [String]::IsNullOrEmpty($fromDir))
{
    Set-Location (Split-Path $PSCommandPath)
    Set-Location '..'
    $RootDir = Get-Location
}
else
{
    $RootDir = $fromDir
}
if ( -not (Test-Path $RootDir))
{
    Write-Host "Il path $RootDir non esiste !" -ForegroundColor Red
    return 
}
Write-Host "root dir = $RootDir" -ForegroundColor Green
$DtRif = [datetime]::parseexact('1970-01-01', 'yyyy-MM-dd', $null)
# Directory da escludere (nomi di cartella, case-insensitive)
$excludeDirs = @('log', 'target', 'zip', '.git', '.settings', '.vscode', '.wix')
# Estensioni da escludere
$excludeExt = @('.db', '.class', '.log', '.tmp', '.bak', '.zip')
$zipDir = 'zip'
# alla ricerca del file .ZIP piu nuovo
Get-ChildItem -Path "$RootDir\$zipDir" -Filter '*_20*.zip' -File | Where-Object {
    $zipFil = $_
    # Write-Host "Zip file = $zipFil" -ForegroundColor Green    
    $locDt = $zipFil.LastWriteTime
    $DtRif = $DtRif.CompareTo($locDt) -lt 0 ? $locDt : $DtRif
} 
$szRif = $DtRif.ToString('yyyy-MM-dd HH:mm:ss')
$szNow = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss')
Write-Host "Dt Rif = $szRif`t(now:$szNow)" -ForegroundColor Green    

# === SCANSIONE ===
$liFiles = $null
$liFiles = Get-ChildItem -Path . -Recurse -File | Where-Object {
    $file = $_
    # Esclude se il percorso contiene una delle directory escluse
    $inExcludedDir = $excludeDirs | Where-Object {
        $file.FullName -match "\\$([Regex]::Escape($_))\\"
    }

    # Esclude se l'estensione è tra quelle escluse
    $isExcludedExt = $excludeExt -contains $file.Extension.ToLower()

    # Tiene solo i file più recenti del riferimento, non esclusi
    ($file.LastWriteTime -gt $dtRif) -and (-not $inExcludedDir) -and (-not $isExcludedExt)
}
$sz = 'Dt Rif = {0}' -f $DtRif.ToString('yyyy-MM-dd HH:mm:ss' )
# $isPresent = $null -ne $liFiles -or $liFiles.getType() -eq [System.IO.FileInfo] -or $liFiles.Count -gt 0
$countFiles = $null
if ( $null -eq $liFiles )
{
    $countFiles = 0
}
elseif ($liFiles.getType() -eq [System.IO.FileInfo])
{
    $countFiles = 1    
}
else
{
    $countFiles = $liFiles.count
}


if (  $null -eq $countFiles -or 0 -eq $countFiles )
{
    Write-Host "Nessun file posteriore alla data $sz." -ForegroundColor Yellow
    return
}
Write-Host "Trovati $countFiles files più recenti del riferimento." -ForegroundColor Green
$liFiles | Where-Object {
    $file = $_
    $sz = $file.LastWriteTime.ToString('yyyy-MM-dd HH:mm:ss' )    
    Write-Host ("Dt: {0}`tPath:{1}" -f $sz, $file.FullName ) -ForegroundColor DarkYellow
}
Write-Host ("Creo l'archive con savzip.cmd" ) -ForegroundColor Green
Start-Process -Wait -FilePath 'savzip.cmd'
# === COMPRESSIONE ===
# if ($liFiles.Count -gt 0)
# {
#     Compress-Archive -Path $liFiles.FullName -DestinationPath $zipOutput -Force
#     Write-Host "Archivio creato: $zipOutput"
# } else {
#     Write-Host 'Nessun file da comprimere.'
# }