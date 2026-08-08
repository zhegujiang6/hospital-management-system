package com.example.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        // 创建JSON转换器：
        // Java对象写进Redis前转换成JSON，
        // 从Redis读取时再转换回Java对象。
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer();

        // 支持LocalDateTime等Java时间类型。
        valueSerializer.configure(
                objectMapper ->
                        objectMapper.findAndRegisterModules()
        );

        // 创建缓存规则。
        RedisCacheConfiguration cacheConfiguration =
                RedisCacheConfiguration
                        .defaultCacheConfig()

                        // 每条缓存保存10分钟，过期后自动删除。
                        .entryTtl(Duration.ofMinutes(10))

                        // 方法返回null时不写入缓存。
                        .disableCachingNullValues()

                        // Redis的Key按照普通字符串保存。
                        .serializeKeysWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                new StringRedisSerializer()
                                        )
                        )

                        // Redis的Value按照JSON保存。
                        .serializeValuesWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                valueSerializer
                                        )
                        );

        // 创建并返回Spring使用的Redis缓存管理器。
        return RedisCacheManager
                .builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}