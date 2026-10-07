/****** Oggetto: View dbo.listaMovimenti    Data dello script 07/10/2026 08:30:46 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO





CREATE view listaMovimenti
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
    LEFT OUTER JOIN causali ca 
	  on mo.abicaus = ca.abicaus
	left outer join CodiciStat cs
	  on mo.idCodStat=cs.idCodStat
GO
