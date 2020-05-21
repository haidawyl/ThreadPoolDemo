package org.hdwyl.threadpool.executors;

import java.util.Date;
import java.util.concurrent.*;

/**
 * 自定义计划线程池
 * <p>
 * Created by wangyanl on 2020/5/21.
 */
public class CustomScheduledThreadPoolDemo {

    static class CustomScheduledTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

        private RunnableScheduledFuture<V> task;

        private ScheduledThreadPoolExecutor executor;

        private long period;

        private long startDate;

        public CustomScheduledTask(Runnable runnable, V result, RunnableScheduledFuture<V> task, ScheduledThreadPoolExecutor executor) {
            super(runnable, result);
            this.task = task;
            this.executor = executor;
        }

        @Override
        public boolean isPeriodic() {
            return task.isPeriodic();
        }

        @Override
        public long getDelay(TimeUnit unit) {
            if (!isPeriodic()) { // 非周期性任务
                return task.getDelay(unit); // 返回储存在 task 属性中原先任务的延迟值
            } else { // 周期性任务
                if (startDate == 0) {
                    return task.getDelay(unit); // 返回储存在 task 属性中原先任务的延迟值
                } else {
                    long delay = startDate - System.currentTimeMillis(); // 计算 startDate 与当前时间的差值
                    return unit.convert(delay, TimeUnit.MILLISECONDS);
                }
            }
        }

        @Override
        public int compareTo(Delayed o) {
            return task.compareTo(o);
        }

        @Override
        public void run() {
            if (isPeriodic() && (!executor.isShutdown())) { // 周期性任务，且线程池未关闭
                startDate = System.currentTimeMillis() + period; // 计算下一次任务的运行时间
                executor.getQueue().add(this); // 任务添加到等待队列中
            }

            System.out.printf("Pre-CustomScheduledTask: %s\n", new Date());
            System.out.printf("CustomScheduledTask: Is Periodic:%s\n", isPeriodic());
            super.runAndReset(); // 运行任务
            System.out.printf("Post-CustomScheduledTask: %s\n", new Date());
        }

        public void setPeriod(long period) { // 设置任务的周期时间
            this.period = period;
        }
    }

    static class CustomScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

        public CustomScheduledThreadPoolExecutor(int corePoolSize) {
            super(corePoolSize);
        }

        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
            CustomScheduledTask<V> customTask = new CustomScheduledTask<V>(runnable, null, task, this);
            return customTask;
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            ScheduledFuture<?> task = super.scheduleAtFixedRate(command, initialDelay, period, unit);
            CustomScheduledTask<?> customTask = (CustomScheduledTask<?>) task;
            customTask.setPeriod(TimeUnit.MILLISECONDS.convert(period, unit));
            return task;
        }
    }

    static class CustomTask implements Runnable {

        @Override
        public void run() {
            System.out.printf("Task: Begin.\n");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("Task: End.\n");
        }
    }

    public static void main(String[] args) throws Exception {
        CustomScheduledThreadPoolExecutor threadPool = new CustomScheduledThreadPoolExecutor(2);
        CustomTask task1 = new CustomTask();
        System.out.printf("Main: %s\n", new Date());
        threadPool.schedule(task1, 1, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(3);

        CustomTask task2 = new CustomTask();
        System.out.printf("Main: %s\n", new Date());
        threadPool.scheduleAtFixedRate(task2, 1, 3, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(10);

        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.DAYS);

        System.out.printf("Main: End of the program.\n");
    }
}
