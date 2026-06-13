WITH grup (
        conta
      , dtmov
      , dare
      , avere
) AS (
    SELECT 
        count(*) AS conta
      , dtmov
      , dare
      , avere
    FROM listaMovimenti
    GROUP BY  
        dtmov
      , dare
      , avere
)
SELECT li.* 
FROM listaMovimenti li
    INNER JOIN grup 
        ON li.dtmov = grup.dtmov
        AND li.dare = grup.dare
        AND li.avere = grup.avere
WHERE grup.conta > 1
  AND (grup.dare + grup.avere) >= 0