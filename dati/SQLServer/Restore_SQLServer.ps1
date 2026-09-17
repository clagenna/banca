# Requisiti: Install-Module -Name SqlServer -Scope CurrentUser

param(
    [string]$SqlInstance = 'localhost',
    [string]$DatabaseName = 'Banca',
    [string]$BackupFile = 'd:/java/conmod/banca2/dati/SQLServer/Banca_2026-09-06.bak',
    [string]$DataPath = 'F:\SQL2022\MSSQL16.MSSQLSERVER\MSSQL\DATA',
    [string]$LogPath = 'F:\SQL2022\MSSQL16.MSSQLSERVER\MSSQL\DATA'
)

Import-Module SqlServer
$TestSQL = 'SELECT @@VERSION AS Version'
try
{
    Invoke-Sqlcmd -ConnectionString $SqlConnectionString -Query $TestSQL | Out-Null
    Write-Host 'Connessione a SQL Server riuscita!' -ForegroundColor Green
}
catch
{
    Write-Error "Errore di connessione a SQL Server: $_"
    exit 1
}
try
{
    # 1. Leggo l'header del backup per ottenere i nomi logici dei file
    # $fileList = Read-SqlTableData -ServerInstance $SqlInstance -Query `
    #    "RESTORE FILELISTONLY FROM DISK = N'$BackupFile'"
    # In alternativa (più affidabile per RESTORE FILELISTONLY):
    $fileListQuery = "RESTORE FILELISTONLY FROM DISK = N'$BackupFile'"
    $SqlConnectionString = "Server=$SqlInstance;Database=Master;Integrated Security=True;TrustServerCertificate=True;"
    $files = Invoke-Sqlcmd -ConnectionString $SqlConnectionString -Query $fileListQuery

    $moveClauses = @()
    foreach ($f in $files)
    {
        $ext = if ($f.Type -eq 'L') { '.ldf' } else { '.mdf' }
        $newPath = Join-Path $LogPath ("$DatabaseName" + '_' + $f.LogicalName + $ext)
        $moveClauses += "MOVE N'$($f.LogicalName)' TO N'$newPath'"
    }
    $moveSql = $moveClauses -join ", `n"
    write-host 'Move clauses for RESTORE:' -ForegroundColor Green
    write-host $moveSql -ForegroundColor Green

    # 2. Metto il DB offline / in modalità single user se esiste già (opzionale)
    $checkDbSql = "SELECT database_id FROM sys.databases WHERE name = N'$DatabaseName'"
    $dbExists = Invoke-Sqlcmd -ConnectionString $SqlConnectionString -Query $checkDbSql

    if ($dbExists)
    {
        Write-Host "Il database $DatabaseName esiste già, lo metto in SINGLE_USER per il ripristino..." -ForegroundColor Yellow
        Invoke-Sqlcmd -ConnectionString $SqlConnectionString -Query `
            "ALTER DATABASE [$DatabaseName] SET SINGLE_USER WITH ROLLBACK IMMEDIATE" `
            -QueryTimeout 0 -ErrorAction Stop
    }

    # 3. Eseguo il RESTORE
    $restoreSql = @"
RESTORE DATABASE [$DatabaseName]
FROM DISK = N'$BackupFile'
WITH REPLACE, RECOVERY,
$moveSql,
STATS = 10
"@

    Write-Host "Avvio ripristino del database $DatabaseName..." -ForegroundColor Yellow
    Invoke-Sqlcmd -ConnectionString $SqlConnectionString -Query $restoreSql -QueryTimeout 0 -ErrorAction Stop
    Write-Host 'Ripristino Files eseguito !' -ForegroundColor Green

    # 4. Rimetto il DB in MULTI_USER
    Invoke-Sqlcmd -ConnectionString $SqlConnectionString -Query `
        "ALTER DATABASE [$DatabaseName] SET MULTI_USER" -ErrorAction Stop
    Write-Host "Database $DatabaseName rimesso in MULTI_USER." -ForegroundColor Green
    Write-Host "`nRipristino $DatabaseName completato con successo." -ForegroundColor Green
}
catch
{
    Write-Error "Errore durante il ripristino: $_"
}