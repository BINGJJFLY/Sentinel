package com.wjz.sentinel.cluster.flow.control.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动时加参数：
 * -Dproject.name=appA -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dcsp.sentinel.log.use.pid=true
 * 可以在对应的 sentinel 的 dashboard 中查看效果
 */
@SpringBootApplication
public class ClusterFlowClientApplication {
    public static void main(String[] args) {
        System.setProperty("project.name", "appA");
        System.setProperty("csp.sentinel.dashboard.server", "127.0.0.1:8080");
        System.setProperty("csp.sentinel.log.use.pid", "true");
        SpringApplication.run(ClusterFlowClientApplication.class, args);
    }
}
