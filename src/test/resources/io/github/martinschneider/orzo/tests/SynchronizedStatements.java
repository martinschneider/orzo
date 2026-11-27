package io.github.martinschneider.orzo.tests;

// synchronized statements and methods
public class SynchronizedStatements {
  int count;
  final Object lock = new Object();

  synchronized void inc() {
    count++;
  }

  int get() {
    synchronized (this) {
      return count;
    }
  }

  void add(int n) {
    for (int i = 0; i < n; i++) {
      synchronized (lock) {
        if (i == 3) continue;
        count += Thread.holdsLock(lock) ? 1 : 100;
      }
    }
  }

  public static void main(String[] args) throws Exception {
    SynchronizedStatements y = new SynchronizedStatements();
    Thread[] threads = new Thread[4];
    for (int t = 0; t < threads.length; t++) {
      threads[t] = new Thread(() -> { for (int i = 0; i < 1000; i++) y.inc(); });
      threads[t].start();
    }
    for (Thread t : threads) t.join();
    y.add(5);
    System.out.println(y.get() + " " + Thread.holdsLock(y.lock));
    try {
      synchronized (y) {
        throw new IllegalStateException("x");
      }
    } catch (IllegalStateException e) {
      System.out.println(e.getMessage() + " " + Thread.holdsLock(y));
    }
  }
}
