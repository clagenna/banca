--
-- File generato con SQLiteStudio v3.4.4 su ven set 11 17:39:09 2026
--
-- Codifica del testo utilizzata: System
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Tabella: causali
DROP TABLE IF EXISTS causali;

CREATE TABLE IF NOT EXISTS causali (
    abicaus   TEXT,
    descrcaus TEXT,
    costo     INTEGER
);


-- Tabella: CodiciStat
DROP TABLE IF EXISTS CodiciStat;

CREATE TABLE IF NOT EXISTS CodiciStat (
    idCodStat INTEGER,
    codstat   TEXT,
    descrstat TEXT
);


-- Tabella: impFiles
DROP TABLE IF EXISTS impFiles;

CREATE TABLE IF NOT EXISTS impFiles (
    id       INTEGER,
    filename TEXT,
    reldir   TEXT,
    size     INTEGER,
    qtarecs  INTEGER,
    dtmin    TEXT,
    dtmax    TEXT,
    ultagg   TEXT
);


-- Tabella: movimenti
DROP TABLE IF EXISTS movimenti;

CREATE TABLE IF NOT EXISTS movimenti (
    id        INTEGER,
    tipo      TEXT,
    idfile    INTEGER,
    dtmov     TEXT,
    dtval     TEXT,
    dare      REAL,
    avere     REAL,
    descr     TEXT,
    abicaus   TEXT,
    cardid    TEXT,
    idCodStat INTEGER
);


-- Indice: inxmovimenti
DROP INDEX IF EXISTS inxmovimenti;

CREATE INDEX IF NOT EXISTS inxmovimenti ON movimenti (
    dtmov
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
           0 AS flag
      FROM movimenti mo
           LEFT OUTER JOIN
           causali ca ON mo.abicaus = ca.abicaus
           LEFT OUTER JOIN
           codiciStat cs ON mo.idCodStat = cs.idCodStat;


-- Vista: movimentiDoppi
DROP VIEW IF EXISTS movimentiDoppi;
CREATE VIEW IF NOT EXISTS movimentiDoppi AS
WITH grup (
        conta,
        dtmov,
        dare,
        avere
    )
    AS (
        SELECT count( * ) AS conta,
               dtmov,
               dare,
               avere
          FROM listaMovimenti
         GROUP BY dtmov,
                  dare,
                  avere
    )
    SELECT li.*
      FROM listaMovimenti li
           INNER JOIN
           grup ON li.dtmov = grup.dtmov AND 
                   li.dare = grup.dare AND 
                   li.avere = grup.avere
     WHERE grup.conta > 1 AND 
           (grup.dare + grup.avere) >= 0;


COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
