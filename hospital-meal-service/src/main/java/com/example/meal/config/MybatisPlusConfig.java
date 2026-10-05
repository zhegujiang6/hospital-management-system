package com.example.meal.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus功能配置。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 创建MyBatis-Plus拦截器，并启用乐观锁。
     *
     * @return MyBatis-Plus总拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        MybatisPlusInterceptor interceptor =
                new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(
                new OptimisticLockerInnerInterceptor()
        );
        interceptor.addInnerInterceptor(
                new PaginationInnerInterceptor(
                        DbType.MYSQL
                )
        );

        return interceptor;
    }


}