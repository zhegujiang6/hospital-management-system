package com.example.meal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meal.entity.MealProduct;
import com.example.meal.vo.MealProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜品数据访问接口，负责操作meal_product表。
 */
@Mapper
public interface MealProductMapper
        extends BaseMapper<MealProduct> {

    /**
     * 关联餐厅和分类查询菜品。
     *
     * @param storeId 可选的餐厅ID
     * @param categoryId 可选的分类ID
     * @param status 可选的菜品状态
     * @return 菜品列表
     */
    @Select("""
        <script>
        SELECT
            p.id,
            s.id AS storeId,
            s.name AS storeName,
            c.id AS categoryId,
            c.name AS categoryName,
            p.product_no AS productNo,
            p.name,
            p.description,
            p.price,
            p.image_url AS imageUrl,
            p.dietary_tags AS dietaryTags,
            p.allergen_info AS allergenInfo,
            p.status,
            p.create_time AS createTime,
            p.update_time AS updateTime
        FROM meal_product p
        INNER JOIN meal_category c
            ON c.id = p.category_id
        INNER JOIN meal_store s
            ON s.id = c.store_id
        <where>
            <if test="storeId != null">
                s.id = #{storeId}
            </if>
            <if test="categoryId != null">
                AND c.id = #{categoryId}
            </if>
            <if test="status != null and status != ''">
                AND p.status = #{status}
            </if>
        </where>
        ORDER BY p.id DESC
        </script>
        """)
    List<MealProductVO> selectProductList(
            @Param("storeId") Long storeId,
            @Param("categoryId") Long categoryId,
            @Param("status") String status
    );
}