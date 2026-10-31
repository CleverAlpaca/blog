package com.example.blog.util;

import com.example.blog.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 基于 Redis 的分钟级滑窗限流（评论/留言防刷）
 */
@Component
@RequiredArgsConstructor
public class RateLimitUtil {

    private static final long WINDOW_MILLIS = 60_000L;

    private final StringRedisTemplate stringRedisTemplate;

    /** 默认分钟级窗口（评论/留言防刷） */
    public void check(String scene, String ip, int limit) {
        check(scene, ip, limit, Duration.ofMinutes(1));
    }

    /** 自定义窗口（如注册：同 IP 每小时 N 次） */
    public void check(String scene, String ip, int limit, Duration window) {
        String windowKey = String.valueOf(System.currentTimeMillis() / window.toMillis());
        String key = "blog:rate:" + scene + ":" + ip + ":" + windowKey;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, window.multipliedBy(2));
        }
        if (count != null && count > limit) {
            throw new BusinessException(400, "操作太频繁，请稍后再试");
        }
    }
}
