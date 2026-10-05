package com.example.auth;

public class CurrentUserVO {

    private final Long userId;
    private final String username;
    private final String realName;
    private final String role;
    private final Long doctorId;
    private final Long patientId;

    public CurrentUserVO(
            Long userId,
            String username,
            String realName,
            String role,
            Long doctorId,
            Long patientId) {

        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
        this.doctorId = doctorId;
        this.patientId = patientId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public String getRole() {
        return role;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public Long getPatientId() {
        return patientId;
    }
}