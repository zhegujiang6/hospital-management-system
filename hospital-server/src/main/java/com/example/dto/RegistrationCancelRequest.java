package com.example.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationCancelRequest {

    @NotBlank(message = "取消原因不能为空")
    @Size(max = 255, message = "取消原因不能超过255个字符")
    private String cancelReason;

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
