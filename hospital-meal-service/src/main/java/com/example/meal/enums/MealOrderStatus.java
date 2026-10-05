package com.example.meal.enums;

/**
 * 餐饮订单状态。
 */
public enum MealOrderStatus {

    /**
     * 已创建订单，等待患者支付。
     */
    PENDING_PAYMENT,

    /**
     * 已完成支付，等待餐厅处理。
     */
    PAID,

    /**
     * 餐厅正在制作。
     */
    PREPARING,

    /**
     * 配送人员正在配送。
     */
    DELIVERING,

    /**
     * 订单已经送达并完成。
     */
    COMPLETED,

    /**
     * 订单已经取消。
     */
    CANCELLED
}