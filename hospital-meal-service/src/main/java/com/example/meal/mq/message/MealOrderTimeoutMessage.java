package com.example.meal.mq.message;

import java.time.LocalDateTime;

/**
 * 订单超时消息。
 *
 * RocketMQ 到时间后会把这个消息交给消费者，
 * 消费者再根据 orderId 判断订单是否仍然未支付。
 */
public class MealOrderTimeoutMessage {

    /**
     * 需要检查的订单ID。
     */
    private Long orderId;

    /**
     * 订单支付截止时间。
     */
    private LocalDateTime paymentDeadline;

    public MealOrderTimeoutMessage() {
    }

    public MealOrderTimeoutMessage(Long orderId,
                                   LocalDateTime paymentDeadline) {
        this.orderId = orderId;
        this.paymentDeadline = paymentDeadline;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(LocalDateTime paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }
}