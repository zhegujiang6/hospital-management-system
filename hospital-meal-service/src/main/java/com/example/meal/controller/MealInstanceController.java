package com.example.meal.controller;

import com.example.meal.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 展示当前处理请求的餐饮服务实例。
 */
@RestController
@RequestMapping("/meal/instance")
public class MealInstanceController {

    private final String applicationName;

    private final int serverPort;

    public MealInstanceController(
            @Value("${spring.application.name}")
            String applicationName,
            @Value("${server.port}")
            int serverPort) {

        this.applicationName = applicationName;
        this.serverPort = serverPort;
    }

    /**
     * 返回当前服务名和端口。
     */
    @GetMapping
    public Result<String> getCurrentInstance() {

        return Result.success(
                applicationName + ":" + serverPort
        );
    }
}