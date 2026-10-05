package com.example.payment;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.mq.message.PaymentSuccessMessage;
import com.example.registration.RegistrationOrder;
import com.example.registration.RegistrationOrderStatus;
import com.example.exception.BusinessException;
import com.example.registration.RegistrationOrderService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
/*
 * 支付订单业务实现类。
 * PaymentOrderController 调用 PaymentOrderService，真正的创建支付单和支付成功逻辑写在这里。
 * 它还会调用 RegistrationOrderService 查询和修改挂号订单。
 * ServiceImpl<PaymentOrderMapper, PaymentOrder> 负责连接 Mapper 和 payment_order 表。
 */
public class PaymentOrderServiceImpl
        extends ServiceImpl<
        PaymentOrderMapper,
        PaymentOrder
        >
        implements PaymentOrderService {

    // 保存挂号订单业务接口，因为支付单必须依附于一张挂号订单。
    private final RegistrationOrderService
            registrationOrderService;


    // 用于发布Spring项目内部事件
    private final ApplicationEventPublisher
            applicationEventPublisher;


    // Spring 创建本类时，会把 RegistrationOrderService 的实现对象传进来。
    public PaymentOrderServiceImpl(
            RegistrationOrderService registrationOrderService,
            ApplicationEventPublisher applicationEventPublisher) {

        this.registrationOrderService =
                registrationOrderService;

        this.applicationEventPublisher =
                applicationEventPublisher;
    }

    // 对应 PaymentOrderService 中的 create，用来为挂号订单创建支付单。
    @Override
    // 开启事务：方法中的数据库操作发生异常时一起回滚。
    @Transactional(rollbackFor = Exception.class)
    public Long create(PaymentCreateRequest request) {

        // 从 PaymentCreateRequest DTO 中取出挂号订单 id。
        Long registrationOrderId =
                request.getRegistrationOrderId();

        /*
         * 相同挂号订单重复请求创建支付单时，
         * 直接返回原来的支付单。
         */
        // 查询 payment_order 表，看这个挂号订单是否已经有支付单。
        PaymentOrder existingPayment = lambdaQuery()
                // 查询条件：registration_order_id 等于 DTO 传来的挂号订单 id。
                .eq(
                        PaymentOrder::
                                getRegistrationOrderId,
                        registrationOrderId
                )
                // one 表示最多查询一条，并使用 PaymentOrder 实体接收。
                .one();

        // 不为 null 表示之前已经创建过支付单。
        if (existingPayment != null) {
            // 不重复插入 payment_order，直接返回原支付单 id，这就是创建支付单的幂等。
            return existingPayment.getId();
        }

        // 调用挂号模块的 Service，根据 registrationOrderId 查询 RegistrationOrder 实体。
        RegistrationOrder registrationOrder =
                registrationOrderService
                        .getById(registrationOrderId);

        // getById 返回 null，表示 registration_order 表没有这条订单。
        if (registrationOrder == null) {
            // 没有挂号订单就不能创建支付单。
            throw new BusinessException(
                    "挂号订单不存在"
            );
        }

        // 从挂号状态枚举中取得 PENDING_PAYMENT，并转换为字符串。
        String pendingRegistrationStatus =
                RegistrationOrderStatus
                        .PENDING_PAYMENT
                        .name();

        // 判断挂号订单当前状态是否还是待支付。
        if (!pendingRegistrationStatus.equals(
                registrationOrder.getStatus())) {

            // 已支付、已取消等状态都不能再创建新的支付单。
            throw new BusinessException(
                    "当前挂号订单状态不能创建支付单"
            );
        }

        // 取挂号订单中的 paymentDeadline，与服务器当前时间比较。
        if (registrationOrder
                .getPaymentDeadline()
                .isBefore(LocalDateTime.now())) {

            // 截止时间早于现在，说明订单已经超时。
            throw new BusinessException(
                    "挂号订单已经超过支付时间"
            );
        }

        // 创建 PaymentOrder 实体，对应 payment_order 表中准备新增的一行。
        PaymentOrder paymentOrder =
                new PaymentOrder();

        // 调用本类 generatePaymentNo 生成支付单号，并放进实体。
        paymentOrder.setPaymentNo(
                generatePaymentNo()
        );

        // 把挂号订单 id 写入支付实体，建立支付单与挂号单的关联。
        paymentOrder.setRegistrationOrderId(
                registrationOrderId
        );

        // 当前没有接真实支付平台，所以渠道固定写成 MOCK。
        paymentOrder.setChannel("MOCK");

        /*
         * 金额从挂号订单读取，
         * 不从前端PaymentCreateRequest读取。
         */
        // 从数据库查出的挂号订单中取得金额，再写进支付单，防止前端篡改价格。
        paymentOrder.setAmount(
                registrationOrder.getAmount()
        );

        // 新建支付单的初始状态是 PENDING。
        paymentOrder.setStatus(
                // PaymentStatus.PENDING 是枚举值，name 把它转成数据库需要的字符串。
                PaymentStatus.PENDING.name()
        );

        // save 内部调用 PaymentOrderMapper，真正向 payment_order 表插入数据。
        save(paymentOrder);

        // 插入成功后数据库 id 会回填到实体，这里把支付单 id 返回给 Controller。
        return paymentOrder.getId();
    }

    // 本类内部的支付单号生成方法，不在 Service 接口中，所以没有 @Override。
    private String generatePaymentNo() {
        // 获取当前时间，并格式化成订单号中的时间部分。
        String timeText = LocalDateTime.now()
                .format(
                        // 格式包含年月日时分秒和毫秒。
                        DateTimeFormatter.ofPattern(
                                "yyyyMMddHHmmssSSS"
                        )
                );

        // 生成一段随机文字，降低同一时间产生重复支付单号的可能。
        String randomText = UUID.randomUUID()
                // 把 UUID 对象变成字符串。
                .toString()
                // 删除 UUID 字符串中的短横线。
                .replace("-", "")
                // 只取前 6 个字符。
                .substring(0, 6)
                // 统一变成大写。
                .toUpperCase();

        // PAY + 时间文字 + 随机文字，组成最终支付单号。
        return "PAY" + timeText + randomText;
    }
    // 本类内部的模拟第三方交易号生成方法。
    private String generateGatewayTransactionNo() {
        // MOCK 表示这是模拟支付，不是真实微信或支付宝流水号。
        return "MOCK"
                // 加上当前毫秒时间戳。
                + System.currentTimeMillis()
                // 再生成 UUID，避免交易号重复。
                + UUID.randomUUID()
                // 把 UUID 对象转成字符串。
                .toString()
                // 删除短横线。
                .replace("-", "")
                // 取前 8 位。
                .substring(0, 8)
                // 统一转成大写。
                .toUpperCase();
    }

    // 对应 PaymentOrderService 中的 mockSuccess；前端点击“模拟支付”后会调用它。
    @Override
    // 开启事务：挂号单改为已支付和支付单改为成功，必须一起成功或一起回滚。
    @Transactional
    public void mockSuccess(Long paymentId) {
        // 1. 查询并锁住支付单
        // baseMapper 就是 PaymentOrderMapper；FOR UPDATE 会锁住这条支付记录，避免并发重复处理。
        PaymentOrder paymentOrder =
                baseMapper.selectByIdForUpdate(paymentId);

        // Mapper 返回 null 表示 payment_order 表没有这个 id。
        if (paymentOrder == null) {
            // 没有支付单就停止处理。
            throw new BusinessException("支付单不存在");
        }

        // 2. 重复的成功通知直接结束
        // 把 PaymentStatus.SUCCESS 转成字符串，再与数据库查出的 status 比较。
        if (PaymentStatus.SUCCESS.name().equals(paymentOrder.getStatus())) {
            // 已经成功就直接结束，不会再次修改挂号订单，这就是支付回调幂等。
            return;
        }

        // 3. 只有待支付状态才能支付
        // 如果当前不是 PENDING，例如已经关闭或退款，就不允许支付。
        if (!PaymentStatus.PENDING.name().equals(paymentOrder.getStatus())) {
            // 抛出业务异常，事务会回滚。
            throw new BusinessException("当前支付单状态不能支付");
        }

        // 4. 查询对应的挂号订单
        // 先从支付实体中取得 registrationOrderId，再交给挂号 Service 查询。
        RegistrationOrder registrationOrder =
                registrationOrderService.getById(
                        paymentOrder.getRegistrationOrderId()
                );

        // 返回 null 表示支付单关联的挂号订单不存在，属于异常数据。
        if (registrationOrder == null) {
            // 不允许继续把孤立的支付单改成成功。
            throw new BusinessException("对应的挂号订单不存在");
        }

        // 取得一次当前时间，后面挂号单和支付单都使用同一个支付成功时间。
        LocalDateTime now = LocalDateTime.now();

        // 5. 检查支付是否超时
        // 先确认截止时间不为 null，再判断截止时间是否早于 now。
        if (registrationOrder.getPaymentDeadline() != null
                && registrationOrder.getPaymentDeadline().isBefore(now)) {
            // 超时后拒绝支付；后续还需要由超时任务关闭订单并归还号源。
            throw new BusinessException("挂号订单已超过支付时间");
        }

        // 6. 把挂号订单改成已支付
        // 调用 RegistrationOrderService.markPaid，最终更新 registration_order 表。
        boolean registrationUpdated =
                registrationOrderService.markPaid(
                        // 第一个参数告诉挂号 Service 要修改哪张挂号订单。
                        registrationOrder.getId(),
                        // 第二个参数告诉挂号 Service 支付成功时间。
                        now
                );

        // false 表示挂号订单已经不是 PENDING_PAYMENT，可能同时被取消了。
        if (!registrationUpdated) {
            // 抛异常后，本事务中之前的数据库操作会回滚。
            throw new BusinessException("挂号订单状态已变化，支付失败");
        }

        // 7. 把支付单改成成功
        // 这一行只修改内存中 paymentOrder 对象的 status，还没有更新数据库。
        paymentOrder.setStatus(PaymentStatus.SUCCESS.name());
        // 生成模拟第三方交易号，并放进内存中的 paymentOrder 对象。
        paymentOrder.setGatewayTransactionNo(
                generateGatewayTransactionNo()
        );
        // 把内存中支付单的 paidTime 设置成刚才取得的 now。
        paymentOrder.setPaidTime(now);
        // callbackTime 表示系统收到支付成功通知的时间，这里同样使用 now。
        paymentOrder.setCallbackTime(now);

        // updateById 才会通过 PaymentOrderMapper，把上面修改的字段真正写入 payment_order 表。
        boolean paymentUpdated = updateById(paymentOrder);

        // false 表示数据库没有成功更新这条支付单。
        if (!paymentUpdated) {
            // 抛出异常后，前面 markPaid 对挂号订单的修改也会被事务回滚。
            throw new BusinessException("支付单更新失败");
        }

        // 创建一条支付成功消息
        PaymentSuccessMessage message =
                new PaymentSuccessMessage();

        // 每次事件生成独立编号
        message.setEventId(
                UUID.randomUUID().toString()
        );

        // 消息中的数据全部来自后端数据库对象
        message.setPaymentId(
                paymentOrder.getId()
        );

        message.setRegistrationOrderId(
                registrationOrder.getId()
        );

        message.setPatientId(
                registrationOrder.getPatientId()
        );

        message.setPaymentNo(
                paymentOrder.getPaymentNo()
        );

        message.setAmount(
                paymentOrder.getAmount()
        );

        message.setPaidTime(now);

// 这里只发布Spring内部事件。
// PaymentSuccessMessageProducer会等事务提交后再发送RabbitMQ。
        applicationEventPublisher.publishEvent(message);
    }

    @Override
    public void closePendingByRegistrationOrderId(
            Long registrationOrderId) {

        lambdaUpdate()
                .eq(
                        PaymentOrder::getRegistrationOrderId,
                        registrationOrderId
                )
                .eq(
                        PaymentOrder::getStatus,
                        PaymentStatus.PENDING.name()
                )
                .set(
                        PaymentOrder::getStatus,
                        PaymentStatus.CLOSED.name()
                )
                .update();
    }

}
