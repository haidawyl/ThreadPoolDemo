package org.hdwyl.threadpool.executors;

import java.util.Date;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangyanl on 2020/5/14.
 */
public class CustomThreadFactoryDemo {

    static class CustomThread extends Thread {
        private Date creationDate;
        private Date startDate;
        private Date finishDate;

        public CustomThread(Runnable target, String name) {
            super(target, name);
            setCreationDate();
        }

        @Override
        public void run() {
            setStartDate();
            super.run();
            setFinishDate();
        }

        public void setCreationDate() {
            this.creationDate = new Date();
        }

        public void setStartDate() {
            this.startDate = new Date();
        }

        public void setFinishDate() {
            this.finishDate = new Date();
        }

        public long getExecutionTime() {
            return this.finishDate.getTime() - this.startDate.getTime();
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append(getName());
            buffer.append(": ");
            buffer.append(" Creation Date: ");
            buffer.append(creationDate);
            buffer.append(" : Running time: ");
            buffer.append(getExecutionTime());
            buffer.append(" Milliseconds.");
            return buffer.toString();
        }
    }

    static class CustomThreadFactory implements ThreadFactory {

        private int counter;

        private String prefix;

        public CustomThreadFactory(String prefix) {
            this.prefix = prefix;
            this.counter = 1;
        }

        @Override
        public Thread newThread(Runnable r) {
            CustomThread customThread = new CustomThread(r, prefix + "-" + counter);
            counter++;
            return customThread;
        }
    }

    static class CustomTask implements Runnable {

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        CustomThreadFactory customThreadFactory = new CustomThreadFactory("CustomThreadFactory");
        CustomTask task = new CustomTask();
        Thread thread = customThreadFactory.newThread(task);
        thread.start();
        thread.join();

        System.out.printf("Main: Thread information.\n");
        System.out.printf("%s\n", thread);
        System.out.printf("Main: End of the example.\n");
    }
}
