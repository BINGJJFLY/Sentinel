package com.wjz.chain.ext;

import com.wjz.chain.AbstractLinkedHandler;

public class IpHandler extends AbstractLinkedHandler {
    @Override
    public void handle(String uri, String userId) {
        System.out.println("IP Handler");
        super.handle(uri, userId);
    }
}
