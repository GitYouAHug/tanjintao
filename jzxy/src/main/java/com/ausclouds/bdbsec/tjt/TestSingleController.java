package com.ausclouds.bdbsec.tjt;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author tjt
 * @time 2020-08-25
 * @desc 验证Controller 单例
 */
@Controller
@ResponseBody
@Scope("prototype") // 将Controller 设置为多例模式
@RequestMapping("/tjt")
public class TestSingleController {

    private long money = 10;

    @GetMapping("/test1")
    public long testSingleOne(){
        money = ++money;
        System.out.println("/tjt/test1: after use @Scope the money I have: " + money);
        return money;
    }

    @GetMapping("test2")
    public long testSingleTwo(){
        money = ++money;
        System.out.println("/tjt/test2: after use @Scope the money I have: " + money);
        return money;
    }

}
