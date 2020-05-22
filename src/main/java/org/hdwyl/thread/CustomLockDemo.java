package org.hdwyl.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义锁
 * <p>
 * Created by wangyanl on 2020/5/21.
 */
public class CustomLockDemo {

    static class CustomAbstractQueuedSynchronizer extends AbstractQueuedSynchronizer {
        private AtomicInteger state;

        public CustomAbstractQueuedSynchronizer() {
            this.state = new AtomicInteger(0);
        }

        @Override
        protected boolean tryAcquire(int arg) {
            return state.compareAndSet(0, 1);
        }

        @Override
        protected boolean tryRelease(int arg) {
            return state.compareAndSet(1, 0);
        }
    }

    static class CustomLock implements Lock {
        private AbstractQueuedSynchronizer synchronizer;

        public CustomLock() {
            synchronizer = new CustomAbstractQueuedSynchronizer();
        }

        @Override
        public void lock() {
            synchronizer.acquire(1);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            synchronizer.acquireInterruptibly(1);
        }

        @Override
        public boolean tryLock() {
            try {
                return synchronizer.tryAcquireNanos(1, 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return synchronizer.tryAcquireNanos(1, TimeUnit.NANOSECONDS.convert(time, unit));
        }

        @Override
        public void unlock() {
            synchronizer.release(1);
        }

        @Override
        public Condition newCondition() {
            return synchronizer.new ConditionObject();
        }
    }

    static class CustomTask implements Runnable {

        private CustomLock lock;

        private String name;

        public CustomTask(CustomLock lock, String name) {
            this.lock = lock;
            this.name = name;
        }

        @Override
        public void run() {
            lock.lock();
            System.out.printf("Task: %s: Take the lock\n", name);
            try {
                TimeUnit.SECONDS.sleep(2);
                System.out.printf("Task: %s: Free the lock\n", name);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        CustomLock lock = new CustomLock();
        for (int i = 0; i < 10; i++) {
            CustomTask task = new CustomTask(lock, "Task-" + i);
            Thread thread = new Thread(task);
            thread.start();
        }

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean value;
        do {
            try {
                value = lock.tryLock(1, TimeUnit.SECONDS);
                if (!value) {
                    System.out.printf("Main: Trying to get the Lock\n");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                value = false;
            }
        } while (!value);

        System.out.printf("Main: Got the lock\n");
        lock.unlock();
        System.out.printf("Main: End of the program\n");
    }
}