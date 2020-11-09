package com.keroz.beancopyutils;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import lombok.Data;
import lombok.ToString;

public class ConcurrentTest {

    @Data
    public static class Source {
        private int id = 1;
        private String name = "source";

        public Source(String name) {
            this.name = name;
        }
    }

    @Data
    @ToString
    public static class Target {
        private int id;
        private String name;
    }

    @Data
    @ToString
    public static class Bean {

        public Bean() {

        }

        public Bean(String name) {
            this.name = name;
        }

        private int id;
        private String name;
    }

    public Thread[] getThreads(int num, Runnable runnable) {
        Thread[] threads = new Thread[num];
        for (int i = 0; i < num; i++) {
            threads[i] = new Thread(runnable);
        }
        return threads;
    }

    @Test
    public void testCopyDifferentTypes() {
        int threadNum = 10;
        CountDownLatch latch = new CountDownLatch(threadNum);
        Thread[] threads = getThreads(threadNum, () -> {
            String threadName = Thread.currentThread().getName();
            long start = System.currentTimeMillis();
            System.out.println(threadName + " is copying...");
            System.out.println(threadName + " copied " + BeanCopyUtils.copy(new Source(threadName), Target.class));
            System.out.println(threadName + " finished copying, taken " + (System.currentTimeMillis() - start));
            latch.countDown();
        });
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testCopySameType() {
        int threadNum = 10;
        CountDownLatch latch = new CountDownLatch(threadNum);
        Thread[] threads = getThreads(threadNum, () -> {
            String threadName = Thread.currentThread().getName();
            long start = System.currentTimeMillis();
            System.out.println(threadName + " is copying...");
            System.out.println(threadName + " copied " + BeanCopyUtils.copy(new Bean(threadName), Bean.class));
            System.out.println(threadName + " finished copying, taken " + (System.currentTimeMillis() - start));
            latch.countDown();
        });
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}