package com.example.meal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.meal.common.PageResult;
import com.example.meal.dto.MealOrderCreateRequest;
import com.example.meal.dto.MealOrderItemCreateRequest;
import com.example.meal.entity.MealOrder;
import com.example.meal.entity.MealOrderItem;
import com.example.meal.enums.MealOrderStatus;
import com.example.meal.exception.BusinessException;
import com.example.meal.mapper.MealOrderItemMapper;
import com.example.meal.mapper.MealOrderMapper;
import com.example.meal.mapper.MealOrderQueryMapper;
import com.example.meal.mapper.MealProductStockMapper;
import com.example.meal.mq.producer.MealOrderTimeoutProducer;
import com.example.meal.service.MealOrderService;
import com.example.meal.service.model.ResolvedMealDelivery;
import com.example.meal.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 患者餐饮订单业务实现类。
 */
@Service
public class MealOrderServiceImpl
        implements MealOrderService {

    /**
     * 订单编号中的时间格式。
     * DateTimeFormatter是线程安全的，可以作为常量复用。
     */
    private static final DateTimeFormatter
            ORDER_NO_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyyMMddHHmmssSSS"
            );

    private final MealOrderMapper mealOrderMapper;

    private final MealOrderItemMapper mealOrderItemMapper;

    private final MealOrderQueryMapper mealOrderQueryMapper;

    /**
     * 订单超时延时消息发送器。
     */
    private final MealOrderTimeoutProducer
            mealOrderTimeoutProducer;


    private final MealProductStockMapper
            mealProductStockMapper;

    public MealOrderServiceImpl(
            MealOrderMapper mealOrderMapper,
            MealOrderItemMapper mealOrderItemMapper,
            MealOrderQueryMapper mealOrderQueryMapper,
            MealProductStockMapper mealProductStockMapper,
            MealOrderTimeoutProducer mealOrderTimeoutProducer) {

        this.mealOrderMapper = mealOrderMapper;
        this.mealOrderItemMapper =
                mealOrderItemMapper;
        this.mealOrderQueryMapper =
                mealOrderQueryMapper;
        this.mealProductStockMapper =
                mealProductStockMapper;
        this.mealOrderTimeoutProducer =
                mealOrderTimeoutProducer;
    }

    /**
     * 校验购物车、计算金额、扣减库存并保存订单。
     *
     * 整个方法处于同一个数据库事务中：
     * 任意一条库存不足或者保存失败，所有修改都会回滚。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MealOrderCreateVO createOrder(
            Long patientId,
            MealOrderCreateRequest request,
            ResolvedMealDelivery delivery) {

        if (patientId == null || patientId <= 0) {
            throw new BusinessException(
                    "当前账号没有关联患者档案"
            );
        }

        /*
         * 先建立stockId和购买数量之间的关系。
         * 同时检查前端有没有重复提交相同库存ID。
         */
        Map<Long, Integer> quantityMap =
                new HashMap<>();

        Set<Long> stockIdSet =
                new HashSet<>();

        for (MealOrderItemCreateRequest item
                : request.getItems()) {

            boolean firstAppearance =
                    stockIdSet.add(item.getStockId());

            if (!firstAppearance) {
                throw new BusinessException(
                        "订单中存在重复菜品"
                );
            }

            quantityMap.put(
                    item.getStockId(),
                    item.getQuantity()
            );
        }

        /*
         * 所有订单都按照stockId升序扣库存。
         *
         * 如果两个订单包含相同的多种菜品，
         * 固定加锁顺序可以降低数据库死锁概率。
         */
        List<Long> stockIds =
                stockIdSet.stream()
                        .sorted()
                        .toList();

        List<MealMenuItemVO> products =
                mealOrderQueryMapper
                        .selectOrderableItems(stockIds);

        if (products.size() != stockIds.size()) {
            throw new BusinessException(
                    "部分菜品不存在或已经下架，请刷新菜单"
            );
        }

        Map<Long, MealMenuItemVO> productMap =
                new HashMap<>();

        for (MealMenuItemVO product : products) {
            productMap.put(
                    product.getStockId(),
                    product
            );
        }

        MealMenuItemVO firstProduct =
                productMap.get(stockIds.get(0));

        if (firstProduct.getServiceDate()
                .isBefore(LocalDate.now())) {

            throw new BusinessException(
                    "不能购买过去日期的餐食"
            );
        }

        /*
         * 一张订单只能属于同一个日期和餐次。
         *
         * 否则早餐、午餐和晚餐混在同一个订单中，
         * 后续配送和订单状态会非常难处理。
         */
        for (MealMenuItemVO product : products) {

            boolean sameDate =
                    firstProduct.getServiceDate()
                            .equals(
                                    product.getServiceDate()
                            );

            boolean samePeriod =
                    firstProduct.getMealPeriod()
                            == product.getMealPeriod();

            if (!sameDate || !samePeriod) {
                throw new BusinessException(
                        "一张订单只能购买同一日期和餐次的菜品"
                );
            }
        }

        /*
         * 使用数据库中的真实价格计算金额。
         * 不接受前端传递的单价和总金额。
         */
        Map<Long, BigDecimal> subtotalMap =
                new HashMap<>();

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        for (Long stockId : stockIds) {

            MealMenuItemVO product =
                    productMap.get(stockId);

            Integer quantity =
                    quantityMap.get(stockId);

            BigDecimal subtotal =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(quantity)
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            subtotalMap.put(stockId, subtotal);

            totalAmount =
                    totalAmount.add(subtotal);
        }

        LocalDateTime now =
                LocalDateTime.now();

        MealOrder order =
                new MealOrder();

        order.setOrderNo(generateOrderNo());
        order.setPatientId(patientId);
        order.setServiceDate(
                firstProduct.getServiceDate()
        );
        order.setMealPeriod(
                firstProduct.getMealPeriod()
        );
        order.setTotalAmount(totalAmount);
        order.setStatus(
                MealOrderStatus.PENDING_PAYMENT
        );
        order.setDeliveryType(
                request.getDeliveryType()
        );
        /*
         * 姓名、电话和位置已经由PlacementService校验，
         * 这里不再直接使用前端的原始配送信息。
         */
        order.setRecipientName(
                delivery.recipientName()
        );

        order.setRecipientPhone(
                delivery.recipientPhone()
        );

        order.setDeliveryLocation(
                delivery.deliveryLocation()
        );
        order.setRemark(
                normalizeOptional(request.getRemark())
        );

        /*
         * 订单创建后15分钟内没有支付，
         * 后面由超时任务或RabbitMQ取消并归还库存。
         */
        order.setPaymentDeadline(
                now.plusMinutes(15)
        );
        order.setVersion(0);

        int orderRows =
                mealOrderMapper.insert(order);

        if (orderRows != 1) {
            throw new BusinessException(
                    "创建订单失败"
            );
        }

        /*
         * 按stockId固定顺序逐条扣库存。
         */
        for (Long stockId : stockIds) {

            MealMenuItemVO product =
                    productMap.get(stockId);

            Integer quantity =
                    quantityMap.get(stockId);

            int stockRows =
                    mealProductStockMapper
                            .deductStock(
                                    stockId,
                                    quantity
                            );

            if (stockRows != 1) {
                throw new BusinessException(
                        product.getProductName()
                                + "库存不足，请刷新菜单"
                );
            }

            MealOrderItem orderItem =
                    new MealOrderItem();

            orderItem.setOrderId(order.getId());
            orderItem.setStockId(stockId);
            orderItem.setProductId(
                    product.getProductId()
            );
            orderItem.setProductNo(
                    product.getProductNo()
            );
            orderItem.setProductName(
                    product.getProductName()
            );
            orderItem.setUnitPrice(
                    product.getPrice()
            );
            orderItem.setQuantity(quantity);
            orderItem.setSubtotalAmount(
                    subtotalMap.get(stockId)
            );

            int itemRows =
                    mealOrderItemMapper
                            .insert(orderItem);

            if (itemRows != 1) {
                throw new BusinessException(
                        "保存订单明细失败"
                );
            }
        }
        /*
         * 订单、明细和库存都处理完成后，
         * 发送一条在支付截止时间投递的RocketMQ延时消息。
         */
        mealOrderTimeoutProducer
                .sendOrderTimeoutMessage(
                        order.getId(),
                        order.getPaymentDeadline()
                );


        return new MealOrderCreateVO(
                order.getId(),
                order.getOrderNo(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentDeadline()
        );
    }

    /**
     * 生成具有时间信息和随机部分的订单编号。
     */
    private String generateOrderNo() {

        String timePart =
                LocalDateTime.now()
                        .format(
                                ORDER_NO_TIME_FORMATTER
                        );

        String randomPart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase(Locale.ROOT);

        return "MO" + timePart + randomPart;
    }

    /**
     * 清理非必填字符串。
     */
    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    /**
     * 校验患者身份和分页参数，然后查询患者自己的订单。
     *
     * @param patientId 当前登录患者ID
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param status 可选的订单状态
     * @return 患者订单分页数据
     */
    @Override
    public PageResult<MealOrderListVO> listPatientOrders(
            Long patientId,
            Long pageNum,
            Long pageSize,
            MealOrderStatus status) {

        if (patientId == null || patientId <= 0) {
            throw new BusinessException(
                    "当前账号没有关联患者档案"
            );
        }

        if (pageNum == null || pageNum < 1) {
            throw new BusinessException(
                    "页码必须大于等于1"
            );
        }

        if (pageSize == null
                || pageSize < 1
                || pageSize > 50) {

            throw new BusinessException(
                    "每页数量必须在1到50之间"
            );
        }

        Page<MealOrderListVO> page =
                new Page<>(
                        pageNum,
                        pageSize
                );

        IPage<MealOrderListVO> result =
                mealOrderMapper
                        .selectPatientOrderPage(
                                page,
                                patientId,
                                status
                        );

        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    /**
     * 查询订单主信息和菜品明细，并组合成完整订单详情。
     *
     * @param patientId 当前登录患者ID
     * @param orderId 订单ID
     * @return 完整订单详情
     */
    @Override
    @Transactional(readOnly = true)
    public MealOrderDetailVO getPatientOrderDetail(
            Long patientId,
            Long orderId) {

        if (patientId == null || patientId <= 0) {
            throw new BusinessException(
                    "当前账号没有关联患者档案"
            );
        }

        if (orderId == null || orderId <= 0) {
            throw new BusinessException(
                    "订单ID必须大于0"
            );
        }

        MealOrderDetailVO order =
                mealOrderMapper
                        .selectPatientOrderDetail(
                                orderId,
                                patientId
                        );

        if (order == null) {
            throw new BusinessException(
                    "订单不存在"
            );
        }

        List<MealOrderItemVO> items =
                mealOrderItemMapper
                        .selectOrderItems(orderId);

        order.setItems(items);

        return order;
    }


    /**
     * 校验订单归属和当前状态，然后原子修改为已支付。
     *
     * 重复支付已经成功的订单时直接返回成功，
     * 保证接口具有幂等性。
     *
     * @param patientId 当前患者ID
     * @param orderId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void simulatePayment(
            Long patientId,
            Long orderId) {

        if (patientId == null || patientId <= 0) {
            throw new BusinessException(
                    "当前账号没有关联患者档案"
            );
        }

        if (orderId == null || orderId <= 0) {
            throw new BusinessException(
                    "订单ID必须大于0"
            );
        }

        MealOrder order =
                selectPatientOrder(
                        patientId,
                        orderId
                );

        if (order == null) {
            throw new BusinessException(
                    "订单不存在"
            );
        }

        /*
         * 已经支付成功时直接返回。
         *
         * 这叫幂等：
         * 同一个支付结果重复处理多次，
         * 最终状态与处理一次相同。
         */
        if (order.getStatus()
                == MealOrderStatus.PAID) {

            return;
        }

        if (order.getStatus()
                != MealOrderStatus.PENDING_PAYMENT) {

            throw new BusinessException(
                    "当前订单状态不能支付"
            );
        }

        if (order.getPaymentDeadline()
                .isBefore(LocalDateTime.now())) {

            throw new BusinessException(
                    "订单已经超过支付期限"
            );
        }

        int affectedRows =
                mealOrderMapper.markOrderPaid(
                        orderId,
                        patientId
                );

        if (affectedRows == 1) {
            return;
        }

        /*
         * affectedRows为0有可能是另一个并发请求
         * 已经把订单支付成功，因此重新查询一次。
         */
        MealOrder latestOrder =
                selectPatientOrder(
                        patientId,
                        orderId
                );

        if (latestOrder != null
                && latestOrder.getStatus()
                == MealOrderStatus.PAID) {

            return;
        }

        throw new BusinessException(
                "订单状态已经发生变化，请刷新后重试"
        );
    }
    /**
     * 根据订单ID和患者ID查询患者自己的订单实体。
     *
     * @param patientId 当前患者ID
     * @param orderId 订单ID
     * @return 订单实体，不存在或不属于患者时返回null
     */
    private MealOrder selectPatientOrder(
            Long patientId,
            Long orderId) {

        return mealOrderMapper.selectOne(
                new LambdaQueryWrapper<MealOrder>()
                        .eq(
                                MealOrder::getId,
                                orderId
                        )
                        .eq(
                                MealOrder::getPatientId,
                                patientId
                        )
        );
    }
    /**
     * 原子取消订单，并在同一个事务中归还全部库存。
     *
     * 如果任何一条库存恢复失败，
     * 订单取消和已经恢复的库存都会一起回滚。
     *
     * @param patientId 当前患者ID
     * @param orderId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(
            Long patientId,
            Long orderId) {

        if (patientId == null || patientId <= 0) {
            throw new BusinessException(
                    "当前账号没有关联患者档案"
            );
        }

        if (orderId == null || orderId <= 0) {
            throw new BusinessException(
                    "订单ID必须大于0"
            );
        }

        MealOrder order =
                selectPatientOrder(
                        patientId,
                        orderId
                );

        if (order == null) {
            throw new BusinessException(
                    "订单不存在"
            );
        }

        /*
         * 已经取消的订单重复请求取消，
         * 直接按照成功处理，不重复恢复库存。
         */
        if (order.getStatus()
                == MealOrderStatus.CANCELLED) {

            return;
        }

        if (order.getStatus()
                != MealOrderStatus.PENDING_PAYMENT) {

            throw new BusinessException(
                    "只有待支付订单可以取消"
            );
        }

        /*
         * 先抢占订单状态。
         *
         * 如果支付请求已经抢先把订单变成PAID，
         * 这里影响行数就是0，也不会恢复库存。
         */
        int orderRows =
                mealOrderMapper.markOrderCancelled(
                        orderId,
                        patientId
                );

        if (orderRows != 1) {

            MealOrder latestOrder =
                    selectPatientOrder(
                            patientId,
                            orderId
                    );

            /*
             * 另一个取消请求已经完成，
             * 当前请求也按照成功处理。
             */
            if (latestOrder != null
                    && latestOrder.getStatus()
                    == MealOrderStatus.CANCELLED) {

                return;
            }

            throw new BusinessException(
                    "订单状态已经发生变化，无法取消"
            );
        }

        restoreOrderStock(orderId);


        }

    /**
     * 处理RocketMQ投递的订单超时消息。
     *
     * 只有订单仍是待支付状态，并且已经超过支付期限时，
     * 才会取消订单并恢复库存。
     *
     * @param orderId 需要检查的订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredOrder(Long orderId) {

        if (orderId == null || orderId <= 0) {
            throw new BusinessException(
                    "订单ID必须大于0"
            );
        }

        MealOrder order =
                mealOrderMapper.selectById(orderId);

        /*
         * 消息可能重复投递。
         *
         * 如果订单不存在，说明不需要继续处理；
         * 如果已经支付或取消，也不再重复恢复库存。
         */
        if (order == null
                || order.getStatus()
                != MealOrderStatus.PENDING_PAYMENT) {

            return;
        }

        /*
         * 真正的状态判断由一条原子SQL完成。
         *
         * 只有“待支付并且已经超时”的订单，
         * 才能被修改成已取消。
         */
        int orderRows =
                mealOrderMapper
                        .markExpiredOrderCancelled(
                                orderId
                        );

        if (orderRows != 1) {

            MealOrder latestOrder =
                    mealOrderMapper.selectById(orderId);

            /*
             * 支付或其他取消请求已经抢先处理完成，
             * 当前超时消息直接按照处理成功返回。
             */
            if (latestOrder == null
                    || latestOrder.getStatus()
                    != MealOrderStatus.PENDING_PAYMENT) {

                return;
            }

            /*
             * 订单仍然是待支付，说明消息可能提前到达，
             * 抛出异常让后面的消费者要求RocketMQ重试。
             */
            throw new BusinessException(
                    "订单尚未达到支付截止时间"
            );
        }

        /*
         * 只有订单状态成功变成CANCELLED之后，
         * 才可以恢复库存。
         */
        restoreOrderStock(orderId);
    }
    /**
     * 查询订单明细并归还订单占用的全部库存。
     *
     * 这个方法由患者取消和系统超时取消共同使用。
     *
     * @param orderId 需要恢复库存的订单ID
     */
    private void restoreOrderStock(Long orderId) {

        List<MealOrderItemVO> orderItems =
                mealOrderItemMapper
                        .selectOrderItems(orderId);

        if (orderItems.isEmpty()) {
            throw new BusinessException(
                    "订单明细不存在，无法恢复库存"
            );
        }

        /*
         * 扣库存和恢复库存都按照stockId升序，
         * 可以降低并发事务产生数据库死锁的概率。
         */
        List<MealOrderItemVO> sortedItems =
                orderItems.stream()
                        .sorted(
                                Comparator.comparing(
                                        MealOrderItemVO::getStockId
                                )
                        )
                        .toList();

        for (MealOrderItemVO item : sortedItems) {

            int stockRows =
                    mealProductStockMapper
                            .restoreStock(
                                    item.getStockId(),
                                    item.getQuantity()
                            );

            if (stockRows != 1) {
                throw new BusinessException(
                        item.getProductName()
                                + "库存恢复失败"
                );
            }
        }
    }


    /**
     * 校验管理员查询条件，并分页查询全部餐饮订单。
     *
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param status 可选订单状态
     * @param serviceDate 可选供餐日期
     * @param keyword 可选搜索关键词
     * @return 管理员订单分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<MealOrderListVO> listAdminOrders(
            Long pageNum,
            Long pageSize,
            MealOrderStatus status,
            LocalDate serviceDate,
            String keyword) {

        if (pageNum == null || pageNum < 1) {
            throw new BusinessException(
                    "页码必须大于等于1"
            );
        }

        if (pageSize == null
                || pageSize < 1
                || pageSize > 50) {

            throw new BusinessException(
                    "每页数量必须在1到50之间"
            );
        }

        /*
         * 把空字符串处理成null。
         *
         * 这样Mapper中的动态SQL就不会执行无意义的
         * LIKE '%%'查询。
         */
        String normalizedKeyword =
                normalizeOptional(keyword);

        Page<MealOrderListVO> page =
                new Page<>(
                        pageNum,
                        pageSize
                );

        IPage<MealOrderListVO> result =
                mealOrderMapper
                        .selectAdminOrderPage(
                                page,
                                status,
                                serviceDate,
                                normalizedKeyword
                        );

        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }
    /**
     * 按照规定的状态顺序推进餐饮订单。
     *
     * 允许的流程：
     * PAID -> PREPARING
     * PREPARING -> DELIVERING
     * DELIVERING -> COMPLETED
     *
     * @param orderId 订单ID
     * @param targetStatus 目标状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void advanceOrderStatus(
            Long orderId,
            MealOrderStatus targetStatus) {

        if (orderId == null || orderId <= 0) {
            throw new BusinessException(
                    "订单ID必须大于0"
            );
        }

        if (targetStatus == null) {
            throw new BusinessException(
                    "目标状态不能为空"
            );
        }

        /*
         * 根据目标状态，反推出订单当前必须处于什么状态。
         *
         * 这里就是订单状态机的业务规则。
         */
        MealOrderStatus expectedStatus =
                switch (targetStatus) {

                    case PREPARING ->
                            MealOrderStatus.PAID;

                    case DELIVERING ->
                            MealOrderStatus.PREPARING;

                    case COMPLETED ->
                            MealOrderStatus.DELIVERING;

                    default ->
                            throw new BusinessException(
                                    "不允许把订单推进到该状态"
                            );
                };

        int affectedRows =
                mealOrderMapper
                        .transitionOrderStatus(
                                orderId,
                                expectedStatus,
                                targetStatus
                        );

        if (affectedRows == 1) {
            return;
        }

        /*
         * 更新失败后重新查询订单，
         * 用于区分订单不存在、重复操作和状态冲突。
         */
        MealOrder latestOrder =
                mealOrderMapper.selectById(orderId);

        if (latestOrder == null) {
            throw new BusinessException(
                    "订单不存在"
            );
        }

        /*
         * 如果订单已经是目标状态，
         * 说明可能是重复点击，直接按照成功处理。
         */
        if (latestOrder.getStatus() == targetStatus) {
            return;
        }

        throw new BusinessException(
                "订单当前状态为"
                        + latestOrder.getStatus()
                        + "，不能修改为"
                        + targetStatus
        );
    }
    /**
     * 查询管理员订单主信息和全部菜品明细。
     *
     * @param orderId 订单ID
     * @return 完整订单详情
     */
    @Override
    @Transactional(readOnly = true)
    public MealOrderDetailVO getAdminOrderDetail(
            Long orderId) {

        if (orderId == null || orderId <= 0) {
            throw new BusinessException(
                    "订单ID必须大于0"
            );
        }

        MealOrderDetailVO order =
                mealOrderMapper
                        .selectAdminOrderDetail(orderId);

        if (order == null) {
            throw new BusinessException(
                    "订单不存在"
            );
        }

        List<MealOrderItemVO> items =
                mealOrderItemMapper
                        .selectOrderItems(orderId);

        order.setItems(items);

        return order;
    }

}