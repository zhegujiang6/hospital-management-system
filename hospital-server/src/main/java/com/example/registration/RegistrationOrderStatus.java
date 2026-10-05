package com.example.registration;

/**
 * 挂号订单状态枚举
 * 定义挂号订单在整个生命周期中可能出现的各种状态
 */
public enum RegistrationOrderStatus {

    /**
     * 待支付状态
     * 患者已提交挂号申请，但尚未完成支付
     */
    PENDING_PAYMENT,

    /**
     * 已支付状态
     * 患者已完成挂号费用的支付，挂号成功
     */
    PAID,

    /**
     * 已取消状态
     * 患者主动取消了挂号订单
     */
    CANCELLED,

    /**
     * 已过期状态
     * 挂号订单在规定时间内未完成支付，系统自动标记为过期
     */
    EXPIRED,

    /**
     * 已完成状态
     * 患者已完成就诊，挂号订单正常结束
     */
    COMPLETED
}