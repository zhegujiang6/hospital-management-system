package com.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    // 支付业务交换机名称
    public static final String PAYMENT_EXCHANGE =
            "hospital.payment.exchange";

    // 支付成功队列名称
    public static final String PAYMENT_SUCCESS_QUEUE =
            "hospital.payment.success.queue";

    // 支付成功消息的路由键
    public static final String PAYMENT_SUCCESS_ROUTING_KEY =
            "payment.success";


    /**
     * 创建支付交换机。
     * durable=true表示RabbitMQ重启后交换机仍然存在。
     */
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(
                PAYMENT_EXCHANGE,
                true,
                false
        );
    }


    /**
     * 创建支付成功队列。
     * 消费者以后从这个队列中获取消息。
     */
    @Bean
    public Queue paymentSuccessQueue() {

        return QueueBuilder
                // 创建持久化的正常支付成功队列
                .durable(PAYMENT_SUCCESS_QUEUE)

                // 消息处理失败后发送到哪个死信交换机
                .deadLetterExchange(
                        PAYMENT_DEAD_LETTER_EXCHANGE
                )

                // 进入死信交换机时使用什么路由键
                .deadLetterRoutingKey(
                        PAYMENT_DEAD_LETTER_ROUTING_KEY
                )
                .build();
    }

    /**
     * 把支付成功队列绑定到支付交换机。
     * 只有路由键为payment.success的消息才会进入这个队列。
     */
    @Bean
    public Binding paymentSuccessBinding(
            @Qualifier("paymentSuccessQueue")
            Queue paymentSuccessQueue,
            TopicExchange paymentExchange) {

        return BindingBuilder
                .bind(paymentSuccessQueue)
                .to(paymentExchange)
                .with(PAYMENT_SUCCESS_ROUTING_KEY);
    }



    /**
     * Java对象发送到RabbitMQ之前转成JSON，
     * 消费者收到后再把JSON转回Java对象。
     */
    @Bean
    public MessageConverter rabbitMessageConverter(
            ObjectMapper objectMapper) {

        return new Jackson2JsonMessageConverter(
                objectMapper
        );
    }
    @Bean
    public DirectExchange paymentDeadLetterExchange() {

        return new DirectExchange(
                PAYMENT_DEAD_LETTER_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue paymentDeadLetterQueue() {

        return QueueBuilder
                .durable(PAYMENT_DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding paymentDeadLetterBinding(
            @Qualifier("paymentDeadLetterQueue")
            Queue paymentDeadLetterQueue,
            DirectExchange paymentDeadLetterExchange) {

        return BindingBuilder
                .bind(paymentDeadLetterQueue)
                .to(paymentDeadLetterExchange)
                .with(PAYMENT_DEAD_LETTER_ROUTING_KEY);
    }

    // 死信交换机
    public static final String PAYMENT_DEAD_LETTER_EXCHANGE =
            "hospital.payment.dlx";

    // 支付成功死信队列
    public static final String PAYMENT_DEAD_LETTER_QUEUE =
            "hospital.payment.success.dlq";

    // 死信使用的路由键
    public static final String PAYMENT_DEAD_LETTER_ROUTING_KEY =
            "payment.success.dead";
}