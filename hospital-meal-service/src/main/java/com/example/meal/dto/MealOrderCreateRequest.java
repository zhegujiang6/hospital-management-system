package com.example.meal.dto;

import com.example.meal.enums.MealDeliveryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 患者创建餐饮订单时提交的参数。
 */
public class MealOrderCreateRequest {

    @NotNull(message = "配送类型不能为空")
    private MealDeliveryType deliveryType;


    @Size(max = 100, message = "收餐人姓名不能超过100个字符")
    private String recipientName;

    @Size(max = 30, message = "联系电话不能超过30个字符")
    private String recipientPhone;

    @Size(max = 255, message = "配送位置不能超过255个字符")
    private String deliveryLocation;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    /**
     * @Valid表示继续校验List里面每一个菜品参数。
     */
    @Valid
    @NotEmpty(message = "订单中至少需要一个菜品")
    @Size(max = 20, message = "一张订单最多选择20种菜品")
    private List<MealOrderItemCreateRequest> items;

    public MealDeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(
            MealDeliveryType deliveryType) {

        this.deliveryType = deliveryType;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(
            String recipientName) {

        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(
            String recipientPhone) {

        this.recipientPhone = recipientPhone;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(
            String deliveryLocation) {

        this.deliveryLocation = deliveryLocation;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<MealOrderItemCreateRequest> getItems() {
        return items;
    }

    public void setItems(
            List<MealOrderItemCreateRequest> items) {

        this.items = items;
    }
}