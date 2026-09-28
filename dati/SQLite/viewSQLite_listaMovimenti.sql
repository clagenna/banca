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
	            ON mo.idCodStat=cs.idCodStat