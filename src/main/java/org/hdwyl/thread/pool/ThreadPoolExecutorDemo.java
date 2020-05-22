package org.hdwyl.thread.pool;

import org.hdwyl.thread.utils.IdGenerator;

import java.util.concurrent.*;

/**
 * Created by wangyanl on 2020/5/9.
 */
public class ThreadPoolExecutorDemo {

    public void run() {
        int threads = 100;

        int corePoolSize = 4;
        int maximumPoolSize = 16;
        long keepAliveTime = 0;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        ExecutorService threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);

        CountDownLatch latch = new CountDownLatch(threads);

        long begin = System.currentTimeMillis();

        for (int i = 0; i < threads; i++) {
            threadPool.execute(new MyThread(latch));
        }
        threadPool.shutdown();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        System.out.println(String.format("Spent %d ms", end - begin));
    }

    class MyThread implements Runnable {
        CountDownLatch latch;

        public MyThread(CountDownLatch latch) {
            this.latch = latch;
        }

        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.out.println(String.format("threadName:%s, id:%d", Thread.currentThread().getName(), IdGenerator.getNextIdWithAtomicLong()));

            latch.countDown();
        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutorDemo tpe = new ThreadPoolExecutorDemo();
        tpe.run();
    }
}
