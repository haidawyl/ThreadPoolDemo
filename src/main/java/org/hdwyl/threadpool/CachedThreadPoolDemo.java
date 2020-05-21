package org.hdwyl.threadpool;


import org.hdwyl.threadpool.utils.IdGenerator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangyanl on 2019/4/25.
 */
public class CachedThreadPoolDemo {

    public void run() {
        long begin = System.currentTimeMillis();
        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 20; i++) {
            threadPool.execute(new MyThread(i));
        }
        threadPool.shutdown();
        long end = System.currentTimeMillis();
        System.out.println(String.format("Spent %d ms", end - begin));
    }

    class MyThread implements Runnable {
        int index;

        public MyThread(int index) {
            this.index = index;
        }

        public void run() {
            try {
                Thread.sleep(index * 1000);
            } catch (InterruptedException e) {
            }
            for (int i = 0; i < 10; i++) {
                System.out.println(String.format("threadName:%s, index:%d, number:%d, id:%d", Thread.currentThread().getName(), index, index * 10 + i, IdGenerator.getNextIdWithAtomicLong()));
            }
        }
    }

    public static void main(String[] args) {
        CachedThreadPoolDemo te = new CachedThreadPoolDemo();
        te.run();
    }

}
