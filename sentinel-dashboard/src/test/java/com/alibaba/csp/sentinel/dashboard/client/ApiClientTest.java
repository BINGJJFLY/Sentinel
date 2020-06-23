package com.alibaba.csp.sentinel.dashboard.client;

import cn.hutool.http.HttpUtil;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ApiClientTest {

    @Test
    public void getAsync() {
        try {
            CompletableFuture<String> future = new ApiClient()
                    .getAsync("http://172.17.4.1/get?id=1", null);
            String rel = future.get(5000, TimeUnit.MILLISECONDS);
            System.out.println(rel);
            Thread.sleep(7000);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Test
    public void get() {
        String s = HttpUtil.get("http://172.17.4.1/get?id=1");
        System.out.println(s);
    }

    @Test
    public void getAsyncForTimes() throws InterruptedException {
        long s = System.currentTimeMillis();
        CompletableFuture<String> future = null;
        for (int i = 0; i < 5; i++) {
            try {
                future = new ApiClient()
                        .getAsync("http://172.17.4.1/get?id=1", null);
//                String rel = future.get(5000, TimeUnit.MILLISECONDS);
//                System.out.println(rel);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        long e = System.currentTimeMillis();
        // =============[18812]============
        System.out.println("=============["+(e-s)+"]============");
        Thread.sleep(17000);
    }

    @Test
    public void getForTimes() {
        long st = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            String s = HttpUtil.get("http://172.17.4.1/get?id=1");
            System.out.println(s);
        }
        long e = System.currentTimeMillis();
        // =============[15283]============
        System.out.println("=============["+(e-st)+"]============");
    }

}
