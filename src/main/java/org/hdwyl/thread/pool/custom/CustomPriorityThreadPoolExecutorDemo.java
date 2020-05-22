package org.hdwyl.thread.pool.custom;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建带有优先级的线程池
 * <p>
 * Created by wangyanl on 2020/5/14.
 */
public class CustomPriorityThreadPoolExecutorDemo {

    static class PriorityTask implements Runnable, Comparable<PriorityTask> {

        private String name;

        private int priority;

        public PriorityTask(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public int compareTo(PriorityTask o) {
            if (this.getPriority() < o.getPriority()) {
                return 1;
            }
            if (this.getPriority() > o.getPriority()) {
                return -1;
            }
            return 0;
        }

        @Override
        public void run() {
            System.out.printf("PriorityTask: %s Priority :%d in %s\n", name, priority, Thread.currentThread().getName());
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 2, 1, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
        for (int i = 0; i < 4; i++) {
            PriorityTask task = new PriorityTask("Task " + i, i);
            threadPoolExecutor.execute(task);
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 4; i < 8; i++) {
            PriorityTask task = new PriorityTask("Task " + i, i);
            threadPoolExecutor.execute(task);
        }
        threadPoolExecutor.shutdown();
        try {
            threadPoolExecutor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("Main: End of the program.\n");
    }
}
