package com.zhangmei.staff.security;

import java.util.Set;

public final class StaffContext {

    private static final ThreadLocal<Long> EMPLOYEE = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> PERMISSIONS = new ThreadLocal<>();

    private StaffContext() {
    }

    public static void set(Long employeeId, Set<String> permissions) {
        EMPLOYEE.set(employeeId);
        PERMISSIONS.set(permissions);
    }

    public static Long requireEmployeeId() {
        Long employeeId = EMPLOYEE.get();
        if (employeeId == null) {
            throw new com.zhangmei.staff.common.BizException(401, "请先登录");
        }
        return employeeId;
    }

    public static boolean has(String permission) {
        Set<String> permissions = PERMISSIONS.get();
        return permissions != null && permissions.contains(permission);
    }

    public static void clear() {
        EMPLOYEE.remove();
        PERMISSIONS.remove();
    }
}
