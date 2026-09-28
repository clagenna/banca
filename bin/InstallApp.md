1) andare al link "https://drive.google.com/drive/folders/1NCVlJXbmU0oubqFB79kE-WmkMv0fXm2q?usp=sharing" 
2) e scaricare Banca_App.zip 
3) Scegliere sul disco dove installare l'applicazione "Banca"
	es: c:\winapp\  ( deve esistere! )
4) Spostare Banca_App.zip su c:\winapp\
5) Estrarre tutto lo ZIP  (sul direttorio proposto c:\winapp\Banca_App )
6) Aprire una finestra DOS *autoritativa* (di admin)
	Ctrl + Esc
	digitare "cmd" --> "Prompt dei comandi"
	cliccare su "Esegui come amministratore"
	confermare di voler aprire una shell DOS admin
7) nella finestra DOS compare il prompt 
	C:\Windows\System32>
8) digitare:
	C:\Windows\System32>cd /d "c:\WinApp\Banca_App"
                            ---------------------------
9) Se non e' presente/installato PowerShell 7.0 allora parte la sua installazione
La pagina mostra il seguente output
---------------------------------------------------------------------------------------------------
c:\WinApp\Banca_App>installApp.cmd
INFORMAZIONI: impossibile trovare file corrispondenti ai
criteri di ricerca indicati.
.
Installo POWERSHELL perche' non presente!
.
Found PowerShell [Microsoft.PowerShell] Version 7.5.4.0
This application is licensed to you by its owner.
Microsoft is not responsible for, nor does it grant any licenses to, third-party packages.
Downloading https://github.com/PowerShell/PowerShell/releases/download/v7.5.4/PowerShell-7.5.4-win-x64.msi
  ¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦¦   107 MB /  107 MB
Successfully verified installer hash
Starting package install...
Successfully installed
SecurityError: 
	File C:\WinApp\Banca_App\installApp.ps1 
		cannot be loaded. 
		The file C:\WinApp\Banca_App\installApp.ps1 is not digitally signed. 
		You cannot run this script on the current system. 
		For more information about running scripts and setting execution policy, 
		see about_Execution_Policies at https://go.microsoft.com/fwlink/?LinkID=135170.
Fatto !!!
Premere un tasto per continuare . . .
---------------------------------------------------------------------------------------------------
10) Occorre dare a PowerShell i permessi di eseguire scripts PS1, 
    digitare i seguenti comendi dentro PowerShell (pwsh.exe)


C:\WinApp\Banca_App> pwsh.exe
                     --------
PS C:\WinApp\Banca_App> Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy Unrestricted
			Unblock-File .\installApp.ps1
                        -------------------------------------------------
11) A questo punto rilanciamo l'istallazione con:

	C:\WinApp\Banca_App>installApp.cmd
        	            --------------
12) Dovrebbe comparire una DialogBox che chiede dove volete installare la vera applicazion "Banca", 
     digitate dopo la scritta Cartella: C:\WinApp
                                        ---------
     Click su : Selezione Cartella

     e il processo di istallazione continua.
13) Compare la raccomandazione di creare le variabili d'ambiente corrette

---------------------------------------------------------------------------------------------------
Ho creato i direttori temporanei per il JDK in:
  C:\Temp\java\jdk-25.0.2
  C:\Temp\java\javafx-sdk-25.0.2

ATTENZIONE!!!
-------------
Questi andranno spostati *A MANO* nel direttorio:
  C:\Program Files\java    !!!
Di modo che la struttura sia:
  C:\Program Files\java\jdk-25.0.2
  C:\Program Files\java\javafx-sdk-25.0.2
Inoltre dovrai dare *A MANO* i seguenti comandi da amministratore:

setx JAVA_HOME "C:\Program Files\java\jdk-25.0.2" /M
setx JAVAFX_HOME "C:\Program Files\java\javafx-sdk-25.0.2" /M
setx PATH "%PATH%;%JAVA_HOME%\bin" /M	
---------------------------------------------------------------------------------------------------

Eseguite le istruzioni sopra, a questo punto il programma è pronto!

lanciare sotto la cartella "C:\WinApp\Banca" il comando Banca.cmd e il programma dovrebbe partire


