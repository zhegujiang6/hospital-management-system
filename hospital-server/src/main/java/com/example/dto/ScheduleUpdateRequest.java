package com.example.dto;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ScheduleUpdateRequest {

    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @NotNull(message = "排班日期不能为空")
    @FutureOrPresent(message = "排班日期不能早于今天")
    private LocalDate scheduleDate;

    @NotBlank(message = "排班时段不能为空")
    @Pattern(
            regexp = "MORNING|AFTERNOON",
            message = "排班时段只能是MORNING或AFTERNOON"
    )
    private String period;

    @NotNull(message = "挂号费不能为空")
    @DecimalMin(value = "0.00", message = "挂号费不能小于0")
    @Digits(integer = 8, fraction = 2, message = "挂号费最多保留两位小数")
    private BigDecimal registrationFee;

    @NotNull(message = "总号源数不能为空")
    @Min(value = 1, message = "总号源数至少为1")
    @Max(value = 500, message = "总号源数不能超过500")
    private Integer totalSlots;

    @NotNull(message = "排班状态不能为空")
    @Min(value = 0, message = "排班状态只能是0或1")
    @Max(value = 1, message = "排班状态只能是0或1")
    private Integer status;


    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(BigDecimal registrationFee) {
        this.registrationFee = registrationFee;
    }

    public Integer getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(Integer totalSlots) {
        this.totalSlots = totalSlots;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
