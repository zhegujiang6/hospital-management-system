package com.example.payment;

import com.baomidou.mybatisplus.spring.service.IService;

public interface PaymentOrderService
        extends IService<PaymentOrder> {

    Long create(PaymentCreateRequest request);

    void mockSuccess(Long paymentId);


    void closePendingByRegistrationOrderId(
            Long registrationOrderId
    );
}