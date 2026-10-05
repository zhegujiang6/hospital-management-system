package com.example.meal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meal.entity.MealOrderItem;
import com.example.meal.vo.MealOrderItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 餐饮订单子表数据访问接口。
 */
@Mapper
public interface MealOrderItemMapper
        extends BaseMapper<MealOrderItem> {

    /**
     * 查询指定订单中的全部菜品快照。
     *
     * @param orderId 订单ID
     * @return 订单菜品列表
     */
    @Select("""
    SELECT
        id AS itemId,
        stock_id AS stockId,
        product_id AS productId,
        product_no AS productNo,
        product_name AS productName,
        unit_price AS unitPrice,
        quantity,
        subtotal_amount AS subtotalAmount
    FROM meal_order_item
    WHERE order_id = #{orderId}
    ORDER BY id ASC
    """)
    List<MealOrderItemVO> selectOrderItems(
            @Param("orderId") Long orderId
    );
}
