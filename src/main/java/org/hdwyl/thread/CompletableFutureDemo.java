package org.hdwyl.thread;

import java.util.concurrent.CompletableFuture;

/**
 * Created by wangyanl on 2020/5/21.
 */
public class CompletableFutureDemo {

    static Double fetchPrice() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (Math.random() < 0.3) {
            throw new RuntimeException("fetch price failed!");
        }
        return 5 + Math.random() * 20;
    }

    static String queryCode(String name) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return "601857";
    }

    static Double fetchPrice(String code) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return 5 + Math.random() * 20;
    }

    static String queryCode(String name, String url) {
        System.out.println("query code from " + url + "...");
        try {
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
        }
        return "601857";
    }

    static Double fetchPrice(String code, String url) {
        System.out.println("query price from " + url + "...");
        try {
            Thread.sleep((long) (Math.random() * 100));
        } catch (InterruptedException e) {
        }
        return 5 + Math.random() * 20;
    }

    public static void main(String[] args) {
        // 创建异步执行任务
        CompletableFuture<Double> cf = CompletableFuture.supplyAsync(CompletableFutureDemo::fetchPrice);
        // 如果执行成功，CompletableFuture会调用Consumer对象
        cf.thenAccept((result) -> {
            System.out.println("price: " + result);
        });
        // 如果执行异常，CompletableFuture会调用Function对象
        cf.exceptionally((e) -> {
            e.printStackTrace();
            throw null;
        });
        // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 两个CompletableFuture实现串行操作
        // 第一个任务
        CompletableFuture<String> cfQuery = CompletableFuture.supplyAsync(() -> {
            return queryCode("中国石油");
        });
        // 第一个任务成功后继续执行下一个任务
        CompletableFuture<Double> cfFetch = cfQuery.thenApplyAsync((code) -> {
            return fetchPrice(code);
        });
        // 第二个任务成功后打印结果
        cfFetch.thenAccept((result) -> {
            System.out.println("price: " + result);
        });
        // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 两个CompletableFuture执行异步查询
        CompletableFuture<String> cfQueryFromSina = CompletableFuture.supplyAsync(() -> {
            return queryCode("中国石油", "https://finance.sina.com.cn/code/");
        });
        CompletableFuture<String> cfQueryFrom163 = CompletableFuture.supplyAsync(() -> {
            return queryCode("中国石油", "https://money.163.com/code/");
        });
        // 用anyOf合并为一个新的CompletableFuture
        CompletableFuture<Object> cfQueryAny = CompletableFuture.anyOf(cfQueryFromSina, cfQueryFrom163);
        // 两个CompletableFuture执行异步查询
        CompletableFuture<Double> cfFetchFromSina = cfQueryAny.thenApplyAsync((code) -> {
            return fetchPrice((String) code, "https://finance.sina.com.cn/price/");
        });
        CompletableFuture<Double> cfFetchFrom163 = cfQueryAny.thenApplyAsync((code) -> {
            return fetchPrice((String) code, "https://money.163.com/price/");
        });
        // 用anyOf合并为一个新的CompletableFuture
        CompletableFuture<Object> cfFetchAny = CompletableFuture.anyOf(cfFetchFromSina, cfFetchFrom163);
        // 最终结果
        cfFetchAny.thenAccept((result) -> {
            System.out.println("price: " + result);
        });
        // 主线程不要立刻结束，否则CompletableFuture默认使用的线程池会立刻关闭
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
