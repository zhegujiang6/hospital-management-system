package com.example.meal.vo;

import com.example.meal.enums.MealDeliveryType;
import com.example.meal.enums.MealOrderStatus;
import com.example.meal.enums.MealPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 返回给患者的完整订单详情。
 */
public class MealOrderDetailVO {

    private Long orderId;

    private String orderNo;

    private LocalDate serviceDate;

    private MealPeriod mealPeriod;

    private BigDecimal totalAmount;

    private MealOrderStatus status;

    private MealDeliveryType deliveryType;

    private String recipientName;

    private String recipientPhone;

    private String deliveryLocation;

    private String remark;

    private LocalDateTime paymentDeadline;

    private LocalDateTime paidTime;

    private LocalDateTime cancelTime;

    private LocalDateTime createTime;

    private List<MealOrderItemVO> items;

    /**
     * 下单患者ID，管理员查看详情时使用。
     */
    private Long patientId;


    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
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

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public List<MealOrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<MealOrderItemVO> items) {
        this.items = items;
    }
}