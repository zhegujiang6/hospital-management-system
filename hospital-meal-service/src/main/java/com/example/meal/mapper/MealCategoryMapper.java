package com.example.meal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meal.entity.MealCategory;
import com.example.meal.vo.MealCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜品分类数据访问接口，负责操作meal_category表。
 */
@Mapper
public interface MealCategoryMapper
        extends BaseMapper<MealCategory> {

    /**
     * 查询分类及其所属餐厅名称。
     * storeId为空时查询全部，否则只查询指定餐厅。
     *
     * @param storeId 可选的餐厅ID
     * @return 分类列表
     */
    @Select("""
        <script>
        SELECT
            c.id,
            c.store_id AS storeId,
            s.name AS storeName,
            c.name,
            c.sort_order AS sortOrder,
            c.status,
            c.create_time AS createTime,
            c.update_time AS updateTime
        FROM meal_category c
        INNER JOIN meal_store s
            ON s.id = c.store_id
        <where>
            <if test="storeId != null">
                c.store_id = #{storeId}
            </if>
        </where>
        ORDER BY c.sort_order ASC, c.id ASC
        </script>
        """)
    List<MealCategoryVO> selectCategoryList(
            @Param("storeId") Long storeId
    );
}