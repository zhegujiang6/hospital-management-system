package com.example.dto;


import jakarta.validation.constraints.NotNull;

public class PaymentCreateRequest {
    @NotNull(message = "挂号订单ID不能为空")
    private Long registrationOrderId;

    public Long getRegistrationOrderId() {
        return registrationOrderId;
    }

    public void setRegistrationOrderId(
            Long registrationOrderId) {

        this.registrationOrderId =
                registrationOrderId;
    }


}