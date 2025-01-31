## Merge del branch sul main
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
	
	
	