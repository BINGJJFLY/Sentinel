package com.wjz.chain;

public class DefaultHandlerChain extends HandlerChain {

    AbstractLinkedHandler head = new AbstractLinkedHandler() {
        @Override
        public void handle(String uri, String userId) {
            super.handle(uri, userId);
        }
    };
    AbstractLinkedHandler tail = head;

    @Override
    void add(AbstractLinkedHandler handler) {
        // 相当于head.setNext(handler); head -> ip -> param -> auth
        tail.setNext(handler);
        tail = handler;
    }

    @Override
    public void handle(String uri, String userId) {
        head.handle(uri, userId);
    }
}
