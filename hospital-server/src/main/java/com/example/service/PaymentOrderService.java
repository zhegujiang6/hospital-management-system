package com.example.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.dto.PaymentCreateRequest;
import com.example.entity.PaymentOrder;

public interface PaymentOrderService
        extends IService<PaymentOrder> {

    Long create(PaymentCreateRequest request);

    void mockSuccess(Long paymentId);


    void closePendingByRegistrationOrderId(
            Long registrationOrderId
    );
}