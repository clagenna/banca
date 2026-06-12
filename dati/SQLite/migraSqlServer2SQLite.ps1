$ErrorActionPreference = 'Stop'
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
$SqlServer = 'localhost' # Nome del server SQL Server (può includere istanza)
$SqlDatabase = 'Banca'         # Nome del database SQL Server

$SqlConnectionString = "Server=$SqlServer;Database=$SqlDatabase;Integrated Security=True;TrustServerCertificate=True;"
# Carica il provider SQL moderno
# Import-Module Microsoft.Data.SqlClient
# Import-Module System.Data.SqlClient
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

# 2. Caricamento Assembly .NET per SQLite (Incluso in PowerShell 7)
# ---------------------------------------------------------------------
# Add-Type -AssemblyName 'System.Data.Sqlite'
# 
# se non funziona Sotto uno dei dir di:
#       $Env:PSModulePath -split ';'
# creare un dir System.Data.SQLite, esempio:
#       F:\OneDrive\Documents\PowerShell\Modules\System.Data.SQLite
# deve essere presente le DLL 
#       SQLite.interop.dll
#       System.Data.SQLite.dll
# ---------------------------------------------------------------------
Import-Module System.Data.Sqlite
$SQLitePath = 'D:\java\conmod\banca\dati\SQLite\fromSQLServer.db'   # Percorso dove salvare il file SQLite
$SQLiteConnectionString = "Data Source=$SQLitePath;Version=3"
$TestSQL = 'select sqlite_version();'
# Connessione a SQLite
$SqliteConn = $null
# Verifico che il DB esista, altrimenti lo creo
try
{
    if ( Test-Path $SQLitePath )
    {
        Write-Host "File SQLite esistente trovato: $SQLitePath" -ForegroundColor Yellow
        Write-Host 'Lo rimuovo ' -ForegroundColor Yellow
        Remove-Item -Path $SQLitePath -Force -ErrorAction Stop
    }
    Write-Host "Creazione nuovo file SQLite: $SQLitePath" -ForegroundColor Cyan
    # Creazione del file vuoto (SQLite lo popolerà automaticamente)
    New-Item -Path $SQLitePath -ItemType File -Force -ErrorAction Stop | Out-Null    
}
catch
{
    Write-Error "Errore di Creazione DB SQLite: $_"
    exit 1
}
# Ora provo ad aprire la connessione per verificare che tutto sia a posto
try
{
    $SqliteConn = New-Object System.Data.Sqlite.SqliteConnection($SqliteConnectionString)
    $SqliteConn.Open()
}
catch
{   
    Write-Error "Errore di connessione a SQLite: $_"
    exit 1
}

try
{
    $Cmd = $SqliteConn.CreateCommand()
    $Cmd.CommandText = $TestSQL
    Write-Host 'Connessione a SQLite riuscita!' -ForegroundColor Green
    $SqliteConn.Close()
}
catch
{
    Write-Error "Errore di connessione a SQLite: $_"
    $SqliteConn.Close()
    exit 1
}

# ---------------------------------------------------------------------
# 3. Recupero lista delle tabelle da SQL Server
Write-Host 'Recupero elenco tabelle da SQL Server...' -ForegroundColor Cyan
$TablesQuery = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'"
$Tables = Invoke-Sqlcmd -ConnectionString $SqlConnectionString -Query $TablesQuery

# Connessione a SQLite
$SqliteConn = [System.Data.Sqlite.SqliteConnection]::new($SqliteConnectionString)
$SqliteConn.Open()

foreach ($Row in $Tables)
{
    $TableName = $Row.TABLE_NAME
    if ( $TableName.ToLower().StartsWith('sys') )
    {
        continue
    }
    Write-Host "Elaborazione tabella: $TableName" -ForegroundColor Yellow

    # 4. Lettura Schema Colonne da SQL Server
    $SchemaQuery = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '$TableName'"
    $Columns = Invoke-Sqlcmd -ConnectionString $SqlConnectionString -Query $SchemaQuery

    # Mappatura dei tipi di dato SQL Server -> SQLite
    $ColDefs = foreach ($Col in $Columns)
    {
        $SqlType = $Col.DATA_TYPE.ToLower()
        $Type = switch ($SqlType)
        {
            { $_ -in 'int', 'bigint', 'smallint', 'tinyint', 'bit' } { 'INTEGER' }
            { $_ -in 'decimal', 'numeric', 'float', 'real', 'money' } { 'REAL' }
            { $_ -in 'binary', 'varbinary', 'image' } { 'BLOB' }
            default { 'TEXT' } # varchar, nvarchar, char, datetime, uniqueidentifier, ecc.
        }
        $Null = if ($Col.IS_NULLABLE -eq 'NO') { 'NOT NULL' } else { '' }
        "[$($Col.COLUMN_NAME)] $Type $Null"
    }

    # 5. Creazione Tabella in SQLite
    $CreateTableSql = "CREATE TABLE IF NOT EXISTS [$TableName] ($( $ColDefs -join ', ' ));"
    $Cmd = $SqliteConn.CreateCommand()
    $Cmd.CommandText = $CreateTableSql
    $null = $Cmd.ExecuteNonQuery()

    # 6. Estrazione Dati da SQL Server usando i moduli .NET per massima velocità
    $SqlConn = [System.Data.SqlClient.SqlConnection]::new($SqlConnectionString)
    $SqlCmd = [System.Data.SqlClient.SqlCommand]::new("SELECT * FROM [$TableName]", $SqlConn)
    
    try
    {
        $SqlConn.Open()
        $Reader = $SqlCmd.ExecuteReader()

        # Generazione comando INSERT parametrizzato per SQLite
        $ParamNames = foreach ($Col in $Columns) { "@$($Col.COLUMN_NAME)" }
        $ColNames = foreach ($Col in $Columns) { "[$($Col.COLUMN_NAME)]" }
        
        $InsertSql = "INSERT INTO [$TableName] ($( $ColNames -join ', ' )) VALUES ($( $ParamNames -join ', ' ))"
        
        # Avvio Transazione SQLite per aumentare drasticamente le performance di scrittura
        $Transaction = $SqliteConn.BeginTransaction()
        $InsertCmd = $SqliteConn.CreateCommand()
        $InsertCmd.CommandText = $InsertSql
        $InsertCmd.Transaction = $Transaction

        # Creazione dei parametri SQLite riutilizzabili
        foreach ($Col in $Columns)
        {
            $null = $InsertCmd.Parameters.Add([System.Data.Sqlite.SqliteParameter]::new("@$($Col.COLUMN_NAME)", [object][System.DBNull]::Value))
        }

        # Lettura ciclica e scrittura bulk
        $Count = 0
        while ($Reader.Read())
        {
            for ($i = 0; $i -lt $Reader.FieldCount; $i++)
            {
                $Value = $Reader.GetValue($i)
                $InsertCmd.Parameters[$i].Value = if ($Value -eq [System.DBNull]::Value) { [System.DBNull]::Value } else { $Value }
            }
            $null = $InsertCmd.ExecuteNonQuery()
            $Count++
        }

        $Transaction.Commit()
        Write-Host "->Da $TableName Copiati con successo $Count record." -ForegroundColor Green
    }
    catch
    {
        if ($Transaction) { $Transaction.Rollback() }
        Write-Error "Errore durante la migrazione della tabella $TableName : $_"
    }
    finally
    {
        if ($Reader) { $Reader.Close() }
        $SqlConn.Close()
    }
}

$SqliteConn.Close()
Write-Host 'Migrazione completata con successo!' -ForegroundColor Green
