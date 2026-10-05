package com.example.mq.producer;

import com.example.config.RabbitMqConfig;
import com.example.mq.message.PaymentSuccessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentSuccessMessageProducer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PaymentSuccessMessageProducer.class
            );

    // RabbitTemplate专门负责向RabbitMQ发送消息
    private final RabbitTemplate rabbitTemplate;

    public PaymentSuccessMessageProducer(
            RabbitTemplate rabbitTemplate) {

        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * AFTER_COMMIT表示：
     * 只有支付数据库事务真正提交成功后，才执行这个方法。
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void send(
            PaymentSuccessMessage message) {

        rabbitTemplate.convertAndSend(
                // 消息发到哪个交换机
                RabbitMqConfig.PAYMENT_EXCHANGE,

                // 消息使用什么路由键
                RabbitMqConfig.PAYMENT_SUCCESS_ROUTING_KEY,

                // 真正发送的消息对象
                message
        );

        log.info(
                "支付成功消息已发送，eventId={}，paymentId={}",
                message.getEventId(),
                message.getPaymentId()
        );
    }
}
