package com.alibaba.csp.sentinel.demo.spring.webmvc.service;

import org.springframework.stereotype.Component;

public interface HumanService<T> {

    void print(T t);

    class AbstractHumanService<T> implements HumanService<T> {
        @Override
        public void print(T t) {
            System.out.println(t.getClass().getName());
        }
    }
}

@Component
class WomanService extends HumanService.AbstractHumanService<String> {
}

@Component
class ManService extends HumanService.AbstractHumanService<Integer> {
}
