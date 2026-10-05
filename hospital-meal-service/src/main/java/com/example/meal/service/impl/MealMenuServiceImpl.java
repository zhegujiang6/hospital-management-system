package com.example.meal.service.impl;

import com.example.meal.enums.MealPeriod;
import com.example.meal.exception.BusinessException;
import com.example.meal.mapper.MealMenuMapper;
import com.example.meal.service.MealMenuService;
import com.example.meal.vo.MealMenuItemVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者菜单业务实现类。
 */
@Service
public class MealMenuServiceImpl
        implements MealMenuService {

    private final MealMenuMapper mealMenuMapper;

    public MealMenuServiceImpl(
            MealMenuMapper mealMenuMapper) {

        this.mealMenuMapper =
                mealMenuMapper;
    }

    /**
     * 校验日期和餐次，然后查询真正可售的菜品。
     *
     * @param serviceDate 供应日期
     * @param mealPeriod 供应餐次
     * @return 可售菜单列表
     */
    @Override
    public List<MealMenuItemVO> listAvailableMenu(
            LocalDate serviceDate,
            MealPeriod mealPeriod) {

        if (serviceDate == null) {
            throw new BusinessException("供应日期不能为空");
        }

        if (mealPeriod == null) {
            throw new BusinessException("供应餐次不能为空");
        }

        if (serviceDate.isBefore(LocalDate.now())) {
            throw new BusinessException(
                    "不能查询过去日期的菜单"
            );
        }

        return mealMenuMapper
                .selectAvailableMenu(
                        serviceDate,
                        mealPeriod
                );
    }
}