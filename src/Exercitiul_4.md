# Exercițiul 4 -- Implementarea algoritmilor Anderson și CLH

## 1. Cerință

Implementarea lock-urilor Anderson și CLH pentru protejarea unui contor partajat, incrementat până la `120000`, cu 4, 8 și 12 thread-uri. Pentru fiecare configurație se fac 5 rulări și se raportează timpul mediu.

## 2. Algoritmii

**Anderson** este un lock bazat pe un tablou de sloturi. Fiecare thread primește un slot printr-un contor atomic (`getAndIncrement`) și așteaptă ca `flags[slot]` să devină activ. La `unlock()` dezactivează slotul propriu și activează slotul următor. Capacitatea tabloului este egală cu numărul de thread-uri.

**CLH** este o coadă implicită de noduri. Fiecare thread își creează un nod cu `locked = true`, îl pune la coadă cu `getAndSet` pe `tail` și așteaptă pe câmpul `locked` al nodului predecesorului. La `unlock()` setează `locked = false` pe nodul propriu și reutilizează nodul predecesorului.

În ambele cazuri, fiecare thread așteaptă pe o variabilă diferită, ceea ce în teorie reduce traficul de coerență a cache-ului față de un spin lock simplu.

## 3. Implementare și rulare

Codul este în `LockBenchmark.java` (`AndersonLock`, `CLHLock`, contor partajat, 4/8/12 thread-uri, 5 rulări pe configurație). Măsurarea pornește după ce toate thread-urile sunt create (`CountDownLatch`), iar la final se verifică dacă valoarea contorului este exact `120000`, altfel programul aruncă o excepție.

```bash
javac LockBenchmark.java
java LockBenchmark
```

Programul a fost executat **de 6 ori** (6 execuții complete, fiecare cu 5 rulări pe configurație, deci 30 de rulări pe configurație), pentru a vedea cât variază rezultatele de la o execuție la alta. Toate valorile brute sunt în `rezultate_brute.txt`.

## 4. Sistemul de test

| Componentă | Specificație |
|---|---|
| CPU | Intel Core i5-12450H (8 nuclee: 4 P-core cu Hyper-Threading + 4 E-core, 12 thread-uri logice) |
| RAM | 16 GB DDR5 |
| Sistem de operare | Windows 11 |
| JDK | JDK 25 |

## 5. Rezultate

### 5.1 Prima execuție (5 rulări pe configurație, valori în ms)

| Algoritm | Thread-uri | Rulare 1 | Rulare 2 | Rulare 3 | Rulare 4 | Rulare 5 | Media |
|---|---:|---:|---:|---:|---:|---:|---:|
| Anderson | 4 | 41,212 | 41,081 | 34,202 | 28,329 | 30,753 | 35,116 |
| CLH | 4 | 40,345 | 30,989 | 30,579 | 27,394 | 32,738 | 32,409 |
| Anderson | 8 | 38,136 | 48,714 | 33,828 | 47,452 | 52,119 | 44,049 |
| CLH | 8 | 41,491 | 28,215 | 26,940 | 30,611 | 30,203 | 31,492 |
| Anderson | 12 | 252,464 | 157,670 | 101,910 | 52,325 | 49,737 | 122,821 |
| CLH | 12 | 39,507 | 44,801 | 597,565 | 57,629 | 41,868 | 156,274 |

### 5.2 Media celor 5 rulări în fiecare din cele 6 execuții (ms)

| Execuția | Anderson 4 | CLH 4 | Anderson 8 | CLH 8 | Anderson 12 | CLH 12 |
|---|---:|---:|---:|---:|---:|---:|
| 1 | 35,116 | 32,409 | 44,049 | 31,492 | 122,821 | 156,274 |
| 2 | 33,394 | 26,315 | 36,933 | 33,782 | 311,488 | 88,386 |
| 3 | 37,192 | 31,915 | 51,829 | 37,846 | 264,393 | 58,804 |
| 4 | 35,257 | 30,264 | 43,251 | 34,499 | 99,546 | 90,081 |
| 5 | 35,100 | 29,748 | 50,456 | 38,796 | 63,605 | 57,795 |
| 6 | 34,517 | 28,800 | 46,219 | 35,399 | 303,238 | 68,784 |

### 5.3 Sinteză pe cele 30 de rulări per configurație (ms)

| Configurație | Media | Mediana | Minim | Maxim | CLH mai rapid în (din 6 execuții) |
|---|---:|---:|---:|---:|---:|
| Anderson, 4 thread-uri | 35,1 | 34,1 | 27,5 | 48,9 | |
| CLH, 4 thread-uri | 29,9 | 28,8 | 20,5 | 40,3 | 6 |
| Anderson, 8 thread-uri | 45,5 | 45,2 | 32,1 | 66,4 | |
| CLH, 8 thread-uri | 35,3 | 34,4 | 26,9 | 49,9 | 6 |
| Anderson, 12 thread-uri | 194,2 | 83,3 | 42,3 | 1075,3 | |
| CLH, 12 thread-uri | 86,7 | 51,4 | 31,8 | 597,6 | 5 |

## 6. Analiza rezultatelor

**4 thread-uri.** CLH este mai rapid decât Anderson în toate cele 6 execuții: 29,9 ms față de 35,1 ms în medie (cu aproximativ 15%), iar mediana urmează aceeași ordine (28,8 ms față de 34,1 ms).

**8 thread-uri.** CLH este din nou mai rapid în toate cele 6 execuții: 35,3 ms față de 45,5 ms (cu aproximativ 22%). Anderson se degradează mai mult decât CLH când crește numărul de thread-uri (de la 4 la 8: +30% pentru Anderson, +18% pentru CLH).

**12 thread-uri.** Ambii algoritmi au timpi mult mai mari și foarte variabili. Procesorul are exact 12 thread-uri logice, iar thread-urile benchmark-ului concurează cu sistemul de operare. Media pe 30 de rulări este 194,2 ms pentru Anderson și 86,7 ms pentru CLH, iar mediile pe execuții dau CLH câștigător în 5 din 6 cazuri. Anderson are rulări de peste 200 ms de mai multe ori, cu maxim 1075 ms. CLH are un maxim de 598 ms. Mediana (83,3 ms față de 51,4 ms) arată aceeași ordine, dar diferența se micșorează, deci o mare parte din media Anderson vine din valori extreme. Prima execuție (Anderson 122,8 ms față de CLH 156,3 ms) este singura în care ordinea se inversează și este dominată de o singură rulare CLH de 598 ms.

**Prima rulare dintr-un grup este mai lentă.** La 4 thread-uri, rularea 1 este cea mai lentă la Anderson în toate cele 6 execuții și printre cele mai lente la CLH. Este compatibil cu încălzirea JIT-ului (cod compilat din mers), dar nu am măsurat-o separat.

**Explicații posibile** (ipoteze, fără profiling):

- *False sharing la Anderson.* Tabloul `AtomicIntegerArray` are 4–12 elemente de câte 4 octeți, deci încape într-o singură linie de cache de 64 de octeți. Toate thread-urile care așteaptă citesc aceeași linie, iar fiecare `unlock()` o invalidează la toți. Asta anulează avantajul unei variabile separate pe thread. La CLH, fiecare thread așteaptă pe un nod propriu, probabil alocat în memorie separată. Ipoteza se potrivește cu degradarea mai mare a lui Anderson între 4 și 8 thread-uri.
- *Supraabonare la 12 thread-uri.* Cu spin lock-uri, dacă planificatorul preemptează thread-ul care urmează la rând, ceilalți așteaptă activ fără să progreseze. Ambii algoritmi predau lock-ul într-o ordine strictă (FIFO), deci ambii sunt afectați. Nu pot explica pe baza codului de ce Anderson ar fi afectat mai tare.
- *Procesor hibrid.* P-core și E-core au performanțe diferite, deci timpii depind de nucleul pe care ajunge fiecare thread.

**Limitări.** Rulările nu sunt independente (aceeași mașină, aceeași execuție JVM în grupul de 5), nu s-a făcut încălzire explicită a JIT-ului, thread-urile nu au fost fixate pe nuclee, iar sistemul de operare a rulat și alte procese în paralel.

## 7. Concluzie

**CLH este mai eficient pe acest sistem.** Este mai rapid la 4 thread-uri (media 29,9 ms față de 35,1 ms) și la 8 thread-uri (35,3 ms față de 45,5 ms) în toate cele 6 execuții, iar la 12 thread-uri în 5 din 6 execuții, cu media și mediana pe cele 30 de rulări mai mici decât ale lui Anderson. Explicația cea mai probabilă este că așteptarea pe noduri separate evită false sharing-ul care afectează tabloul compact al lui Anderson. Aceasta rămâne o ipoteză neverificată prin profiling.

La 12 thread-uri variația este foarte mare pentru ambii algoritmi, deci acolo avantajul lui CLH este mai puțin sigur decât la 4 și 8 thread-uri.