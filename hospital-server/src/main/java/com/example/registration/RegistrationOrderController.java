package com.example.registration;

import com.example.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registrations")
public class RegistrationOrderController {

    private final RegistrationOrderService orderService;

    public RegistrationOrderController(
            RegistrationOrderService orderService) {

        this.orderService = orderService;
    }

    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody RegistrationCreateRequest request) {

        return Result.success(
                orderService.create(request)
        );
    }

    @GetMapping
    public Result<List<RegistrationOrderVO>> list() {
        return Result.success(
                orderService.listDetails()
        );
    }

    @GetMapping("/{id}")
    public Result<RegistrationOrderVO> getDetail(
            @PathVariable Long id) {

        return Result.success(
                orderService.getDetail(id)
        );
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(
            @PathVariable Long id,
            @Valid
            @RequestBody RegistrationCancelRequest request) {

        orderService.cancel(id, request);
        return Result.success(null);
    }
}