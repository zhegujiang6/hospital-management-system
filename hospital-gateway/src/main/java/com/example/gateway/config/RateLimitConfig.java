package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * 配置 Gateway 限流时使用的访问者标识。
 */
@Configuration
public class RateLimitConfig {

    /**
     * 使用客户端 IP 作为限流键。
     *
     * 同一个 IP 的请求会共享同一个令牌桶。
     */
    @Bean
    public KeyResolver ipKeyResolver() {

        return exchange -> {

            InetSocketAddress remoteAddress =
                    exchange.getRequest()
                            .getRemoteAddress();

            String clientIp;

            if (remoteAddress == null
                    || remoteAddress.getAddress() == null) {

                clientIp = "unknown";

            } else {

                clientIp =
                        remoteAddress.getAddress()
                                .getHostAddress();
            }

            return Mono.just(clientIp);
        };
    }
}