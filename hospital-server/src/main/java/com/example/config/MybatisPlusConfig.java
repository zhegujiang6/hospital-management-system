package com.example.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        // 创建MyBatis-Plus总拦截器
        MybatisPlusInterceptor interceptor =
                new MybatisPlusInterceptor();

        // 创建分页拦截器，并告诉它当前使用的是MySQL
        PaginationInnerInterceptor paginationInterceptor =
                new PaginationInnerInterceptor(
                        DbType.MYSQL
                );

        // 请求超过最大页码时不自动跳回第一页
        paginationInterceptor.setOverflow(false);

        // 每页最多允许查询100条，防止一次查询过多数据
        paginationInterceptor.setMaxLimit(100L);

        // 把分页能力加入MyBatis-Plus总拦截器
        interceptor.addInnerInterceptor(
                paginationInterceptor
        );

        return interceptor;
    }
}