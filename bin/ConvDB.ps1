Set-StrictMode -Version 3.0

# $sqliteDLL="D:\Program Files\SQLite NetFx4.5\System.Data.SQLite.dll"
# [Reflection.Assembly]::LoadFile($sqliteDLL)
# @see https://github.com/RamblingCookieMonster/PSSQLite
import-module PSSQLite


$DBPath = "F:\winapp\Banca\Banca.db"
# $sConnString = "Data Source=$DBPath"

# $SQLiteConnection = New-Object System.Data.SQLite.SQLiteConnection 
# $SQLiteConnection.ConnectionString = $sConnString
# $SQLiteConnection.Open()

$sQRY = "SELECT * FROM ListaMovimentiUNION"
Invoke-SqliteQuery -Query $sQRY -DataSource $DBPath | 
    ForEach-Object {
        $riga = $_
        Write-Output $riga
    }

# $command = $SQLiteConnection.createCommand()
# $command.Commandtext = $sQRY
# $command.CommandType = [System.Data.CommandType]::Text
# $reader = $command.ExecuteReader()
# $reader.getValues()
while ( $reader.hasRows) {
    if ( ! $reader.Read() ) {
        break
    }
    $sTipo = $reader["tipo"]
    $dtMov = $reader["dtmov"]
    $dtVal = $reader["dtval"]
    $nDare = $reader["dare"]
    $nAvere = $reader["avere"]
    $sCardid = $reader["cardid"]
    $sDescr = $reader["descr"]
    $sAbicaus = $reader["abicaus"]
    $nCosto = $reader["costo"]
    $sCodstat = $reader["codstat"]
    $sOut="Tipo $sTipo `t dtMov $dtMov `t dtVal $dtVal `t Dare $nDare `t Avere $nAvere `t Cardid $sCardid `t Descr $sDescr `t Abicaus $sAbicaus `tCosto $nCosto `t Codstat $sCodstat `t `t"
    Write-Output $sOut
}
Write-Output "----------------------- fine lettura"

