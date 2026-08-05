package com.example.controller;

import com.example.common.Result;
import com.example.service.DashboardService;
import com.example.vo.DashboardVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(
            DashboardService dashboardService) {

        this.dashboardService = dashboardService;
    }

    @GetMapping
    public Result<DashboardVO> getOverview() {

        DashboardVO dashboard =
                dashboardService.getOverview();

        return Result.success(dashboard);
    }
}