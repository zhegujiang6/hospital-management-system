package com.example.meal.service;

import com.example.meal.common.PageResult;
import com.example.meal.dto.MealOrderCreateRequest;
import com.example.meal.enums.MealOrderStatus;
import com.example.meal.service.model.ResolvedMealDelivery;
import com.example.meal.vo.MealOrderCreateVO;
import com.example.meal.vo.MealOrderDetailVO;
import com.example.meal.vo.MealOrderListVO;

import java.time.LocalDate;

/**
 * 患者餐饮订单业务接口。
 */
public interface MealOrderService {

    /**
     * 使用JWT中的患者ID创建订单并扣减库存。
     *
     * @param patientId 当前登录患者ID
     * @param request 创建订单参数
     * @return 创建成功的订单信息
     */
    MealOrderCreateVO createOrder(
            Long patientId,
            MealOrderCreateRequest request,
            ResolvedMealDelivery delivery
    );

    /**
     * 分页查询当前患者自己的订单。
     *
     * @param patientId 当前登录患者ID
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param status 可选的订单状态
     * @return 患者订单分页数据
     */
    PageResult<MealOrderListVO> listPatientOrders(
            Long patientId,
            Long pageNum,
            Long pageSize,
            MealOrderStatus status
    );
    /**
     * 查询当前患者自己的订单详情。
     *
     * @param patientId 当前登录患者ID
     * @param orderId 订单ID
     * @return 完整订单详情
     */
    MealOrderDetailVO getPatientOrderDetail(
            Long patientId,
            Long orderId
    );

    /**
     * 模拟当前患者支付自己的订单。
     *
     * @param patientId 当前患者ID
     * @param orderId 订单ID
     */
    void simulatePayment(
            Long patientId,
            Long orderId
    );

    /**
     * 取消当前患者自己的待支付订单并归还库存。
     *
     * @param patientId 当前患者ID
     * @param orderId 订单ID
     */
    void cancelOrder(
            Long patientId,
            Long orderId
    );

    /**
     * 由订单超时消息触发，自动取消仍未支付的订单并恢复库存。
     *
     * @param orderId 需要检查的订单ID
     */
    void cancelExpiredOrder(Long orderId);

    /**
     * 分页查询管理员需要处理的餐饮订单。
     *
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param status 可选订单状态
     * @param serviceDate 可选供餐日期
     * @param keyword 可选搜索关键词
     * @return 管理员订单分页结果
     */
    PageResult<MealOrderListVO> listAdminOrders(
            Long pageNum,
            Long pageSize,
            MealOrderStatus status,
            LocalDate serviceDate,
            String keyword
    );

    /**
     * 管理员按照业务流程推进订单状态。
     *
     * @param orderId 订单ID
     * @param targetStatus 需要进入的目标状态
     */
    void advanceOrderStatus(
            Long orderId,
            MealOrderStatus targetStatus
    );
    /**
     * 查询管理员需要查看的完整订单详情。
     *
     * @param orderId 订单ID
     * @return 订单主信息和菜品明细
     */
    MealOrderDetailVO getAdminOrderDetail(
            Long orderId
    );
}