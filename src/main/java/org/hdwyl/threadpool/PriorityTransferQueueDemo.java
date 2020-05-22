package org.hdwyl.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wangyanl on 2020/5/22.
 */
public class PriorityTransferQueueDemo {

    static class CustomPriorityTransferQueue<E> extends PriorityBlockingQueue<E> implements TransferQueue<E> {
        private AtomicInteger counter;

        private LinkedBlockingQueue<E> transfered;

        private ReentrantLock lock;

        public CustomPriorityTransferQueue() {
            this.counter = new AtomicInteger(0);
            this.transfered = new LinkedBlockingQueue<>();
            this.lock = new ReentrantLock();
        }

        @Override
        public boolean tryTransfer(E e) {
            lock.lock();
            try {
                if (counter.get() == 0) { // 没有消费者在等待，则什么都不做，立即返回false
                    return false;
                } else { // 有消费者在等待，立刻发送元素给消费者，同时返回true
                    put(e);
                    return true;
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void transfer(E e) throws InterruptedException {
            lock.lock();
            try {
                if (counter.get() != 0) { // 有消费者在等待，立刻发送元素给消费者
                    put(e);
                    lock.unlock();
                } else { // 没有消费者在等待，则元素添加到transfered队列，同时线程进入等待状态
                    transfered.add(e);
                    lock.unlock();
                    synchronized (e) {
                        e.wait();
                    }
                }
            } finally {
                if (lock.isLocked()) {
                    lock.unlock();
                }
            }
        }

        @Override
        public boolean tryTransfer(E e, long timeout, TimeUnit unit) throws InterruptedException {
            lock.lock();
            try {
                if (counter.get() != 0) { // 有消费者在等待，立刻发送元素给消费者，同时返回true
                    put(e);
                    lock.unlock();
                    return true;
                } else { // 没有消费者在等待，则元素添加到transfered队列，同时转化为新的超时时长并使用 wait() 方法让线程进入等待状态。
                    transfered.add(e);
                    long newTimeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
                    lock.unlock();
                    synchronized (e) {
                        e.wait(newTimeout);
                    }

                    lock.lock();
                    if (transfered.contains(e)) { // 等待超时后，如果没有消费者取走元素，则从transfered队列中删除该元素，同时返回false
                        transfered.remove(e);
                        lock.unlock();
                        return false;
                    } else { // 等待超时后，如果有消费者已经取走元素，则什么都不做，然后立即返回true
                        lock.unlock();
                        return true;
                    }
                }
            } finally {
                if (lock.isLocked()) {
                    lock.unlock();
                }
            }
        }

        @Override
        public boolean hasWaitingConsumer() {
            return (counter.get() != 0);
        }

        @Override
        public int getWaitingConsumerCount() {
            return counter.get();
        }

        /**
         * 当消费者需要元素时调用本方法
         *
         * @return
         * @throws InterruptedException
         */
        @Override
        public E take() throws InterruptedException {
            lock.lock(); // 获取锁
            try {
                counter.incrementAndGet(); // 增加等待的消费者数量

                E value = transfered.poll(); // 从transferred队列中获取元素
                if (value == null) { // transferred队列中无元素
                    lock.unlock(); // 释放锁
                    value = super.take(); // 调用父类take()方法获取元素，此方法将让线程进入睡眠状态直到有元素可以消费
                    lock.lock(); // 获取锁
                } else { // transferred队列中有元素
                    synchronized (value) {
                        value.notifyAll(); // 唤醒正在等待消费元素的线程
                    }
                }
                counter.decrementAndGet(); // 减少等待的消费者数量
                lock.unlock(); // 释放锁
                return value;
            } finally {
                if (lock.isLocked()) {
                    lock.unlock();
                }
            }
        }
    }

    static class Event implements Comparable<Event> {

        private String thread;

        private int priority;

        public Event(String thread, int priority) {
            this.thread = thread;
            this.priority = priority;
        }

        public String getThread() {
            return thread;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public int compareTo(Event e) {
            if (this.priority > e.getPriority()) {
                return -1;
            } else if (this.priority < e.getPriority()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    static class Producer implements Runnable {

        private CustomPriorityTransferQueue<Event> buffer;

        public Producer(CustomPriorityTransferQueue<Event> buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                Event event = new Event(Thread.currentThread().getName(), i);
                buffer.put(event);
            }
        }
    }

    static class Consumer implements Runnable {

        private CustomPriorityTransferQueue<Event> buffer;

        public Consumer(CustomPriorityTransferQueue<Event> buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
            for (int i = 0; i < 1002; i++) {
                try {
                    Event value = buffer.take();
                    System.out.printf("Consumer: %s: %d\n", value.getThread(), value.getPriority());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        CustomPriorityTransferQueue<Event> buffer = new CustomPriorityTransferQueue<Event>();
        Producer producer = new Producer(buffer);
        Thread producerThreads[] = new Thread[10];
        for (int i = 0; i < producerThreads.length; i++) {
            producerThreads[i] = new Thread(producer);
            producerThreads[i].start();
        }

        Consumer consumer = new Consumer(buffer);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        TimeUnit.MILLISECONDS.sleep(100);

        System.out.printf("Main: Buffer: Consumer count: %d\n", buffer.getWaitingConsumerCount());

        // 使用 transfer() 方法传输一个事件给消费者
        Event event = new Event("Core Event", 0);
        buffer.transfer(event);
        System.out.printf("Main: My Event has been transfered.\n");

        for (int i = 0; i < producerThreads.length; i++) {
            try {
                producerThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        TimeUnit.SECONDS.sleep(1);
        System.out.printf("Main: Buffer: Consumer count: %d\n", buffer.getWaitingConsumerCount());

        event = new Event("Core Event 2", 0);
        buffer.transfer(event);

        consumerThread.join();

        System.out.printf("Main: End of the program\n");

    }
}
