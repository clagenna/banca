
select 'INSERT INTO causali (abicaus, descrcaus, costo) VALUES (' +
	' ''' + abicaus + ''',' +
	' ''' + descrcaus + ''', ' +
	  cast( costo as varchar(12)) + ')'
from causali


