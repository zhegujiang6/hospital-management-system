package com.example.meal.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 配置餐饮服务发起 Feign 请求时需要传递的请求头。
 */
@Configuration
public class FeignRequestConfig {

    private static final String AUTHORIZATION_HEADER =
            "Authorization";

    private static final String REQUEST_ID_HEADER =
            "X-Request-Id";

    /**
     * 将当前患者的JWT和请求ID传给医院主服务。
     */
    @Bean
    public RequestInterceptor
    forwardRequestHeadersInterceptor() {

        return requestTemplate -> {

            /*
             * 获取当前正在处理的HTTP请求。
             *
             * Feign调用发生在患者下单请求中，
             * 因此这里可以取得患者原始请求。
             */
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder
                                    .getRequestAttributes();

            /*
             * 如果当前不是HTTP请求，
             * 例如RocketMQ消费者触发的后台任务，
             * 就没有可转发的请求头。
             */
            if (attributes == null) {
                return;
            }

            HttpServletRequest request =
                    attributes.getRequest();

            forwardHeader(
                    request,
                    requestTemplate,
                    AUTHORIZATION_HEADER
            );

            forwardHeader(
                    request,
                    requestTemplate,
                    REQUEST_ID_HEADER
            );
        };
    }

    /**
     * 请求头存在时，将它复制到Feign请求。
     */
    private void forwardHeader(
            HttpServletRequest request,
            feign.RequestTemplate requestTemplate,
            String headerName) {

        String headerValue =
                request.getHeader(headerName);

        if (StringUtils.hasText(headerValue)) {

            requestTemplate.header(
                    headerName,
                    headerValue
            );
        }
    }
}