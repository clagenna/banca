
USE Banca
GO
/****** Object:  Table dbo.causali    Script Date: 26/02/2026 14:32:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.causali(
	abicaus varchar(32) NOT NULL,
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
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZB', N'Incasso certificati conformita’', 0)
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
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZI', N'Bonifico dall’estero', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZL', N'Bonifico sull’estero', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZM', N'Sconto effetti sull’estero', 0)
GO
INSERT INTO dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZN', N'Negoziazione assegni sull’estero', 0)
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


/****** Object:  Table dbo.movimenti    Script Date: 26/02/2026 14:32:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimenti(
	id int IDENTITY(1) NOT NULL,
	tipo nvarchar(32) NOT NULL,
	idfile int NULL,
	dtmov datetime NULL,
	dtval datetime NULL,
	dare money NULL,
	avere money NULL,
	descr nvarchar(512) NULL,
	abicaus nvarchar(32) NULL,
	cardid nvarchar(20) NULL,
	idCodStat int NULL,
PRIMARY KEY CLUSTERED (	id ASC ) 
	WITH (PAD_INDEX = OFF, 
	  	  STATISTICS_NORECOMPUTE = OFF, 
		  IGNORE_DUP_KEY = OFF, 
		  ALLOW_ROW_LOCKS = ON, 
		  ALLOW_PAGE_LOCKS = ON, 
		  OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) 
) 
GO

/****** Object:  Index IXMovim    Script Date: 26/02/2026 14:32:07 ******/
CREATE NONCLUSTERED INDEX IXMovim ON dbo.movimenti
					(
						tipo ASC,
						dtmov ASC,
						dtval ASC
					)
WITH (PAD_INDEX = OFF, 
	  STATISTICS_NORECOMPUTE = OFF, 
	  SORT_IN_TEMPDB = OFF, 
	  DROP_EXISTING = OFF, 
	  ONLINE = OFF, 
	  ALLOW_ROW_LOCKS = ON, 
	  ALLOW_PAGE_LOCKS = ON, 
	  OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) 
GO


/****** Object:  Table dbo.CodiciStat    Script Date: 26/02/2026 14:32:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.CodiciStat(
	idCodStat int IDENTITY(1) NOT NULL,
	codstat varchar(12) NOT NULL,
	descrstat varchar(256) NOT NULL,
 CONSTRAINT PK_CodiciStat PRIMARY KEY CLUSTERED ( idCodStat ASC )
	WITH ( PAD_INDEX = OFF, 
		   STATISTICS_NORECOMPUTE = OFF, 
		   IGNORE_DUP_KEY = OFF, 
		   ALLOW_ROW_LOCKS = ON, 
		   ALLOW_PAGE_LOCKS = ON, 
		   OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) 
) 
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index uix_codstat    Script Date: 26/02/2026 14:32:07 ******/
CREATE UNIQUE NONCLUSTERED INDEX uix_codstat ON dbo.CodiciStat ( codstat ASC )
	WITH (PAD_INDEX = OFF, 
		  STATISTICS_NORECOMPUTE = OFF, 
		  SORT_IN_TEMPDB = OFF, 
		  IGNORE_DUP_KEY = OFF, 
		  DROP_EXISTING = OFF, 
		  ONLINE = OFF, 
		  ALLOW_ROW_LOCKS = ON, 
		  ALLOW_PAGE_LOCKS = ON, 
		  OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) 
GO
/****** Object:  View dbo.listaMovimenti    Script Date: 26/02/2026 14:32:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE   view dbo.listaMovimenti
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
	  ,mo.idCodStat
      ,cs.codstat
	  ,0 as flag
  FROM movimenti mo
    LEFT OUTER JOIN causali ca
	  ON mo.abicaus = ca.abicaus
	LEFT OUTER JOIN CodiciStat cs
	  on mo.idCodStat=cs.idCodStat
GO

/****** Object:  Table dbo.impFiles    Script Date: 26/02/2026 14:32:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.impFiles(
	id int IDENTITY(1) NOT NULL,
	filename varchar(128) NOT NULL,
	reldir varchar(128) NOT NULL,
	size int NULL,
	qtarecs int NULL,
	dtmin datetime NULL,
	dtmax datetime NULL,
	ultagg datetime NULL,
PRIMARY KEY CLUSTERED ( id ASC )
	WITH (PAD_INDEX = OFF, 
		STATISTICS_NORECOMPUTE = OFF, 
		IGNORE_DUP_KEY = OFF, 
		ALLOW_ROW_LOCKS = ON, 
		ALLOW_PAGE_LOCKS = ON, 
		OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) 
) 
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index UXImpFiles    Script Date: 26/02/2026 14:32:07 ******/
CREATE UNIQUE NONCLUSTERED INDEX UXImpFiles ON dbo.impFiles
(
	filename ASC,
	reldir ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) 
GO
SET ANSI_PADDING ON
GO


SET IDENTITY_INSERT dbo.CodiciStat OFF
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'01', N'Alimentare')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'01.01', N'Spese alimentari (titancoop, Baguette, etc..)')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'01.02', N'cene e ristoranti')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'01.03', N'asporto')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'01.04', N'bar e spizzichi')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'01.05', N'Mensa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'01.06', N'Caffe')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'01.10', N'altri')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02', N'Automezzi')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.01', N'Auto AUDI')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.01.01', N'Benzina e/o gasolio')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.01.02', N'Gomme')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.01.03', N'Revisione e/o bollo')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.01.04', N'tagliando')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.01.05', N'Accessori')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.02', N'Auto Vitara Eug')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.02.01', N'Benzina e/o gasolio')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.02.02', N'Gomme')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.02.03', N'Revisione e/o bollo')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.02.04', N'tagliando')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.03', N'Moto')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.07', N'Autostrada')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'02.08', N'Noleggio Auto')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03', N'Divertimenti')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.01', N'teatro')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.02', N'cinema')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.03', N'concerti')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.04', N'Musei')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.05', N'Libri')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.05.01', N'Acquisto Libri Amazon Kindle')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.05.02', N'Acquisto Libri Kobo')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.05.03', N'Abbonamenti a Giornali')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.06', N'Corsi Internet')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.07', N'Abbonamento TV')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'03.08', N'Associazioni Culturali')
GO

INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04', N'Viaggi e Vacanze')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.01', N'Costo biglietti, treno, aereo')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.02', N'Hotel')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.03', N'Noleggio mezzi')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.03.01', N'Noleggio auto')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.03.02', N'Noleggio bici')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.03.03', N'Bus Turistici')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.03.04', N'Noleggio Sci')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.03.05', N'Ski Pass')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.04', N'Viaggi Organizzati')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'04.04.01', N'Viaggi Organizzati')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05', N'Finanza e Banche')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.01', N'SMAC Card')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.02', N'Assegni vari')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.03', N'Rendite, Stipendi e pensioni')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.03.01', N'Stipendi')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.03.02', N'Pensione')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.03.03', N'Affitti percepiti')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.03.04', N'Eredita')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.04', N'Giroconto fra banche')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.05', N'Donazioni Varie')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.10', N'Banca CaRisp')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.10.01', N'Interessi bancario Carisp')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.10.02', N'Comissione Carisp')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.10.03', N'Costo manutenzione conto Carisp')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.10.04', N'Investimenti Carisp')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.10.05', N'Trasferimenti denaro Carisp')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.10.06', N'Prelev. Bancomat Carisp')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.20', N'Banca BSI')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.20.01', N'Interessi bancari BSI')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.20.02', N'Comissioni BSI')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.20.03', N'Costo manutenzione conto BSI')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.20.04', N'Investimenti BSI')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.20.05', N'Trasferimenti denaro BSI')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.20.06', N'Prelev. Bancomat BSI')
GO

INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.30', N'BKN301')
GO

INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.31', N'WISE Brussel')
GO

INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.50', N'Assicurazione Pancotti')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.60', N'Assicurazione Unipol')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.70', N'Assicurazione Zurich')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'05.80', N'Assicurazione Allianz')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'06', N'Famiglia')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'06.01', N'Alessandro')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'06.01', N'Andrea')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'06.03', N'Lucia')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'10', N'E-commerce')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'10.01', N'Amazon')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'10.01.01', N'Accrediti/Addebiti da amazon')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'10.01.02', N'Spese su Amazon')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'10.02', N'Alibaba')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20', N'Salute e Medicina')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.01', N'Spese in farmacia')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.02', N'Spese presso specialisti medici')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.02.01', N'Dentista')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.02.02', N'Fisio terapista')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.02.03', N'Ottico')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.02.05', N'Ospedali')
GO

INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.03', N'Wellfare')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.03.01', N'Barbiere')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.03.02', N'Parucchiere')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.03.03', N'Estetista')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.03.04', N'Sport e/o palestra')
GO

INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.10', N'Abbigliamento')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.10.10', N'Abbigliamento uomo')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.10.20', N'Abbigliamento donna')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.10.30', N'Abbigliamento bambino')
GO

INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.15', N'Regali vari')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'20.20', N'Spese scuola e/o scolastiche')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30', N'Casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01', N'spese manutenzione casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.01', N'Idraulico per la casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.02', N'Manutenzione Caldaia')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.03', N'Spese GE.CO o geco')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.04', N'Foto voltaico')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.05', N'Elettricista per la casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.06', N'Imbianchino per la casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.07', N'Falegname per la casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.08', N'Architetto per la casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.10', N'Accessori X la casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.11', N'Televisione')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.12', N'Sanatoria')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.13', N'Avvocati X S. Michele')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.15', N'Giardinaggio')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.20', N'Rifacimenti della casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.01.99', N'Spese ausiliarie')
GO

INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.02', N'Azienda dei Servizi')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.02.01', N'Luce')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.02.02', N'Acqua')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.02.03', N'Gas')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.03', N'Giardinaggio per la casa')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.04', N'telefono')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.06', N'Uffici Stato')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.07', N'Internet')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'30.08', N'Software')
GO
INSERT dbo.CodiciStat (codstat, descrstat) VALUES ( N'99', N'Spese Non Classificate')
GO
SET IDENTITY_INSERT dbo.CodiciStat OFF
GO




USE master
GO
ALTER DATABASE Banca SET  READ_WRITE 
GO
