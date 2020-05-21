package org.hdwyl.threadpool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangyanl on 2020/5/14.
 */
public class ScheduledThreadPoolDemo {
    public static void main(String[] args) {
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(3);

        long startTime = System.currentTimeMillis();
        threadPool.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("time wait:" + (System.currentTimeMillis() - startTime) + ",this is 线程1");
            }
        }, 0, 1, TimeUnit.SECONDS);

        threadPool.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("time wait:" + (System.currentTimeMillis() - startTime) + ",this is 线程2");
            }
        }, 0, 2, TimeUnit.SECONDS);

        threadPool.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("time wait:" + (System.currentTimeMillis() - startTime) + ",this is 线程3");
            }
        }, 0, 3, TimeUnit.SECONDS);

    }
}
