package com.wjz.chain.ext;

import com.wjz.chain.AbstractLinkedHandler;

public class AuthHandler extends AbstractLinkedHandler {
    @Override
    public void handle(String uri, String userId) {
        System.out.println("Auth Handler");
        super.handle(uri, userId);
    }
}
