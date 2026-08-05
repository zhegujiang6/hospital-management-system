package com.example.service.impl;

import com.example.mapper.DashboardMapper;
import com.example.service.DashboardService;
import com.example.vo.DashboardVO;
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