package com.wjz.chain;

import com.wjz.chain.ext.AuthHandler;
import com.wjz.chain.ext.IpHandler;
import com.wjz.chain.ext.ParamHandler;
import org.junit.Test;

public class ChainTest {

    @Test
    public void test1() {
        HandlerChain chain = new DefaultHandlerChain();
        chain.add(new IpHandler());
        chain.add(new ParamHandler());
        chain.add(new AuthHandler());
        chain.handle("hello", "1001");
    }
}
