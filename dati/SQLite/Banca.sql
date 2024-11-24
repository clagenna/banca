--
-- File generato con SQLiteStudio v3.4.4 su ven nov 22 17:53:21 2024
--
-- Codifica del testo utilizzata: System
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Tabella: causali
CREATE TABLE IF NOT EXISTS causali (
    abicaus   VARCHAR (4)    PRIMARY KEY
                             NOT NULL,
    descrcaus NVARCHAR (256) 
                             DEFAULT NULL,
    costo     INT
                             DEFAULT NULL
);

INSERT causali (abicaus, descrcaus, costo) VALUES (N'0', N'Voci Generali', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'05', N'Prelev. Bancomat', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'13', N'Assegno', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'14', N'Acquisto Titoli BSI', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'16', N'Comissioni su pagamenti', 1);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'17', N'Assicurazione Bancaria', 1);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'18', N'Interessi Bancari', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'19', N'Ritenute', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'22', N'Diritti custodia Titoli', 1);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'26', N'Bonifico', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'27', N'Stipendio/pensione', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'34', N'Estinzioni conto previd.', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'43', N'Pagamento POS', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'45', N'Pagamento Carta Credito', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'48', N'Versamento con Bonifico', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'5', N'Prelev. Bancomat', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'50', N'RID Rapporto Interbancario Diretto', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'66', N'Canoni vari', 1);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'68', N'Storni vari', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'83', N'Iscriz. Fondi', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'84', N'Rimborso Titoli', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'S1', N'SMAC - Pagamento con SMAC', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'S2', N'SMAC - Pagamento con SMAC con Ricarica', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'S3', N'SMAC - SMAC Fiscale', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'S4', N'SMAC - Ricarica su SMAC', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'S5', N'SMAC - Accredito su SMAC', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'S6', N'SMAC - Manca la decodificata ', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Y1', N'Anticipazioni su fatture Italia', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Z1', N'Disposizioni di giro di cash pooling', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Z2', N'Versamento di assegni bancari, assegni di conto corrente postale', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Z3', N'Versamento di assegni circolari emessi da altre banche', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Z4', N'Versamento di assegni postali non standardizzati', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Z5', N'Versamento indiretto. Versamento di contante e/o assegni eseguito da soggetto diverso dal titolare del conto', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Z6', N'Prelevamento eseguito da soggetto diverso dal titolare del conto', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Z7', N'Accredito RID', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Z8', N'Accredito MAV', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'Z9', N'Insoluto/storno RID', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZA', N'Insoluto MAV', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZB', N'Incasso certificati conformita’', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZC', N'Pagamento per fornitura elettrica', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZD', N'Pagamento per servizio telefonico', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZE', N'Pagamento per servizi acqua/gas', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZF', N'Pagamento per operazioni su prodotti derivati', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZG', N'Accredito per operazioni su prodotti derivati', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZH', N'Rimborso titoli e/o fondi comuni', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZI', N'Bonifico dall’estero', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZL', N'Bonifico sull’estero', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZM', N'Sconto effetti sull’estero', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZN', N'Negoziazione assegni sull’estero', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZP', N'Commissioni e spese su fideiussioni (Da utilizzare per operazioni estero e Italia)', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZQ', N'Commissioni e spese su crediti documentari', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZR', N'Penali', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZS', N'Erogazione prestiti personali e finanziamenti diversi', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZT', N'Pagamento/incasso bollettino bancario', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZU', N'Bonifico per previdenza complementare', 0);
INSERT causali (abicaus, descrcaus, costo) VALUES (N'ZX', N'Bonifico oggetto di oneri deducibili o detrazioni di imposta', 0);


-- Tabella: movimentiBSI
CREATE TABLE IF NOT EXISTS movimentiBSI (
    dtmov                  DEFAULT NULL,
    dtval                  DEFAULT NULL,
    dare    FLOAT (19, 4)  DEFAULT NULL,
    avere   FLOAT (19, 4)  DEFAULT NULL,
    descr   NVARCHAR (512) DEFAULT NULL,
    abicaus VARCHAR (20)   DEFAULT NULL,
    cardid  NVARCHAR (20)  DEFAULT NULL
);


-- Tabella: movimentiBSICredit
CREATE TABLE IF NOT EXISTS movimentiBSICredit (
    dtmov                  DEFAULT NULL,
    dtval                  DEFAULT NULL,
    dare    FLOAT (19, 4)  DEFAULT NULL,
    avere   FLOAT (19, 4)  DEFAULT NULL,
    descr   NVARCHAR (512) DEFAULT NULL,
    abicaus VARCHAR (20)   DEFAULT NULL,
    cardid  NVARCHAR (20)  DEFAULT NULL
);


-- Tabella: movimentiCarisp
CREATE TABLE IF NOT EXISTS movimentiCarisp (
    dtmov                  DEFAULT NULL,
    dtval                  DEFAULT NULL,
    dare    FLOAT (19, 4)  DEFAULT NULL,
    avere   FLOAT (19, 4)  DEFAULT NULL,
    descr   NVARCHAR (512) DEFAULT NULL,
    abicaus VARCHAR (20)   DEFAULT NULL,
    cardid  NVARCHAR (20)  DEFAULT NULL
);


-- Tabella: movimentiCarispCredit
CREATE TABLE IF NOT EXISTS movimentiCarispCredit (
    dtmov                  DEFAULT NULL,
    dtval                  DEFAULT NULL,
    dare    FLOAT (19, 4)  DEFAULT NULL,
    avere   FLOAT (19, 4)  DEFAULT NULL,
    descr   NVARCHAR (512) DEFAULT NULL,
    abicaus NVARCHAR (20)  DEFAULT NULL,
    cardid  NVARCHAR (20)  DEFAULT NULL
);
								   

-- Tabella: movimentiWise
CREATE TABLE IF NOT EXISTS movimentiWise (
    dtmov                  DEFAULT NULL,
    dtval                  DEFAULT NULL,
    dare    FLOAT (19, 4)  DEFAULT NULL,
    avere   FLOAT (19, 4)  DEFAULT NULL,
    descr   NVARCHAR (512) DEFAULT NULL,
    abicaus NVARCHAR (20)  DEFAULT NULL,
    cardid  NVARCHAR (20)  DEFAULT NULL
);


-- Tabella: movimentiContanti
CREATE TABLE IF NOT EXISTS movimentiContanti (
    id      INTEGER        UNIQUE ON CONFLICT ROLLBACK,
    dtmov                  DEFAULT NULL,
    dtval                  DEFAULT NULL,
    dare    FLOAT (19, 4)  DEFAULT NULL,
    avere   FLOAT (19, 4)  DEFAULT NULL,
    descr   NVARCHAR (512) DEFAULT NULL,
    abicaus VARCHAR (20)   DEFAULT NULL,
    cardid  NVARCHAR (20)  DEFAULT NULL
);


-- Tabella: movimentiSmac
CREATE TABLE IF NOT EXISTS movimentiSmac (
    id      INTEGER        UNIQUE ON CONFLICT ROLLBACK,
    dtmov                  DEFAULT NULL,
    dtval                  DEFAULT NULL,
    dare    FLOAT (19, 4)  DEFAULT NULL,
    avere   FLOAT (19, 4)  DEFAULT NULL,
    descr   NVARCHAR (512) DEFAULT NULL,
    abicaus VARCHAR (20)   DEFAULT NULL,
    cardid  NVARCHAR (20)  DEFAULT NULL
);


-- Indice: indxMovCarCred
CREATE INDEX IF NOT EXISTS indxMovCarCred ON movimentiCarispCredit (
    dtmov,
    dtval
);


-- Vista: listaMovimentiBSI
CREATE VIEW IF NOT EXISTS listaMovimentiBSI AS
    SELECT 'BSI' AS tipo,
           dtmov,
           dtval,
           strftime('%Y.%m', mo.dtmov) AS movstr,
           strftime('%Y.%m', mo.dtval) AS valstr,
           dare,
           avere/* ,CASE */,
           cardid,
           descr,
           mo.abicaus,
           ca.descrcaus,
           ca.costo
      FROM movimentiBSI mo
           LEFT OUTER JOIN
           causali ca ON mo.abicaus = ca.abicaus;
-- WHEN CHARINDEX('84806', descr) > 0 THEN 'Claudio'-- WHEN CHARINDEX('85928', descr) > 0 THEN 'Eugenia'-- ELSE null-- END as chipaga

-- Vista: listaMovimentiBSICredit
CREATE VIEW IF NOT EXISTS listaMovimentiBSICredit AS
    SELECT 'BSIcrd' AS tipo,
           dtmov,
           dtval,
           strftime('%Y.%m', mo.dtmov) AS movstr,
           strftime('%Y.%m', mo.dtval) AS valstr,
           dare,
           avere/* ,CASE */,
           cardid,
           descr,
           mo.abicaus,
           ca.descrcaus,
           ca.costo
      FROM movimentiBSICredit mo
           LEFT OUTER JOIN
           causali ca ON mo.abicaus = ca.abicaus;
-- WHEN CHARINDEX('84806', descr) > 0 THEN 'Claudio'-- WHEN CHARINDEX('85928', descr) > 0 THEN 'Eugenia'-- ELSE null-- END as chipaga

-- Vista: listaMovimentiCARISP
CREATE VIEW IF NOT EXISTS listaMovimentiCARISP AS
    SELECT 'CARISP' AS tipo,
           dtmov,
           dtval,
           strftime('%Y.%m', mo.dtmov) AS movstr,
           strftime('%Y.%m', mo.dtval) AS valstr,
           dare,
           avere,
           cardid,
           descr,
           mo.abicaus,
           ca.descrcaus,
           ca.costo
      FROM movimentiCarisp mo
           LEFT OUTER JOIN
           causali ca ON mo.abicaus = ca.abicaus;


-- Vista: listaMovimentiCARISPCredit
CREATE VIEW IF NOT EXISTS listaMovimentiCARISPCredit AS
    SELECT 'CARISPcrd' AS tipo,
           dtmov,
           dtval,
           strftime('%Y.%m', mo.dtmov) AS movstr,
           strftime('%Y.%m', mo.dtval) AS valstr,
           dare,
           avere,
           cardid,
           descr,
           mo.abicaus,
           ca.descrcaus,
           ca.costo
      FROM movimentiCarispCredit mo
           LEFT OUTER JOIN
           causali ca ON mo.abicaus = ca.abicaus;
												
-- Vista: listaMovimentiWise
CREATE VIEW IF NOT EXISTS listaMovimentiWise AS
    SELECT 'wise' AS tipo,
           dtmov,
           dtval,
           strftime('%Y.%m', mo.dtmov) AS movstr,
           strftime('%Y.%m', mo.dtval) AS valstr,
           dare,
           avere,
           cardid,
           descr,
           mo.abicaus,
           ca.descrcaus,
           ca.costo
      FROM movimentiWise mo
           LEFT OUTER JOIN
           causali ca ON mo.abicaus = ca.abicaus;


-- Vista: ListaMovimentiContanti
CREATE VIEW IF NOT EXISTS ListaMovimentiContanti AS
    SELECT 'Cont' AS tipo,
           dtmov,
           dtval,
           strftime('%Y.%m', mo.dtmov) AS movstr,
           strftime('%Y.%m', mo.dtval) AS valstr,
           dare,
           avere,
           cardid,
           descr,
           mo.abicaus,
           ca.descrcaus,
           ca.costo
      FROM movimentiContanti mo
           LEFT OUTER JOIN
           causali ca ON mo.abicaus = ca.abicaus;


-- Vista: ListaMovimentiSmac
CREATE VIEW IF NOT EXISTS ListaMovimentiSmac AS
    SELECT 'SMAC' AS tipo,
           dtmov,
           dtval,
           strftime('%Y.%m', mo.dtmov) AS movstr,
           strftime('%Y.%m', mo.dtval) AS valstr,
           dare,
           avere,
           cardid,
           descr,
           mo.abicaus,
           ca.descrcaus,
           ca.costo
      FROM movimentiSmac mo
           LEFT OUTER JOIN
           causali ca ON mo.abicaus = ca.abicaus;


-- Vista: ListaMovimentiUNION
CREATE VIEW IF NOT EXISTS ListaMovimentiUNION AS
    SELECT tipo,
           dtmov,
           dtval,
           movstr,
           valstr,
           dare,
           avere,
           cardid,
           descr,
           abicaus,
           descrcaus,
           costo
      FROM listaMovimentiCarisp
    UNION
    SELECT tipo,
           dtmov,
           dtval,
           movstr,
           valstr,
           dare,
           avere,
           cardid,
           descr,
           abicaus,
           descrcaus,
           costo
      FROM listaMovimentiBSI
    UNION
    SELECT tipo,
           dtmov,
           dtval,
           movstr,
           valstr,
           dare,
           avere,
           cardid,
           descr,
           abicaus,
           descrcaus,
           costo
      FROM listaMovimentiBSICredit
    UNION
    SELECT tipo,
           dtmov,
           dtval,
           movstr,
           valstr,
           dare,
           avere,
           cardid,
           descr,
           abicaus,
           descrcaus,
           costo
      FROM listaMovimentiCarispCredit
    UNION
    SELECT tipo,
           dtmov,
           dtval,
           movstr,
           valstr,
           dare,
           avere,
           cardid,
           descr,
           abicaus,
           descrcaus,
           costo
      FROM listaMovimentiWise
    UNION
    SELECT tipo,
           dtmov,
           dtval,
           movstr,
           valstr,
           dare,
           avere,
           cardid,
           descr,
           abicaus,
           descrcaus,
           costo
      FROM listaMovimentiContanti
    UNION
    SELECT tipo,
           dtmov,
           dtval,
           movstr,
           valstr,
           dare,
           avere,
           cardid,
           descr,
           abicaus,
           descrcaus,
           costo
      FROM listaMovimentiSmac;



COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
