package com.example.mq.message;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付成功消息。
 * 它不是数据库实体，也不是前端DTO，
 * 而是支付模块发送给其他业务的一张消息清单。
 */
public class PaymentSuccessMessage {

    // 每条消息自己的唯一编号，以后用于防止重复消费
    private String eventId;

    private Long paymentId;

    private Long registrationOrderId;

    private Long patientId;

    private String paymentNo;

    private BigDecimal amount;

    private LocalDateTime paidTime;

    // RabbitMQ把JSON转回Java对象时需要无参构造方法
    public PaymentSuccessMessage() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getRegistrationOrderId() {
        return registrationOrderId;
    }

    public void setRegistrationOrderId(
            Long registrationOrderId) {

        this.registrationOrderId =
                registrationOrderId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(LocalDateTime paidTime) {
        this.paidTime = paidTime;
    }
}