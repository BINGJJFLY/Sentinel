package com.wjz.chain;

public abstract class HandlerChain extends AbstractLinkedHandler {

    abstract void add(AbstractLinkedHandler handler);
}
