package com.example.meal.mq.consumer;

import com.example.meal.mq.message.MealOrderTimeoutMessage;
import com.example.meal.service.MealOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * 订单超时消息消费者。
 *
 * 负责接收RocketMQ消息、解析消息内容，
 * 然后调用订单Service处理超时业务。
 */
@Component
public class MealOrderTimeoutConsumer
        implements MessageListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MealOrderTimeoutConsumer.class
            );

    private final ObjectMapper objectMapper;
    private final MealOrderService mealOrderService;

    public MealOrderTimeoutConsumer(
            ObjectMapper objectMapper,
            MealOrderService mealOrderService) {

        this.objectMapper = objectMapper;
        this.mealOrderService = mealOrderService;
    }

    /**
     * 消费一条订单超时消息。
     *
     * 返回SUCCESS表示消息处理完成；
     * 返回FAILURE表示处理失败，需要RocketMQ稍后重试。
     */
    @Override
    public ConsumeResult consume(
            MessageView messageView) {

        try {
            /*
             * RocketMQ提供的消息内容是ByteBuffer，
             * 先转换成普通byte数组。
             */
            ByteBuffer bodyBuffer =
                    messageView.getBody();

            byte[] body =
                    new byte[bodyBuffer.remaining()];

            bodyBuffer.get(body);

            /*
             * 把JSON消息重新转换成Java对象。
             */
            MealOrderTimeoutMessage timeoutMessage =
                    objectMapper.readValue(
                            body,
                            MealOrderTimeoutMessage.class
                    );

            if (timeoutMessage.getOrderId() == null) {
                throw new IllegalArgumentException(
                        "订单超时消息缺少orderId"
                );
            }

            /*
             * 消费者只负责转交消息。
             *
             * 是否超时、能否取消、是否恢复库存，
             * 全部由Service完成。
             */
            mealOrderService.cancelExpiredOrder(
                    timeoutMessage.getOrderId()
            );

            log.info(
                    "订单超时消息处理成功，orderId={}，messageId={}",
                    timeoutMessage.getOrderId(),
                    messageView.getMessageId()
            );

            return ConsumeResult.SUCCESS;

        } catch (Exception e) {

            log.error(
                    "订单超时消息处理失败，messageId={}",
                    messageView.getMessageId(),
                    e
            );

            return ConsumeResult.FAILURE;
        }
    }
}