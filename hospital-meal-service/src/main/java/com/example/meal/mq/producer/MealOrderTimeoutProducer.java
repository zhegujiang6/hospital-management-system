package com.example.meal.mq.producer;

import com.example.meal.exception.BusinessException;
import com.example.meal.mq.message.MealOrderTimeoutMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 订单超时消息发送器。
 *
 * 下单成功后，由它向RocketMQ发送一条延时消息。
 */
@Component
public class MealOrderTimeoutProducer {

    private static final Logger log =
            LoggerFactory.getLogger(MealOrderTimeoutProducer.class);

    private static final String MESSAGE_TAG = "ORDER_TIMEOUT";

    private final ClientServiceProvider provider;
    private final Producer producer;
    private final ObjectMapper objectMapper;
    private final String topic;

    public MealOrderTimeoutProducer(
            ClientServiceProvider provider,
            @Qualifier("mealOrderTimeoutRocketMqProducer")
            Producer producer,
            ObjectMapper objectMapper,
            @Value("${rocketmq.topic.order-timeout}")
            String topic) {

        this.provider = provider;
        this.producer = producer;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    /**
     * 发送订单超时延时消息。
     *
     * RocketMQ不会立即投递消息，
     * 而是等到paymentDeadline到达后再交给消费者。
     */
    public void sendOrderTimeoutMessage(
            Long orderId,
            LocalDateTime paymentDeadline) {

        MealOrderTimeoutMessage timeoutMessage =
                new MealOrderTimeoutMessage(
                        orderId,
                        paymentDeadline
                );

        byte[] messageBody;

        try {
            // 把Java对象转换成JSON字节数组，作为消息内容
            messageBody =
                    objectMapper.writeValueAsBytes(timeoutMessage);
        } catch (JsonProcessingException e) {
            log.error("订单超时消息序列化失败，orderId={}",
                    orderId, e);
            throw new BusinessException("订单超时消息创建失败");
        }

        // LocalDateTime转换成RocketMQ需要的时间戳
        long deliveryTimestamp = paymentDeadline
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        Message message = provider.newMessageBuilder()
                .setTopic(topic)
                .setTag(MESSAGE_TAG)
                .setKeys("meal-order-" + orderId)
                .setDeliveryTimestamp(deliveryTimestamp)
                .setBody(messageBody)
                .build();

        try {
            SendReceipt sendReceipt = producer.send(message);

            log.info(
                    "订单超时消息发送成功，orderId={}，messageId={}，投递时间={}",
                    orderId,
                    sendReceipt.getMessageId(),
                    paymentDeadline
            );
        } catch (ClientException e) {
            log.error("订单超时消息发送失败，orderId={}",
                    orderId, e);
            throw new BusinessException("订单超时消息发送失败");
        }
    }
}