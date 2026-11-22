package com.zhangmei.booking.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotCacheKeysTest {

    @Test
    void buildsVersionedSlotKeyAndLock() {
        LocalDate date = LocalDate.of(2026, 11, 6);
        assertEquals("booking:slot-ver:18:20261106", SlotCacheKeys.version(18, date));
        assertEquals("booking:slots:18:7:20261106:v3", SlotCacheKeys.slots(18, 7, date, 3));
        assertEquals("booking:lock:beautician:18", SlotCacheKeys.beauticianLock(18));
        assertEquals("18:202611061030", SlotCacheKeys.slotLockValue(18, LocalDateTime.of(2026, 11, 6, 10, 30)));
    }
}
