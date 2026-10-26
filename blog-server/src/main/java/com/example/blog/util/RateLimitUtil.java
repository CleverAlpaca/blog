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

    public void check(String scene, String ip, int limit) {
        String window = String.valueOf(System.currentTimeMillis() / WINDOW_MILLIS);
        String key = "blog:rate:" + scene + ":" + ip + ":" + window;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(120));
        }
        if (count != null && count > limit) {
            throw new BusinessException(400, "操作太频繁，请稍后再试");
        }
    }
}
