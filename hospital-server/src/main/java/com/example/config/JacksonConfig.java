package com.example.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson JSON序列化配置类
 * 用于自定义JSON序列化和反序列化的行为
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义Jackson的ObjectMapper配置
     * 将Long类型序列化为字符串，防止前端JavaScript因精度丢失导致的数值错误
     * （JavaScript的Number类型最大安全整数为2^53-1，而Java的Long类型可能超出该范围）
     *
     * @return Jackson自定义配置器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // 将包装类型Long序列化为字符串
            builder.serializerByType(
                    Long.class,
                    ToStringSerializer.instance
            );

            // 将基本类型long序列化为字符串
            builder.serializerByType(
                    Long.TYPE,
                    ToStringSerializer.instance
            );
        };
    }
}