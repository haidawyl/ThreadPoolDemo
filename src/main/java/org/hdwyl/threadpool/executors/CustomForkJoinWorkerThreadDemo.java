package org.hdwyl.threadpool.executors;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * 自定义ForkJoinWorkerThread
 * <p>
 * Created by wangyanl on 2020/5/21.
 */
public class CustomForkJoinWorkerThreadDemo {

    static class CustomForkJoinWorkerThread extends ForkJoinWorkerThread {

        private static ThreadLocal<Integer> taskCounter = new ThreadLocal<Integer>();

        /**
         * Creates a ForkJoinWorkerThread operating in the given pool.
         *
         * @param pool the pool this thread works in
         * @throws NullPointerException if pool is null
         */
        protected CustomForkJoinWorkerThread(ForkJoinPool pool) {
            super(pool);
        }

        @Override
        protected void onStart() {
            super.onStart();
            System.out.printf("CustomForkJoinWorkerThread %s: Initializing task counter.\n", getName());
            taskCounter.set(0);
        }

        @Override
        protected void onTermination(Throwable exception) {
            System.out.printf("CustomForkJoinWorkerThread %s: %d\n", getName(), taskCounter.get());
            super.onTermination(exception);
        }

        public void addTask() {
            int counter = taskCounter.get();
            counter++;
            taskCounter.set(counter);
        }
    }

    static class CustomForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new CustomForkJoinWorkerThread(pool);
        }
    }

    static class CustomRecursiveTask extends RecursiveTask<Integer> {
        static final int THRESHOLD = 100;

        private int array[];

        private int start, end;

        public CustomRecursiveTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            CustomForkJoinWorkerThread thread = (CustomForkJoinWorkerThread) Thread.currentThread();
            thread.addTask();

            if (end - start <= THRESHOLD) { // 如果任务足够小,直接计算
                int sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            }

            // 任务太大,一分为二
            int middle = (end + start) / 2;
            System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
            // “分裂”子任务
            CustomRecursiveTask subTask1 = new CustomRecursiveTask(array, start, middle);
            CustomRecursiveTask subTask2 = new CustomRecursiveTask(array, middle, end);
            // invokeAll会并行运行两个子任务
            invokeAll(subTask1, subTask2);
            // 获得子任务的结果
            Integer subResult1 = subTask1.join();
            Integer subResult2 = subTask2.join();
            Integer result = subResult1 + subResult2;
            // System.out.println("result = " + subResult1 + " + " + subResult2 + " ==> " + result);
            return result;
        }
    }

    static Random random = new Random(0);

    static int random() {
        return random.nextInt(10000);
    }

    public static void main(String[] args) throws Exception {
        CustomForkJoinWorkerThreadFactory threadFactory = new CustomForkJoinWorkerThreadFactory();
        ForkJoinPool pool = new ForkJoinPool(4, threadFactory, null, false);
        int array[] = new int[1000];
        long expectedSum = 0;
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < array.length; i++) {
            array[i] = random();
            expectedSum += array[i];
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("Sum: %d, Spent %d ms.\n", expectedSum, (endTime - beginTime));

        beginTime = System.currentTimeMillis();
        CustomRecursiveTask task = new CustomRecursiveTask(array, 0, array.length);
        pool.execute(task);
        task.join();
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.DAYS);
        endTime = System.currentTimeMillis();
        System.out.printf("Sum: %d, Spent %d ms.\n", task.join(), (endTime - beginTime));
        System.out.printf("Main: End of the program\n");
    }
}