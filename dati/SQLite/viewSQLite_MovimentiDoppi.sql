WITH grup (
        conta
      ,tipo
      , dtmov
      , dare
      , avere
  ) AS (
    SELECT 
        count(*) AS conta
      ,tipo
      , dtmov
      , dare
      , avere
    FROM listaMovimenti
    GROUP BY  
      tipo
      ,dtmov
      , dare
      , avere
)
SELECT li.* 
FROM listaMovimenti li
    INNER JOIN grup 
      ON li.tipo = grup.tipo
      AND li.dtmov = grup.dtmov
        AND li.dare = grup.dare
        AND li.avere = grup.avere

WHERE 1=1
  AND grup.conta > 1
  AND (grup.dare + grup.avere) >= 0