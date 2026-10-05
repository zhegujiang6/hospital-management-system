package com.example.meal.service.model;

/**
 * 经过餐饮服务校验和解析后的可信配送信息。
 *
 * 与前端提交的原始参数不同，
 * 这里的数据可以安全地写入订单。
 */
public record ResolvedMealDelivery(

        String recipientName,

        String recipientPhone,

        String deliveryLocation

) {
}