package com.example.meal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增餐厅时，前端需要提交的参数。
 */
public class MealStoreCreateRequest {

    @NotBlank(message = "餐厅编号不能为空")
    @Size(max = 30, message = "餐厅编号不能超过30个字符")
    private String storeNo;

    @NotBlank(message = "餐厅名称不能为空")
    @Size(max = 100, message = "餐厅名称不能超过100个字符")
    private String name;

    @NotBlank(message = "餐厅位置不能为空")
    @Size(max = 200, message = "餐厅位置不能超过200个字符")
    private String location;

    @Size(max = 30, message = "联系电话不能超过30个字符")
    private String phone;

    public String getStoreNo() {
        return storeNo;
    }

    public void setStoreNo(String storeNo) {
        this.storeNo = storeNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}