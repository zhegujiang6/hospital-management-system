package com.example.meal.controller;

import com.example.meal.common.Result;
import com.example.meal.dto.MealStoreCreateRequest;
import com.example.meal.service.MealStoreService;
import com.example.meal.vo.MealStoreVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员餐厅管理接口。
 */
@RestController
@RequestMapping("/meal/admin/stores")
public class MealStoreController {

    private final MealStoreService mealStoreService;

    public MealStoreController(
            MealStoreService mealStoreService) {

        this.mealStoreService = mealStoreService;
    }

    /**
     * 接收管理员提交的餐厅信息并新增餐厅。
     *
     * @param request 新增餐厅的请求参数
     * @return 新增成功后的餐厅ID
     */
    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody MealStoreCreateRequest request) {

        Long storeId = mealStoreService.create(request);

        return Result.success(storeId);
    }

    /**
     * 查询全部餐厅。
     *
     * @return 餐厅列表
     */
    @GetMapping
    public Result<List<MealStoreVO>> list() {

        List<MealStoreVO> stores = mealStoreService.list();

        return Result.success(stores);
    }
}