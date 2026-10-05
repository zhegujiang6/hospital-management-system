package com.example.meal.service;

import com.example.meal.dto.MealProductStockAdjustRequest;
import com.example.meal.dto.MealProductStockCreateRequest;
import com.example.meal.dto.MealProductStockUpdateRequest;
import com.example.meal.enums.MealPeriod;
import com.example.meal.vo.MealProductStockVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 菜品分时库存业务接口。
 */
public interface MealProductStockService {

    /**
     * 为指定菜品设置某天某餐次的初始库存。
     *
     * @param request 库存新增参数
     * @return 新增成功后的库存ID
     */
    Long create(MealProductStockCreateRequest request);

    /**
     * 按照餐厅、菜品、日期和餐次查询库存。
     *
     * @param storeId 可选的餐厅ID
     * @param productId 可选的菜品ID
     * @param serviceDate 可选的供应日期
     * @param mealPeriod 可选的餐次
     * @return 库存列表
     */
    List<MealProductStockVO> list(
            Long storeId,
            Long productId,
            LocalDate serviceDate,
            MealPeriod mealPeriod
    );

    /**
     * 修改库存总量，并保留已经售出的数量。
     *
     * @param stockId 库存ID
     * @param request 库存修改参数
     */
    void updateTotalStock(
            Long stockId,
            MealProductStockUpdateRequest request
    );

    /**
     * 增加或减少指定库存记录的库存数量。
     *
     * @param stockId 库存记录ID
     * @param request 库存调整参数
     */
    void adjustStock(
            Long stockId,
            MealProductStockAdjustRequest request
    );
}