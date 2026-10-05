package com.example.meal.vo;

import com.example.meal.enums.MealDeliveryType;
import com.example.meal.enums.MealOrderStatus;
import com.example.meal.enums.MealPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者订单列表中的一条订单。
 */
public class MealOrderListVO {

    private Long orderId;

    private String orderNo;

    private LocalDate serviceDate;

    private MealPeriod mealPeriod;

    private BigDecimal totalAmount;

    private MealOrderStatus status;

    private MealDeliveryType deliveryType;

    private String recipientName;

    private String deliveryLocation;

    /**
     * 订单包含多少种菜品。
     */
    private Integer itemKindCount;

    /**
     * 订单一共购买多少份。
     */
    private Integer totalQuantity;

    private LocalDateTime paymentDeadline;

    private LocalDateTime paidTime;

    private LocalDateTime cancelTime;

    private LocalDateTime createTime;
    /**
     * 下单患者ID，管理员查看订单时使用。
     */
    private Long patientId;

    /**
     * 收餐人联系电话。
     */
    private String recipientPhone;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    public MealPeriod getMealPeriod() {
        return mealPeriod;
    }

    public void setMealPeriod(MealPeriod mealPeriod) {
        this.mealPeriod = mealPeriod;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public MealOrderStatus getStatus() {
        return status;
    }

    public void setStatus(MealOrderStatus status) {
        this.status = status;
    }

    public MealDeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(MealDeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public Integer getItemKindCount() {
        return itemKindCount;
    }

    public void setItemKindCount(Integer itemKindCount) {
        this.itemKindCount = itemKindCount;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public LocalDateTime getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(LocalDateTime paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public LocalDateTime getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(LocalDateTime paidTime) {
        this.paidTime = paidTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}