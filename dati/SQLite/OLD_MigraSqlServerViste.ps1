<#
.SYNOPSIS
    Migra le definizioni delle Viste da SQL Server o MySQL verso SQLite.
#>

# ==========================================
# 1. CONFIGURAZIONE
# ==========================================
$DbType = 'SQLServer' # Cambia in "MySQL" se la sorgente è MySQL

# Connection string di Origine
$SourceConnString = 'Server=localhost;Database=MioDB;Integrated Security=True;TrustServerCertificate=True;' # Per SQL Server
# $SourceConnString = "Server=localhost;Database=MioDB;Uid=root;Pwd=password;" # Per MySQL

# Percorso del file SQLite di Destinazione
$SQLiteFilePath = 'C:\Percorso\DatabaseDestinazione.db'
$SQLiteConnString = "Data Source=$SQLiteFilePath;"

# ==========================================
# 2. CARICAMENTO ASSEMBLY E DRIVER
# ==========================================
Add-Type -AssemblyName 'Microsoft.Data.Sqlite' | Out-Null

if ($DbType -eq 'SQLServer')
{
    Add-Type -AssemblyName 'System.Data.SqlClient' | Out-Null
}
else
{
    # Carica l'assembly MySQL
    [System.Reflection.Assembly]::LoadWithPartialName('MySql.Data') | Out-Null
}

# ==========================================
# 3. LETTURA DELLE VISTE DAL DB ORIGINE
# ==========================================
Write-Host "Lettura delle viste dal database $DbType..." -ForegroundColor Cyan

$views = @()

if ($DbType -eq 'SQLServer')
{
    $srcConn = New-Object System.Data.SqlClient.SqlConnection($SourceConnString)
    # Query per estrarre nome e SQL CREATE VIEW da SQL Server
    $query = 'SELECT TABLE_NAME AS ViewName, VIEW_DEFINITION AS ViewScript 
              FROM INFORMATION_SCHEMA.VIEWS'
    
    $cmd = New-Object System.Data.SqlClient.SqlCommand($query, $srcConn)
    $srcConn.Open()
    $reader = $cmd.ExecuteReader()
    while ($reader.Read())
    {
        $views += [PSCustomObject]@{
            Name   = $reader['ViewName']
            Script = $reader['ViewScript']
        }
    }
    $srcConn.Close()

}
elseif ($DbType -eq 'MySQL')
{
    $srcConn = New-Object MySql.Data.MySqlClient.MySqlConnection($SourceConnString)
    # Query per estrarre nome e definizione da MySQL
    $query = 'SELECT TABLE_NAME AS ViewName, VIEW_DEFINITION AS ViewScript 
              FROM INFORMATION_SCHEMA.VIEWS 
              WHERE TABLE_SCHEMA = DATABASE()'
    
    $cmd = New-Object MySql.Data.MySqlClient.MySqlCommand($query, $srcConn)
    $srcConn.Open()
    $reader = $cmd.ExecuteReader()
    while ($reader.Read())
    {
        $views += [PSCustomObject]@{
            Name   = $reader['ViewName']
            Script = $reader['ViewScript']
        }
    }
    $srcConn.Close()
}

Write-Host "Trovate $($views.Count) viste." -ForegroundColor Green

# ==========================================
# 4. CREAZIONE DELLE VISTE IN SQLITE
# ==========================================
$destConn = New-Object Microsoft.Data.Sqlite.SqliteConnection($SQLiteConnString)
$destConn.Open()

foreach ($view in $views)
{
    try
    {
        Write-Host "Migrazione vista: $($view.Name)..." -NoNewline
        
        # Elimina la vista se esiste già in SQLite
        $dropCmd = $destConn.CreateCommand()
        $dropCmd.CommandText = "DROP VIEW IF EXISTS [$($view.Name)];"
        $dropCmd.ExecuteNonQuery() | Out-Null

        # Adatta lo script per SQLite se necessario
        $sqliteScript = "CREATE VIEW [$($view.Name)] AS $($view.Script);"
        
        # Esegue la creazione della vista
        $createCmd = $destConn.CreateCommand()
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

$destConn.Close()
Write-Host 'Migrazione completata!' -ForegroundColor Cyan