package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 统一记录经过 Gateway 的请求信息和处理耗时。
 */
@Component
public class AccessLogGlobalFilter
        implements GlobalFilter, Ordered {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AccessLogGlobalFilter.class
            );

    /**
     * 在请求开始时记录时间，在请求结束后输出访问日志。
     */
    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        long startTime = System.nanoTime();

        String requestId =
                exchange.getAttribute(
                        RequestIdGlobalFilter
                                .REQUEST_ID_ATTRIBUTE
                );

        String method =
                exchange.getRequest()
                        .getMethod()
                        .name();

        String path =
                exchange.getRequest()
                        .getURI()
                        .getRawPath();

        return chain.filter(exchange)
                .doFinally(signalType -> {

                    long durationNanos =
                            System.nanoTime() - startTime;

                    long durationMillis =
                            durationNanos / 1_000_000;

                    HttpStatusCode statusCode =
                            exchange.getResponse()
                                    .getStatusCode();

                    int status =
                            statusCode == null
                                    ? 0
                                    : statusCode.value();

                    LOGGER.info(
                            "requestId={} method={} path={} status={} durationMs={}",
                            requestId,
                            method,
                            path,
                            status,
                            durationMillis
                    );
                });
    }

    /**
     * 在请求 ID 过滤器之后执行。
     */
    @Override
    public int getOrder() {
        return -90;
    }
}