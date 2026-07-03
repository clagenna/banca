param(  [Parameter()][string] $prmDir )
Set-StrictMode -Version 3.0

# base dei progetti maven
$script:rootDir = $null
# lista dei progetti maven
$script:liPrjs = $null
# quantita di progetti trovati
$script:countPrj = $null
# elenco di files posteriore alla data dell'ultimo archive
$script:liFiles = $null
# data del'ultimo archive 
$script:dtRif = $null
# quantita di files posteriore alla data dell'ultimo archive
$script:countFiles = $null
# la data piu piccola in ordine temporale
$script:dtLow = [datetime]::parseexact('1970-01-01', 'yyyy-MM-dd', $null)
# Directory da escludere (nomi di cartella, case-insensitive)
$script:excludeDirs = @('log', 'target', 'zip', '.git', '.settings', '.vscode', '.wix')
# Estensioni da escludere
$script:excludeExt = @('.db', '.class', '.log', '.tmp', '.bak', '.zip')
# direttorio di progetto contenente gli zip di backup
$script:zipDir = 'zip'
function Test-RootDir
{
    [CmdletBinding()]
    param ( [string]$fromDir)
    <#
    .DESCRIPTION
    Verifica la presenza del root directory da dove partire la scansione dei progetti
    #>
    # Write-Host "from dir = $fromDir" -ForegroundColor Cyan
    if ( [String]::IsNullOrEmpty($fromDir))
    {
        Set-Location (Split-Path $PSCommandPath)
        Set-Location '..'
        $script:RootDir = Get-Location
    }
    else
    {
        $script:RootDir = $fromDir
    }
    if ( -not (Test-Path $script:RootDir))
    {
        Write-Host ( 'Il path {0} non esiste !' -f $script:RootDir) -ForegroundColor Red
        exit 1957 
    }
}

function Find-projects
{
    $local:liPoms = $null
    $liPoms = Get-ChildItem -Path $script:rootDir -Depth 1 -Filter 'pom.xml' -File | Where-Object {
        $pom = $_
        $pom.PSParentPath
    }
    if ( $null -eq $liPoms )
    {
        $script:countPrj = 0
    }
    elseif ($liPoms.getType() -eq [System.IO.FileInfo])
    {
        $script:countPrj = 1    
    }
    else
    {
        $script:countPrj = $liPoms.count
    }
    if ( $null -eq $script:countPrj -or 0 -eq $script:countPrj)
    {
        Write-Host "Nessun progetto con POM.XML trovato in $script:rootDir" -ForegroundColor red
        exit 1957
    }
    $script:liPrjs = @()
    $liPoms | Where-Object {
        $dirPar = $_.Directory
        $Prj = $dirPar.name
        $script:liPrjs += $Prj
    }
}
function Test-Archive
{
    [CmdletBinding()]
    param ( [string]$prjDir)
    $script:countFiles = $null
    Write-Host "`t$prjDir" -ForegroundColor Magenta
    $locZipDir = '{0}\{1}' -f $prjDir, $Script:zipDir
    $script:dtRif = $Script:dtLow

    if ( Test-Path $locZipDir)
    {
        # alla ricerca del file .ZIP piu nuovo
        Get-ChildItem -Path "$locZipDir" -Filter '*_20*.zip' -File | Where-Object {
            $zipFil = $_
            # Write-Host "Zip file = $zipFil" -ForegroundColor Green    
            $locDt = $zipFil.LastWriteTime
            $script:DtRif = $script:DtRif.CompareTo($locDt) -lt 0 ? $locDt : $script:DtRif
        } 
    }
    
    # === SCANSIONE ===
    $script:liFiles = $null
    $script:liFiles = Get-ChildItem -Path $prjDir -Recurse -File | Where-Object {
        $file = $_
        # Esclude se il percorso contiene una delle directory escluse
        $inExcludedDir = $script:excludeDirs | Where-Object {
            $file.FullName -match "\\$([Regex]::Escape($_))\\"
        }
        # Esclude se l'estensione è tra quelle escluse
        $isExcludedExt = $script:excludeExt -contains $file.Extension.ToLower()

        # Tiene solo i file più recenti del riferimento, non esclusi
        ($file.LastWriteTime -gt $script:dtRif) -and (-not $inExcludedDir) -and (-not $isExcludedExt)
    }
    # $isPresent = $null -ne $liFiles -or $liFiles.getType() -eq [System.IO.FileInfo] -or $liFiles.Count -gt 0

    if ( $null -eq $script:liFiles )
    {
        $script:countFiles = 0
    }
    elseif ($script:liFiles.getType() -eq [System.IO.FileInfo])
    {
        $script:countFiles = 1    
    }
    else
    {
        $script:countFiles = $liFiles.count
    }
}

function Update-Archive
{
    [CmdletBinding()]
    param ( [string]$prjDir)
    Set-Location $prjDir
    $sz = $script:dtRif.ToString('yyyy-MM-dd HH:mm:ss' )    
    if (  $null -eq $script:countFiles -or 0 -eq $script:countFiles )
    {
        $sz = 'Dt Rif = {0}' -f $script:DtRif.ToString('yyyy-MM-dd HH:mm:ss' )
        Write-Host "Nessun file in $prjDir e' posteriore alla data $sz." -ForegroundColor Blue
        return
    }
    Write-Host "Trovati $countFiles files più recenti della data di riferimento $sz." -ForegroundColor Green
    # $liFiles | Where-Object {
    #    $file = $_
    #    $sz = $file.LastWriteTime.ToString('yyyy-MM-dd HH:mm:ss' )    
    #    Write-Host ("Dt: {0}`tPath:{1}" -f $sz, $file.FullName ) -ForegroundColor DarkYellow
    # }
    Write-Host ("Creo l'archive per $prjDir con savzip.cmd" ) -ForegroundColor Green
    Start-Process -Wait -FilePath 'savzip.cmd'
}

# --------------------------------------------------------------------------------------------
Test-RootDir $prmDir
Set-Location $script:RootDir
Write-Host "root dir = $RootDir" -ForegroundColor Green
Find-projects
Write-Host "Trovati $script:countPrj progetti in $script:RootDir" -ForegroundColor yellow
if ( $countPrj -gt 0 )
{
    $script:liPrjs | ForEach-Object {
        $szPrj = '{0}\{1}' -f $Script:rootDir, $_
        Test-Archive $szPrj
        Update-Archive $szPrj
    }
}

