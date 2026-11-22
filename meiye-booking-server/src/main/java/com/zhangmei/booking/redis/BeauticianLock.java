package com.zhangmei.booking.redis;

import com.zhangmei.booking.config.BookingProperties;
import com.zhangmei.booking.service.SlotCacheKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BeauticianLock {

    private final StringRedisTemplate redis;
    private final BookingProperties properties;

    public boolean tryLock(long beauticianId) {
        Boolean ok = redis.opsForValue().setIfAbsent(
                SlotCacheKeys.beauticianLock(beauticianId),
                "1",
                Duration.ofSeconds(properties.getBeauticianLockSeconds()));
        return Boolean.TRUE.equals(ok);
    }

    public void unlock(long beauticianId) {
        redis.delete(SlotCacheKeys.beauticianLock(beauticianId));
    }
}
