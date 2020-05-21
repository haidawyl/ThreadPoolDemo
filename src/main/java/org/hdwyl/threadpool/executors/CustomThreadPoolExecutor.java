package org.hdwyl.threadpool.executors;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 自定义线程池
 * <p>
 * Created by wangyanl on 2020/5/14.
 */
public class CustomThreadPoolExecutor extends ThreadPoolExecutor {

    private ConcurrentHashMap<String, Date> startTimes;

    public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.startTimes = new ConcurrentHashMap<>();
    }

    @Override
    public void shutdown() {
        System.out.printf("CustomThreadPoolExecutor: Going to shutdown.\n");
        System.out.printf("CustomThreadPoolExecutor: Executed tasks:%d\n", getCompletedTaskCount());
        System.out.printf("CustomThreadPoolExecutor: Running tasks:%d\n", getActiveCount());
        System.out.printf("CustomThreadPoolExecutor: Pending tasks:%d\n", getQueue().size());
        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        System.out.printf("CustomThreadPoolExecutor: Going to immediately shutdown.\n");
        System.out.printf("CustomThreadPoolExecutor: Executed tasks: %d\n", getCompletedTaskCount());
        System.out.printf("CustomThreadPoolExecutor: Running tasks: %d\n", getActiveCount());
        System.out.printf("CustomThreadPoolExecutor: Pending tasks: %d\n", getQueue().size());
        return super.shutdownNow();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        System.out.printf("CustomThreadPoolExecutor: A task is beginning: %s : %s\n", t.getName(), r.hashCode());
        startTimes.put(String.valueOf(r.hashCode()), new Date());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Future<?> result = (Future<?>) r;
        try {
            System.out.printf("*********************************\n");
            System.out.printf("CustomThreadPoolExecutor: A task is finishing.\n");
            System.out.printf("CustomThreadPoolExecutor: Result: %s\n", result.get());
            Date startDate = startTimes.remove(String.valueOf(r.hashCode()));
            Date finishDate = new Date();
            long diff = finishDate.getTime() - startDate.getTime();
            System.out.printf("CustomThreadPoolExecutor: Duration: %d\n", diff);
            System.out.printf("*********************************\n");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
