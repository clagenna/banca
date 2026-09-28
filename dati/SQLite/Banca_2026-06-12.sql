--
-- File generato con SQLiteStudio v3.4.4 su mer giu 10 16:15:53 2026
--
-- Codifica del testo utilizzata: System
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Tabella: causali
DROP TABLE IF EXISTS causali;

CREATE TABLE IF NOT EXISTS causali (
    abicaus   VARCHAR (4)   PRIMARY KEY
                            NOT NULL,
    descrcaus VARCHAR (256) DEFAULT NULL,
    costo     INT           DEFAULT NULL
);


-- Tabella: codiciStat
DROP TABLE IF EXISTS codiciStat;

CREATE TABLE IF NOT EXISTS codiciStat (
    idCodStat INTEGER        PRIMARY KEY ASC,
    codstat   NVARCHAR (12)  NOT NULL,
    descrstat NVARCHAR (256) NOT NULL
);


-- Tabella: impFiles
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


-- Tabella: movimenti
DROP TABLE IF EXISTS movimenti;

CREATE TABLE IF NOT EXISTS movimenti (
    id        INTEGER        PRIMARY KEY ASC,
    tipo      NVARCHAR (32),
    idfile    INTEGER        DEFAULT NULL,
    dtmov                    DEFAULT NULL,
    dtval                    DEFAULT NULL,
    dare      FLOAT (19, 4)  DEFAULT NULL,
    avere     FLOAT (19, 4)  DEFAULT NULL,
    descr     NVARCHAR (512) DEFAULT NULL,
    abicaus   VARCHAR (20)   DEFAULT NULL,
    cardid    NVARCHAR (20)  DEFAULT NULL,
    idcodstat INTEGER        DEFAULT NULL
);


-- Indice: IX_Movim_dtMov
DROP INDEX IF EXISTS IX_Movim_dtMov;

CREATE INDEX IF NOT EXISTS IX_Movim_dtMov ON movimenti (
    tipo ASC,
    dtmov ASC
);


-- Indice: IX_Movim_idcodstat
DROP INDEX IF EXISTS IX_Movim_idcodstat;

CREATE INDEX IF NOT EXISTS IX_Movim_idcodstat ON movimenti (
    idcodstat ASC
);


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
           0 AS flag
      FROM movimenti mo
           LEFT OUTER JOIN
           causali ca ON mo.abicaus = ca.abicaus
           LEFT OUTER JOIN
           codiciStat cs ON mo.idCodStat = cs.idCodStat;


COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
