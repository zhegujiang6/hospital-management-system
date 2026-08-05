package com.example.vo;

import java.math.BigDecimal;

public class DashboardVO {

    // 启用中的科室数量
    private Long enabledDepartmentCount;

    // 启用中的医生数量
    private Long enabledDoctorCount;

    // 启用中的患者数量
    private Long enabledPatientCount;

    // 今天的有效排班数量
    private Long todayScheduleCount;

    // 今天创建的挂号订单数量
    private Long todayRegistrationCount;

    // 今天已经完成的接诊数量
    private Long todayVisitCount;

    // 已支付、等待医生接诊的订单数量
    private Long pendingVisitCount;

    // 等待支付且没有超时的挂号订单数量
    private Long pendingPaymentCount;

    // 系统全部就诊记录数量
    private Long totalVisitCount;

    // 今天成功支付的总金额
    private BigDecimal todayRevenue;

    public Long getEnabledDepartmentCount() {
        return enabledDepartmentCount;
    }

    public void setEnabledDepartmentCount(Long enabledDepartmentCount) {
        this.enabledDepartmentCount = enabledDepartmentCount;
    }

    public Long getEnabledDoctorCount() {
        return enabledDoctorCount;
    }

    public void setEnabledDoctorCount(Long enabledDoctorCount) {
        this.enabledDoctorCount = enabledDoctorCount;
    }

    public Long getEnabledPatientCount() {
        return enabledPatientCount;
    }

    public void setEnabledPatientCount(Long enabledPatientCount) {
        this.enabledPatientCount = enabledPatientCount;
    }

    public Long getTodayScheduleCount() {
        return todayScheduleCount;
    }

    public void setTodayScheduleCount(Long todayScheduleCount) {
        this.todayScheduleCount = todayScheduleCount;
    }

    public Long getTodayRegistrationCount() {
        return todayRegistrationCount;
    }

    public void setTodayRegistrationCount(Long todayRegistrationCount) {
        this.todayRegistrationCount = todayRegistrationCount;
    }

    public Long getTodayVisitCount() {
        return todayVisitCount;
    }

    public void setTodayVisitCount(Long todayVisitCount) {
        this.todayVisitCount = todayVisitCount;
    }

    public Long getPendingVisitCount() {
        return pendingVisitCount;
    }

    public void setPendingVisitCount(Long pendingVisitCount) {
        this.pendingVisitCount = pendingVisitCount;
    }

    public Long getPendingPaymentCount() {
        return pendingPaymentCount;
    }

    public void setPendingPaymentCount(Long pendingPaymentCount) {
        this.pendingPaymentCount = pendingPaymentCount;
    }

    public Long getTotalVisitCount() {
        return totalVisitCount;
    }

    public void setTotalVisitCount(Long totalVisitCount) {
        this.totalVisitCount = totalVisitCount;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }
}