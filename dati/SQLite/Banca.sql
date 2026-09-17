--
-- File generato con SQLiteStudio v3.4.4 su gio feb 26 15:09:46 2026
--
-- Codifica del testo utilizzata: System
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Tabella: causali XXXXXXXXXXXXXXX
DROP TABLE IF EXISTS causali;

CREATE TABLE IF NOT EXISTS causali (
    abicaus   VARCHAR (4)   PRIMARY KEY
                            NOT NULL,
    descrcaus VARCHAR (256) DEFAULT NULL,
    costo     INT           DEFAULT NULL
);


-- Tabella: impFiles XXXXXXXXXXXXXXXXXXXXX
DROP TABLE IF EXISTS impFiles;

CREATE TABLE IF NOT EXISTS impFiles (
    id       INTEGER       PRIMARY KEY ASC,
    filename VARCHAR (128) NOT NULL,
    reldir   VARCHAR (128) NOT NULL,
    size     INT           DEFAULT NULL,
    qtarecs  INT           DEFAULT NULL,
    dtmin                  DEFAULT NULL,
    dtmax                  DEFAULT NULL,
    ultagg                 DEFAULT NULL
);


-- Tabella: movimenti  XXXXXXXXXXXXXXXXXXXXX
DROP TABLE IF EXISTS movimenti;

CREATE TABLE IF NOT EXISTS movimenti (
    id      INTEGER        PRIMARY KEY ASC,
    tipo    NVARCHAR (32),
    idfile  INTEGER        DEFAULT NULL,
    dtmov                  DEFAULT NULL,
    dtval                  DEFAULT NULL,
    dare    FLOAT (19, 4)  DEFAULT NULL,
    avere   FLOAT (19, 4)  DEFAULT NULL,
    descr   NVARCHAR (512) DEFAULT NULL,
    abicaus VARCHAR (20)   DEFAULT NULL,
    cardid  NVARCHAR (20)  DEFAULT NULL,
    idcodstat INTEGER      DEFAULT NULL
);


-- Indice: IX_Movim_codstat
DROP INDEX IF EXISTS IX_Movim_idcodstat;

CREATE INDEX IF NOT EXISTS IX_Movim_idcodstat ON movimenti ( idcodstat ASC );
-- Indice: IX_Movim_dtMov
DROP INDEX IF EXISTS IX_Movim_dtMov;

CREATE INDEX IF NOT EXISTS IX_Movim_dtMov ON movimenti (
    tipo ASC,
    dtmov ASC
);


-- Tabella: codiciStat  XXXXXXXXXXXXXXXXXXXXX
CREATE TABLE IF NOT EXISTS codiciStat (
    idCodStat   INTEGER       PRIMARY KEY ASC, 
	codstat     NVARCHAR(12)  NOT NULL,
	descrstat   NVARCHAR(256) NOT NULL
);  
-- IDENTITY(1) NOT NULL, --> 
-- Questa viene espletata dalla ROWID della tabella codiciStat


-- Indice: UXImpFiles
DROP INDEX IF EXISTS UXImpFiles;

CREATE UNIQUE INDEX IF NOT EXISTS UXImpFiles ON impFiles (
    filename,
    reldir
);


-- Vista: listaMovimenti
DROP VIEW IF EXISTS listaMovimenti;
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
           mo.idcodstat,
           cs.codstat,
           cs.descrstat,
           0 as flag
      FROM movimenti mo
           LEFT OUTER JOIN causali ca 
                ON mo.abicaus = ca.abicaus
           left outer join codiciStat cs
	            ON mo.idCodStat=cs.idCodStat;


COMMIT TRANSACTION;
PRAGMA foreign_keys = on;


BEGIN TRANSACTION;

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

COMMIT TRANSACTION;

BEGIN TRANSACTION;
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '01', 'Alimentare');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '01.01', 'Spese alimentari (titancoop, Baguette, etc..)');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '01.02', 'cene e ristoranti');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '01.03', 'asporto');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '01.04', 'bar e spizzichi');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '01.05', 'Mensa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '01.06', 'Caffe');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '01.10', 'altri');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02', 'Automezzi');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.01', 'Auto AUDI');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.01.01', 'Benzina e/o gasolio');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.01.03', 'Revisione e/o bollo');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.01.04', 'tagliando');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.01.05', 'Accessori');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.02', 'Auto Vitara Eug');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.02.01', 'Benzina e/o gasolio');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.02.03', 'Revisione e/o bollo');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.02.04', 'tagliando');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.03', 'Moto');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.07', 'Autostrada');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '02.08', 'Noleggio Auto');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03', 'Divertimenti');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.01', 'teatro');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.02', 'cinema');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.03', 'concerti');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.04', 'Musei');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.05', 'Libri');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.05.01', 'Acquisto Libri Amazon Kindle');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.05.02', 'Acquisto Libri Kobo');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.05.03', 'Abbonamenti a Giornali');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.06', 'Corsi Internet');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.07', 'Abbonamento TV');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '03.08', 'Associazioni Culturali');

INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04', 'Viaggi e Vacanze');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.01', 'Costo biglietti, treno, aereo');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.02', 'Hotel');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.03', 'Noleggio mezzi');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.03.01', 'Noleggio auto');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.03.02', 'Noleggio bici');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.03.03', 'Bus Turistici');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.03.04', 'Noleggio Sci');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.03.05', 'Ski Pass');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.04', 'Viaggi Organizzati');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '04.04.01', 'Viaggi Organizzati');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05', 'Finanza e Banche');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.01', 'SMAC Card');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.02', 'Assegni vari');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.03', 'Rendite, Stipendi e pensioni');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.03.01', 'Stipendi');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.03.02', 'Pensione');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.03.03', 'Affitti percepiti');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.03.04', 'Eredita');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.04', 'Giroconto fra banche');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.05', 'Donazioni Varie');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.10', 'Banca CaRisp');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.10.01', 'Interessi bancario Carisp');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.10.02', 'Comissione Carisp');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.10.03', 'Costo manutenzione conto Carisp');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.10.04', 'Investimenti Carisp');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.10.05', 'Trasferimenti denaro Carisp');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.10.06', 'Prelev. Bancomat Carisp');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.20', 'Banca BSI');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.20.01', 'Interessi bancari BSI');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.20.02', 'Comissioni BSI');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.20.03', 'Costo manutenzione conto BSI');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.20.04', 'Investimenti BSI');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.20.05', 'Trasferimenti denaro BSI');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.20.06', 'Prelev. Bancomat BSI');

INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.30', 'BKN301');

INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.31', 'WISE Brussel');

INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.50', 'Assicurazione Pancotti');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.60', 'Assicurazione Unipol');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.70', 'Assicurazione Zurich');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '05.80', 'Assicurazione Allianz');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '06', 'Famiglia');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '06.01', 'Alessandro');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '06.01', 'Andrea');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '06.03', 'Lucia');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '10', 'E-commerce');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '10.01', 'Amazon');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '10.01.01', 'Accrediti/Addebiti da amazon');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '10.01.02', 'Spese su Amazon');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '10.02', 'Alibaba');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20', 'Salute e Medicina');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.01', 'Spese in farmacia');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.02', 'Spese presso specialisti medici');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.02.01', 'Dentista');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.02.02', 'Fisio terapista');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.02.03', 'Ottico');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.02.05', 'Ospedali');

INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.03', 'Wellfare');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.03.01', 'Barbiere');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.03.02', 'Parucchiere');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.03.03', 'Estetista');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.03.04', 'Sport e/o palestra');

INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.10', 'Abbigliamento');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.10.10', 'Abbigliamento uomo');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.10.20', 'Abbigliamento donna');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.10.30', 'Abbigliamento bambino');

INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.15', 'Regali vari');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '20.20', 'Spese scuola e/o scolastiche');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30', 'Casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01', 'spese manutenzione casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.01', 'Idraulico per la casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.02', 'Manutenzione Caldaia');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.03', 'Spese GE.CO o geco');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.04', 'Foto voltaico');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.05', 'Elettricista per la casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.06', 'Imbianchino per la casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.07', 'Falegname per la casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.08', 'Architetto per la casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.10', 'Accessori X la casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.11', 'Televisione');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.12', 'Sanatoria');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.13', 'Avvocati X S. Michele');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.15', 'Giardinaggio');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.20', 'Rifacimenti della casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.01.99', 'Spese ausiliarie');

INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.02', 'Azienda dei Servizi');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.02.01', 'Luce');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.02.02', 'Acqua');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.02.03', 'Gas');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.03', 'Giardinaggio per la casa');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.04', 'telefono');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.06', 'Uffici Stato');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.07', 'Internet');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '30.08', 'Software');
INSERT INTO CodiciStat (codstat, descrstat) VALUES ( '99', 'Spese Non Classificate');

COMMIT TRANSACTION;