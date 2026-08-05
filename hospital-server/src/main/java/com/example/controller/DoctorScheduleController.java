package com.example.controller;

import com.example.common.Result;
import com.example.security.LoginUser;
import com.example.service.ScheduleService;
import com.example.vo.ScheduleVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/doctor-schedules")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorScheduleController {

    private final ScheduleService scheduleService;

    public DoctorScheduleController(
            ScheduleService scheduleService) {

        this.scheduleService = scheduleService;
    }

    @GetMapping
    public Result<List<ScheduleVO>> listMine(
            @AuthenticationPrincipal
            LoginUser loginUser) {

        return Result.success(
                scheduleService.listByDoctorId(
                        loginUser.getDoctorId()
                )
        );
    }
}