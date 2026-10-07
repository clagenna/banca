/****** Oggetto: Table dbo.causali    Data dello script 07/10/2026 08:30:46 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE dbo.causali(
	abicaus varchar(32) NOT NULL,
	descrcaus nvarchar(256) NULL,
	costo int NULL,
 CONSTRAINT PK_causali PRIMARY KEY CLUSTERED 
(
	abicaus ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF)
)
GO

GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'0', N'Voci Generali', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'05', N'Prelev. Bancomat', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'13', N'Assegno', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'14', N'Acquisto Titoli BSI', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'16', N'Comissioni su pagamenti', 2)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'17', N'Assicurazione Bancaria', 2)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'18', N'Interessi Bancari', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'19', N'Ritenute', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'22', N'Diritti custodia Titoli', 2)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'26', N'Bonifico', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'27', N'Stipendio/pensione', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'34', N'Estinzioni conto previd.', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'43', N'Pagamento POS', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'45', N'Pagamento Carta Credito', 0)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'48', N'Versamento con Bonifico', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'5', N'Prelev. Bancomat', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'50', N'RID Rapporto Interbancario Diretto', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'66', N'Canoni vari', 2)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'68', N'Storni vari', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'83', N'Iscriz. Fondi', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'84', N'Rimborso Titoli', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'AMZ', N'Amazon', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'CO', N'Contante', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'PP', N'PayPal', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S1', N'SMAC - Pagamento con SMAC', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S2', N'SMAC - Pagamento con SMAC con Ricarica', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S3', N'SMAC - SMAC Fiscale', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S4', N'SMAC - Ricarica su SMAC', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S5', N'SMAC - Accredito su SMAC', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'S6', N'SMAC - Manca la decodificata ', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Y1', N'Anticipazioni su fatture Italia', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z1', N'Disposizioni di giro di cash pooling', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z2', N'Versamento di assegni bancari, assegni di conto corrente postale', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z3', N'Versamento di assegni circolari emessi da altre banche', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z4', N'Versamento di assegni postali non standardizzati', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z5', N'Versamento indiretto. Versamento di contante e/o assegni eseguito da soggetto diverso dal titolare del conto', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z6', N'Prelevamento eseguito da soggetto diverso dal titolare del conto', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z7', N'Accredito RID', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z8', N'Accredito MAV', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'Z9', N'Insoluto/storno RID', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZA', N'Insoluto MAV', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZB', N'Incasso certificati conformita?', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZC', N'Pagamento per fornitura elettrica', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZD', N'Pagamento per servizio telefonico', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZE', N'Pagamento per servizi acqua/gas', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZF', N'Pagamento per operazioni su prodotti derivati', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZG', N'Accredito per operazioni su prodotti derivati', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZH', N'Rimborso titoli e/o fondi comuni', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZI', N'Bonifico dall?estero', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZL', N'Bonifico sull?estero', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZM', N'Sconto effetti sull?estero', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZN', N'Negoziazione assegni sull?estero', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZP', N'Commissioni e spese su fideiussioni (Da utilizzare per operazioni estero e Italia)', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZQ', N'Commissioni e spese su crediti documentari', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZR', N'Penali', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZS', N'Erogazione prestiti personali e finanziamenti diversi', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZT', N'Pagamento/incasso bollettino bancario', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZU', N'Bonifico per previdenza complementare', 1)
GO
INSERT dbo.causali (abicaus, descrcaus, costo) VALUES (N'ZX', N'Bonifico oggetto di oneri deducibili o detrazioni di imposta', 1)
GO
