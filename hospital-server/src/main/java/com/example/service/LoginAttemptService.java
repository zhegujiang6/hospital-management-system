package com.example.service;

public interface LoginAttemptService {

    // 判断这个账号是否已经达到失败次数上限。
    boolean isBlocked(String username);

    // 记录一次失败，并返回还可以尝试多少次。
    int recordFailure(String username);

    // 登录成功后清除失败记录。
    void clearFailures(String username);
}