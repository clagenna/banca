Add-Type -AssemblyName System.Windows.Forms
# Set-PSDebug -Trace 2
Set-Location (Split-Path $PSCommandPath)
# ---------------------------------------
$AppName = 'Banca'
$JdkVers = '25.0.2'
$JavaFXVers = '25.0.2'
# ---------------------------------------
$zipApp = ".\${AppName}_Inst.zip"
$zipJdk = "openjdk-${JdkVers}_windows-x64_bin.zip"
$zipJfx = "openjfx-${JavaFXVers}_windows-x64_bin-sdk.zip"

$javaDir_Good = 'C:\Program Files\java'
$javaDir_Temp = 'C:\Temp\java'
$jdkDir_Good = "$javaDir_Good\jdk-${JdkVers}"
$jfxDir_Good = "$javaDir_Good\javafx-sdk-${JavaFXVers}"
$jdkDir_Temp = "$javaDir_Temp\jdk-${JdkVers}"
$jfxDir_Temp = "$javaDir_Temp\javafx-sdk-${JavaFXVers}"




# test se eseguito come amministratore di sistema
# $currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
# $isAdmin = $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
# $isAdmin2 = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
# Write-Host ('Admin_1={0}, Admin_2={1}' -f $isAdmin, $isAdmin2) -ForegroundColor Yellow
# test verifica se esistono gia' i direttori
$isDirs = (Test-Path -Path $javaDir_Good ) -or (Test-Path -Path $jdkDir_Good ) -or (Test-Path -Path $jfxDir_Good  )
if (! $isDirs )
{
    Write-Host "Manca l'istallazione del JDK e/o JFX ..."
    #    if ( ! $isAdmin)
    #    {
    #        Write-host 'Non sei amministratore!' -ForegroundColor Red
    #        exit
    #    }
}
# sono amministratore, posso partire con l'istallazione
Write-host 'Vabbé ... sono amministratore!' -ForegroundColor green


$FileBrowser = New-Object System.Windows.Forms.FolderBrowserDialog -Property @{ 
    InitialDirectory = [Environment]::GetFolderPath('Desktop');
    Description      = "Scegli il direttorio di base dove vuoi installare $AppName"
}
$dirName = $null
if ( $FileBrowser.showDialog() -eq [System.Windows.Forms.DialogResult]::OK)
{
    $dirName = $FileBrowser.SelectedPath
    Write-Host "Dir selected = $dirName"
}
if ( $null -eq $dirName )
{
    Write-Host 'Non hai selezionato nulla!' -ForegroundColor Red
    exit
}
if ( !(Test-Path -path $zipApp) )
{
    Write-Host 'Non trovo ${zipApp} !' -ForegroundColor Red
    exit
}
# estraggo tutto il contenuto di $zipApp
Expand-Archive -DestinationPath "${dirName}\${AppName}" -force -LiteralPath $zipApp

# scelta del tipo di DB su cui lavorare
Set-Location "${dirName}\${AppName}"
Write-Host 'imposto il DB di default SQLite' -ForegroundColor Yellow
copy-item -path 'BancaNuovo.db' -Destination 'Banca.db' -Force
copy-item -path 'Banca_SQLite.properties' -destination 'Banca.properties' -Force
# rimpiazzo il nome db con quello copiato
$text = Get-Content -Path 'banca.properties'
$sz1 = "${dirName}\${AppName}\Banca.db" -replace '\\', '\\' -replace ':', '\:'
$sz2 = "DB.name=${sz1}"
$text = $text -replace 'DB\.name=.*', $sz2
$sz1 = "${dirName}\${AppName}" -replace '\\', '\\' -replace ':', '\:'
$sz2 = "last.dir=${sz1}"
$text = $text -replace 'last\.dir=.*', $sz2
$text | Out-File 'banca.properties' -Force
Get-ChildItem

# -----------------------------------------
# mi occupo del JDK
$addPath = $false
$addJFX = $false
$szCmd = ''
if (! (Test-Path -Path $javaDir_Good ))
{
    Set-Location (Split-Path $PSCommandPath)
    Write-host ('Creo la base del JDK:' + $javaDir_Temp) -ForegroundColor green
    New-Item -Path $javaDir_Temp -ItemType Directory -Force | out-null
}
$jdkPath = $null
if (! (Test-Path -Path $jdkDir_Good ))
{
    if ( Test-Path -Path $jdkDir_Temp)
    {
        Remove-Item -Path $jdkDir_Temp -Force -Recurse
    }
    Write-host ('Creo il JDK dir:' + $jdkDir_Temp) -ForegroundColor green
    Set-Location (Split-Path $PSCommandPath)
    Expand-Archive -DestinationPath $javaDir_Temp -force -LiteralPath $zipJdk
    # [Environment]::SetEnvironmentVariable('JAVA_HOME', $jdkDir_Good, 'Machine')
    $szCmd = "setx JAVA_HOME `"${jdkDir_Good}`" /M"
    $addPath = $true
    $pth = Get-ChildItem -Path $javaDir_Temp -Filter 'java.exe' -Recurse
    $jdkPath = $pth.Directory.FullName
}

# -----------------------------------------
# mi occupo del JavaFX
if (! (Test-Path -Path $jfxDir_Good) )
{
    Write-host ('Creo dir:' + $jfxDir_Temp) -ForegroundColor green
    $addJFX = $true
    if ( Test-Path -Path $jfxDir_Temp)
    {
        Remove-Item -Path $jfxDir_Temp -Force -Recurse
    }
    Set-Location (Split-Path $PSCommandPath)
    Expand-Archive -DestinationPath $javaDir_Temp -force -LiteralPath $zipJfx
    #[Environment]::SetEnvironmentVariable('JAVAFX_HOME', $jfxDir_Good, 'Machine')
    $szCmd = ($szCmd, "setx JAVAFX_HOME `"${jfxDir_Good}`" /M") -join "`n"
}

if ( $addPath -and $null -ne $jdkPath)
{
    $PathVar = [Environment]::GetEnvironmentVariable('PATH', 'Machine')
    if (! $PathVar.contains('jdk-21'))
    {
        Write-host ('Aggiungo a PATH il javadir') -ForegroundColor green
        $PathVar = $PathVar + [IO.Path]::PathSeparator + ( '{0}\bin' -f $jdkDir_Good )
        $PathVar = $PathVar.Replace(';;', ';')
        # [Environment]::SetEnvironmentVariable( 'Path', $PathVar, 'Machine' )
        $szCmd = ($szCmd, "setx PATH `"%PATH%;%JAVA_HOME%\bin`" /M") -join "`n"
        foreach ( $sz in $PathVar.split(';'))
        {
            Write-Host ('PATH={0}' -f $sz)
        }
    }
    else
    {
        Write-host ('Gia presente in PATH il javadir') -ForegroundColor yellow
    }
}

if ( $addPath -or $addJFX )
{
    $msg = @"
*****   LEGGI BENE  ******
--------------------------
Ho creato i direttori temporanei per il JDK in:
  ${jdkDir_Temp}
  ${jfxDir_Temp}

ATTENZIONE!!!
-------------
Questi andranno spostati *A MANO* nel direttorio:
  ${javaDir_Good}    !!!
Di modo che la struttura sia:
  ${jdkDir_Good}
  ${jfxDir_Good}
Inoltre dovrai dare *A MANO* i seguenti comandi da amministratore:

${szCmd}  

"@
    Write-host $msg -ForegroundColor yellow
    $msg | Out-File -FilePath "${dirName}\${AppName}\ReadMe.txt"
}