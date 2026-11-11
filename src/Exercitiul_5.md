
### Exercițiul 5 – Paralelizarea algoritmului Dijkstra bidirecțional

**Pentru o paralelizare eficientă, ar trebui utilizate 2 thread-uri**, câte unul pentru fiecare direcție de căutare:

- **T1** – execută Dijkstra din nodul sursă spre destinație.
- **T2** – execută Dijkstra din nodul destinație spre sursă (pe graful transpus, dacă graful este orientat).

### Argumentație

1. **Fiecare căutare este în mare parte secvențială.** La fiecare pas se extrage nodul cu distanța minimă din coada de priorități, iar pasul următor depinde de rezultatele celui curent. Paralelizarea unei singure căutări ar introduce costuri suplimentare de sincronizare.

2. **Cele două căutări pot rula în paralel.** Fiecare thread are propria coadă de priorități și propriul vector de distanțe, evitând astfel competiția directă pentru modificarea acelorași structuri de date.

3. **Sincronizarea este relativ redusă.** Thread-urile trebuie să comunice pentru identificarea punctelor de întâlnire, actualizarea celei mai bune distanțe găsite (`μ`) și verificarea condiției de oprire:

   $$\min(Q_f) + \min(Q_b) \geq \mu$$

   unde `Q_f` și `Q_b` sunt cozile de priorități ale celor două căutări, iar `μ` reprezintă lungimea celui mai bun drum complet găsit până în acel moment. Condiția trebuie verificată folosind valori sincronizate corespunzător.

4. **Costul thread-urilor suplimentare:** Folosirea a mai mult de 2 thread-uri ar necesita paralelizarea internă a căutărilor, introducând overhead de sincronizare și competiție pentru resurse, fără a garanta un câștig de performanță.

### Concluzie

**Utilizarea a 2 thread-uri este alegerea cea mai potrivită pentru paralelizarea directă a algoritmului**, deoarece permite executarea simultană a celor două direcții, cu un cost relativ redus de sincronizare.

În condiții ideale, pentru aceeași cantitate de lucru, s-ar putea obține un speedup apropiat de 2× față de varianta bidirecțională secvențială. În practică, acesta poate fi mai mic din cauza sincronizării și a dezechilibrului de sarcină (*load imbalance*) dintre cele două căutări.
