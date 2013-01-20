4. Labos
========
Na generatoru koda rade globalne varijable, izrazi ( osim && i || ) i pozivanje funkcija bez parametar.

Rade ukupno otprilike 20 / 40 primjera sada. Možda i koji primjer manje... 

Sprut
------
Pokušavam uploadat na SPRUT ali kaže da nevalja. Any clues? Poslao sam sad mail asistentu...

generirani program:


        `BASE D
    start
        MOVE %H 40000, R7
        CALL GLOBAL_INITIALIZERS
        CALL GLOBAL_main
        HALT
    GLOBAL_main

        PUSH R5
        MOVE R7, R5
        MOVE 71, R1
        PUSH R1
        POP R6
        JR RET_FROM_main
    RET_FROM_main
        POP R5
        RET
    GLOBAL_INITIALIZERS
        RET

        
pogresan rezultat izvodjenja ili greska u izvodjenju:
ulazni podaci:

ocekivani izlaz:
71

dobiveni izlaz:


stderr:
        