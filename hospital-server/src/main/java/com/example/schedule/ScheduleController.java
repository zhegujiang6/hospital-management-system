package com.example.schedule;

import com.example.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(
            ScheduleService scheduleService) {

        this.scheduleService = scheduleService;
    }

    @PostMapping
    public Result<Long> create(
            @Valid
            @RequestBody ScheduleCreateRequest request) {

        return Result.success(
                scheduleService.create(request)
        );
    }

    @GetMapping
    public Result<List<ScheduleVO>> list() {
        return Result.success(
                scheduleService.listDetails()
        );
    }

    @GetMapping("/{id}")
    public Result<ScheduleVO> getDetail(
            @PathVariable Long id) {

        return Result.success(
                scheduleService.getDetail(id)
        );
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid
            @RequestBody ScheduleUpdateRequest request) {

        scheduleService.update(id, request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id) {

        scheduleService.delete(id);
        return Result.success(null);
    }
}