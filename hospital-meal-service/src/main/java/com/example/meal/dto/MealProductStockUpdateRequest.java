package com.example.meal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 修改菜品库存总量时接收的参数。
 */
public class MealProductStockUpdateRequest {

    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "总库存不能小于0")
    private Integer totalStock;

    @NotNull(message = "库存版本号不能为空")
    @Min(value = 0, message = "库存版本号不能小于0")
    private Integer version;

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}