
## Deadlocks

**Deadlocks**: A deadlock occurs when two or more threads are waiting for each other to release resources, leading to a situation where none of the threads can proceed. 
This typically happens when multiple threads hold locks and try to acquire additional locks held by other threads.

```java
public class DeadlockExample {
    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (lock2) {
                    System.out.println("Thread 1 acquired both locks.");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (lock1) {
                    System.out.println("Thread 2 acquired both locks.");
                }
            }
        });

        thread1.start();
        thread2.start();
    }
}
```
### Prevention:

- **Lock ordering**: Always acquire locks in a consistent order.
- **Timeout**: Use try-lock mechanisms with timeouts to avoid indefinite waiting.
- **Deadlock detection**: Implement algorithms to detect deadlocks and recover.
