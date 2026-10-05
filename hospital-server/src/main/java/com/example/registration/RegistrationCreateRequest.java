package com.example.registration;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegistrationCreateRequest {


    @NotBlank(message = "幂等请求号不能为空")
    @Size(max = 64, message = "幂等请求号不能超过64个字符")
    private String requestId;

    @NotNull(message = "患者ID不能为空")
    private Long patientId;

    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
}
