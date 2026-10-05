package com.example.dashboard;

import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl
        implements DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardServiceImpl(
            DashboardMapper dashboardMapper) {

        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public DashboardVO getOverview() {

        return dashboardMapper.selectOverview();
    }
}