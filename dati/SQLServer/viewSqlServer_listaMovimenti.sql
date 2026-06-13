
CREATE VIEW [dbo].[MovimentiDoppi]
AS
WITH grup (
        conta
      ,dtmov
      ,dare
      ,avere
   )  as (
SELECT 
	count(*) as conta
      ,dtmov
      ,dare
      ,avere
  FROM listaMovimenti
  GROUP BY  
      dtmov
      ,dare
      ,avere
)
SELECT li.* 
   FROM dbo.listaMovimenti li
	FULL OUTER JOIN grup 
      ON li.dtmov = grup.dtmov
	  AND li.dare = grup.dare
      AND li.avere = grup.avere
	  
	WHERE 1=1
	  AND grup.conta > 1
	  AND ( grup.dare + grup.avere ) >= 0
-- ORDER BY dtmov, dare

GO


