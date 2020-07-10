/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.spring.webmvc;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.datasource.redis.RedisDataSource;
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

/**
 * <p>Add the JVM parameter to connect to the dashboard:</p>
 * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-spring-webmvc}
 *
 * @author kaizi2009
 */
@SpringBootApplication
public class WebMvcDemoApplication {

    private static final Converter<String, List<FlowRule>> parser = source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});

    public static void main(String[] args) {
        /** 规则持久化 **/
        // 可以自定义一个类来实现 Sentinel 中的 InitFunc 接口来完成初始化
        /*
         * new Thread(() -> InitExecutor.doInit()).start();
         */

//        new Thread(() -> file()).start();
//        new Thread(() -> nacos()).start();
//        new Thread(() -> redis()).start();
//        new Thread(() -> zookeeper()).start();
        SpringApplication.run(WebMvcDemoApplication.class);
    }

    private static void file() {
        try {
            String filename = "D:/flowRule.yml";
            FileRefreshableDataSource<List<FlowRule>> flowRuleDataSource = new FileRefreshableDataSource<>(filename, parser);
            FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void nacos() {
        String serverAddr = "192.168.88.128:8848";
        String groupId = "Sentinel:Demo";
        String dataId = "com.alibaba.csp.sentinel.demo.flow.rule";
        NacosDataSource<List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(serverAddr, groupId, dataId, parser);
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

    private static void redis() {
        String host = "192.168.88.128";
        int port = 6379;
        String ruleKey = "sentinel.rules.flow.ruleKey";
        String channel = "sentinel.rules.flow.channel";
        RedisConnectionConfig config = RedisConnectionConfig.builder()
                .withHost(host)
                .withPort(port)
                .build();
        RedisDataSource<List<FlowRule>> flowRuleDataSource = new RedisDataSource<>(config, ruleKey, channel, parser);
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

    private static void zookeeper() {
        String remoteAddress = "192.168.88.128:2181";
        String path = "/flowRule";
        ZookeeperDataSource flowRuleDataSource = new ZookeeperDataSource(remoteAddress, path, parser);
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }
}
