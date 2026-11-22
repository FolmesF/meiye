package com.zhangmei.booking.dto;

import lombok.Data;

@Data
public class SendCodeResult {

    private int expiresIn;
    /** 简化登录时回传验证码。关闭 meiye.booking.expose-login-code 后为空。 */
    private String code;
}
