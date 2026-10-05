package com.example.mq.consumer;

import com.example.config.RabbitMqConfig;
import com.example.mq.message.PaymentSuccessMessage;
import com.example.payment.MqConsumeRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentSuccessMessageConsumer {

    private static final String CONSUMER_NAME =
            "payment-success-notification";

    private static final Logger log =
            LoggerFactory.getLogger(
                    PaymentSuccessMessageConsumer.class
            );

    private final MqConsumeRecordMapper
            mqConsumeRecordMapper;

    public PaymentSuccessMessageConsumer(
            MqConsumeRecordMapper mqConsumeRecordMapper) {

        this.mqConsumeRecordMapper =
                mqConsumeRecordMapper;
    }

    /**
     * 监听支付成功队列。
     * 队列中出现消息时，Spring会自动调用这个方法。
     */
    @RabbitListener(
            queues = RabbitMqConfig.PAYMENT_SUCCESS_QUEUE
    )
    @Transactional(rollbackFor = Exception.class)
    public void consume(
            PaymentSuccessMessage message) {

        /*
         * eventId和consumerName在数据库中有唯一索引。
         * 第一次消费插入成功返回1；重复投递返回0并直接结束。
         */
        int inserted = mqConsumeRecordMapper
                .insertIfAbsent(
                        message.getEventId(),
                        CONSUMER_NAME
                );

        if (inserted == 0) {
            log.info(
                    "支付成功消息已处理，跳过重复消费，eventId={}",
                    message.getEventId()
            );
            return;
        }

        // 证明消费者已经从RabbitMQ取得了消息
        log.info(
                "收到支付成功消息，eventId={}，paymentId={}，registrationOrderId={}",
                message.getEventId(),
                message.getPaymentId(),
                message.getRegistrationOrderId()
        );

        /*
         * 目前没有接短信平台，
         * 所以先通过日志模拟给患者发送挂号成功通知。
         */
        log.info(
                "模拟发送挂号成功通知：patientId={}，paymentNo={}，amount={}，paidTime={}",
                message.getPatientId(),
                message.getPaymentNo(),
                message.getAmount(),
                message.getPaidTime()
        );

        /*
         * 如果后续真实通知抛出异常，当前数据库事务会回滚消费记录，
         * RabbitMQ重试时仍然可以再次处理这条消息。
         */
    }
}
