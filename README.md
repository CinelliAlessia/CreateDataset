- In Start viene chiamata la funzione `main` che si occupa di inizializzare il dataset e popolarlo.
- In `popolateDataset()`:
  - viene letto il file `VersionInfo` e tradotto per avere una lista di Versioni, a ciascuna versione i propri cambi.
  - Vengono associati a ciascuna versione i commit relativi (effettuati nella versione precedente).
  - Inizia un ciclo for che effettua il checkout di ciascuna versione.
- Per ogni versione vengono calcolate le LOC.

Tool per 