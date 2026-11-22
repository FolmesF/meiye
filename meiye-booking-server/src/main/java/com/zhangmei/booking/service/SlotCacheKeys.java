package com.zhangmei.booking.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class SlotCacheKeys {

    public static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    private SlotCacheKeys() {
    }

    /** 美容师某天的时段版本。预约写入时自增，让旧的时段列表失效。 */
    public static String version(long beauticianId, LocalDate date) {
        return "booking:slot-ver:" + beauticianId + ":" + DAY.format(date);
    }

    /** 某个项目在该美容师当天的可约开始时间列表，带版本号。 */
    public static String slots(long beauticianId, long serviceItemId, LocalDate date, long version) {
        return "booking:slots:" + beauticianId + ":" + serviceItemId + ":" + DAY.format(date) + ":v" + version;
    }

    /** 同一美容师的预约互斥锁，覆盖重叠时段而不只是相同开始时间。 */
    public static String beauticianLock(long beauticianId) {
        return "booking:lock:beautician:" + beauticianId;
    }

    public static String loginCode(String mobile) {
        return "booking:login:code:" + mobile;
    }

    public static String loginLimit(String mobile) {
        return "booking:login:limit:" + mobile;
    }

    public static String token(String token) {
        return "booking:token:" + token;
    }

    public static String slotLockValue(long beauticianId, java.time.LocalDateTime appointTime) {
        return beauticianId + ":" + appointTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }
}
