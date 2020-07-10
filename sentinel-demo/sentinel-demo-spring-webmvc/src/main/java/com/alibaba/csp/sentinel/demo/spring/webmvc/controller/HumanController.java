package com.alibaba.csp.sentinel.demo.spring.webmvc.controller;

import com.alibaba.csp.sentinel.demo.spring.webmvc.service.HumanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试继承方式注入组件
 */
@RestController
public class HumanController {

    @Autowired
    private HumanService.AbstractHumanService<String> womanService;

    @Autowired
    private HumanService.AbstractHumanService<Integer> manService;

    @GetMapping("/woman")
    public void woman() {
        womanService.print("woman");
    }

    @GetMapping("/man")
    public void man() {
        manService.print(1);
    }
}
