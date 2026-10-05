package com.example.payment;

import com.example.common.Result;
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