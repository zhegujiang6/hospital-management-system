package com.example.meal.config;

import com.example.meal.mq.consumer.MealOrderTimeoutConsumer;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * RocketMQ 客户端配置。
 *
 * 这里负责创建公共配置和消息生产者，
 * 后面的业务类只负责发送消息，不负责重复创建连接。
 */
@Configuration
public class RocketMqConfig {

    /**
     * RocketMQ 客户端服务提供者。
     *
     * 它相当于RocketMQ客户端对象的创建入口，
     * Producer、Consumer以及Message都通过它创建。
     */
    @Bean
    public ClientServiceProvider rocketMqClientServiceProvider() {
        return ClientServiceProvider.loadService();
    }

    /**
     * 创建RocketMQ客户端连接配置。
     */
    @Bean
    public ClientConfiguration rocketMqClientConfiguration(
            @Value("${rocketmq.endpoints}") String endpoints) {

        return ClientConfiguration.newBuilder()
                .setEndpoints(endpoints)
                .enableSsl(false)
                .build();
    }

    /**
     * 创建订单超时消息生产者。
     *
     * setTopics表示这个生产者需要向哪个主题发送消息。
     * destroyMethod表示项目停止时关闭生产者并释放连接。
     */
    @Bean(name = "mealOrderTimeoutRocketMqProducer",
            destroyMethod = "close")
    public Producer mealOrderTimeoutRocketMqProducer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            @Value("${rocketmq.topic.order-timeout}") String topic)
            throws ClientException {

        return provider.newProducerBuilder()
                .setClientConfiguration(clientConfiguration)
                .setTopics(topic)
                .build();
    }
    /**
     * 创建订单超时消息消费者。
     *
     * 消费者只订阅ORDER_TIMEOUT标签的消息，
     * 收到消息后交给MealOrderTimeoutConsumer处理。
     */
    @Bean(name = "mealOrderTimeoutPushConsumer",
            destroyMethod = "close")
    public PushConsumer mealOrderTimeoutPushConsumer(
            ClientServiceProvider provider,
            ClientConfiguration clientConfiguration,
            MealOrderTimeoutConsumer messageListener,
            @Value("${rocketmq.consumer-group.order-timeout}")
            String consumerGroup,
            @Value("${rocketmq.topic.order-timeout}")
            String topic)
            throws ClientException {

        return provider.newPushConsumerBuilder()
                .setClientConfiguration(
                        clientConfiguration
                )
                .setConsumerGroup(
                        consumerGroup
                )
                .setSubscriptionExpressions(
                        Collections.singletonMap(
                                topic,
                                new FilterExpression(
                                        "ORDER_TIMEOUT",
                                        FilterExpressionType.TAG
                                )
                        )
                )
                .setMessageListener(
                        messageListener
                )
                .build();
    }
}