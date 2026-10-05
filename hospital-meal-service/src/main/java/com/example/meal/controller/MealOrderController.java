package com.example.meal.controller;

import com.example.meal.common.PageResult;
import com.example.meal.common.Result;
import com.example.meal.dto.MealOrderCreateRequest;
import com.example.meal.enums.MealOrderStatus;
import com.example.meal.exception.BusinessException;
import com.example.meal.security.MealLoginUser;
import com.example.meal.service.MealOrderPlacementService;
import com.example.meal.service.MealOrderService;
import com.example.meal.vo.MealOrderCreateVO;
import com.example.meal.vo.MealOrderDetailVO;
import com.example.meal.vo.MealOrderListVO;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 患者餐饮订单接口。
 */
@RestController
@RequestMapping("/meal/patient/orders")
public class MealOrderController {

    private final MealOrderService mealOrderService;
    private final MealOrderPlacementService
            mealOrderPlacementService;

    public MealOrderController(
            MealOrderService mealOrderService, MealOrderPlacementService
                    mealOrderPlacementService) {

        this.mealOrderService = mealOrderService;

        this.mealOrderPlacementService =
                mealOrderPlacementService;
    }

    /**
     * 使用当前JWT中的患者身份创建订单。
     *
     * @param loginUser JWT验证后的当前用户
     * @param request 前端提交的购物车和配送信息
     * @return 创建成功的订单
     */
    @PostMapping
    public Result<MealOrderCreateVO> createOrder(
            @AuthenticationPrincipal
            MealLoginUser loginUser,
            @Valid
            @RequestBody
            MealOrderCreateRequest request) {

        if (loginUser == null
                || loginUser.getPatientId() == null) {

            throw new BusinessException(
                    "无法取得当前患者身份"
            );
        }

        MealOrderCreateVO result =
                mealOrderPlacementService.placeOrder(
                        loginUser.getPatientId(),
                        request
                );

        return Result.success(result);
    }

    /**
     * 分页查询当前患者自己的订单。
     *
     * @param loginUser JWT验证后的当前患者
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param status 可选的订单状态
     * @return 患者订单分页结果
     */
    @GetMapping
    public Result<PageResult<MealOrderListVO>>
    listPatientOrders(
            @AuthenticationPrincipal
            MealLoginUser loginUser,
            @RequestParam(defaultValue = "1")
            Long pageNum,
            @RequestParam(defaultValue = "10")
            Long pageSize,
            @RequestParam(required = false)
            MealOrderStatus status) {

        if (loginUser == null
                || loginUser.getPatientId() == null) {

            throw new BusinessException(
                    "无法取得当前患者身份"
            );
        }

        PageResult<MealOrderListVO> result =
                mealOrderService.listPatientOrders(
                        loginUser.getPatientId(),
                        pageNum,
                        pageSize,
                        status
                );

        return Result.success(result);
    }
    /**
     * 查询当前患者自己的订单详情。
     *
     * @param loginUser JWT验证后的当前患者
     * @param orderId 订单ID
     * @return 完整订单详情
     */
    @GetMapping("/{orderId}")
    public Result<MealOrderDetailVO>
    getPatientOrderDetail(
            @AuthenticationPrincipal
            MealLoginUser loginUser,
            @PathVariable
            Long orderId) {

        if (loginUser == null
                || loginUser.getPatientId() == null) {

            throw new BusinessException(
                    "无法取得当前患者身份"
            );
        }

        MealOrderDetailVO result =
                mealOrderService
                        .getPatientOrderDetail(
                                loginUser.getPatientId(),
                                orderId
                        );

        return Result.success(result);
    }
    /**
     * 模拟支付当前患者自己的订单。
     *
     * 正式接入微信支付后，
     * 订单状态应由支付平台的回调接口修改。
     *
     * @param loginUser JWT验证后的当前患者
     * @param orderId 订单ID
     * @return 支付成功结果
     */
    @PostMapping("/{orderId}/simulate-payment")
    public Result<Void> simulatePayment(
            @AuthenticationPrincipal
            MealLoginUser loginUser,
            @PathVariable
            Long orderId) {

        if (loginUser == null
                || loginUser.getPatientId() == null) {

            throw new BusinessException(
                    "无法取得当前患者身份"
            );
        }

        mealOrderService.simulatePayment(
                loginUser.getPatientId(),
                orderId
        );

        return Result.success(null);
    }

    /**
     * 取消当前患者自己的待支付订单。
     *
     * @param loginUser JWT验证后的当前患者
     * @param orderId 订单ID
     * @return 取消成功结果
     */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(
            @AuthenticationPrincipal
            MealLoginUser loginUser,
            @PathVariable
            Long orderId) {

        if (loginUser == null
                || loginUser.getPatientId() == null) {

            throw new BusinessException(
                    "无法取得当前患者身份"
            );
        }

        mealOrderService.cancelOrder(
                loginUser.getPatientId(),
                orderId
        );

        return Result.success(null);
    }
}