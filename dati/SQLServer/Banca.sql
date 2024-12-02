/****** Object:  Table dbo.causali    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.causali(
	abicaus varchar(4) NOT NULL,
	descrcaus nvarchar(256) NULL,
	costo int NULL,
 CONSTRAINT PK_causali PRIMARY KEY CLUSTERED ( 	abicaus ASC )
       WITH (PAD_INDEX = OFF, 
	         STATISTICS_NORECOMPUTE = OFF, 
			 IGNORE_DUP_KEY = OFF, 
			 ALLOW_ROW_LOCKS = ON, 
			 ALLOW_PAGE_LOCKS = ON, 
			 OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF)
) 
GO

/****** Object:  Table dbo.impFiles    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.impFiles (
	id INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	filename  	varchar(128) NOT NULL,
	reldir  	varchar(128) NOT NULL,
	size 		int NULL,
	qtarecs		int NULL,
	dtmin 		datetime NULL,
	dtmax 		datetime NULL,
	ultagg 		datetime NULL
)
GO
CREATE UNIQUE INDEX UXImpFiles ON dbo.impFiles( filename, reldir )
GO

/****** Object:  View dbo.listaMovimentiBSI    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'0', N'Voci Generali', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'05', N'Prelev. Bancomat', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'13', N'Assegno', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'14', N'Acquisto Titoli BSI', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'16', N'Comissioni su pagamenti', 1)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'17', N'Assicurazione Bancaria', 1)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'18', N'Interessi Bancari', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'19', N'Ritenute', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'22', N'Diritti custodia Titoli', 1)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'26', N'Bonifico', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'27', N'Stipendio/pensione', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'34', N'Estinzioni conto previd.', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'43', N'Pagamento POS', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'45', N'Pagamento Carta Credito', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'48', N'Versamento con Bonifico', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'5', N'Prelev. Bancomat', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'50', N'RID Rapporto Interbancario Diretto', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'66', N'Canoni vari', 1)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'68', N'Storni vari', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'83', N'Iscriz. Fondi', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'84', N'Rimborso Titoli', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'CO', N'Contante', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'PP', N'PayPal', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'S1', N'SMAC - Pagamento con SMAC', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'S2', N'SMAC - Pagamento con SMAC con Ricarica', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'S3', N'SMAC - SMAC Fiscale', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'S4', N'SMAC - Ricarica su SMAC', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'S5', N'SMAC - Accredito su SMAC', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'S6', N'SMAC - Manca la decodificata ', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Y1', N'Anticipazioni su fatture Italia', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z1', N'Disposizioni di giro di cash pooling', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z2', N'Versamento di assegni bancari, assegni di conto corrente postale', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z3', N'Versamento di assegni circolari emessi da altre banche', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z4', N'Versamento di assegni postali non standardizzati', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z5', N'Versamento indiretto. Versamento di contante e/o assegni eseguito da soggetto diverso dal titolare del conto', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z6', N'Prelevamento eseguito da soggetto diverso dal titolare del conto', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z7', N'Accredito RID', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z8', N'Accredito MAV', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z9', N'Insoluto/storno RID', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZA', N'Insoluto MAV', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZB', N'Incasso certificati conformita�', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZC', N'Pagamento per fornitura elettrica', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZD', N'Pagamento per servizio telefonico', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZE', N'Pagamento per servizi acqua/gas', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZF', N'Pagamento per operazioni su prodotti derivati', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZG', N'Accredito per operazioni su prodotti derivati', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZH', N'Rimborso titoli e/o fondi comuni', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZI', N'Bonifico dall�estero', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZL', N'Bonifico sull�estero', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZM', N'Sconto effetti sull�estero', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZN', N'Negoziazione assegni sull�estero', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZP', N'Commissioni e spese su fideiussioni (Da utilizzare per operazioni estero e Italia)', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZQ', N'Commissioni e spese su crediti documentari', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZR', N'Penali', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZS', N'Erogazione prestiti personali e finanziamenti diversi', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZT', N'Pagamento/incasso bollettino bancario', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZU', N'Bonifico per previdenza complementare', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZX', N'Bonifico oggetto di oneri deducibili o detrazioni di imposta', 0)
GO

/****** Object:  Table dbo.movimentiBSI    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimentiBSI(
    id INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	idfile INT NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus varchar(4) NULL,
	cardid nvarchar(20) NULL
) 
GO
CREATE INDEX IXMovBSI ON dbo.movimentiBSI( dtmov, dtval )
GO
/****** Object:  Table dbo.movimentiBSICredit    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimentiBSICredit(
    id INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	idfile INT NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus varchar(4) NULL,
	cardid nvarchar(20) NULL
)
GO
CREATE INDEX IXMovBsiCredit ON dbo.movimentiBsiCredit( dtmov, dtval )
GO

/****** Object:  Table dbo.movimentiCarisp    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimentiCarisp(
    id INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	idfile INT NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus varchar(4) NULL,
	cardid nvarchar(20) NULL
)
GO
CREATE INDEX IXMovCarisp ON dbo.movimentiCarisp( dtmov, dtval )
GO
/****** Object:  Table dbo.movimentiCarispCredit    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimentiCarispCredit(
    id INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	idfile INT NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus varchar(4) NULL,
	cardid nvarchar(20) NULL
)
GO
CREATE INDEX IXMovCarispCredit ON dbo.movimentiCarispCredit( dtmov, dtval )
GO
/****** Object:  Table dbo.movimentiCarispCredit    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimentiPaypal(
    id INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	idfile INT NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus nvarchar(512) NULL,
	cardid nvarchar(20) NULL
)
GO
CREATE INDEX IXMovPaypal ON dbo.movimentiPaypal( dtmov, dtval )
GO
/****** Object:  Table dbo.movimentiWise    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimentiWise(
    id INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	idfile INT NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus nvarchar(512) NULL,
	cardid nvarchar(20) NULL
)
GO
CREATE INDEX IXMovWise ON dbo.movimentiWise( dtmov, dtval )
GO
/****** Object:  Table dbo.movimentiContanti    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimentiContanti(
    id INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	idfile INT NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus varchar(4) NULL,
	cardid nvarchar(20) NULL
)
GO
CREATE INDEX IXMovContanti ON dbo.movimentiContanti( dtmov, dtval )
GO
/****** Object:  Table dbo.movimentiSmac    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimentiSmac(
    id INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
	idfile INT NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus nvarchar(512) NULL,
	cardid nvarchar(20) NULL
)
GO
CREATE INDEX IXMovSmac ON dbo.movimentiSmac( dtmov, dtval )
GO

CREATE VIEW dbo.listaMovimentiBSI
as 
SELECT 'BSI' as tipo
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
  FROM movimentiBSI mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
GO

/****** Object:  View dbo.listaMovimentiBSICredi ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE view dbo.listaMovimentiBSICredit
as 
SELECT 'BSICred' as tipo 
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
  FROM movimentiBSICredit mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
GO

/****** Object:  View dbo.listaMovimentiPaypal ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE view dbo.listaMovimentiPaypal
as 
SELECT 'Paypal' as tipo 
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
  FROM movimentiPaypal mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
GO

/****** Object:  View dbo.listaMovimentiCarisp    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE view dbo.listaMovimentiCarisp
as 
SELECT 'Carisp' as Tipo
      ,idfile
      ,dtmov
      ,dtval
	  , SUBSTRING( convert(varchar,dtmov,102), 1,7) as movstr
	  , SUBSTRING( convert(varchar,dtval,102), 1,7) as valstr
      ,dare
      ,avere
      ,descr
	  ,cardid
      ,mo.abicaus
	  ,ca.descrcaus
	  ,ca.costo
  FROM movimentiCarisp mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
GO


/****** Object:  View dbo.listaMovimentiCarispCredit    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


CREATE view dbo.listaMovimentiCarispCredit
as 
SELECT 'CarispCred' as tipo
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
  FROM movimentiCarispCredit mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
GO

/****** Object:  View dbo.listaMovimentiWise    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE view dbo.listaMovimentiWise
as 
SELECT 'wise' as tipo
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
  FROM movimentiWise mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
GO


/****** Object:  View dbo.listaMovimentiContanti    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO



CREATE view dbo.listaMovimentiContanti
as 
SELECT 'Cont' as tipo
      ,idfile
      ,dtmov
      ,dtval
	  , SUBSTRING( convert(varchar,dtmov,102), 1,7) as movstr
	  , SUBSTRING( convert(varchar,dtval,102), 1,7) as valstr
      ,dare
      ,avere
      ,descr
	  ,cardid
      ,mo.abicaus
	  ,ca.descrcaus
	  ,ca.costo
  FROM movimentiContanti mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
GO

/****** Object:  View dbo.listaMovimentiSmac    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE view dbo.listaMovimentiSmac
as 
SELECT 'Smac' as tipo
      ,idfile
      ,dtmov
      ,dtval
      , SUBSTRING( convert(varchar,dtmov,102), 1,7) as movstr
      , SUBSTRING( convert(varchar,dtval,102), 1,7) as valstr
      -- ,dare
      ,CASE mo.abicaus
          WHEN 'S3'  THEN 0
          WHEN 'S4'  THEN 0
          ELSE mo.dare
       END dare
      ,avere
      ,descr
      ,cardid
      ,mo.abicaus
      ,ca.descrcaus
      ,ca.costo
  FROM movimentiSmac mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
GO


/****** Object:  View dbo.ListaMovimentiUNION    Script Date: 22/11/2024 18:24:23 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE     view dbo.ListaMovimentiUNION
as
SELECT tipo 
      ,idfile
      ,dtmov
      ,dtval
      ,movstr
      ,valstr
      ,dare
      ,avere
	  ,cardid
      ,descr
      ,abicaus
      ,descrcaus
      ,costo
  FROM dbo.listaMovimentiCarisp
UNION
SELECT tipo
      ,idfile
      ,dtmov
      ,dtval
      ,movstr
      ,valstr
      ,dare
      ,avere
	  ,cardid
      ,descr
      ,abicaus
      ,descrcaus
      ,costo
  FROM dbo.listaMovimentiBSI
UNION 
SELECT tipo
      ,idfile
      ,dtmov
      ,dtval
      ,movstr
      ,valstr
      ,dare
      ,avere
	  ,cardid
      ,descr
      ,abicaus
      ,descrcaus
      ,costo
  FROM dbo.listaMovimentiBSICredit
UNION 
SELECT tipo
      ,idfile
      ,dtmov
      ,dtval
      ,movstr
      ,valstr
      ,dare
      ,avere
	  ,cardid
      ,descr
      ,abicaus
      ,descrcaus
      ,costo
  FROM dbo.listaMovimentiCarispCredit
UNION 
SELECT tipo
      ,idfile
      ,dtmov
      ,dtval
      ,movstr
      ,valstr
      ,dare
      ,avere
	  ,cardid
      ,descr
      ,abicaus
      ,descrcaus
      ,costo
  FROM dbo.listaMovimentiWise
UNION 
SELECT tipo
      ,idfile
      ,dtmov
      ,dtval
      ,movstr
      ,valstr
      ,dare
      ,avere
	  ,cardid
      ,descr
      ,abicaus
      ,descrcaus
      ,costo
  FROM dbo.listaMovimentiSmac
UNION 
SELECT tipo
      ,idfile
      ,dtmov
      ,dtval
      ,movstr
      ,valstr
      ,dare
      ,avere
	  ,cardid
      ,descr
      ,abicaus
      ,descrcaus
      ,costo
  FROM dbo.listaMovimentiPaypal
UNION 
SELECT tipo
      ,idfile
      ,dtmov
      ,dtval
      ,movstr
      ,valstr
      ,dare
      ,avere
	  ,cardid
      ,descr
      ,abicaus
      ,descrcaus
      ,costo
  FROM dbo.listaMovimentiContanti
GO


USE master
GO
ALTER DATABASE Banca SET  READ_WRITE 
GO
