package com.example.meal.mapper;

import com.example.meal.vo.MealMenuItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 创建订单时需要的菜品、库存关联查询。
 */
@Mapper
public interface MealOrderQueryMapper {

    /**
     * 根据多个库存ID一次查询菜品、价格、日期和餐次。
     *
     * @param stockIds 前端提交的库存ID集合
     * @return 可以参与下单的菜品库存信息
     */
    @Select("""
        <script>
        SELECT
            stock.id AS stockId,
            product.id AS productId,
            product.product_no AS productNo,
            product.name AS productName,
            product.price,
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
        WHERE stock.id IN
        <foreach
            collection="stockIds"
            item="stockId"
            open="("
            separator=","
            close=")">
            #{stockId}
        </foreach>
          AND product.status = 'ON_SALE'
          AND category.status = 'ENABLED'
          AND store.status = 'ENABLED'
        ORDER BY stock.id ASC
        </script>
        """)
    List<MealMenuItemVO> selectOrderableItems(
            @Param("stockIds") List<Long> stockIds
    );
}