package com.zhangmei.booking.redis;

import com.zhangmei.booking.config.BookingProperties;
import com.zhangmei.booking.service.SlotCacheKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class LoginCodeStore {

    private final StringRedisTemplate redis;
    private final BookingProperties properties;

    public boolean tryAcquireSendPermit(String mobile) {
        Boolean ok = redis.opsForValue().setIfAbsent(
                SlotCacheKeys.loginLimit(mobile), "1", Duration.ofSeconds(60));
        return Boolean.TRUE.equals(ok);
    }

    public void save(String mobile, String code) {
        redis.opsForValue().set(SlotCacheKeys.loginCode(mobile), code,
                Duration.ofSeconds(properties.getLoginCodeTtlSeconds()));
    }

    public boolean matchesAndConsume(String mobile, String code) {
        String cached = redis.opsForValue().get(SlotCacheKeys.loginCode(mobile));
        if (cached == null || !cached.equals(code)) {
            return false;
        }
        redis.delete(SlotCacheKeys.loginCode(mobile));
        return true;
    }
}
