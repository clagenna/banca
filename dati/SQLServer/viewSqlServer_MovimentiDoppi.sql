/****** Oggetto: View dbo.MovimentiDoppi    Data dello script 07/10/2026 08:30:46 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW dbo.MovimentiDoppi
AS
WITH grup (
        conta
      ,tipo
      ,dtmov
      ,dare
      ,avere
   )  as (
SELECT 
	count(*) as conta
      ,tipo
      ,dtmov
      ,dare
      ,avere
  FROM listaMovimenti
  GROUP BY 
      tipo
      ,dtmov
      ,dare
      ,avere
)
SELECT li.* 
   FROM dbo.listaMovimenti li
	FULL OUTER JOIN grup 
      ON li.tipo = grup.tipo
      AND li.dtmov = grup.dtmov
	  AND li.dare = grup.dare
      AND li.avere = grup.avere
	  
	WHERE 1=1
	  AND grup.conta > 1
	  AND ( grup.dare + grup.avere ) >= 0
-- ORDER BY dtmov, dare

