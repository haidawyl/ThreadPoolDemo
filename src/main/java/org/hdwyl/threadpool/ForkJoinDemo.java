package org.hdwyl.threadpool;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Created by wangyanl on 2020/5/10.
 */
public class ForkJoinDemo {

    static Random random = new Random(0);

    static long random() {
        return random.nextInt(10000);
    }

    public static void main(String[] args) {
        // 创建2000个随机数组成的数组
        long[] array = new long[2000];
        long expectedSum = 0;
        for (int i = 0; i < array.length; i++) {
            array[i] = random();
            expectedSum += array[i];
        }
        System.out.println("Expected sum: " + expectedSum);

        // fork/join
        ForkJoinTask<Long> task = new SumTask(array, 0, array.length);
        long startTime = System.currentTimeMillis();
        Long result = ForkJoinPool.commonPool().invoke(task);
        long endTime = System.currentTimeMillis();
        System.out.println("Fork/join sum: " + result + " in " + (endTime - startTime) + " ms.");
    }

    static class SumTask extends RecursiveTask<Long> {
        static final int THRESHOLD = 100;
        long[] array;
        int start;
        int end;

        public SumTask(long[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= THRESHOLD) {
                // 如果任务足够小,直接计算
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += this.array[i];
                    // 故意放慢计算速度:
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
                return sum;
            }
            // 任务太大,一分为二
            int middle = (end + start) / 2;
            System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
            // “分裂”子任务
            SumTask subTask1 = new SumTask(this.array, start, middle);
            SumTask subTask2 = new SumTask(this.array, middle, end);
            // invokeAll会并行运行两个子任务
            invokeAll(subTask1, subTask2);
            // 获得子任务的结果
            Long subResult1 = subTask1.join();
            Long subResult2 = subTask2.join();
            // 汇总结果
            Long result = subResult1 + subResult2;
            System.out.println("result = " + subResult1 + " + " + subResult2 + " ==> " + result);
            return result;
        }
    }
}
