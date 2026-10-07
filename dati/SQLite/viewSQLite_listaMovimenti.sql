CREATE VIEW IF NOT EXIST listaMovimenti
AS
SELECT tipo
       ,id
       ,idfile
       ,dtmov
       ,dtval
       ,strftime('%Y.%m', mo.dtmov) AS movstr
       ,strftime('%Y.%m', mo.dtval) AS valstr
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
           LEFT OUTER JOIN causali ca 
                ON mo.abicaus = ca.abicaus
           left outer join CodiciStat cs
	            ON mo.idCodStat=cs.idCodStat