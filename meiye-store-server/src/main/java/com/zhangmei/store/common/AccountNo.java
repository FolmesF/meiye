package com.zhangmei.store.common;

public final class AccountNo {

    private AccountNo() {
    }

    public static String of(String mobile) {
        String value = mobile == null ? "" : mobile.trim();
        if (value.isEmpty()) {
            throw new BizException("手机号不能为空");
        }
        return "MY" + value;
    }
}
