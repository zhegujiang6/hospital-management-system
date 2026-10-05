package com.example.ward;

import com.example.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wards")
public class WardController {

    private final WardService wardService;

    public WardController(WardService wardService) {
        this.wardService = wardService;
    }

    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody WardCreateRequest request) {

        Long wardId = wardService.create(request);

        return Result.success(wardId);
    }

    @GetMapping
    public Result<List<WardVO>> list() {

        List<WardVO> wards =
                wardService.listDetails();

        return Result.success(wards);
    }

    @GetMapping("/{id}")
    public Result<WardVO> getDetail(
            @PathVariable Long id) {

        WardVO ward =
                wardService.getDetail(id);

        return Result.success(ward);
    }
}