package com.example.meal.controller;

import com.example.meal.common.Result;
import com.example.meal.dto.MealProductStockAdjustRequest;
import com.example.meal.dto.MealProductStockCreateRequest;
import com.example.meal.dto.MealProductStockUpdateRequest;
import com.example.meal.enums.MealPeriod;
import com.example.meal.service.MealProductStockService;
import com.example.meal.vo.MealProductStockVO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理员菜品分时库存接口。
 */
@RestController
@RequestMapping("/meal/admin/stocks")
public class MealProductStockController {

    private final MealProductStockService mealProductStockService;

    public MealProductStockController(
            MealProductStockService mealProductStockService) {

        this.mealProductStockService =
                mealProductStockService;
    }

    /**
     * 接收管理员提交的库存信息并创建分时库存。
     *
     * @param request 库存新增参数
     * @return 新增成功后的库存ID
     */
    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody
            MealProductStockCreateRequest request) {

        Long stockId =
                mealProductStockService.create(request);

        return Result.success(stockId);
    }
    /**
     * 按照条件查询菜品分时库存。
     *
     * @param storeId 可选的餐厅ID
     * @param productId 可选的菜品ID
     * @param serviceDate 可选的供应日期
     * @param mealPeriod 可选的餐次
     * @return 库存列表
     */
    @GetMapping
    public Result<List<MealProductStockVO>> list(
            @RequestParam(required = false)
            Long storeId,
            @RequestParam(required = false)
            Long productId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate serviceDate,
            @RequestParam(required = false)
            MealPeriod mealPeriod) {

        List<MealProductStockVO> stocks =
                mealProductStockService.list(
                        storeId,
                        productId,
                        serviceDate,
                        mealPeriod
                );

        return Result.success(stocks);
    }

    /**
     * 修改指定库存记录的总库存。
     *
     * @param stockId 库存ID
     * @param request 库存修改参数
     * @return 操作成功结果
     */
    @PutMapping("/{stockId}")
    public Result<Void> updateTotalStock(
            @PathVariable Long stockId,
            @Valid
            @RequestBody
            MealProductStockUpdateRequest request) {

        mealProductStockService.updateTotalStock(
                stockId,
                request
        );

        return Result.success(null);
    }

    /**
     * 增加或减少指定库存记录的库存。
     *
     * @param stockId 库存记录ID
     * @param request 库存调整参数
     * @return 操作成功结果
     */
    @PatchMapping("/{stockId}/adjust")
    public Result<Void> adjustStock(
            @PathVariable Long stockId,
            @Valid
            @RequestBody
            MealProductStockAdjustRequest request) {

        mealProductStockService.adjustStock(
                stockId,
                request
        );

        return Result.success(null);
    }
}