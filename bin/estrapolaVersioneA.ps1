# Legge il pom.xml e ne fa il parsing come XML
$pom = [xml](Get-Content -Path '..\pom.xml' -Raw)

# Namespace manager necessario perché Maven usa un namespace default
$ns = New-Object System.Xml.XmlNamespaceManager($pom.NameTable)
$ns.AddNamespace('mvn', 'https://maven.apache.org/POM/4.0.0')

# Estrae la versione del progetto (non quella del parent)
$version = $pom.SelectSingleNode('/mvn:project/mvn:version', $ns)?.InnerText

if ($version)
{
    Write-Host "Versione progetto: $version"
}
else
{
    Write-Host 'Versione non trovata (potrebbe essere ereditata dal parent)'
}