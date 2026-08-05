package com.example.controller;

import com.example.common.Result;
import com.example.dto.PaymentCreateRequest;
import com.example.service.PaymentOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentOrderController {

    private final PaymentOrderService paymentOrderService;

    public PaymentOrderController(
            PaymentOrderService paymentOrderService) {

        this.paymentOrderService =
                paymentOrderService;
    }

    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody PaymentCreateRequest request) {

        return Result.success(
                paymentOrderService.create(request)
        );
    }

    @PostMapping("/{id}/mock-success")
    public Result<Void> mockSuccess(@PathVariable Long id) {
        paymentOrderService.mockSuccess(id);
        return Result.success(null);
    }
}