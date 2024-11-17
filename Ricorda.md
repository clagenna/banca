# Start dell'applicativo
Nei parametri di debug va messo :

	--module-path "c:\Program Files\Java\javafx-sdk-21.0.5\lib" 
	--add-modules javafx.swing,javafx.graphics,javafx.fxml,javafx.media,javafx.web
# Registrazione estratti conto con applicativo "Banca"
##Criteri dei nomi files CSV
I files CSV devono sottostare ad un criterio di nomina specifico per poter trasmettere informazioni **non** contenute nei file stessi. 

I files devono avere la seguente conformazione:

	PPPPP_BBBBB[_Credit]_AAMM[GG][_CCC].csv

dove:

**PPPP**	è il prefisso file contemplato nelle (vedi sotto) "Opzioni" -> "Filtro"

**BBBB**	e' il nome della banca di riferimento (BSI, Carisp, Wise, Etc)

**Credit**	se si tratta di estratto conto della carta di credito di quella banca

**AAMM**	e' la data finale dei l'ultimo movimento formato Anno-Mese[Giono]
**CCCC**	e' il _Card Holder_ identificativo (3 lettere) di chi appartienne il file CSV. Nel caso di conto cointestato si ommette. Nel caso di carta di credito invece ha senso specificarlo.

Per cui un esempio di nomi di files estratti conti potrebbe essere:

	estrattoconto_BSI_2112.csv
	estrattoconto_BSI_2212.csv
	estrattoconto_BSI_2312.csv
	estrattoconto_BSI_2410.csv
	estrattoconto_BSI_Credit 2024-11_cla.csv
	estrattoconto_CARISP_2012.csv
	estrattoconto_CARISP_2112.csv
	estrattoconto_CARISP_2212.csv
	estrattoconto_CARISP_2312.csv
	estrattoconto_CARISP_2410.csv
	estrattoconto_Carisp_Credit 2024-11_eug.csv
	estrattoconto_TPay 2021-06-26_cla.csv
	estrattoconto_TPay 2023-05-19_cla.csv
	estrattoconto_TPay 2023-06-26_cla.csv
	estrattoconto_TPay 2023-07-19_cla.csv
	estrattoconto_TPay 2024-11-29_cla.csv
	estrattoconto_wise-2023-11-05_cla.csv
	estrattoconto_wise-2024-11-14_cla.csv
	estrattoconto_wise_2024-01-01_2024-07-11_cla.csv



## Opzioni
### Server Data Base
Qui si sceglie con quale DB si intende salvare le registrazioni
1. SQLite
2. SQL Server
3. Sybase

Per adesso sono supportati solo SQLite e SQL Server

**SQLite**

Per SQLite è sufficiente secificare il path completo del file di DB.

**SQL Server**

Per la connessione a SQL Server occorre specificare:
- il nome DB
- Il nome del Host del server SQL
- il port del Server (default: 1433)
- lo username e Password per la conessione SQL

con il pulsante "Salva" si salvano le info sul file di _properties_ "Banca.properties"
### Opzioni
Il check "Sovrascrivi le registrazioni" serve per sapere se in fase di memorizzazione su DB devo ricoprire on no le registrazioni che hanno le stesse info in base ai criteri di ricerca specificati nella sezione "Filtro"

Con i  _combo_  "Quantita di thread" posso specificare quanti thread paralleli dedicare all'analisi dei files di estratto conto. Per l'elaborazione parallela molto dipende dalla CPU con cui gira il programma.

Il _Text Box_ Filtro files mi permette si identificare i file CSV grazie ai loro prefissi nei nomi. Nel caso nostro abbiamo prefissato i valori di "estra" e "wise" per identificare i files "estra*.csv" e "wise*.csv"
### Filtro 
Qui specifichiamo quali campi delle registrazioni devono partecipare durante la fase di memorizzazione su DB per l'identificazione di ugualianza. 

Ergo, se ho "data Movimento" spuntata allora verifico se la registrazione che provviene dal CSV ha la stessa  "data Movimento" dei record nel DB. Ovviamente meno campi specifico e più record di DB verranno cancellati a fronte i un record CSV. Di norma i criteri di uguaglianza dovrebbero includere *tutti* i campi.
