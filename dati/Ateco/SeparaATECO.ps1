$fileIn = ".\FiscoNews24-Elenco-completo-Codici-Ateco-2024.txt"
$fileOut = $fileIn -replace ".txt", "_MAP.txt"
Set-Location (Split-Path $PSCommandPath)
$Script:key = ""
$Script:val = ""
$Script:map = [ordered]@{} 
Get-Content -Path $fileIn -Encoding utf8 | Where-Object { $_.Length -gt 1 } | ForEach-Object {
    # Write-Output $_
    if ( $_ -match "\d+") {
        $Script:key = $_
        return
    }
    if ( $_ -match "\d+\.\d+") {
        $Script:key = $_
        return
    }
    if ( $_ -match "\d+\.\d+\.\d+") {
        $Script:key = $_
        return
    }
    if ( $_ -match "[a-z]+.*") {
        # if ( $_.Contains("Attivit", 'InvariantCultureIgnoreCase') ) {
        #     Write-Output "eccolo"
        # }
        if ( $Script:key.EndsWith(".0")  -or $Script:key.EndsWith(".00")  ) {
            # Write-Output ("scarto {0} {1}" -f $Script:key, $Script:val)
            return
        }
        if ( $Script:key.Length -gt 1 )  {
            $Script:val = $_
            $Script:map[$Script:key] = $Script:val
        }
        $Script:key = ""
        $Script:val = ""
    }
}

Write-Output $Script:map
$Script:map | Out-File -FilePath $fileOut -Encoding utf8
