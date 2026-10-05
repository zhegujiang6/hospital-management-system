package com.example.meal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meal.entity.MealProductStock;
import com.example.meal.enums.MealPeriod;
import com.example.meal.vo.MealMenuItemVO;
import com.example.meal.vo.MealProductStockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 菜品分时库存数据访问接口。
 */
@Mapper
public interface MealProductStockMapper
        extends BaseMapper<MealProductStock> {
    /**
     * 关联餐厅、分类和菜品查询分时库存。
     *
     * @param storeId 可选的餐厅ID
     * @param productId 可选的菜品ID
     * @param serviceDate 可选的供应日期
     * @param mealPeriod 可选的餐次
     * @return 库存列表
     */
    @Select("""
        <script>
        SELECT
            stock.id,
            store.id AS storeId,
            store.name AS storeName,
            category.id AS categoryId,
            category.name AS categoryName,
            product.id AS productId,
            product.product_no AS productNo,
            product.name AS productName,
            stock.service_date AS serviceDate,
            stock.meal_period AS mealPeriod,
            stock.total_stock AS totalStock,
            stock.available_stock AS availableStock,
            (
                stock.total_stock
                - stock.available_stock
            ) AS soldStock,
            stock.version,
            stock.update_time AS updateTime
        FROM meal_product_stock stock
        INNER JOIN meal_product product
            ON product.id = stock.product_id
        INNER JOIN meal_category category
            ON category.id = product.category_id
        INNER JOIN meal_store store
            ON store.id = category.store_id
        <where>
            <if test="storeId != null">
                store.id = #{storeId}
            </if>
            <if test="productId != null">
                AND product.id = #{productId}
            </if>
            <if test="serviceDate != null">
                AND stock.service_date = #{serviceDate}
            </if>
            <if test="mealPeriod != null">
                AND stock.meal_period = #{mealPeriod}
            </if>
        </where>
        ORDER BY
            stock.service_date DESC,
            FIELD(
                stock.meal_period,
                'BREAKFAST',
                'LUNCH',
                'DINNER'
            ),
            product.id ASC
        </script>
        """)
    List<MealProductStockVO> selectStockList(
            @Param("storeId") Long storeId,
            @Param("productId") Long productId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("mealPeriod") MealPeriod mealPeriod
    );

    /**
     * 原子增加或减少库存。
     *
     * 一条SQL同时完成库存检查和修改，
     * 防止多个请求并发时出现超卖或库存变成负数。
     *
     * @param stockId 库存记录ID
     * @param quantityDelta 库存变化量，正数增加，负数减少
     * @return 受影响的行数，1表示成功，0表示库存不足或记录不存在
     */
    @Update("""
    UPDATE meal_product_stock
    SET total_stock = total_stock + #{quantityDelta},
        available_stock = available_stock + #{quantityDelta},
        version = version + 1
    WHERE id = #{stockId}
      AND total_stock + #{quantityDelta} >= 0
      AND available_stock + #{quantityDelta} >= 0
    """)
    int adjustStock(
            @Param("stockId") Long stockId,
            @Param("quantityDelta") Integer quantityDelta
    );

    /**
     * 创建订单时原子扣减可用库存。
     *
     * @param stockId 库存记录ID
     * @param quantity 购买数量
     * @return 1表示扣减成功，0表示库存不足
     */
    @Update("""
    UPDATE meal_product_stock
    SET available_stock = available_stock - #{quantity},
        version = version + 1
    WHERE id = #{stockId}
      AND available_stock >= #{quantity}
    """)
    int deductStock(
            @Param("stockId") Long stockId,
            @Param("quantity") Integer quantity
    );

    /**
     * 订单取消后原子归还库存。
     *
     * @param stockId 库存记录ID
     * @param quantity 需要归还的数量
     * @return 1表示归还成功，0表示库存数据异常
     */
    @Update("""
    UPDATE meal_product_stock
    SET available_stock = available_stock + #{quantity},
        version = version + 1
    WHERE id = #{stockId}
      AND available_stock + #{quantity}
          <= total_stock
    """)
    int restoreStock(
            @Param("stockId") Long stockId,
            @Param("quantity") Integer quantity
    );


}