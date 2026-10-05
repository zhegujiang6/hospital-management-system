package com.example.bed;

import com.example.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/beds")
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @GetMapping
    public Result<List<BedVO>> list() {

        return Result.success(
                bedService.listDetails()
        );
    }

    @GetMapping("/{id}")
    public Result<BedVO> getDetail(
            @PathVariable Long id) {

        return Result.success(
                bedService.getDetail(id)
        );
    }

    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody BedCreateRequest request) {

        return Result.success(
                bedService.create(request)
        );
    }
}