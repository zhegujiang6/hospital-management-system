package com.example.service.impl;

import com.example.service.LoginAttemptService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
public class LoginAttemptServiceImpl
        implements LoginAttemptService {

    // 同一个账号最多允许连续失败5次。
    private static final int MAX_FAILURE_COUNT = 5;

    // 失败记录保存3分钟。
    private static final Duration FAILURE_DURATION =
            Duration.ofMinutes(3);

    // Redis Key的统一开头。
    private static final String KEY_PREFIX =
            "login:fail:";

    private final StringRedisTemplate stringRedisTemplate;

    public LoginAttemptServiceImpl(
            StringRedisTemplate stringRedisTemplate) {

        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean isBlocked(String username) {

        String key = buildKey(username);

        // 根据Key从Redis读取当前失败次数。
        String failureCountText =
                stringRedisTemplate
                        .opsForValue()
                        .get(key);

        // Redis里没有记录，说明还没有失败过。
        if (failureCountText == null) {
            return false;
        }

        long failureCount =
                Long.parseLong(failureCountText);

        return failureCount >= MAX_FAILURE_COUNT;
    }

    @Override
    public int recordFailure(String username) {

        String key = buildKey(username);

        // Redis执行自增：
        // 没有这个Key时从0变成1，
        // 已经存在时在原来的数字上加1。
        Long failureCount =
                stringRedisTemplate
                        .opsForValue()
                        .increment(key);

        // 设置10分钟过期时间，避免失败记录永久存在。
        stringRedisTemplate.expire(
                key,
                FAILURE_DURATION
        );

        long currentCount =
                failureCount == null
                        ? 1
                        : failureCount;

        // 例如当前失败2次，5 - 2 = 还可以尝试3次。
        return (int) Math.max(
                0,
                MAX_FAILURE_COUNT - currentCount
        );
    }

    @Override
    public void clearFailures(String username) {

        // 登录成功后，删除这个账号以前的失败次数。
        stringRedisTemplate.delete(
                buildKey(username)
        );
    }

    private String buildKey(String username) {

        // admin登录失败时生成：
        // login:fail:admin
        return KEY_PREFIX
                + username
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}