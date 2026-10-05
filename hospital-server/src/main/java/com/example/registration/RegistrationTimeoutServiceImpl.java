package com.example.registration;

import com.example.payment.PaymentOrderService;
import com.example.schedule.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationTimeoutServiceImpl
        implements RegistrationTimeoutService {

    private final RegistrationOrderService
            registrationOrderService;

    private final ScheduleService scheduleService;

    private final PaymentOrderService paymentOrderService;

    public RegistrationTimeoutServiceImpl(
            RegistrationOrderService registrationOrderService,
            ScheduleService scheduleService,
            PaymentOrderService paymentOrderService) {

        this.registrationOrderService =
                registrationOrderService;

        this.scheduleService = scheduleService;

        this.paymentOrderService =
                paymentOrderService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeExpiredOrder(
            Long registrationOrderId) {

        // 根据ID查询准备关闭的挂号订单
        RegistrationOrder order =
                registrationOrderService.getById(
                        registrationOrderId
                );

        // 没查到订单就直接结束
        if (order == null) {
            return;
        }

        // 尝试把待支付订单原子地改成EXPIRED
        boolean expired =
                registrationOrderService.markExpired(
                        registrationOrderId
                );

        // 修改失败，说明订单可能已经支付或取消
        if (!expired) {
            return;
        }

        // 只有真正变成EXPIRED的请求才能归还号源
        scheduleService.restoreSlot(
                order.getScheduleId()
        );

        // 如果已经创建过支付单，就把待支付单关闭
        paymentOrderService
                .closePendingByRegistrationOrderId(
                        registrationOrderId
                );
    }
}