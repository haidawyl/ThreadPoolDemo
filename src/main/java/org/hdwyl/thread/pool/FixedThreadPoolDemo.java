package org.hdwyl.thread.pool;


import org.hdwyl.thread.utils.IdGenerator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangyanl on 2019/4/25.
 */
public class FixedThreadPoolDemo {

    public void run() {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 20; i++) {
            threadPool.execute(new MyThread(i));
        }
        threadPool.shutdown();
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
        FixedThreadPoolDemo te = new FixedThreadPoolDemo();
        te.run();
    }

}
