package com.zhangmei.booking.redis;

import com.zhangmei.booking.config.BookingProperties;
import com.zhangmei.booking.service.SlotCacheKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoginTokenStore {

    private final StringRedisTemplate redis;
    private final BookingProperties properties;

    public String issue(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(SlotCacheKeys.token(token), String.valueOf(userId),
                Duration.ofDays(properties.getTokenTtlDays()));
        return token;
    }

    public Long getUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String value = redis.opsForValue().get(SlotCacheKeys.token(token));
        if (value == null) {
            return null;
        }
        return Long.valueOf(value);
    }
}
