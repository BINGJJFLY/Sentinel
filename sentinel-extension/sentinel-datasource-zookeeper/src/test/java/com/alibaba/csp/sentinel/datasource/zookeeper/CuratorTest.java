package com.alibaba.csp.sentinel.datasource.zookeeper;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CuratorTest {

    String remoteAddress = "192.168.88.128:2181";
    int baseSleepTimeMs = 1000;
    int maxRetries = 3;
    String path = "/curator-test";

    @Test
    public void test1() {
//        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
//                .connectString(remoteAddress)
//                .retryPolicy(new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries))
//                .build();
        CuratorFramework zkClient = CuratorFrameworkFactory.newClient(remoteAddress, new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries));
        zkClient.start();

        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
            }
            zkClient.setData().forPath(path, "hello curator".getBytes("utf-8"));
            int count = 0;
            while (true) {
                if (count > 10) {
                    break;
                }
                byte[] bytes = zkClient.getData().forPath(path);
                if (bytes != null) {
                    System.out.println(new String(bytes, 0, bytes.length, "utf-8"));
                    break;
                }
                count++;
                Thread.sleep(500);
            }
            zkClient.delete().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            zkClient.close();
        }
    }

    @Test
    public void test2() {
        CuratorFramework zkClient = CuratorFrameworkFactory.newClient(remoteAddress, new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries));
        zkClient.start();
        ExecutorService pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1), new NamedThreadFactory("curator-test"), new ThreadPoolExecutor.DiscardOldestPolicy());
        final NodeCache nodeCache = new NodeCache(zkClient, path);
        NodeCacheListener listener = new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println(System.currentTimeMillis() + " nodeChanged ");
                ChildData childData = nodeCache.getCurrentData();
                if (null != childData && childData.getData() != null) {
                    System.out.println(System.currentTimeMillis() + " nodeChanged: " + new String(childData.getData()));
                }
            }
        };
        try {
            nodeCache.getListenable().addListener(listener, pool);
            nodeCache.start();

            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
            }
            zkClient.setData().forPath(path, "hello curator".getBytes("utf-8"));
            int count = 0;
            while (true) {
                if (count > 10) {
                    break;
                }
                byte[] bytes = zkClient.getData().forPath(path);
                if (bytes != null) {
                    System.out.println(new String(bytes, 0, bytes.length, "utf-8"));
                    break;
                }
                count++;
                Thread.sleep(500);
            }
            zkClient.delete().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            zkClient.close();
            nodeCache.getListenable().removeListener(listener);
            try {
                nodeCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pool.shutdown();
        }
        try {
            Thread.sleep(900000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
