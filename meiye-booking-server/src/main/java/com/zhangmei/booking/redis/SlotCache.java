package com.zhangmei.booking.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangmei.booking.config.BookingProperties;
import com.zhangmei.booking.service.SlotCacheKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SlotCache {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final BookingProperties properties;

    public Optional<List<LocalTime>> get(long beauticianId, long serviceItemId, LocalDate date) {
        long version = currentVersion(beauticianId, date);
        String raw = redis.opsForValue().get(SlotCacheKeys.slots(beauticianId, serviceItemId, date, version));
        if (raw == null) {
            return Optional.empty();
        }
        try {
            List<LocalTime> slots = objectMapper.readValue(raw, STRING_LIST).stream().map(LocalTime::parse).toList();
            return Optional.of(slots);
        } catch (Exception exception) {
            redis.delete(SlotCacheKeys.slots(beauticianId, serviceItemId, date, version));
            return Optional.empty();
        }
    }

    public void put(long beauticianId, long serviceItemId, LocalDate date, List<LocalTime> slots) {
        long version = currentVersion(beauticianId, date);
        List<String> values = slots.stream().map(LocalTime::toString).toList();
        try {
            int seconds = date.equals(LocalDate.now())
                    ? properties.getTodaySlotTtlSeconds()
                    : properties.getFutureSlotTtlSeconds();
            redis.opsForValue().set(
                    SlotCacheKeys.slots(beauticianId, serviceItemId, date, version),
                    objectMapper.writeValueAsString(values),
                    Duration.ofSeconds(seconds));
        } catch (Exception exception) {
            throw new IllegalStateException("写入可约时段缓存失败", exception);
        }
    }

    public void bump(long beauticianId, LocalDate date) {
        String key = SlotCacheKeys.version(beauticianId, date);
        redis.opsForValue().increment(key);
        redis.expire(key, Duration.ofHours(properties.getSlotVersionTtlHours()));
    }

    private long currentVersion(long beauticianId, LocalDate date) {
        String value = redis.opsForValue().get(SlotCacheKeys.version(beauticianId, date));
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value);
    }
}
