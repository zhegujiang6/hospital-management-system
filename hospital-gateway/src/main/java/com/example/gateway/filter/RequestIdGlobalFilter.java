package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 为每个经过 Gateway 的请求生成并传递唯一请求 ID。
 */
@Component
public class RequestIdGlobalFilter
        implements GlobalFilter, Ordered {

    public static final String REQUEST_ID_HEADER =
            "X-Request-Id";

    public static final String REQUEST_ID_ATTRIBUTE =
            "requestId";

    /**
     * 读取或生成请求 ID，并将其传给下游服务和客户端。
     */
    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String originalRequestId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(REQUEST_ID_HEADER);

        String requestId =
                StringUtils.hasText(originalRequestId)
                        ? originalRequestId
                        : UUID.randomUUID().toString();

        // 保存到本次请求上下文，方便后续日志过滤器读取。
        exchange.getAttributes().put(
                REQUEST_ID_ATTRIBUTE,
                requestId
        );

        // Gateway 中的请求对象不可直接修改，需要复制后增加请求头。
        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .headers(headers ->
                                headers.set(
                                        REQUEST_ID_HEADER,
                                        requestId
                                )
                        )
                        .build();

        // 将相同的请求 ID 返回给客户端，方便反馈问题时提供。
        exchange.getResponse()
                .getHeaders()
                .set(REQUEST_ID_HEADER, requestId);

        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(request)
                        .build();

        return chain.filter(mutatedExchange);
    }

    /**
     * 数值越小越早执行，让请求 ID 在其他过滤器之前生成。
     */
    @Override
    public int getOrder() {
        return -100;
    }
}