package com.example.meal.service;

import com.example.meal.dto.MealProductCreateRequest;
import com.example.meal.vo.MealProductVO;

import java.util.List;

/**
 * 菜品业务接口。
 */
public interface MealProductService {

    /**
     * 新增一个菜品。
     *
     * @param request 菜品新增参数
     * @return 新增成功后的菜品ID
     */
    Long create(MealProductCreateRequest request);

    /**
     * 查询菜品，可以按照餐厅、分类和状态筛选。
     *
     * @param storeId 可选的餐厅ID
     * @param categoryId 可选的分类ID
     * @param status 可选的菜品状态
     * @return 菜品列表
     */
    List<MealProductVO> list(
            Long storeId,
            Long categoryId,
            String status
    );

}