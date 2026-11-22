package com.zhangmei.store.service;

import com.zhangmei.store.common.BizException;

import java.util.Set;

public final class Checks {

    private static final Set<String> GENDERS = Set.of("MALE", "FEMALE", "UNKNOWN");
    private static final Set<String> CUSTOMER_STATUSES = Set.of("ACTIVE", "DISABLED");

    private Checks() {
    }

    public static void customerStatus(String status) {
        if (!CUSTOMER_STATUSES.contains(status)) {
            throw new BizException("顾客状态只允许 ACTIVE、DISABLED");
        }
    }

    public static void gender(String gender) {
        if (gender == null || gender.isBlank()) {
            return;
        }
        if (!GENDERS.contains(gender)) {
            throw new BizException("性别只允许 MALE、FEMALE、UNKNOWN");
        }
    }

    public static long page(long page) {
        return page < 1 ? 1 : page;
    }

    public static long size(long size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
