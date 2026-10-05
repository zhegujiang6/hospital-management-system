package com.example.meal.service;

import com.example.meal.dto.MealCategoryCreateRequest;
import com.example.meal.vo.MealCategoryVO;

import java.util.List;

/**
 * 菜品分类业务接口。
 */
public interface MealCategoryService{

    /**
     * 在指定餐厅下面新增菜品分类。
     *
     * @param request 分类新增参数
     * @return 新增成功后的分类ID
     */
    Long create(MealCategoryCreateRequest request);

    /**
     * 查询分类，可以根据餐厅ID进行筛选。
     *
     * @param storeId 可选的餐厅ID
     * @return 分类列表
     */
    List<MealCategoryVO> list(Long storeId);
}