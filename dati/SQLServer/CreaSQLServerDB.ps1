#import SqlServer module
Import-Module -Name "SqlServer"

Set-StrictMode -Version 3.0
Set-Location (Split-Path $PSCommandPath)

$locDBHost = "localhost"
$dbPort = 1433
$ServerInstance = "{0},{1}" -f $locDBHost,$dbPort
$DbName = "Banca"
$DbDir = "F:\SQL2022\MSSQL16.MSSQLSERVER\MSSQL\DATA"
$LogDir = "F:\SQL2022\MSSQL16.MSSQLSERVER\MSSQL\DATA"
$DbUser = "sqlgianni"
$DbPswd = "sicuelserver"

$lqry = "SELECT DataPath = CONVERT(sysname, SERVERPROPERTY('InstanceDefaultDataPath')),
                 LogPath = CONVERT(sysname, SERVERPROPERTY('InstanceDefaultLogPath'))"

$allRet = Invoke-Sqlcmd -Query $lqry `
                          -ServerInstance $ServerInstance `
                          -Database "master" `
                          -Username $DbUser `
                          -Password $DbPswd `
                          -TrustServerCertificate

$DbDir = $allRet.DataPath
$LogDir = $allRet.LogPath                          

$sql = "
DECLARE @SQL AS NVARCHAR (1000);

IF EXISTS (SELECT 1
           FROM sys.databases
           WHERE [name] = N'{0}')
    BEGIN
        SET @SQL = N'USE [{0}];

                 ALTER DATABASE {0} SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
                 USE [tempdb];

                 DROP DATABASE {0};';
        EXECUTE (@SQL);
    END
"
$lqry = $sql -f $DbName
Invoke-Sqlcmd -Query $lqry `
                          -ServerInstance $ServerInstance `
                          -Database "tempdb" `
                          -Username $DbUser `
                          -Password $DbPswd `
                          -TrustServerCertificate

write-host ("Delete of {0} Done!" -f $DbName )



# create variable with SQL to execute
$sql = "
CREATE DATABASE [{0}]
 CONTAINMENT = NONE
 ON  PRIMARY
( NAME = N'{0}', FILENAME = N'{1}\{0}.mdf' , SIZE = 65536KB , FILEGROWTH = 2048KB )
 LOG ON
( NAME = N'{0}_log', FILENAME = N'{2}\{0}_log.ldf' , SIZE = 8196KB , FILEGROWTH = 2048KB )
GO

USE [master]
GO
ALTER DATABASE [{0}] SET RECOVERY SIMPLE WITH NO_WAIT
GO

ALTER AUTHORIZATION ON DATABASE::[{0}] TO [{3}]
GO "
$lqry = $sql -f $DbName,$DbDir,$LogDir,$DbUser

Invoke-Sqlcmd -Query $lqry `
                          -ServerInstance $ServerInstance `
                          -Database "tempdb" `
                          -Username $DbUser `
                          -Password $DbPswd `
                          -TrustServerCertificate
write-host ("Creation of {0} Done!" -f $DbName )


Invoke-Sqlcmd -InputFile ".\Banca.sql" `
                          -ServerInstance $ServerInstance `
                          -Database $DbName `
                          -Username $DbUser `
                          -Password $DbPswd `
                          -TrustServerCertificate
write-host ("Creation Tables {0} Done!" -f $DbName )