/****** Oggetto: Table dbo.CodiciStat    Data dello script 07/10/2026 08:30:46 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.CodiciStat(
	idCodStat int IDENTITY(1,1) NOT NULL,
	codstat varchar(12) NOT NULL,
	descrstat varchar(256) NOT NULL,
 CONSTRAINT PK_CodiciStat PRIMARY KEY CLUSTERED 
(
	idCodStat ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF)
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
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (166, N'06.01.04', N'Trasf. denaro X Alessandro')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (167, N'30.52', N'Baba')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (168, N'30.52.01', N'Bandante Baba')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (169, N'30.52.02', N'Casa Baba')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (170, N'05.20.07', N'Utilizzo carta di credito BSI')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (171, N'05.10.07', N'Utilizzo carta di credito Carisp')
GO
INSERT dbo.CodiciStat (idCodStat, codstat, descrstat) VALUES (172, N'10.03', N'Temu')
GO
SET IDENTITY_INSERT dbo.CodiciStat OFF
GO
SET ANSI_PADDING ON
GO
/****** Oggetto: Index uix_codstat    Data dello script 07/10/2026 08:30:46 ******/
CREATE UNIQUE NONCLUSTERED INDEX uix_codstat ON dbo.CodiciStat
(
	codstat ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF)
GO
