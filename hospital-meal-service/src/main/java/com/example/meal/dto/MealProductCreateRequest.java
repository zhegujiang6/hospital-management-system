package com.example.meal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 新增菜品时接收的参数。
 */
public class MealProductCreateRequest {

    @NotNull(message = "所属分类不能为空")
    @Positive(message = "分类ID必须大于0")
    private Long categoryId;

    @NotBlank(message = "菜品编号不能为空")
    @Size(max = 40, message = "菜品编号不能超过40个字符")
    private String productNo;

    @NotBlank(message = "菜品名称不能为空")
    @Size(max = 100, message = "菜品名称不能超过100个字符")
    private String name;

    @Size(max = 1000, message = "菜品描述不能超过1000个字符")
    private String description;

    @NotNull(message = "菜品价格不能为空")
    @DecimalMin(value = "0.00", message = "菜品价格不能小于0")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "菜品价格最多8位整数和2位小数"
    )
    private BigDecimal price;

    @Size(max = 500, message = "图片地址不能超过500个字符")
    private String imageUrl;

    @Size(max = 255, message = "饮食标签不能超过255个字符")
    private String dietaryTags;

    @Size(max = 500, message = "过敏原信息不能超过500个字符")
    private String allergenInfo;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductNo() {
        return productNo;
    }

    public void setProductNo(String productNo) {
        this.productNo = productNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDietaryTags() {
        return dietaryTags;
    }

    public void setDietaryTags(String dietaryTags) {
        this.dietaryTags = dietaryTags;
    }

    public String getAllergenInfo() {
        return allergenInfo;
    }

    public void setAllergenInfo(String allergenInfo) {
        this.allergenInfo = allergenInfo;
    }
}