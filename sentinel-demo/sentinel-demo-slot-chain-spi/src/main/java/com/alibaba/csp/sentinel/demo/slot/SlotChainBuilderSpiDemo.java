/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.slot;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric Zhao
 */
public class SlotChainBuilderSpiDemo {

    private static void initFlowQpsRule() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        FlowRule rule1 = new FlowRule();
        rule1.setResource("abc");
        // set limit qps to 20
        rule1.setCount(20);
        rule1.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule1.setLimitApp("default");
        rules.add(rule1);
        FlowRuleManager.loadRules(rules);
    }

    public static void main(String[] args) throws BlockException {
        initFlowQpsRule();
        // You will see this in record.log, indicating that the custom slot chain builder is activated:
        // [SlotChainProvider] Global slot chain builder resolved: com.alibaba.csp.sentinel.demo.slot.DemoSlotChainBuilder
        Entry entry = null;
        try {
            entry = SphU.entry("abc");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }

        /*ContextUtil.enter("contextName1");
        Entry entry1 = null;
        try {
            entry1 = SphU.entry("resourceName1");
            System.out.println("run method 1");
            Entry entry2 = null;
            try {
                entry2 = SphU.entry("resourceName1");
                System.out.println("run method 1");
            }finally {
                if (entry2 != null) {
                    entry2.exit();
                }
            }
        } finally {
            if (entry1 != null) {
                entry1.exit();
            }
        }*/
    }
}
