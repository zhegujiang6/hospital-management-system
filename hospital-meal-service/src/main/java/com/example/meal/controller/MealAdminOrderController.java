package com.example.meal.controller;

import com.example.meal.common.PageResult;
import com.example.meal.common.Result;
import com.example.meal.enums.MealOrderStatus;
import com.example.meal.service.MealOrderService;
import com.example.meal.vo.MealOrderDetailVO;
import com.example.meal.vo.MealOrderListVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 管理员餐饮订单接口。
 *
 * 负责查询和处理所有患者的餐饮订单。
 */
@RestController
@RequestMapping("/meal/admin/orders")
public class MealAdminOrderController {

    private final MealOrderService mealOrderService;

    public MealAdminOrderController(
            MealOrderService mealOrderService) {

        this.mealOrderService = mealOrderService;
    }

    /**
     * 分页查询全部餐饮订单。
     *
     * 可以按照状态、供餐日期和关键词筛选。
     *
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param status 可选订单状态
     * @param serviceDate 可选供餐日期
     * @param keyword 可选搜索关键词
     * @return 管理员订单分页结果
     */
    @GetMapping
    public Result<PageResult<MealOrderListVO>>
    listAdminOrders(
            @RequestParam(defaultValue = "1")
            Long pageNum,
            @RequestParam(defaultValue = "10")
            Long pageSize,
            @RequestParam(required = false)
            MealOrderStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate serviceDate,
            @RequestParam(required = false)
            String keyword) {

        PageResult<MealOrderListVO> result =
                mealOrderService.listAdminOrders(
                        pageNum,
                        pageSize,
                        status,
                        serviceDate,
                        keyword
                );

        return Result.success(result);
    }

    /**
     * 管理员接单并开始制作餐品。
     *
     * 只允许把已支付订单从PAID修改成PREPARING。
     *
     * @param orderId 订单ID
     * @return 操作成功结果
     */
    @PostMapping("/{orderId}/prepare")
    public Result<Void> startPreparing(
            @PathVariable Long orderId) {

        mealOrderService.advanceOrderStatus(
                orderId,
                MealOrderStatus.PREPARING
        );

        return Result.success(null);
    }

    /**
     * 餐品制作完成后开始配送。
     *
     * 只允许把制作中订单从PREPARING修改成DELIVERING。
     *
     * @param orderId 订单ID
     * @return 操作成功结果
     */
    @PostMapping("/{orderId}/deliver")
    public Result<Void> startDelivering(
            @PathVariable Long orderId) {

        mealOrderService.advanceOrderStatus(
                orderId,
                MealOrderStatus.DELIVERING
        );

        return Result.success(null);
    }

    /**
     * 配送人员送达餐品后完成订单。
     *
     * 只允许把配送中订单从DELIVERING修改成COMPLETED。
     *
     * @param orderId 订单ID
     * @return 操作成功结果
     */
    @PostMapping("/{orderId}/complete")
    public Result<Void> completeOrder(
            @PathVariable Long orderId) {

        mealOrderService.advanceOrderStatus(
                orderId,
                MealOrderStatus.COMPLETED
        );

        return Result.success(null);
    }
    /**
     * 查询管理员订单详情和菜品明细。
     *
     * @param orderId 订单ID
     * @return 完整餐饮订单详情
     */
    @GetMapping("/{orderId}")
    public Result<MealOrderDetailVO>
    getAdminOrderDetail(
            @PathVariable Long orderId) {

        MealOrderDetailVO result =
                mealOrderService
                        .getAdminOrderDetail(orderId);

        return Result.success(result);
    }
}