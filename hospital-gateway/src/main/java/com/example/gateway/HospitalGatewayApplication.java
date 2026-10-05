package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 医院管理系统统一网关启动类。
 */
@SpringBootApplication
public class HospitalGatewayApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                HospitalGatewayApplication.class,
                args
        );
    }
}