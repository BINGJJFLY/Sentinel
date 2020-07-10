package com.alibaba.csp.sentinel.transport.command.http;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorTest {

    @Test
    public void test() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("test-executor"));
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Thread thread = Thread.currentThread();
                if (thread.isInterrupted()) {
                    System.out.println("Thread-1 is Interrupted");
                } else {
                    System.out.println("task-1");
                }
            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("task-2");
            }
        });
        /*
        将线程池状态置为SHUTDOWN,并不会立即停止：
        停止接收外部submit的任务
        内部正在跑的任务和队列里等待的任务，会执行完
        等到第二步完成后，才真正停止
        */
//        executor.shutdown();
        /*
        将线程池状态置为STOP。企图立即停止，事实上不一定：
        跟shutdown()一样，先停止接收外部提交的任务
                忽略队列里等待的任务
        尝试将正在跑的任务interrupt中断
                返回未执行的任务列表
        它试图终止线程的方法是通过调用Thread.interrupt()方法来实现的，但是大家知道，这种方法的作用有限，如果线程中没有sleep 、wait、Condition、定时锁等应用, interrupt()方法是无法中断当前的线程的。所以，ShutdownNow()并不代表线程池就一定立即就能退出，它也可能必须要等待所有正在执行的任务都执行完成了才能退出。
        但是大多数时候是能立即退出的
        */
        executor.shutdownNow();
        try {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("task-3");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(1000);
        System.out.println("end");
    }
}
