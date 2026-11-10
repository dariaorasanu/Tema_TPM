import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LockBenchmark {
    interface Lock {
        void lock();
        void unlock();
    }

    // Anderson array-based queue lock.
    static final class AndersonLock implements Lock {
        private final AtomicInteger next = new AtomicInteger(0);
        private final AtomicIntegerArray flags;
        private final int capacity;
        private final ThreadLocal<Integer> mySlot = new ThreadLocal<>();

        AndersonLock(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
            this.capacity = capacity;
            this.flags = new AtomicIntegerArray(capacity);
            this.flags.set(0, 1);
        }

        @Override
        public void lock() {
            int slot = next.getAndIncrement() % capacity;
            mySlot.set(slot);
            while (flags.get(slot) == 0) {
                Thread.onSpinWait();
            }
        }

        @Override
        public void unlock() {
            Integer slot = mySlot.get();
            if (slot == null) throw new IllegalStateException("unlock without lock");
            flags.set(slot, 0);
            flags.set((slot + 1) % capacity, 1);
            mySlot.remove();
        }
    }

    // CLH queue lock.
    static final class CLHLock implements Lock {
        private static final class QNode {
            volatile boolean locked;
        }

        private final AtomicReference<QNode> tail;
        private final ThreadLocal<QNode> myNode;
        private final ThreadLocal<QNode> myPred;

        CLHLock() {
            QNode dummy = new QNode();
            tail = new AtomicReference<>(dummy);
            myNode = ThreadLocal.withInitial(QNode::new);
            myPred = new ThreadLocal<>();
        }

        @Override
        public void lock() {
            QNode node = myNode.get();
            node.locked = true;
            QNode pred = tail.getAndSet(node);
            myPred.set(pred);
            while (pred.locked) {
                Thread.onSpinWait();
            }
        }

        @Override
        public void unlock() {
            QNode node = myNode.get();
            node.locked = false;
            myNode.set(myPred.get());
            myPred.remove();
        }
    }

    private static final int TARGET = 120_000;
    private static final int RUNS = 5;

    private static long runOnce(Lock lock, int threadCount) throws InterruptedException {
        final int incrementsPerThread = TARGET / threadCount;
        final int remainder = TARGET % threadCount;
        final int[] counter = {0};
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int increments = incrementsPerThread + (i < remainder ? 1 : 0);
            threads[i] = new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                    for (int j = 0; j < increments; j++) {
                        lock.lock();
                        try {
                            counter[0]++;
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i].start();
        }

        ready.await();
        long begin = System.nanoTime();
        start.countDown();

        for (Thread thread : threads) {
            thread.join();
        }
        long end = System.nanoTime();

        if (counter[0] != TARGET) {
            throw new IllegalStateException(
                "Wrong counter value: " + counter[0] + ", expected " + TARGET
            );
        }
        return end - begin;
    }

    private static void benchmark(String name, int threadCount, boolean anderson)
            throws InterruptedException {
        long totalNanos = 0;
        for (int run = 1; run <= RUNS; run++) {
            Lock lock = anderson
                ? new AndersonLock(threadCount)
                : new CLHLock();
            long elapsed = runOnce(lock, threadCount);
            totalNanos += elapsed;
            System.out.printf("%s | %2d threads | run %d: %.3f ms%n",
                name, threadCount, run, elapsed / 1_000_000.0);
        }
        double averageMs = totalNanos / (double) RUNS / 1_000_000.0;
        System.out.printf("%s | %2d threads | average: %.3f ms%n%n",
            name, threadCount, averageMs);
    }

    public static void main(String[] args) throws InterruptedException {
        int[] configurations = {4, 8, 12};
        for (int count : configurations) {
            benchmark("Anderson", count, true);
            benchmark("CLH", count, false);
        }
    }
}
