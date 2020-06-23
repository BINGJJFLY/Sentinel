package com.wjz.chain;

public abstract class AbstractLinkedHandler implements Handler {

    private AbstractLinkedHandler next = null;

    @Override
    public void handle(String uri, String userId) {
        if (next != null) {
            next.handle(uri, userId);
        }
    }

    public AbstractLinkedHandler getNext() {
        return next;
    }

    public void setNext(AbstractLinkedHandler next) {
        this.next = next;
    }
}
