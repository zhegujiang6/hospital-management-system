package com.example.meal.controller;

import com.example.meal.common.Result;
import com.example.meal.enums.MealPeriod;
import com.example.meal.service.MealMenuService;
import com.example.meal.vo.MealMenuItemVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者菜单查询接口。
 */
@RestController
@RequestMapping("/meal/patient/menu")
public class MealMenuController {

    private final MealMenuService mealMenuService;

    public MealMenuController(
            MealMenuService mealMenuService) {

        this.mealMenuService = mealMenuService;
    }

    /**
     * 查询指定日期和餐次的可售菜单。
     *
     * @param serviceDate 供应日期
     * @param mealPeriod 供应餐次
     * @return 可售菜单
     */
    @GetMapping
    public Result<List<MealMenuItemVO>> listAvailableMenu(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate serviceDate,
            @RequestParam
            MealPeriod mealPeriod) {

        List<MealMenuItemVO> menuItems =
                mealMenuService.listAvailableMenu(
                        serviceDate,
                        mealPeriod
                );

        return Result.success(menuItems);
    }
}