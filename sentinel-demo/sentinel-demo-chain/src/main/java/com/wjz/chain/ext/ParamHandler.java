package com.wjz.chain.ext;

import com.wjz.chain.AbstractLinkedHandler;

public class ParamHandler extends AbstractLinkedHandler {
    @Override
    public void handle(String uri, String userId) {
        System.out.println("Param Handler");
        super.handle(uri, userId);
    }
}
