package com.example.meal.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 增加或减少库存时接收的参数。
 */
public class MealProductStockAdjustRequest {

    /**
     * 库存变化量。
     * 正数表示增加库存，例如20；
     * 负数表示减少库存，例如-10。
     */
    @NotNull(message = "库存变化量不能为空")
    private Integer quantityDelta;

    public Integer getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(Integer quantityDelta) {
        this.quantityDelta = quantityDelta;
    }
}