package com.example.meal.controller;

import com.example.meal.common.Result;
import com.example.meal.dto.MealProductCreateRequest;
import com.example.meal.service.MealProductService;
import com.example.meal.vo.MealProductVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员菜品管理接口。
 */
@RestController
@RequestMapping("/meal/admin/products")
public class MealProductController {

    private final MealProductService mealProductService;

    public MealProductController(
            MealProductService mealProductService) {

        this.mealProductService = mealProductService;
    }

    /**
     * 接收管理员提交的参数并新增菜品。
     *
     * @param request 菜品新增参数
     * @return 新增成功后的菜品ID
     */
    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody MealProductCreateRequest request) {

        Long productId =
                mealProductService.create(request);

        return Result.success(productId);
    }

    /**
     * 查询菜品，可以按照餐厅、分类和状态筛选。
     *
     * @param storeId 可选的餐厅ID
     * @param categoryId 可选的分类ID
     * @param status 可选的菜品状态
     * @return 菜品列表
     */
    @GetMapping
    public Result<List<MealProductVO>> list(
            @RequestParam(required = false)
            Long storeId,
            @RequestParam(required = false)
            Long categoryId,
            @RequestParam(required = false)
            String status) {

        List<MealProductVO> products =
                mealProductService.list(
                        storeId,
                        categoryId,
                        status
                );

        return Result.success(products);
    }
}