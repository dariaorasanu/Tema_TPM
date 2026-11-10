# Exercițiul 5 -- Paralelizarea algoritmului Dijkstra bidirecțional

## Răspuns

**Două thread-uri**: unul pentru căutarea înainte (din sursă) și unul pentru căutarea înapoi (din destinație).

## Argumentare

Dijkstra bidirecțional rulează două căutări care au structuri de date separate: fiecare are propria coadă de priorități și propriul tablou de distanțe. Cele două căutări pot avansa independent, deci se mapează natural pe două thread-uri.

Sincronizarea necesară este mică, dar nu nulă:

- **Cel mai bun cost găsit până acum, `μ`**, este partajat. Când o căutare relaxează o muchie către un nod deja atins de cealaltă căutare, actualizează `μ = min(μ, d_f(u) + w(u,v) + d_b(v))`. Se poate face cu o variabilă atomică.
- **Condiția de oprire**, `top_f + top_b >= μ`, citește vârfurile ambelor cozi, deci cere un mic schimb de informație între thread-uri (valori citite cu `volatile` sau atomic).

Mai mult de două thread-uri nu aduc câștig semnificativ:

- Dijkstra este inerent secvențial în interiorul unei căutări: următorul nod extras depinde de rezultatul relaxărilor anterioare. Paralelizarea unei singure căutări cere o coadă de priorități partajată sau o partiționare a nodurilor, cu contenție și sincronizare frecventă.
- Costul acestei sincronizări poate depăși câștigul, mai ales pe grafuri mici sau medii.

## Concluzie

Numărul eficient este 2, câte unul pe direcție, cu `μ` și condiția de oprire ca singurele date partajate.