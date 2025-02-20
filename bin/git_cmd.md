## Merge del branch sul main
Se mi sono dimenticato di _commit_-are le ultime modifiche può succedere di questo:

	C:\proj>git checkout main
	error: Your local changes to the following files would be overwritten by checkout:
			src/main/java/sm/clagenna/banca/dati/ConvertCsv2RigaBanca.java
	Please commit your changes or stash them before you switch branches.
	Aborting
in questo caso posso fare il _commit_ sul l'ultimo 
### Commit degli ultimi files

	git add *
	C:\proj>git commit --amend --no-edit
	[uniquetb 955fe77] Corretto alcuni eventi
	Date: Thu Feb 20 15:45:54 2025 +0100
	12 files changed, 125 insertions(+), 2274 deletions(-)
	rename amzn_cols.properties => AAamzn_cols.properties (100%)
	delete mode 100644 dati/AtecoValues.properties
	create mode 100644 src/main/resources/amzn_cols.properties
a questo punto il repository locale è up-to-date e lo posso _push_-are

	C:\proj>git push nascasa uniquetb -f
	Enumerating objects: 41, done.
	Counting objects: 100% (41/41), done.
	Delta compression using up to 12 threads
	Compressing objects: 100% (18/18), done.
	Writing objects: 100% (21/21), 2.33 KiB | 398.00 KiB/s, done.
	Total 21 (delta 13), reused 0 (delta 0), pack-reused 0 (from 0)
	To ssh://nascasa/volume1/git/banca
	+ 0e78239...955fe77 uniquetb -> uniquetb (forced update)
	
### Il repository è up-to-date
Dopo varie modifiche del mio **branch** devo avere lo **status** su:

	nothing to commit
a questo punto posso fare il merge del mio branch attuale ( `impfile`  ) su `main`  con la seguente procedura

	robocopy /MIR . ..\banca_ORIG	
	git checkout impfile
	git pull nascasa
	git checkout main
	git pull nascasa main
	git merge --no-ff --no-commit impfile
a questo punto verifico che il mio progetto sia `wndcompare` 	uguale al `..\banca_ORIG` per cui posso fare:

	git commit -m "Eseguito il merge sul main di impfile"
	git push nascasa
	git push github
e les jeux sont fait!
	
	
	