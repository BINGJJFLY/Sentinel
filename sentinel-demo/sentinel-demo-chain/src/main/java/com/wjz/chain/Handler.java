package com.wjz.chain;

/**
 * 责任链节点
 */
public interface Handler {

    void handle(String uri, String userId);
}
