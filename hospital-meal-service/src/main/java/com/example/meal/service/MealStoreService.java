package com.example.meal.service;

import com.example.meal.dto.MealStoreCreateRequest;
import com.example.meal.vo.MealStoreVO;

import java.util.List;

/**
 * 餐厅业务接口。
 */
public interface MealStoreService {

    /**
     * 新增一个餐厅。
     *
     * @param request 新增餐厅的请求参数
     * @return 新增成功后的餐厅ID
     */
    Long create(MealStoreCreateRequest request);

    /**
     * 查询全部餐厅，供管理员查看。
     *
     * @return 餐厅列表
     */
    List<MealStoreVO> list();


}