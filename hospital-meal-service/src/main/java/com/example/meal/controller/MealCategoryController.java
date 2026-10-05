package com.example.meal.controller;

import com.example.meal.common.Result;
import com.example.meal.dto.MealCategoryCreateRequest;
import com.example.meal.service.MealCategoryService;
import com.example.meal.vo.MealCategoryVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员菜品分类管理接口。
 */
@RestController
@RequestMapping("/meal/admin/categories")
public class MealCategoryController {

    private final MealCategoryService mealCategoryService;

    public MealCategoryController(
            MealCategoryService mealCategoryService) {

        this.mealCategoryService = mealCategoryService;
    }

    /**
     * 接收管理员提交的参数并新增菜品分类。
     *
     * @param request 分类新增参数
     * @return 新增成功后的分类ID
     */
    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody MealCategoryCreateRequest request) {

        Long categoryId =
                mealCategoryService.create(request);

        return Result.success(categoryId);
    }

    /**
     * 查询菜品分类，可以按照餐厅筛选。
     *
     * @param storeId 可选的餐厅ID
     * @return 分类列表
     */
    @GetMapping
    public Result<List<MealCategoryVO>> list(
            @RequestParam(required = false)
            Long storeId) {

        List<MealCategoryVO> categories =
                mealCategoryService.list(storeId);

        return Result.success(categories);
    }
}