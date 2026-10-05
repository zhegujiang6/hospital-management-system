package com.example.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hospitalOpenAPI() {

        // JWT认证方式
        SecurityScheme securityScheme =
                new SecurityScheme()
                        .type(
                                SecurityScheme.Type.HTTP
                        )
                        .scheme("bearer")
                        .bearerFormat("JWT");

        // 表示接口可以使用BearerAuth认证
        SecurityRequirement securityRequirement =
                new SecurityRequirement()
                        .addList("BearerAuth");

        return new OpenAPI()

                // 接口文档的基本信息
                .info(
                        new Info()
                                .title(
                                        "医院管理系统接口文档"
                                )
                                .description(
                                        "医院管理系统后端REST API，包含登录、科室、医生、患者、排班、挂号、支付和病历等模块。"
                                )
                                .version("1.0.0")
                )

                // 在Swagger页面增加JWT认证按钮
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "BearerAuth",
                                        securityScheme
                                )
                )

                // 默认接口需要JWT
                .addSecurityItem(
                        securityRequirement
                );
    }
}