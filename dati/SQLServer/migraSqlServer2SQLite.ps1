$ErrorActionPreference = 'Stop'
# Set-StrictMode -Version 3.0
# ---------------------------------------------------------------------
# 1. verifica
# Per vedere se il modulo SqlServer e' installato correttamente esegui:
# 	Get-Module -ListAvailable -Name SqlServer
# Controlla il Path che appare nei risultati.
# Se il percorso contiene 
# 	\Documents\WindowsPowerShell\Modules
# lo hai installato per la versione 5.1.
# 
# Per PowerShell 7, il percorso corretto dovrebbe contenere 
# 	\Documents\PowerShell\Modules 
# oppure 
# 	\Program Files\PowerShell\7\Modules.
# 
# 2. Forza l'installazione specifica per PowerShell 7
# Per tagliare la testa al toro, chiudi e riapri PowerShell 7 come amministratore 
# ed esegui questo comando, che forza l'installazione isolata per l'utente corrente:
# 	Install-Module -Name SqlServer -Scope CurrentUser -Force -AllowClobber
# Nota: Il parametro -AllowClobber è fondamentale perché dice a PowerShell di 
#       sovrascrivere eventuali comandi o alias contrastanti che potrebbero 
#       bloccare l'importazione di Invoke-Sqlcmd.
# ---------------------------------------------------------------------
# 1. Configurazione Connessioni
Import-Module System.Data.Sqlite
Import-Module SqlServer
[System.Reflection.Assembly]::LoadWithPartialName("Microsoft.Data.SqlClient")

# -- SQLServer --
$script:SqlServer = 'localhost' # Nome del server SQL Server (può includere istanza)
$script:SqlDatabase = 'Banca'         # Nome del database SQL Server
$script:SqlConnectionString = "Server=$script:SqlServer;Database=$script:SqlDatabase;Integrated Security=True;TrustServerCertificate=True;"
$script:SqlServerConn = $null
# Carica il provider SQL moderno
# Import-Module Microsoft.Data.SqlClient
# Import-Module System.Data.SqlClient
# -- SQLite --
$script:ProjectBase = 'D:\java\conmod\banca'   # Percorso Progetto
$script:SQLitePath = "$script:ProjectBase\dati\SQLite"   # Percorso dove salvare il file SQLite
$script:SQLiteDB = "$script:SQLitePath\fromSQLServer.db"   # Percorso dove salvare il file SQLite
$script:SQLiteConnectionString = "Data Source=$script:SQLiteDB;Version=3;Pooling=False"
$script:sqliteConn = $null

# 1. Pulizia iniziale preventiva (per sicurezza dal lancio precedente)
[System.Data.SQLite.SQLiteConnection]::ClearAllPools()
[System.GC]::Collect()
[System.GC]::WaitForPendingFinalizers()
$script:Tables = @()
$script:columns = @()
$script:Views = @()

# ---------------------------------------------------------------------
function test-presenza-DBSQLite {
    Write-Host "Verifica presenza del database SQLite $script:SQLiteDB ..." -ForegroundColor Green
    # Aggiungi qui la logica per verificare la presenza del database SQLite
    # Verifico che il DB esista, altrimenti lo creo
    try {
        if ( Test-Path $script:SQLiteDB ) {
            Write-Host "File DB SQLite esistente trovato: $SQLiteDB" -ForegroundColor Yellow
            Write-Host 'Lo rimuovo ' -ForegroundColor Yellow
            Remove-Item -Path $script:SQLiteDB -Force -ErrorAction Stop
        }
        Write-Host "Creazione nuovo file SQLite: $script:SQLiteDB" -ForegroundColor Green
        # Creazione del file vuoto (SQLite lo popolerà automaticamente)
        New-Item -Path $script:SQLiteDB -ItemType File -Force -ErrorAction Stop | Out-Null    
    }
    catch {
        Write-Error "Errore di Creazione DB SQLite: $_" 
        exit 1
    }
}
# ---------------------------------------------------------------------
function test-SQLiteConnection {
    # Ora provo ad aprire la connessione per verificare che tutto sia a posto
    Write-Host "Verifica connessione al database SQLite..." -NoNewline -ForegroundColor Green
    try {
        $script:sqliteConn = New-Object System.Data.SQLite.SQLiteConnection
        $script:sqliteConn.ConnectionString = $script:SQLiteConnectionString
        $script:sqliteConn.Open()
        
        $cmd = $script:sqliteConn.CreateCommand()
        # Eseguo un semplice comando per verificare la connessione
        $cmd.CommandText = "SELECT 1"
        $null = $cmd.ExecuteScalar() 
        Write-Host "Connessione al database SQLite riuscita."  -ForegroundColor Green
    }
    catch {
        Write-Error "Connessione al database SQLite fallita: $_"
    }
}
# ---------------------------------------------------------------------
function test-SQLServerConnection {
    Write-Host "Verifica connessione al server SQL..." -NoNewline -ForegroundColor Green
    try {
        $script:SqlServerConn = New-Object Microsoft.Data.SqlClient.SqlConnection($script:SqlConnectionString)
        $script:SqlServerConn.Open()
        $qry = "SELECT @@Version"
        $cmd = New-Object Microsoft.Data.SqlClient.SqlCommand($qry, $script:SqlServerConn)
        $result = $cmd.ExecuteScalar()
        Write-Host "Connessione al server SQL riuscita. Versione: $result" -ForegroundColor Green
    }
    catch {
        Write-Error "Connessione al server SQL fallita: $_"
        exit 1
    }
}
# ----------------------------------------------------------------------
function read-SQLTables {
    Write-Host 'Recupero elenco tabelle da SQL Server...' -NoNewline -ForegroundColor green
    $qry = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'"
    $script:Tables = Invoke-Sqlcmd -ConnectionString $script:SqlConnectionString -Query $qry
    # try {
    #     $cmd = New-Object Microsoft.Data.SqlClient.SqlCommand($qry, $script:SqlServerConn)
    #     $sqlRdr = $cmd.ExecuteReader()
    #     while ( $sqlRdr.Read() | Out-Null ) {
    #         $tbl = $sqlRdr[0]
    #         $script:Tables += $tbl
    #         Write-Host "Tabella SQL Server: $($tbl)"
    #     }
    # }
    # catch {
    #     Write-Error "Errore durante la lettura delle tabelle SQL Server: $_"
    # }
    Write-Host "Tabelle trovate: $($Tables.Count)" -ForegroundColor green
}
# ----------------------------------------------------------------------
function add-SQLiteTable {
    param (
        [Parameter(Mandatory = $true)]
        $TableName  
    )
    # Creazione delle tabelle SQLite corrispondenti alle tabelle SQL Server e migrazione dei dati
    Write-Host "Elaborazione tabella: $TableName" -ForegroundColor Yellow

    # 4. Lettura Schema Colonne da SQL Server
    $SchemaQuery = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '$TableName'"
    $script:columns = Invoke-Sqlcmd -ConnectionString $script:SqlConnectionString -Query $SchemaQuery

    # Mappatura dei tipi di dato SQL Server -> SQLite
    $ColDefs = foreach ($Col in $script:columns) {
        $SqlType = $Col.DATA_TYPE.ToLower()
        $Type = switch ($SqlType) {
            { $_ -in 'int', 'bigint', 'smallint', 'tinyint', 'bit' } { 'INTEGER' }
            { $_ -in 'decimal', 'numeric', 'float', 'real', 'money' } { 'REAL' }
            { $_ -in 'binary', 'varbinary', 'image' } { 'BLOB' }
            default { 'TEXT' } # varchar, nvarchar, char, datetime, uniqueidentifier, ecc.
        }
        $Null = if ($Col.IS_NULLABLE -eq 'NO') { 'NOT NULL' } else { '' }
        "[$($Col.COLUMN_NAME)] $Type $Null"
    }

    # 5. Creazione Tabella in SQLite con tutti le righe di $colDefs
    try {
        $qry = "CREATE TABLE IF NOT EXISTS [$TableName] ($( $ColDefs -join ', ' ));"
        $cmd = $SqliteConn.CreateCommand() 
        $cmd.CommandText = $qry
        $null = $cmd.ExecuteNonQuery()
        $cmd.Dispose()
    }
    catch {
        Write-Error "Errore durante la creazione della tabella SQLite: $_"
        exit 1
    }
}
# ----------------------------------------------------------------------
function copy-dataSql {
    param (
        [Parameter(Mandatory = $true)]
        $TableName  
    )
    $reader = $null
    $Transaction = $null
    $insertCmd = $null
    # 6. Estrazione Dati da SQL Server usando i moduli .NET per massima velocità
    Try {
        $qry = "SELECT * FROM [$TableName]"
        $SqlCmd = [Microsoft.Data.SqlClient.SqlCommand]::new($script:SqlServerConn) 
        $SqlCmd.CommandText = $qry
        $SqlCmd.Connection = $script:SqlServerConn
        $Reader = $SqlCmd.ExecuteReader()

        # Generazione comando INSERT parametrizzato per SQLite
        $ParamNames = foreach ($Col in $script:columns) { "@$($Col.COLUMN_NAME)" }
        $ColNames = foreach ($Col in $script:columns) { "[$($Col.COLUMN_NAME)]" }
        
        $InsertSql = "INSERT INTO [$TableName] ($( $ColNames -join ', ' )) VALUES ($( $ParamNames -join ', ' ))"
        
        # Avvio Transazione SQLite per aumentare drasticamente le performance di scrittura
        $Transaction = $SqliteConn.BeginTransaction()
        $InsertCmd = $SqliteConn.CreateCommand()
        $InsertCmd.CommandText = $InsertSql
        $InsertCmd.Transaction = $Transaction

        # Creazione dei parametri SQLite riutilizzabili
        foreach ($Col in $script:columns) {
            $null = $InsertCmd.Parameters.Add([System.Data.Sqlite.SqliteParameter]::new("@$($Col.COLUMN_NAME)", [object][System.DBNull]::Value))
        }

        # Lettura ciclica e scrittura bulk
        $Count = 0
        while ($Reader.Read()) {
            for ($i = 0; $i -lt $Reader.FieldCount; $i++) {
                $Value = $Reader.GetValue($i)
                $InsertCmd.Parameters[$i].Value = if ($Value -eq [System.DBNull]::Value) { [System.DBNull]::Value } else { $Value }
            }
            $null = $InsertCmd.ExecuteNonQuery()
            $Count++
        }

        $Transaction.Commit()
        Write-Host "->Da $TableName Copiati con successo $Count record." -ForegroundColor Green
    }
    catch {
        if ($Transaction) { $Transaction.Rollback() }
        Write-Error "Errore durante la migrazione della tabella $TableName : $_"
    }
    finally {
        if ($Reader) { $Reader.Close() }
        if ($InsertCmd) { $InsertCmd.Dispose() }
        if ($Transaction) { $Transaction.Dispose() }
        if ($SqlCmd) { $SqlCmd.Dispose() }
    }
}   
# ----------------------------------------------------------------------
function read-SQLViews {

    $script:views = @()
    # 7.2 Query per estrarre nome e SQL CREATE VIEW da SQL Server
    $qry = 'SELECT TABLE_NAME AS ViewName, VIEW_DEFINITION AS ViewScript 
                FROM INFORMATION_SCHEMA.VIEWS'
    $cmdView = $script:SqlServerConn.CreateCommand()
    $cmdView.CommandText = $qry
    $cmdView.Connection = $script:SqlServerConn
    $readerView = $cmdView.ExecuteReader()

    while ($readerView.Read()) {
        $viewName = $readerView['ViewName']
        $viewScript = $readerView['ViewScript'] -replace '[[\]]', '' -replace 'dbo.', ''
        
        $createViewSql = "CREATE VIEW [$viewName] AS $viewScript"
        $views += [PSCustomObject]@{
            Name   = $viewName
            Script = $viewScript
        }

        $cmd = $SqliteConn.CreateCommand()
        $cmd.CommandText = $createViewSql
        $null = $cmd.ExecuteNonQuery()
    }
    $readerView.Close()

    Write-Host "Trovate $($views.Count) viste." -ForegroundColor Green
}

function Get-SavedSQLiteViews()
{    
    $script:views = @()
    $filesViews = Get-ChildItem -Path $SQLitePath -Filter 'viewSQLite*.sql' -ErrorAction Stop
    foreach ($file in $filesViews)
    {
        $viewContent = Get-Content -Path $file.FullName -ErrorAction Stop -Raw
        $viewName = [System.IO.Path]::GetFileNameWithoutExtension($file.Name) -replace '^viewSQLite_', ''
        $script:views += [PSCustomObject]@{
            Name   = $viewName
            Script = $viewContent
        }
    }
    Write-Host "Trovate $($script:views.Count) viste salvate nel dir SQLite." -ForegroundColor Green
}
function Write-SQLiteViews()
{
    # Scrive le viste di SQLServer in SQLite
    foreach ($view in $script:views)
    {
        try
        {
            Write-Host "Migrazione vista: $($view.Name)..." -NoNewline -ForegroundColor Green
            
            # Elimina la vista se esiste già in SQLite
            $dropCmd = $SqliteConn.CreateCommand()
            $dropCmd.CommandText = "DROP VIEW IF EXISTS [$($view.Name)];"
            $dropCmd.ExecuteNonQuery() | Out-Null

            # Adatta lo script per SQLite se necessario
            if ( $view.Script -like "create view*" ) {
                $sqliteScript = $view.Script
                if ( $sqliteScript -match 'EXIST ' ) {
                    $sqliteScript = $sqliteScript -replace 'EXIST ', 'EXISTS '
                }
                if ( $sqliteScript -notmatch ';$' ) {
                    $sqliteScript += ';'
                }
            } else {
              $sqliteScript = "CREATE VIEW $($view.Name) AS $($view.Script);"
            }
            
            # Esegue la creazione della vista
            $createCmd = $SqliteConn.CreateCommand()
            $createCmd.CommandText = $sqliteScript
            $createCmd.ExecuteNonQuery() | Out-Null

            Write-Host ' [OK]' -ForegroundColor Green
        }
        catch
        {
            Write-Host ' [ERRORE]' -ForegroundColor Red
            Write-Host "  Dettaglio: $_" -ForegroundColor Yellow
        }
    }

    $SqliteConn.Close()
}

# ----------------------------------------------------------------------
# Start della migrazione
Write-Host "Inizio della migrazione..."
test-presenza-DBSQLite
test-SQLiteConnection
test-SQLServerConnection
read-SQLTables

foreach ($table in $Tables) {
    $TableName = $table.TABLE_NAME
    if ( $TableName.ToLower().StartsWith('sys') ) {
        continue
    }
    add-SQLiteTable -Tablename $TableName
    copy-dataSql -TableName $TableName
}
# read-SQLViews
Get-SavedSQLiteViews
Write-SQLiteViews


if ($script:sqliteConn) {
    $script:sqliteConn.Close()
    $script:sqliteConn.Dispose()
    $script:sqliteConn = $null
}
if ($script:SqlServerConn) {
    $script:SqlServerConn.Close()
    $script:SqlServerConn.Dispose()
    $script:SqlServerConn = $null
}
[System.Data.SQLite.SQLiteConnection]::ClearAllPools()
[System.GC]::Collect()
[System.GC]::WaitForPendingFinalizers()
Write-Host "Migrazione completata." -ForegroundColor Green