package com.example.meal.vo;

import com.example.meal.enums.MealOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建订单成功后返回给前端的信息。
 */
public class MealOrderCreateVO {

    private final Long orderId;

    private final String orderNo;

    private final BigDecimal totalAmount;

    private final MealOrderStatus status;

    private final LocalDateTime paymentDeadline;

    public MealOrderCreateVO(
            Long orderId,
            String orderNo,
            BigDecimal totalAmount,
            MealOrderStatus status,
            LocalDateTime paymentDeadline) {

        this.orderId = orderId;
        this.orderNo = orderNo;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentDeadline = paymentDeadline;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public MealOrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getPaymentDeadline() {
        return paymentDeadline;
    }
}