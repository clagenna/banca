# Inizializza il dizionario/map per contenere (filename -> content)
$fileMap = @{}

# Imposta il percorso di ricerca (usa la cartella corrente '.' oppure un percorso specifico)
$searchPath = '.' 

# Cerca i file corrispondenti al pattern
$files = Get-ChildItem -Path $searchPath -Filter 'view*.sql' -File -Recurse

foreach ($file in $files)
{
    # Legge l'intero contenuto del file come singola stringa testo
    $content = Get-Content -Path $file.FullName -Raw
    
    # Inserisce nella map: Chiave = Nome del file, Valore = Contenuto
    $fileMap[$file.Name] = $content
}

# --- Esempio di utilizzo della Map creata ---
Write-Host "File trovati e caricati: $($fileMap.Count)`n"

foreach ($entry in $fileMap.GetEnumerator())
{
    Write-Host "=== FILE: $($entry.Key) ===" -ForegroundColor Cyan
    Write-Host $entry.Value
    Write-Host ('-' * 40)
}