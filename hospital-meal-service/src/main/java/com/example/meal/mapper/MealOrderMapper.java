package com.example.meal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.meal.entity.MealOrder;
import com.example.meal.enums.MealOrderStatus;
import com.example.meal.vo.MealOrderDetailVO;
import com.example.meal.vo.MealOrderListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 餐饮订单主表数据访问接口。
 */
@Mapper
public interface MealOrderMapper
        extends BaseMapper<MealOrder> {

    /**
     * 分页查询指定患者自己的订单。
     *
     * @param page MyBatis-Plus分页参数
     * @param patientId 当前登录患者ID
     * @param status 可选的订单状态
     * @return 患者订单分页数据
     */
    @Select("""
    <script>
    SELECT
        orders.id AS orderId,
        orders.order_no AS orderNo,
        orders.service_date AS serviceDate,
        orders.meal_period AS mealPeriod,
        orders.total_amount AS totalAmount,
        orders.status,
        orders.delivery_type AS deliveryType,
        orders.recipient_name AS recipientName,
        orders.delivery_location AS deliveryLocation,
        COALESCE(
            item_stats.itemKindCount,
            0
        ) AS itemKindCount,
        COALESCE(
            item_stats.totalQuantity,
            0
        ) AS totalQuantity,
        orders.payment_deadline AS paymentDeadline,
        orders.paid_time AS paidTime,
        orders.cancel_time AS cancelTime,
        orders.create_time AS createTime
    FROM meal_order orders
    LEFT JOIN (
        SELECT
            order_id,
            COUNT(*) AS itemKindCount,
            SUM(quantity) AS totalQuantity
        FROM meal_order_item
        GROUP BY order_id
    ) item_stats
        ON item_stats.order_id = orders.id
    WHERE orders.patient_id = #{patientId}
    <if test="status != null">
        AND orders.status = #{status}
    </if>
    ORDER BY orders.create_time DESC,
             orders.id DESC
    </script>
    """)
    IPage<MealOrderListVO> selectPatientOrderPage(
            Page<MealOrderListVO> page,
            @Param("patientId") Long patientId,
            @Param("status") MealOrderStatus status
    );

    /**
     * 根据订单ID和患者ID查询订单详情主信息。
     *
     * 两个条件必须同时匹配，防止患者查看他人的订单。
     *
     * @param orderId 订单ID
     * @param patientId 当前登录患者ID
     * @return 订单详情，不存在或不属于当前患者时返回null
     */
    @Select("""
    SELECT
        id AS orderId,
        order_no AS orderNo,
        service_date AS serviceDate,
        meal_period AS mealPeriod,
        total_amount AS totalAmount,
        status,
        delivery_type AS deliveryType,
        recipient_name AS recipientName,
        recipient_phone AS recipientPhone,
        delivery_location AS deliveryLocation,
        remark,
        payment_deadline AS paymentDeadline,
        paid_time AS paidTime,
        cancel_time AS cancelTime,
        create_time AS createTime
    FROM meal_order
    WHERE id = #{orderId}
      AND patient_id = #{patientId}
    """)
    MealOrderDetailVO selectPatientOrderDetail(
            @Param("orderId") Long orderId,
            @Param("patientId") Long patientId
    );

    /**
     * 把当前患者的待支付订单原子修改为已支付。
     *
     * 只有订单仍为待支付并且没有超过支付期限时，
     * SQL才会真正修改订单。
     *
     * @param orderId 订单ID
     * @param patientId 当前患者ID
     * @return 1表示支付成功，0表示状态不允许支付
     */
    @Update("""
    UPDATE meal_order
    SET status = 'PAID',
        paid_time = CURRENT_TIMESTAMP,
        version = version + 1
    WHERE id = #{orderId}
      AND patient_id = #{patientId}
      AND status = 'PENDING_PAYMENT'
      AND payment_deadline >= CURRENT_TIMESTAMP
    """)
    int markOrderPaid(
            @Param("orderId") Long orderId,
            @Param("patientId") Long patientId
    );
    /**
     * 原子取消当前患者的待支付订单。
     *
     * @param orderId 订单ID
     * @param patientId 当前患者ID
     * @return 1表示取消成功，0表示订单状态已经变化
     */
    @Update("""
    UPDATE meal_order
    SET status = 'CANCELLED',
        cancel_time = CURRENT_TIMESTAMP,
        version = version + 1
    WHERE id = #{orderId}
      AND patient_id = #{patientId}
      AND status = 'PENDING_PAYMENT'
    """)
    int markOrderCancelled(
            @Param("orderId") Long orderId,
            @Param("patientId") Long patientId
    );

    /**
     * 原子取消已经超过支付截止时间的待支付订单。
     *
     * 状态和截止时间都在SQL中判断，
     * 防止消息消费者错误取消已经支付的订单。
     *
     * @param orderId 订单ID
     * @return 1表示取消成功，0表示订单不存在、未超时或状态已经改变
     */
    @Update("""
UPDATE meal_order
SET status = 'CANCELLED',
    cancel_time = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = #{orderId}
  AND status = 'PENDING_PAYMENT'
  AND payment_deadline <= CURRENT_TIMESTAMP
""")
    int markExpiredOrderCancelled(
            @Param("orderId") Long orderId
    );

    /**
     * 分页查询管理员需要处理的餐饮订单。
     *
     * 可以按照状态、供餐日期和关键词筛选。
     *
     * @param page MyBatis-Plus分页参数
     * @param status 可选的订单状态
     * @param serviceDate 可选的供餐日期
     * @param keyword 可选关键词，可匹配订单号、收餐人和配送地点
     * @return 管理员订单分页结果
     */
    @Select("""
<script>
SELECT
    orders.id AS orderId,
    orders.order_no AS orderNo,
    orders.patient_id AS patientId,
    orders.service_date AS serviceDate,
    orders.meal_period AS mealPeriod,
    orders.total_amount AS totalAmount,
    orders.status,
    orders.delivery_type AS deliveryType,
    orders.recipient_name AS recipientName,
    orders.recipient_phone AS recipientPhone,
    orders.delivery_location AS deliveryLocation,
    COALESCE(
        item_stats.itemKindCount,
        0
    ) AS itemKindCount,
    COALESCE(
        item_stats.totalQuantity,
        0
    ) AS totalQuantity,
    orders.payment_deadline AS paymentDeadline,
    orders.paid_time AS paidTime,
    orders.cancel_time AS cancelTime,
    orders.create_time AS createTime
FROM meal_order orders
LEFT JOIN (
    SELECT
        order_id,
        COUNT(*) AS itemKindCount,
        SUM(quantity) AS totalQuantity
    FROM meal_order_item
    GROUP BY order_id
) item_stats
    ON item_stats.order_id = orders.id
<where>
    <if test="status != null">
        orders.status = #{status}
    </if>

    <if test="serviceDate != null">
        AND orders.service_date = #{serviceDate}
    </if>

    <if test="keyword != null and keyword != ''">
        AND (
            orders.order_no
                LIKE CONCAT('%', #{keyword}, '%')
            OR orders.recipient_name
                LIKE CONCAT('%', #{keyword}, '%')
            OR orders.delivery_location
                LIKE CONCAT('%', #{keyword}, '%')
        )
    </if>
</where>
ORDER BY
    orders.create_time DESC,
    orders.id DESC
</script>
""")
    IPage<MealOrderListVO> selectAdminOrderPage(
            Page<MealOrderListVO> page,
            @Param("status")
            MealOrderStatus status,
            @Param("serviceDate")
            LocalDate serviceDate,
            @Param("keyword")
            String keyword
    );

    /**
     * 根据预期状态原子更新订单状态。
     *
     * 只有订单当前状态等于expectedStatus时才会修改，
     * 防止多个管理员同时处理同一张订单。
     *
     * @param orderId 订单ID
     * @param expectedStatus 修改前必须处于的状态
     * @param targetStatus 要修改成的目标状态
     * @return 1表示成功，0表示订单状态已经发生变化
     */
    @Update("""
UPDATE meal_order
SET status = #{targetStatus},
    version = version + 1
WHERE id = #{orderId}
  AND status = #{expectedStatus}
""")
    int transitionOrderStatus(
            @Param("orderId")
            Long orderId,
            @Param("expectedStatus")
            MealOrderStatus expectedStatus,
            @Param("targetStatus")
            MealOrderStatus targetStatus
    );
    /**
     * 根据订单ID查询管理员订单详情。
     *
     * 管理员可以查看任意患者的餐饮订单，
     * 因此这里不使用patientId作为查询条件。
     *
     * @param orderId 订单ID
     * @return 订单详情主信息
     */
    @Select("""
SELECT
    id AS orderId,
    order_no AS orderNo,
    patient_id AS patientId,
    service_date AS serviceDate,
    meal_period AS mealPeriod,
    total_amount AS totalAmount,
    status,
    delivery_type AS deliveryType,
    recipient_name AS recipientName,
    recipient_phone AS recipientPhone,
    delivery_location AS deliveryLocation,
    remark,
    payment_deadline AS paymentDeadline,
    paid_time AS paidTime,
    cancel_time AS cancelTime,
    create_time AS createTime
FROM meal_order
WHERE id = #{orderId}
""")
    MealOrderDetailVO selectAdminOrderDetail(
            @Param("orderId") Long orderId
    );
}