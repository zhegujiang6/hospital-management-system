package com.example.meal.dto;

import com.example.meal.enums.MealPeriod;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * 新增菜品分时库存时接收的参数。
 */
public class MealProductStockCreateRequest {

    @NotNull(message = "菜品不能为空")
    @Positive(message = "菜品ID必须大于0")
    private Long productId;

    @NotNull(message = "供应日期不能为空")
    @FutureOrPresent(message = "供应日期不能早于今天")
    private LocalDate serviceDate;

    @NotNull(message = "供应餐次不能为空")
    private MealPeriod mealPeriod;

    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "总库存不能小于0")
    private Integer totalStock;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }
}