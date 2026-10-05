package com.example.registration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegistrationTimeoutScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    RegistrationTimeoutScheduler.class
            );

    private final RegistrationOrderService
            registrationOrderService;

    private final RegistrationTimeoutService
            registrationTimeoutService;

    public RegistrationTimeoutScheduler(
            RegistrationOrderService registrationOrderService,
            RegistrationTimeoutService registrationTimeoutService) {

        this.registrationOrderService =
                registrationOrderService;

        this.registrationTimeoutService =
                registrationTimeoutService;
    }

    @Scheduled(
            initialDelay = 10_000,
            fixedDelay = 60_000
    )
    public void closeExpiredOrders() {

        List<Long> expiredOrderIds =
                registrationOrderService
                        .findExpiredPendingIds(100);

        for (Long orderId : expiredOrderIds) {
            try {
                registrationTimeoutService
                        .closeExpiredOrder(orderId);
            } catch (Exception exception) {
                log.error(
                        "关闭超时挂号订单失败，orderId={}",
                        orderId,
                        exception
                );
            }
        }
    }
}