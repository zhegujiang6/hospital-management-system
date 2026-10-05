package com.example.meal.security;

/**
 * 餐饮微服务中经过JWT验证的当前登录用户。
 */
public class MealLoginUser {

    private final Long userId;

    private final String username;

    private final String role;

    private final Long patientId;

    public MealLoginUser(
            Long userId,
            String username,
            String role,
            Long patientId) {

        this.userId = userId;
        this.username = username;
        this.role = role;
        this.patientId = patientId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Long getPatientId() {
        return patientId;
    }
}