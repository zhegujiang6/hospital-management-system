package com.example.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class PatientAccountCreateRequest {

    @NotNull(message = "患者不能为空")
    @Positive(message = "患者ID必须大于0")
    private Long patientId;

    @NotBlank(message = "用户名不能为空")
    @Size(
            min = 4,
            max = 50,
            message = "用户名长度必须在4到50个字符之间"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9_]+$",
            message = "用户名只能包含字母、数字和下划线"
    )
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(
            min = 6,
            max = 50,
            message = "密码长度必须在6到50个字符之间"
    )
    private String password;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}