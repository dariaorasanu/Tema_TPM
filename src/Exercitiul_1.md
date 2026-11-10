# Exercițiul 1 -- Linearizabilitate și consistență secvențială

Se consideră istoricul de execuție din enunț, cu valoarea inițială
`r = 0`.

Operațiile sunt: - Thread-ul **A** execută două operații `read()`,
ambele returnând valoarea `2`. - Thread-ul **B** execută `write(1)`,
apoi `write(2)`.

## 1. Istoricul este linearizabil?

**Nu, istoricul nu este linearizabil.**

Linearizabilitatea presupune că operațiile pot fi privite ca și cum s-ar
fi executat într-o ordine secvențială, iar această ordine trebuie să
respecte ordinea temporală a operațiilor care nu se suprapun.

Din diagramă se observă următoarele relații: 1. `write(1)` se termină
înainte să înceapă primul `read()`. 2. Primul `read()` se termină
înainte să înceapă `write(2)`. 3. `write(2)` se termină înainte să
înceapă al doilea `read()`.

Prin urmare, orice ordine liniarizabilă ar trebui să respecte:

``` text
write(1) → read() → write(2) → read()
```

După `write(1)`, valoarea registrului este `1`. Deoarece primul `read()`
începe după terminarea acestei scrieri și înainte de începerea lui
`write(2)`, el ar trebui să returneze `1`.

În istoric, primul `read()` returnează `2`. Nu există, așadar, o
poziționare a punctelor de linearizare care să respecte atât ordinea
temporală, cât și valorile returnate.

**Concluzie:** istoricul nu este linearizabil.

## 2. Istoricul este consistent secvențial?

**Da, istoricul este consistent secvențial.**

Consistența secvențială cere să existe o ordine secvențială a tuturor
operațiilor care: - respectă ordinea operațiilor din fiecare thread; -
produce valorile observate de operațiile `read()`.

Spre deosebire de linearizabilitate, consistența secvențială nu obligă
respectarea ordinii temporale dintre operațiile care aparțin unor
thread-uri diferite.

Putem considera următoarea ordine secvențială:

  Pas   Thread   Operație       Valoarea registrului
  ----- -------- -------------- ----------------------
  1     B        `write(1)`     1
  2     B        `write(2)`     2
  3     A        `read()` → 2   2
  4     A        `read()` → 2   2

Această ordine respectă ordinea operațiilor din thread-ul B (`write(1)`
înainte de `write(2)`) și din thread-ul A (primul `read()` înainte de al
doilea `read()`).

Ambele operații `read()` returnează `2`, exact ca în istoricul inițial.

**Concluzie:** istoricul este consistent secvențial, dar nu este
linearizabil.

## Concluzie finală

Istoricul dat **nu este linearizabil**, deoarece primul `read()` ar
trebui să observe valoarea `1`, dar returnează `2`. În schimb, istoricul
**este consistent secvențial**, deoarece putem construi o ordine
secvențială care respectă ordinea operațiilor din fiecare thread și
valorile returnate.
