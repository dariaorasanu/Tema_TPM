# Exercițiul 3.c -- ShadyChoice

## Pseudocod

```java
class ShadyChoice {
    private volatile boolean getWhite = false;
    private volatile int last = 0;

    public string choose() {
        int me = ThreadId.get();
        last = me;                 // (1)
        if (getWhite)              // (2)
            return "white";
        getWhite = true;           // (3)
        if (last == me)            // (4)
            return "red";
        else
            return "black";
    }
}
```

Se cere: maximum un thread obține `red` și maximum `n - 1` thread-uri obțin `white`.
Fiecare dintre cele `n` thread-uri apelează `choose()` o singură dată.

## Notații și ipoteze

Variabilele `getWhite` și `last` sunt `volatile`, deci toate accesările lor (citiri și scrieri) formează o ordine totală `<`, consistentă cu ordinea din program a fiecărui thread (modelul de memorie Java).

Pentru un thread `T`, notăm:

- `w_T(last)` și `r_T(last)`: scrierea (1) și citirea (4) a lui `last`;
- `r_T(gW)`: citirea (2) a lui `getWhite`;
- `w_T(gW)`: scrierea (3) a lui `getWhite`.

Fapte folosite:

- `getWhite` pornește de la `false`, iar singura valoare scrisă vreodată este `true`. Deci o citire returnează `true` doar dacă o scriere `w_X(gW)` a avut loc înaintea ei.
- Fiecare thread scrie `last` o singură dată, cu propriul id (id-uri unice).
- Un thread `T` citește propriul id din `last` doar dacă `w_T(last)` este ultima scriere a lui `last` înaintea lui `r_T(last)`.

## 1. Maximum un thread obține `red`

Un thread returnează `red` doar dacă a citit `false` la (2) și a citit propriul id la (4).

Presupunem prin absurd că două thread-uri distincte, `A` și `B`, returnează ambele `red`. Fără a pierde generalitatea, `w_A(gW) < w_B(gW)`.

`B` a citit `false` la (2), deci citirea lui a avut loc înaintea primei scrieri de `true`, adică înaintea lui `w_A(gW)`. Din ordinea din program rezultă:

```text
w_B(last) < r_B(gW) < w_A(gW) < r_A(last)          (*)
```

Analizăm ordinea scrierilor în `last`:

**Cazul 1: `w_B(last) < w_A(last)`.**
Din ordinea din program a lui `A` și din ipoteza `w_A(gW) < w_B(gW)` avem:

```text
w_A(last) < w_A(gW) < w_B(gW) < r_B(last)
```

Deci `w_B(last) < w_A(last) < r_B(last)`. Între scrierea lui `B` și citirea lui `B` apare scrierea lui `A`, deci `B` nu poate citi propriul id la (4). Contradicție cu faptul că `B` returnează `red`.

**Cazul 2: `w_A(last) < w_B(last)`.**
Din (*) avem `w_B(last) < r_A(last)`, deci `w_A(last) < w_B(last) < r_A(last)`. Între scrierea lui `A` și citirea lui `A` apare scrierea lui `B`, deci `A` nu poate citi propriul id la (4). Contradicție cu faptul că `A` returnează `red`.

Ambele cazuri duc la contradicție, deci **maximum un thread returnează `red`**.

## 2. Maximum `n - 1` thread-uri obțin `white`

Un thread returnează `white` doar dacă la (2) citește `true`, adică dacă o scriere `w_X(gW)` a avut loc înaintea citirii sale.

Fie `T` thread-ul a cărui citire `r_T(gW)` este prima în ordinea totală a citirilor lui `getWhite`. Presupunem prin absurd că `T` citește `true`. Atunci există un thread `X` cu `w_X(gW) < r_T(gW)`. Dar `r_X(gW) < w_X(gW)` din ordinea din program, deci `r_X(gW) < r_T(gW)`, ceea ce contrazice alegerea lui `T` ca prima citire. Rezultă că `T` citește `false` și nu returnează `white`.

Așadar cel puțin un thread nu returnează `white`, deci **maximum `n - 1` thread-uri returnează `white`**.

## Trace demonstrativ

Două thread-uri, T1 (id 1) și T2 (id 2). Inițial `getWhite = false`, `last = 0`.

| Pas | Thread | Operație | Rezultat |
|---|---|---|---|
| 1 | T1 | `last = 1` | `last = 1` |
| 2 | T1 | citește `getWhite` | `false`, continuă |
| 3 | T2 | `last = 2` | `last = 2` |
| 4 | T2 | citește `getWhite` | `false`, continuă |
| 5 | T1 | `getWhite = true` | `true` |
| 6 | T1 | `last == 1` ? | `2 == 1` fals, returnează `black` |
| 7 | T2 | `getWhite = true` | `true` |
| 8 | T2 | `last == 2` ? | adevărat, returnează `red` |

Aici apar un `red` și un `black`, iar niciun `white`. Dacă T1 execută tot `choose()` înainte ca T2 să înceapă, T1 returnează `red`, iar T2 `white`: un `red` și `n - 1 = 1` de `white`, deci limitele sunt atinse.

## Concluzie

`ShadyChoice` returnează cel mult un `red` (argument pe ordinea evenimentelor volatile, cazurile 1 și 2) și cel mult `n - 1` valori `white` (primul thread care citește `getWhite` vede `false`).