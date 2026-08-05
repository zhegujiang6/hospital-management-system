package com.example.vo;





public class LoginVO {


    private final String token;
    private final Long userId;
    private final String username;
    private final String realName;
    private final String role;
    private final Long doctorId;

    public LoginVO(
            String token,
            Long userId,
            String username,
            String realName,
            String role,
            Long doctorId) {

        this.token = token;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
        this.doctorId = doctorId;
    }

    public String getToken() {
        return token;
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
}