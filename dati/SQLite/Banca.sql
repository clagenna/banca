--
-- File generato con SQLiteStudio v3.4.4 su ven nov 22 17:53:21 2024
--
-- Codifica del testo utilizzata: System
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;



-- Tabella: causali
CREATE TABLE IF NOT EXISTS causali (
    abicaus   VARCHAR (4)    PRIMARY KEY  NOT NULL,
    descrcaus VARCHAR (256)  DEFAULT NULL,
    costo     INT            DEFAULT NULL
);

-- Tabella: CodStat
CREATE TABLE IF NOT EXISTS CodiciStat (
    codstat   VARCHAR (12)   PRIMARY KEY  NOT NULL,
    descrstat VARCHAR (256)  DEFAULT NOT NULL,
);

-- Tabella: impfiles
CREATE TABLE impFiles (
	id      INTEGER PRIMARY KEY ASC,
	filename  	VARCHAR(128) NOT NULL,
	reldir  	VARCHAR(128) NOT NULL,
	size 		int DEFAULT NULL,
	qtarecs		int DEFAULT NULL,
	dtmin 		    DEFAULT NULL,
	dtmax 		    DEFAULT NULL,
	ultagg 		    DEFAULT NULL
);

CREATE UNIQUE INDEX UXImpFiles ON impFiles( filename, reldir );

-- Dati causali ABI generali
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('0', 'Voci Generali', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('05', 'Prelev. Bancomat', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('13', 'Assegno', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('14', 'Acquisto Titoli BSI', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('16', 'Comissioni su pagamenti', 1);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('17', 'Assicurazione Bancaria', 1);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('18', 'Interessi Bancari', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('19', 'Ritenute', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('22', 'Diritti custodia Titoli', 1);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('26', 'Bonifico', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('27', 'Stipendio/pensione', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('34', 'Estinzioni conto previd.', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('43', 'Pagamento POS', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('45', 'Pagamento Carta Credito', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('48', 'Versamento con Bonifico', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('5', 'Prelev. Bancomat', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('50', 'RID Rapporto Interbancario Diretto', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('66', 'Canoni vari', 1);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('68', 'Storni vari', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('83', 'Iscriz. Fondi', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('84', 'Rimborso Titoli', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('PP', 'PayPal', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('CO', 'Contante', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('S1', 'SMAC - Pagamento con SMAC', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('S2', 'SMAC - Pagamento con SMAC con Ricarica', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('S3', 'SMAC - SMAC Fiscale', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('S4', 'SMAC - Ricarica su SMAC', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('S5', 'SMAC - Accredito su SMAC', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('S6', 'SMAC - Manca la decodificata ', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Y1', 'Anticipazioni su fatture Italia', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Z1', 'Disposizioni di giro di cash pooling', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Z2', 'Versamento di assegni bancari, assegni di conto corrente postale', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Z3', 'Versamento di assegni circolari emessi da altre banche', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Z4', 'Versamento di assegni postali non standardizzati', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Z5', 'Versamento indiretto. Versamento di contante e/o assegni eseguito da soggetto diverso dal titolare del conto', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Z6', 'Prelevamento eseguito da soggetto diverso dal titolare del conto', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Z7', 'Accredito RID', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Z8', 'Accredito MAV', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('Z9', 'Insoluto/storno RID', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZA', 'Insoluto MAV', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZB', 'Incasso certificati conformita''', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZC', 'Pagamento per fornitura elettrica', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZD', 'Pagamento per servizio telefonico', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZE', 'Pagamento per servizi acqua/gas', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZF', 'Pagamento per operazioni su prodotti derivati', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZG', 'Accredito per operazioni su prodotti derivati', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZH', 'Rimborso titoli e/o fondi comuni', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZI', 'Bonifico dall''estero', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZL', 'Bonifico sull''estero', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZM', 'Sconto effetti sull''estero', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZN', 'Negoziazione assegni sull''estero', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZP', 'Commissioni e spese su fideiussioni (Da utilizzare per operazioni estero e Italia)', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZQ', 'Commissioni e spese su crediti documentari', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZR', 'Penali', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZS', 'Erogazione prestiti personali e finanziamenti diversi', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZT', 'Pagamento/incasso bollettino bancario', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZU', 'Bonifico per previdenza complementare', 0);
INSERT INTO causali (abicaus, descrcaus, costo) VALUES ('ZX', 'Bonifico oggetto di oneri deducibili o detrazioni di imposta', 0);


-- Tabella: movimenti
CREATE TABLE IF NOT EXISTS movimenti (
    id      INTEGER PRIMARY KEY ASC,
    tipo    NVARCHAR (32),
    idfile  INTEGER DEFAULT NULL,
    dtmov                  DEFAULT NULL,
    dtval                  DEFAULT NULL,
    dare    FLOAT (19, 4)  DEFAULT NULL,
    avere   FLOAT (19, 4)  DEFAULT NULL,
    descr   NVARCHAR (512) DEFAULT NULL,
    abicaus VARCHAR (20)   DEFAULT NULL,
    cardid  NVARCHAR (20)  DEFAULT NULL,
    codstat VARCHAR (20)   DEFAULT NULL
);
-- Index movimenti tipo,dtmov
CREATE  INDEX IF NOT EXISTS IX_Movim_dtMov ON movimenti 
( 
	tipo ASC, 
	dtmov ASC 
);

-- Index movimenti tipo,dtmov
CREATE  INDEX IF NOT EXISTS IX_Movim_codstat ON movimenti 
( 
	codstat ASC
);

-- Vista: listaMovimenti
CREATE VIEW IF NOT EXISTS listaMovimenti AS
    SELECT id,
           tipo,
           idfile,
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
           ca.costo,
	   codstat
      FROM movimenti mo
        LEFT OUTER JOIN causali ca 
           ON mo.abicaus = ca.abicaus;


COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
