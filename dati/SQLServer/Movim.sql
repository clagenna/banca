USE Banca
GO

/****** Object:  Table dbo.movimentiCarisp    Script Date: 02/02/2025 10:15:40 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

DROP TABLE IF EXISTS dbo.movimenti
GO

CREATE TABLE dbo.movimenti(
	id int IDENTITY(1,1) NOT NULL,
	tipo nvarchar(32) not null,
	idfile int NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus nvarchar(32) NULL,
	cardid nvarchar(20) NULL,
	codstat nvarchar(20) NULL,
PRIMARY KEY CLUSTERED  ( id ASC )
   WITH (
	PAD_INDEX = OFF, 
	STATISTICS_NORECOMPUTE = OFF, 
	IGNORE_DUP_KEY = OFF, 
	ALLOW_ROW_LOCKS = ON, 
	ALLOW_PAGE_LOCKS = ON, 
	OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) 
) 
GO

/****** Object:  Index IXMovCarisp    Script Date: 02/02/2025 10:18:29 ******/
CREATE NONCLUSTERED INDEX IXMovim ON dbo.movimenti
(
    tipo ASC, 
	dtmov ASC,
	dtval ASC
) 
WITH (
	PAD_INDEX = OFF, 
	STATISTICS_NORECOMPUTE = OFF, 
	SORT_IN_TEMPDB = OFF, 
	DROP_EXISTING = OFF, 
	ONLINE = OFF, 
	ALLOW_ROW_LOCKS = ON, 
	ALLOW_PAGE_LOCKS = ON, 
	OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF)
GO

/****** Object:  Index IXMovCarisp    Script Date: 02/02/2025 10:18:29 ******/
CREATE NONCLUSTERED INDEX IXMovCodstat ON dbo.movimenti
(
	codstat ASC
) 
WITH (
	PAD_INDEX = OFF, 
	STATISTICS_NORECOMPUTE = OFF, 
	SORT_IN_TEMPDB = OFF, 
	DROP_EXISTING = OFF, 
	ONLINE = OFF, 
	ALLOW_ROW_LOCKS = ON, 
	ALLOW_PAGE_LOCKS = ON, 
	OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF)
GO


INSERT INTO dbo.movimenti (  Tipo, idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat )
                   SELECT 'BSI' ,idfile ,dtmov ,dtval ,dare ,avere ,descr ,abicaus ,cardid ,codstat
  FROM Banca.dbo.movimentiBSI
GO

INSERT INTO dbo.movimenti (  Tipo, idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat )
                   SELECT 'BSICredit' ,idfile ,dtmov ,dtval ,dare ,avere ,descr ,abicaus ,cardid ,codstat
  FROM Banca.dbo.movimentiBSICredit
GO

INSERT INTO dbo.movimenti (  Tipo, idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat )
                   SELECT 'Carisp' ,idfile ,dtmov ,dtval ,dare ,avere ,descr ,abicaus ,cardid ,codstat
  FROM Banca.dbo.movimentiCarisp
GO

INSERT INTO dbo.movimenti (  Tipo, idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat )
                   SELECT 'CarispCredit' ,idfile ,dtmov ,dtval ,dare ,avere ,descr ,abicaus ,cardid ,codstat
  FROM Banca.dbo.movimentiCarispCredit
GO

INSERT INTO dbo.movimenti (  Tipo, idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat )
                   SELECT 'Contanti' ,idfile ,dtmov ,dtval ,dare ,avere ,descr ,abicaus ,cardid ,codstat
  FROM Banca.dbo.movimentiContanti
GO

INSERT INTO dbo.movimenti (  Tipo, idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat )
                   SELECT 'Paypal' ,idfile ,dtmov ,dtval ,dare ,avere ,descr ,abicaus ,cardid ,codstat
  FROM Banca.dbo.movimentiPaypal
GO

INSERT INTO dbo.movimenti (  Tipo, idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat )
                   SELECT 'Smac' ,idfile ,dtmov ,dtval ,dare ,avere ,descr ,abicaus ,cardid ,codstat
  FROM Banca.dbo.movimentiSmac
GO

INSERT INTO dbo.movimenti (  Tipo, idfile,dtmov,dtval,dare,avere,descr,abicaus,cardid,codstat )
                   SELECT 'Wise' ,idfile ,dtmov ,dtval ,dare ,avere ,descr ,abicaus ,cardid ,codstat
  FROM Banca.dbo.movimentiWise
GO


DROP VIEW IF EXISTS dbo.listaMovimenti
GO

CREATE VIEW dbo.listaMovimenti
as 
SELECT tipo
      ,id
      ,idfile
      ,dtmov
      ,dtval
      , SUBSTRING( convert(varchar,dtmov,102), 1,7) as movstr
      , SUBSTRING( convert(varchar,dtval,102), 1,7) as valstr
      ,dare
      ,avere
      ,cardid
      ,descr
      ,mo.abicaus
      ,ca.descrcaus
      ,ca.costo
      ,codstat
  FROM movimenti mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
GO

