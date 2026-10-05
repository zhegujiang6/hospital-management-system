package com.example.meal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meal.entity.MealStore;
import org.apache.ibatis.annotations.Mapper;

/**
 * 餐厅数据访问接口，负责操作 meal_store 表。
 */
@Mapper
public interface MealStoreMapper extends BaseMapper<MealStore> {
}