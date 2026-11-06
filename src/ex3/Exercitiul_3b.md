
### Exercițiul 3.b – VeryShadyLock

**Pseudocod:**

```java
class VeryShadyLock {
    private Lock lock;
    private volatile int x, y = 0;

    public void lock() {
        int me = ThreadId.get();
        x = me;
        while (y != 0) {};
        y = me;
        if (x != me) {
            lock.lock();
        }
    }

    public void unlock() {
        y = 0;
        lock.unlock();
    }
}
```

**Algoritmul nu garantează fairness.**

Un algoritm fair ar trebui să asigure accesul echitabil al thread-urilor la secțiunea critică, fără ca unul să fie favorizat în mod repetat față de celelalte.

Deși lock-ul interior este starvation-free, algoritmul `VeryShadyLock` nu garantează fairness, deoarece thread-urile pot rămâne în bucla `while (y != 0)` fără să ajungă să apeleze lock-ul interior.

### Trace de execuție

Considerăm două thread-uri, **T1** și **T2**, cu ID-urile 1 și 2.

Inițial: `x = 0`, `y = 0`.

| Pas | Thread | Operație | Rezultat |
|---|---|---|---|
| 1 | T1 | `x = me` | `x = 1` |
| 2 | T1 | Verifică `while (y != 0)` | `y = 0`, continuă |
| 3 | T1 | `y = me` | `y = 1` |
| 4 | T1 | Verifică `if (x != me)` | `1 != 1` este fals, intră în secțiunea critică |
| 5 | T2 | `x = me` | `x = 2` |
| 6 | T2 | Verifică `while (y != 0)` | `y = 1`, rămâne în buclă |
| 7 | T1 | `unlock()` | `y = 0`, lock-ul interior este eliberat fără efect |
| 8 | T1 | `x = me` | `x = 1` (încearcă din nou) |
| 9 | T1 | Verifică `while (y != 0)` | `y = 0`, continuă |
| 10 | T1 | `y = me` | `y = 1` |
| 11 | T1 | Verifică `if (x != me)` | `1 != 1` este fals, intră din nou în secțiunea critică |
| 12 | T2 | Verifică `while (y != 0)` | `y = 1`, continuă să aștepte |

### Concluzie

În această execuție, T1 reușește să intre de două ori în secțiunea critică, în timp ce T2 continuă să aștepte.

Dacă această ordine de execuție se repetă, T1 poate obține accesul de oricâte ori, iar T2 poate rămâne blocat în bucla `while (y != 0)`, fără să observe momentele în care `y = 0`.

Faptul că lock-ul interior este starvation-free nu rezolvă problema, deoarece T2 nici măcar nu ajunge să apeleze `lock.lock()`.

Prin urmare, **algoritmul VeryShadyLock nu garantează fairness**, deoarece permite ca un thread să acceseze secțiunea critică în mod repetat, în timp ce altul continuă să aștepte.
