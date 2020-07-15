package com.wjz.sentinel.cluster.flow.control.serveralone;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ClusterServer {

    // namespace set 之一
    private static final String APP_NAME = "appA";
    // nacos dataId suffix
    private static final String FLOW_POSTFIX = "-flow-rules";
    // nacos groupId
    private static final String GROUP_ID = "SENTINEL_GROUP";
    // nacos server address
    private static final String REMOTE_ADDRESS = "192.168.88.128:8848";

    private static final Converter<String, List<FlowRule>> parser = source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});

    /**
     * 初始化集群限流的Supplier
     * 这样如果后期集群限流的规则发生变更的话，系统可以自动感知到
     * namespace 对应的规则发生变更时，系统可以自动感知到
     */
    private void initClusterFlowSupplier() {
        ClusterFlowRuleManager.setPropertySupplier(namespace -> {
            NacosDataSource<List<FlowRule>> clusterFlowRuleDataSource = new NacosDataSource(REMOTE_ADDRESS, GROUP_ID, namespace + FLOW_POSTFIX, parser);
            return clusterFlowRuleDataSource.getProperty();
        });
    }

    /**
     * 为 namespace set 注册一个SentinelProperty
     * 这样如果后期 namespace set 发生变更的话，系统可以自动感知到
     */
    private void registerNamespaceProperty() {
        String dataId = "cluster-server-namespace-set";
        NacosDataSource<Set<String>> namespaceSetDataSource = new NacosDataSource(REMOTE_ADDRESS, GROUP_ID, dataId, parser);
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceSetDataSource.getProperty());
    }

    /**
     * 为ServerTransportConfig注册一个SentinelProperty
     * 这样的话可以动态的更改这些配置
     */
    private void registerServerTransportProperty() {
        String dataId = "cluster-server-transport-config";
        NacosDataSource<ServerTransportConfig> serverTransportConfigDataSource = new NacosDataSource(REMOTE_ADDRESS, GROUP_ID, dataId, parser);
        ClusterServerConfigManager.registerServerTransportProperty(serverTransportConfigDataSource.getProperty());
    }

    /**
     * 加载namespace set以及ServerTransportConfig
     * 最好还要再为他们每个都注册一个SentinelProperty，这样的话可以动态的修改这些配置项
     * 硬编码方式
     */
    private void loadServerConfig() {
        ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton(APP_NAME));
        ClusterServerConfigManager.loadGlobalTransportConfig(new ServerTransportConfig(ClusterConstants.DEFAULT_CLUSTER_SERVER_PORT, ServerTransportConfig.DEFAULT_IDLE_SECONDS));
    }

    private void init() {
        // 初始化集群限流规则
        initClusterFlowSupplier();
        // 初始化Token Server配置
        loadServerConfig();
    }

    /**
     * 注册Property
     */
    public void registerProperty() {
        // 注册namespace的SentinelProperty
        registerNamespaceProperty();
        // 注册ServerTransportConfig的SentinelProperty
        registerServerTransportProperty();
    }

    private void start() throws Exception {
        // 创建一个 ClusterTokenServer 的实例，独立模式
        ClusterTokenServer server = new SentinelDefaultTokenServer(false);
        server.start();
    }

    /**
     * 添加启动参数 -Dproject.name=xxx -Dcsp.sentinel.dashboard.server=consoleIp:port
     * -Dcsp.sentinel.log.use.pid=true
     * 让服务端在启动后可以连接上 sentinel-dashboard
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("project.name", "appA");
        System.setProperty("csp.sentinel.dashboard.server", "127.0.0.1:8080");
        System.setProperty("csp.sentinel.log.use.pid", "true");

        ClusterServer clusterServer = new ClusterServer();
        clusterServer.init();
        clusterServer.start();
    }
}
