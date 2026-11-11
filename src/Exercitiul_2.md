# Exercițiul 2 -- Probleme în pseudocodul MyThread

## Codul din enunț

``` java
public class MyThread extends Thread {
    SharedObject sharedObject;
    SharedLock sharedLock;
    boolean alive = true;

    public void MyThread(SharedObject sharedObject, SharedLock sharedLock) {
        this.sharedObject = sharedObject;
        this.sharedLock = sharedLock;
    }

    public void run() {
        while (alive) {
            try {
                sharedObject.someNonExclusiveMethod();
                sharedLock.lock();
                sharedObject.someExclusiveMethod();
            } finally {
                sharedLock.unlock();
            }
        }
    }
}
```

## Probleme identificate

### 1. Constructorul este declarat greșit

În Java, un constructor nu are tip de retur, nici măcar `void`.

În codul dat, metoda:

``` java
public void MyThread(SharedObject sharedObject, SharedLock sharedLock)
```

este interpretată ca o metodă obișnuită numită `MyThread`, nu ca un
constructor. Constructorul corect trebuie declarat astfel:

``` java
public MyThread(SharedObject sharedObject, SharedLock sharedLock)
```

### 2. `unlock()` poate fi apelat fără ca thread-ul să fi obținut lock-ul

Blocul `try` începe înainte de apelul `lock()`, iar `finally` se execută
indiferent dacă în blocul `try` apare sau nu o excepție.

De exemplu, dacă `someNonExclusiveMethod()` aruncă o excepție, execuția
ajunge în `finally`, unde se apelează `unlock()`, deși thread-ul nu a
apelat încă `lock()`.

De asemenea, dacă apelul `lock()` nu reușește și aruncă o excepție,
`finally` va apela totuși `unlock()`.

Soluția este ca blocul `try-finally` să fie plasat imediat după
obținerea cu succes a lock-ului:

``` java
sharedLock.lock();
try {
    sharedObject.someExclusiveMethod();
} finally {
    sharedLock.unlock();
}
```

Astfel, `unlock()` este apelat numai după ce apelul `lock()` s-a
terminat cu succes.

### 3. Variabila `alive` nu asigură vizibilitatea între thread-uri

Variabila `alive` poate fi modificată de un alt thread pentru a opri
bucla. Fiind un câmp obișnuit, modificările nu sunt garantat vizibile
imediat pentru thread-ul care execută `run()`.

O soluție simplă este declararea ei ca `volatile`:

``` java
volatile boolean alive = true;
```

Astfel, citirile și scrierile asupra variabilei volatile beneficiază de
garanțiile de vizibilitate ale modelului de memorie Java.

## Variantă corectată

``` java
public class MyThread extends Thread {
    private final SharedObject sharedObject;
    private final SharedLock sharedLock;
    private volatile boolean alive = true;

    public MyThread(SharedObject sharedObject, SharedLock sharedLock) {
        this.sharedObject = sharedObject;
        this.sharedLock = sharedLock;
    }

    public void run() {
        while (alive) {
            sharedObject.someNonExclusiveMethod();

            sharedLock.lock();
            try {
                sharedObject.someExclusiveMethod();
            } finally {
                sharedLock.unlock();
            }
        }
    }

    public void stopRunning() {
        alive = false;
    }
}
```

Metoda `someNonExclusiveMethod()` este păstrată în afara secțiunii
protejate, deoarece numele ei indică faptul că nu necesită excludere
mutuală. Dacă metoda accesează totuși date partajate care trebuie
protejate, sincronizarea trebuie analizată separat.

## Concluzie

Problemele principale sunt declararea greșită a constructorului,
apelarea necondiționată a lui `unlock()` și lipsa unei garanții de
vizibilitate pentru variabila `alive`.

Varianta corectată repară aceste probleme: constructorul nu mai are tip
de retur, blocul `try-finally` protejează doar codul executat după
obținerea lock-ului, iar `alive` este declarat `volatile`.
