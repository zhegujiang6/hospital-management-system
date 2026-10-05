package com.example.meal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 创建订单时提交的单个菜品。
 */
public class MealOrderItemCreateRequest {

    /**
     * 指定日期和餐次下的库存ID。
     */
    @NotNull(message = "库存ID不能为空")
    @Positive(message = "库存ID必须大于0")
    private Long stockId;

    /**
     * 购买数量。
     */
    @NotNull(message = "购买数量不能为空")
    @Positive(message = "购买数量必须大于0")
    private Integer quantity;

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}