package org.hdwyl.thread;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangyanl on 2020/5/10.
 */
public class ThreadLocalDemo {

    static void stepOne() {
        System.out.println(String.format("Step 1:Fetch %s in %s", UserContext.currentUser(), Thread.currentThread().getName()));
    }

    static void stepTwo() {
        System.out.println(String.format("Step 2:Fetch %s in %s", UserContext.currentUser(), Thread.currentThread().getName()));
    }

    static void stepThree() {
        System.out.println(String.format("Step 3:Fetch %s in %s", UserContext.currentUser(), Thread.currentThread().getName()));
    }

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        int threadNum = 10;
        for (int i = 0; i < threadNum; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try (UserContext ctx = new UserContext(UUID.randomUUID().toString().replaceAll("-", ""))) {
                        stepOne();
                        stepTwo();
                        stepThree();
                    }
                }
            });
        }
        threadPool.shutdown();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    static class UserContext implements AutoCloseable {

        static final ThreadLocal<String> ctx = new ThreadLocal<>();

        public UserContext(String user) {
            ctx.set(user);
        }

        public static String currentUser() {
            return ctx.get();
        }

        @Override
        public void close() {
            ctx.remove();
        }
    }
}
