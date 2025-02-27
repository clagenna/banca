Set-StrictMode -Version 3.0

$pathForm="F:\java\bin\BuildApp\Form1.Designer.cs"
if ( ! ( Test-Path variable:mFormStart)) {
    Set-Variable -Name mFormStart -Value "startps1" -Option ReadOnly -Scope Script -Force
    Set-Variable -Name mFormEnd -Value "endps1" -Option ReadOnly -Scope Script -Force
    Set-Variable -Name mPrivate -Value "private " -Option ReadOnly -Scope Script -Force
}
Enum StatoRead {
    BeforeStart 
    Inside 
    after
}
$Stato = [StatoRead]::BeforeStart
$DesignCode = Get-Content -Path $pathForm | ForEach-Object {
    $ri = $_
    if ( $Stato -eq [StatoRead]::BeforeStart ) {
            if ( $ri -match $mFormStart) {
                $stato = [StatoRead]::Inside
                $ri
            }
        }
    elseif ( $stato -eq [StatoRead]::Inside ) {
            if ( $ri -match $mFormEnd) {
                $stato = [StatoRead]::After
            }
            if ( $ri -match $mPrivate) {
                $ri = $ri -replace "private ","public "
            }
            $ri
        }
    elseif ( $stato -eq [StatoRead]::after ) {
            # nothing
        }
    }

$DesignCode