package com.wjz.sentinel.cluster.flow.control.client;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ClusterFlowClientController {

    private static final String APP_NAME = "appA";
    private static String RESOURCE_NAME = "cluster-resource";

    private static final String CLUSTER_SERVER_HOST = "localhost";
    private static final int CLUSTER_SERVER_PORT = ClusterConstants.DEFAULT_CLUSTER_SERVER_PORT;
    private static final int REQUEST_TIME_OUT = 200;

    // nacos dataId suffix
    private static final String FLOW_POSTFIX = "-flow-rules";
    // nacos groupId
    private static final String GROUP_ID = "SENTINEL_GROUP";
    // nacos server address
    private static final String REMOTE_ADDRESS = "192.168.88.128:8848";

    public ClusterFlowClientController(){
        loadClusterClientConfig();
        registerClusterClientProperty();
        registerClusterFlowRuleProperty();
    }

    /**
     * 加载集群客户端配置
     * 主要是集群服务端的相关连接信息
     *
     * 客户端配置硬-编码方式
     */
    private void loadClusterClientConfig(){
        ClusterClientAssignConfig assignConfig = new ClusterClientAssignConfig();
        assignConfig.setServerHost(CLUSTER_SERVER_HOST);
        assignConfig.setServerPort(CLUSTER_SERVER_PORT);
        ClusterClientConfigManager.applyNewAssignConfig(assignConfig);

        ClusterClientConfig config = new ClusterClientConfig();
        config.setRequestTimeout(REQUEST_TIME_OUT);
        ClusterClientConfigManager.applyNewConfig(config);
    }

    /**
     * 为ClusterClientConfig注册一个SentinelProperty
     * 这样的话可以动态的更改这些配置
     *
     * 客户端配置-注册动态数据源方式
     */
    private void registerClusterClientProperty() {
        String clientConfigDataId = "cluster-client-config";
        // 初始化一个配置ClusterClientConfig的 Nacos 数据源
        NacosDataSource<ClusterClientConfig> clientConfigDataSource = new NacosDataSource<>(REMOTE_ADDRESS, GROUP_ID, clientConfigDataId,
                source -> JSON.parseObject(source, new TypeReference<ClusterClientConfig>() {}));
        ClusterClientConfigManager.registerClientConfigProperty(clientConfigDataSource.getProperty());

        String clientAssignConfigDataId = "cluster-client-assign-config";
        // 初始化一个配置ClusterClientAssignConfig的 Nacos 数据源
        NacosDataSource<ClusterClientAssignConfig> clientConfigAssignDataSource = new NacosDataSource<>(REMOTE_ADDRESS, GROUP_ID, clientAssignConfigDataId,
                source -> JSON.parseObject(source, new TypeReference<ClusterClientAssignConfig>() {}));
        ClusterClientConfigManager.registerServerAssignProperty(clientConfigAssignDataSource.getProperty());
    }

    /**
     *
     * 客户端配置-通过http接口方式
     *
     * http://<ip>:<port>/cluster/client/modifyConfig?data=<config>
     */

    /**
     * 注册动态规则Property
     * 当client与Server连接中断，退化为本地限流时需要用到的该规则
     */
    private void registerClusterFlowRuleProperty(){
        NacosDataSource<List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(REMOTE_ADDRESS, GROUP_ID, APP_NAME + FLOW_POSTFIX, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

    /**
     * 模拟流量请求该方法
     */
    @GetMapping("/clusterFlow")
    @ResponseBody
    public String clusterFlow() {
        Entry entry = null;
        String retVal;
        try {
            entry = SphU.entry(RESOURCE_NAME, EntryType.IN, 1);
            retVal = "passed";
        } catch (BlockException e) {
            retVal = "blocked";
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
        return retVal;
    }
}
