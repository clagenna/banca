USE master
GO
/****** Object:  Database Banca    Script Date: 28/09/2026 16:01:41 ******/
CREATE DATABASE Banca
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'Banca', FILENAME = N'F:\SQL2022\MSSQL16.MSSQLSERVER\MSSQL\DATA\Banca_Banca.mdf' , SIZE = 65536KB , MAXSIZE = UNLIMITED, FILEGROWTH = 2048KB )
 LOG ON 
( NAME = N'Banca_log', FILENAME = N'F:\SQL2022\MSSQL16.MSSQLSERVER\MSSQL\DATA\Banca_Banca_log.ldf' , SIZE = 12288KB , MAXSIZE = 2048GB , FILEGROWTH = 2048KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO
ALTER DATABASE Banca SET COMPATIBILITY_LEVEL = 160
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC Banca.dbo.sp_fulltext_database @action = 'enable'
end
GO
ALTER DATABASE Banca SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE Banca SET ANSI_NULLS OFF 
GO
ALTER DATABASE Banca SET ANSI_PADDING OFF 
GO
ALTER DATABASE Banca SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE Banca SET ARITHABORT OFF 
GO
ALTER DATABASE Banca SET AUTO_CLOSE OFF 
GO
ALTER DATABASE Banca SET AUTO_SHRINK OFF 
GO
ALTER DATABASE Banca SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE Banca SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE Banca SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE Banca SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE Banca SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE Banca SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE Banca SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE Banca SET  DISABLE_BROKER 
GO
ALTER DATABASE Banca SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE Banca SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE Banca SET TRUSTWORTHY OFF 
GO
ALTER DATABASE Banca SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE Banca SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE Banca SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE Banca SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE Banca SET RECOVERY SIMPLE 
GO
ALTER DATABASE Banca SET  MULTI_USER 
GO
ALTER DATABASE Banca SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE Banca SET DB_CHAINING OFF 
GO
ALTER DATABASE Banca SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE Banca SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE Banca SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE Banca SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'Banca', N'ON'
GO
ALTER DATABASE Banca SET QUERY_STORE = ON
GO
ALTER DATABASE Banca SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE Banca
GO
/****** Object:  Table dbo.causali    Script Date: 28/09/2026 16:01:41 ******/
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
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'0', N'Voci Generali', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'05', N'Prelev. Bancomat', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'13', N'Assegno', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'14', N'Acquisto Titoli BSI', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'16', N'Comissioni su pagamenti', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'17', N'Assicurazione Bancaria', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'18', N'Interessi Bancari', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'19', N'Ritenute', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'22', N'Diritti custodia Titoli', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'26', N'Bonifico', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'27', N'Stipendio/pensione', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'34', N'Estinzioni conto previd.', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'43', N'Pagamento POS', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'45', N'Pagamento Carta Credito', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'48', N'Versamento con Bonifico', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'5', N'Prelev. Bancomat', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'50', N'RID Rapporto Interbancario Diretto', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'66', N'Canoni vari', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'68', N'Storni vari', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'83', N'Iscriz. Fondi', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'84', N'Rimborso Titoli', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'CO', N'Contante', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'PP', N'PayPal', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S1', N'SMAC - Pagamento con SMAC', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S2', N'SMAC - Pagamento con SMAC con Ricarica', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S3', N'SMAC - SMAC Fiscale', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S4', N'SMAC - Ricarica su SMAC', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S5', N'SMAC - Accredito su SMAC', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S6', N'SMAC - Manca la decodificata ', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Y1', N'Anticipazioni su fatture Italia', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z1', N'Disposizioni di giro di cash pooling', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z2', N'Versamento di assegni bancari, assegni di conto corrente postale', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z3', N'Versamento di assegni circolari emessi da altre banche', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z4', N'Versamento di assegni postali non standardizzati', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z5', N'Versamento indiretto. Versamento di contante e/o assegni eseguito da soggetto diverso dal titolare del conto', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z6', N'Prelevamento eseguito da soggetto diverso dal titolare del conto', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z7', N'Accredito RID', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z8', N'Accredito MAV', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z9', N'Insoluto/storno RID', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZA', N'Insoluto MAV', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZB', N'Incasso certificati conformita�', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZC', N'Pagamento per fornitura elettrica', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZD', N'Pagamento per servizio telefonico', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZE', N'Pagamento per servizi acqua/gas', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZF', N'Pagamento per operazioni su prodotti derivati', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZG', N'Accredito per operazioni su prodotti derivati', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZH', N'Rimborso titoli e/o fondi comuni', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZI', N'Bonifico dall�estero', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZL', N'Bonifico sull�estero', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZM', N'Sconto effetti sull�estero', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZN', N'Negoziazione assegni sull�estero', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZP', N'Commissioni e spese su fideiussioni (Da utilizzare per operazioni estero e Italia)', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZQ', N'Commissioni e spese su crediti documentari', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZR', N'Penali', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZS', N'Erogazione prestiti personali e finanziamenti diversi', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZT', N'Pagamento/incasso bollettino bancario', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZU', N'Bonifico per previdenza complementare', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZX', N'Bonifico oggetto di oneri deducibili o detrazioni di imposta', 0)
GO
/****** Object:  Table dbo.movimenti    Script Date: 28/09/2026 16:01:41 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.movimenti(
	id int IDENTITY(1,1) NOT NULL,
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
	WITH (
		PAD_INDEX = OFF, 
		STATISTICS_NORECOMPUTE = OFF, 
		IGNORE_DUP_KEY = OFF, 
		ALLOW_ROW_LOCKS = ON, 
		ALLOW_PAGE_LOCKS = ON, 
		OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) 
) 
GO

/****** Object:  Index IXMovim    Script Date: 28/09/2026 16:01:41 ******/
CREATE NONCLUSTERED INDEX IXMovim ON dbo.movimenti
(
	tipo ASC,
	dtmov ASC,
	dtval ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
GO
ALTER TABLE dbo.movimenti  WITH CHECK ADD  CONSTRAINT FK_Impfile_movim FOREIGN KEY(idfile)
REFERENCES dbo.impFiles (id)
GO
ALTER TABLE dbo.movimenti CHECK CONSTRAINT FK_Impfile_movim
GO

/****** Object:  Table dbo.CodiciStat    Script Date: 28/09/2026 16:01:41 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.CodiciStat(
	idCodStat int IDENTITY(1,1) NOT NULL,
	codstat varchar(12) NOT NULL,
	descrstat varchar(256) NOT NULL,
 CONSTRAINT PK_CodiciStat PRIMARY KEY CLUSTERED ( idCodStat ASC )
	WITH (
		PAD_INDEX = OFF, 
		STATISTICS_NORECOMPUTE = OFF, 
		IGNORE_DUP_KEY = OFF, 
		ALLOW_ROW_LOCKS = ON, 
		ALLOW_PAGE_LOCKS = ON, 
		OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF
		) 
) 
GO
SET IDENTITY_INSERT dbo.CodiciStat ON
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (1, N'01', N'Alimentare')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (2, N'01.01', N'Spese alimentari (titancoop, Baguette, etc..)')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (3, N'01.02', N'cene e ristoranti')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (4, N'01.03', N'asporto')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (5, N'01.04', N'bar e spizzichi')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (6, N'01.05', N'Mensa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (7, N'01.06', N'Caffe')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (8, N'01.10', N'altri')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (9, N'02', N'Automezzi')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (10, N'02.01', N'Auto AUDI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (11, N'02.01.01', N'Benzina e/o gasolio')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (12, N'02.01.02', N'Gomme')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (13, N'02.01.03', N'Revisione e/o bollo')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (14, N'02.01.04', N'tagliando')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (15, N'02.01.05', N'Accessori')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (16, N'02.02', N'Auto Vitara Eug')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (17, N'02.02.01', N'Benzina e/o gasolio')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (18, N'02.02.02', N'Gomme')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (19, N'02.02.03', N'Revisione e/o bollo')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (20, N'02.02.04', N'tagliando')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (21, N'02.03', N'Moto BMW GS 1250')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (22, N'02.04', N'Moto No. 2')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (23, N'02.05', N'Bici No. 1')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (24, N'02.06', N'Bici No. 2')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (25, N'02.07', N'Autostrada Telepass')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (27, N'02.08', N'Noleggio Auto')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (28, N'03', N'Divertimenti')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (29, N'03.01', N'teatro')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (30, N'03.02', N'cinema')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (31, N'03.03', N'concerti')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (32, N'03.04', N'Musei')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (33, N'03.05', N'Libri')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (34, N'03.05.01', N'Acquisto Libri EBook Amazon Kindle')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (35, N'03.05.02', N'Acquisto Libri Ebook Kobo')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (36, N'03.05.03', N'Abbonamenti a Giornali')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (37, N'03.06', N'Acquisto Corsi Internet')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (38, N'03.07', N'Abbonamento TV')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (39, N'03.08', N'Associazioni Culturali')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (40, N'04', N'Viaggi e Vacanze')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (41, N'04.01', N'Costo biglietti, treno, aereo')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (42, N'04.02', N'Hotel')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (43, N'04.03', N'Noleggio mezzi')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (44, N'04.03.01', N'Noleggio auto')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (45, N'04.03.02', N'Noleggio bici')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (46, N'04.03.03', N'Bus Turistici')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (47, N'04.03.04', N'Noleggio Sci')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (48, N'04.03.05', N'Ski Pass')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (49, N'04.04', N'Viaggi Organizzati')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (50, N'04.04.01', N'Viaggi Organizzati')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (51, N'05', N'Finanza e Banche')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (52, N'05.01', N'SMAC Card')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (53, N'05.02', N'Assegni vari')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (54, N'05.03', N'Rendite, Stipendi e pensioni')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (55, N'05.03.01', N'Stipendi')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (56, N'05.03.02', N'Pensione')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (57, N'05.03.03', N'Affitti percepiti')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (58, N'05.03.04', N'Eredita')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (59, N'05.04', N'Giroconto fra banche')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (60, N'05.05', N'Donazioni Varie')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (61, N'05.10', N'Banca CaRisp')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (62, N'05.10.01', N'Interessi bancario Carisp')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (63, N'05.10.02', N'Comissione Carisp')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (64, N'05.10.03', N'Costo manutenzione conto Carisp')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (65, N'05.10.04', N'Investimenti Carisp')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (66, N'05.10.05', N'Trasferimenti denaro Carisp')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (67, N'05.10.06', N'Prelev. Bancomat Carisp')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (68, N'05.20', N'Banca BSI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (69, N'05.20.01', N'Interessi bancari BSI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (70, N'05.20.02', N'Comissioni BSI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (71, N'05.20.03', N'Costo manutenzione conto BSI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (72, N'05.20.04', N'Investimenti BSI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (73, N'05.20.05', N'Trasferimenti denaro BSI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (74, N'05.20.06', N'Prelev. Bancomat BSI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (75, N'05.30', N'BKN301 Cla')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (76, N'05.31', N'WISE Brussel')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (77, N'05.40', N'BKN301 Eug')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (78, N'05.32.50', N'Assicurazione Pancotti')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (79, N'05.32.60', N'Assicurazione Unipol')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (80, N'05.32.70', N'Assicurazione Zurich')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (81, N'05.32.80', N'Assicurazione Allianz')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (82, N'06', N'Famiglia')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (83, N'06.01', N'Alessandro')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (84, N'06.01.01', N'Riscatto Laurea')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (85, N'06.01.03', N'Regali Alessandro')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (86, N'06.02', N'Andrea')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (88, N'06.02.01', N'Regali Andrea')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (89, N'06.03', N'Lucia')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (90, N'06.04', N'Regali')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (91, N'06.06', N'Nonna')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (92, N'10', N'E-commerce')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (93, N'10.01', N'Amazon')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (94, N'10.01.01', N'Accrediti/Addebiti da amazon')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (95, N'10.01.02', N'Spese su Amazon')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (96, N'10.02', N'Alibaba')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (97, N'20', N'Salute e Medicina')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (98, N'20.01', N'Spese in farmacia')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (99, N'20.02', N'Spese presso specialisti medici')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (100, N'20.02.01', N'Dentista')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (101, N'20.02.02', N'Fisio terapista')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (102, N'20.02.03', N'Ottico')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (103, N'20.02.04', N'Amplifon,Otorino')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (104, N'20.02.05', N'Ospedali')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (105, N'20.03', N'Wellfare')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (106, N'20.03.01', N'Barbiere')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (107, N'20.03.02', N'Parucchiere')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (108, N'20.03.03', N'Estetista')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (109, N'20.03.04', N'Sport e/o palestra')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (110, N'20.10', N'Abbigliamento')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (111, N'20.10.10', N'Abbigliamento uomo')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (112, N'20.10.20', N'Abbigliamento donna')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (113, N'20.10.30', N'Abbigliamento bambino')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (114, N'20.15', N'Regali vari')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (115, N'20.20', N'Spese scuola e/o scolastiche')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (116, N'30', N'Casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (117, N'30.01', N'spese manutenzione casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (118, N'30.01.01', N'Idraulico per la casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (119, N'30.01.02', N'Manutenzione Caldaia')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (120, N'30.01.03', N'Spese GE.CO o geco')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (122, N'30.01.05', N'Elettricista per la casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (123, N'30.01.06', N'Imbianchino per la casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (124, N'30.01.07', N'Falegname per la casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (125, N'30.01.08', N'Architetto per la casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (126, N'30.01.10', N'Accessori X la casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (127, N'30.01.11', N'Televisione')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (128, N'30.01.12', N'Sanatoria')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (129, N'30.01.13', N'Avvocati X S. Michele')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (130, N'30.01.15', N'Giardinaggio')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (131, N'30.01.20', N'Rifacimenti della casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (132, N'30.01.99', N'Spese ausiliarie')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (133, N'30.02', N'Azienda dei Servizi')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (134, N'30.02.01', N'Luce')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (135, N'30.02.02', N'Acqua')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (136, N'30.02.03', N'Gas')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (137, N'30.03', N'Giardinaggio per la casa')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (138, N'30.04', N'telefono')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (139, N'30.05', N'foto voltaico')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (140, N'30.06', N'Uffici Stato')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (141, N'30.07', N'Internet')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (142, N'30.08', N'Software')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (143, N'30.50', N'Casa Dogana')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (144, N'30.50.01', N'Rendita casa dogana')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (145, N'30.50.02', N'spese casa dogana')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (146, N'30.50.10', N'Assic. Casa Dogana')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (147, N'30.51', N'Casa Nonna')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (148, N'30.51.10', N'Finanza')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (149, N'99', N'Spese Non Classificate')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (158, N'02.09', N'Multe')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (159, N'02.10', N'Accessori moto')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (160, N'02.11', N'parcheggi')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (161, N'02.02.05', N'Officine Vitara')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (162, N'02.01.06', N'Officina AUDI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (163, N'05.32', N'Assicurazioni')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (164, N'20.10.40', N'Regali abbigliamento ragazzi')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (165, N'06.02.02', N'trasf. denaro X Andrea')
GO
SET IDENTITY_INSERT dbo.CodiciStat OFF
GO
/****** Object:  Index uix_codstat    Script Date: 28/09/2026 16:01:41 ******/
CREATE UNIQUE NONCLUSTERED INDEX uix_codstat ON dbo.CodiciStat ( codstat ASC )
	WITH (
		PAD_INDEX = OFF, 
		STATISTICS_NORECOMPUTE = OFF, 
		SORT_IN_TEMPDB = OFF, 
		IGNORE_DUP_KEY = OFF, 
		DROP_EXISTING = OFF, 
		ONLINE = OFF, 
		ALLOW_ROW_LOCKS = ON, 
		ALLOW_PAGE_LOCKS = ON, 
		OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF
		) 
GO
/****** Object:  Table dbo.impFiles    Script Date: 28/09/2026 16:01:41 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.impFiles(
	id int IDENTITY(1,1) NOT NULL,
	filename varchar(128) NOT NULL,
	reldir varchar(128) NOT NULL,
	size int NULL,
	qtarecs int NULL,
	dtmin datetime NULL,
	dtmax datetime NULL,
	ultagg datetime NULL,
PRIMARY KEY CLUSTERED (	id ASC )
	WITH (
		PAD_INDEX = OFF, 
		STATISTICS_NORECOMPUTE = OFF, 
		IGNORE_DUP_KEY = OFF, 
		ALLOW_ROW_LOCKS = ON, 
		ALLOW_PAGE_LOCKS = ON, 
		OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) 
) 
GO
SET ANSI_PADDING ON
GO
SET ANSI_PADDING ON
GO

/****** Object:  View dbo.listaMovimenti    Script Date: 28/09/2026 16:01:41 ******/
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
	  ,cs.descrstat
	  ,0 as flag
  FROM movimenti mo
    left outer join causali ca
	  on mo.abicaus = ca.abicaus
	left outer join CodiciStat cs
	  on mo.idCodStat=cs.idCodStat
GO
/****** Object:  View dbo.MovimentiDoppi    Script Date: 28/09/2026 16:01:41 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create VIEW dbo.MovimentiDoppi
AS
WITH grup (
        conta
      ,dtmov
      ,dare
      ,avere
   )  as (
SELECT 
	count(*) as conta
      ,dtmov
      ,dare
      ,avere
  FROM listaMovimenti
  GROUP BY  
      dtmov
      ,dare
      ,avere
)
SELECT li.* 
   FROM dbo.listaMovimenti li
	FULL OUTER JOIN grup 
      ON li.dtmov = grup.dtmov
	  AND li.dare = grup.dare
      AND li.avere = grup.avere
	  
	WHERE 1=1
	  AND grup.conta > 1
	  AND ( grup.dare + grup.avere ) >= 0
-- ORDER BY dtmov, dare

GO

/****** Object:  Index UXImpFiles    Script Date: 28/09/2026 16:01:41 ******/
CREATE UNIQUE NONCLUSTERED INDEX UXImpFiles ON dbo.impFiles
(
	filename ASC,
	reldir ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
GO
SET ANSI_PADDING ON
GO
USE master
GO
ALTER DATABASE Banca SET  READ_WRITE 
GO
