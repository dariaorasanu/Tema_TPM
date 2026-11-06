
### Exercițiul 3.a – ShadyLock

### Pseudocod

```java
class ShadyLock {
    private volatile int turn;
    private volatile boolean used = false;

    public void lock() {
        int me = ThreadId.get();
        do {
            do {
                turn = me;
            } while (used);
            used = true;
        } while (turn != me);
    }

    public void unlock() {
        used = false;
    }
}
```

**Algoritmul nu este deadlock-free.**

Un algoritm este deadlock-free dacă, atunci când mai multe thread-uri încearcă să obțină lock-ul, cel puțin unul dintre ele reușește să progreseze. În cazul algoritmului `ShadyLock`, putem construi un contraexemplu cu două thread-uri care rămân blocate la infinit.

### Trace de execuție

Considerăm două thread-uri, **T1** și **T2**, cu ID-urile 1 și 2.

Inițial: `used = false`, `turn = 0`.

| Pas | Thread | Operație | Rezultat |
|---|---|---|---|
| 1 | T1 | `turn = me` | `turn = 1` |
| 2 | T1 | Verifică `while (used)` | `false`, continuă |
| 3 | T1 | `used = true` | `used = true` |
| 4 | T2 | `turn = me` | `turn = 2` |
| 5 | T2 | Verifică `while (used)` | `true`, rămâne în bucla interioară |
| 6 | T1 | Verifică `while (turn != me)` | `2 != 1`, reia bucla exterioară |
| 7 | T1 | `turn = me` | `turn = 1` |
| 8 | T1 | Verifică `while (used)` | `true`, rămâne în bucla interioară |

### Concluzie

În această execuție, T1 setează `used = true`, dar înainte să finalizeze metoda `lock()`, T2 modifică variabila `turn`.

Astfel, T1 este obligat să reia bucla exterioară, însă `used` rămâne `true`. Ambele thread-uri ajung să aștepte în bucla interioară, fără să poată termina metoda `lock()`.

Deoarece niciun thread nu poate ajunge să apeleze `unlock()` pentru a seta `used = false`, **niciunul nu mai poate progresa**.

Prin urmare, există o execuție concurentă în care niciun thread nu reușește să obțină lock-ul, deci **algoritmul nu este deadlock-free**.
