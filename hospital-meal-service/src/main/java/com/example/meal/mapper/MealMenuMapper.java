package com.example.meal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meal.enums.MealPeriod;
import com.example.meal.vo.MealMenuItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;


/**
 * 患者菜单数据访问接口。
 */
@Mapper
public interface MealMenuMapper{
    /**
     * 查询指定日期和餐次下真正可售的患者菜单。
     *
     * 只有餐厅、分类、菜品都处于启用状态，
     * 并且可用库存大于0的菜品才会返回。
     *
     * @param serviceDate 供应日期
     * @param mealPeriod 供应餐次
     * @return 可售菜单列表
     */
    @Select("""
    SELECT
        stock.id AS stockId,
        store.id AS storeId,
        store.name AS storeName,
        category.id AS categoryId,
        category.name AS categoryName,
        product.id AS productId,
        product.product_no AS productNo,
        product.name AS productName,
        product.description,
        product.price,
        product.image_url AS imageUrl,
        product.dietary_tags AS dietaryTags,
        product.allergen_info AS allergenInfo,
        stock.service_date AS serviceDate,
        stock.meal_period AS mealPeriod,
        stock.available_stock AS availableStock
    FROM meal_product_stock stock
    INNER JOIN meal_product product
        ON product.id = stock.product_id
    INNER JOIN meal_category category
        ON category.id = product.category_id
    INNER JOIN meal_store store
        ON store.id = category.store_id
    WHERE stock.service_date = #{serviceDate}
      AND stock.meal_period = #{mealPeriod}
      AND stock.available_stock > 0
      AND product.status = 'ON_SALE'
      AND category.status = 'ENABLED'
      AND store.status = 'ENABLED'
    ORDER BY
        store.id ASC,
        category.sort_order ASC,
        product.id ASC
    """)
    List<MealMenuItemVO> selectAvailableMenu(
            @Param("serviceDate") LocalDate serviceDate,
            @Param("mealPeriod") MealPeriod mealPeriod
    );
}
