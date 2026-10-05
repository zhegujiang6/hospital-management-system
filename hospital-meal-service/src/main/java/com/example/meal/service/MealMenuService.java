package com.example.meal.service;

import com.example.meal.enums.MealPeriod;
import com.example.meal.vo.MealMenuItemVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者菜单业务接口。
 */
public interface MealMenuService {

    /**
     * 查询指定日期和餐次的可售菜单。
     *
     * @param serviceDate 供应日期
     * @param mealPeriod 供应餐次
     * @return 可售菜单列表
     */
    List<MealMenuItemVO> listAvailableMenu(
            LocalDate serviceDate,
            MealPeriod mealPeriod
    );

}