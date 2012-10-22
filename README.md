PPJ
===

PPJ

# WHATS GOING ON
Skoro sve je napravljeno / napisan kod osim ovog InputProcessora i RegDefResolver koji nisu dovrseni.

Treba milijun puta testirati sve sada.

ja sam nesto isprobavao i popravljao pa je stanje sada ovakvo:
- eNFA radi sam za sebe dobro
- RegexToAutomaton iz rucnih testova u tekstualnom obliku radi nakon popravaka
- tandem eNFA+RegexToAutomaton ne radi u kombinaciji zbog epsilon prijelaza. Mora se popraviti `$` i `""` i uskladiti da radi s jednim od tih.
Bolje je koristiti `""` jer nece smetati za literal (escaped) `$` znak. Trivijalni fix je definirati EPSILON="", i to mi na prvi pogled izgleda u redu, ali moram 
jos to pogledati
- do lexer nisam imao priliku isprobavati. Ili prvo treba popraviti ove dvije klase kaj sam napisao gore, ili rucno ali to nije bas korisno

# WAT DO
Svi neka smisljaju nacine za testirati i testirajte dio po dio. Mozete rucno, mozete napisat main, mozete kako hocete, mozete pisati JUnit testove.
I onda napisite sto ste pokrenuli i sto nije radilo i sto ste provjerili i tako dalje.

Ja cu sam ovo gore sa eNFA+RegexToAutomaton kopati.





Opaska: za drugi labos moram bolje organizirat podjelu posla i organizaciju.
