package com.zhangmei.booking.dto;

import lombok.Data;

@Data
public class LoginResult {

    private String token;
    private Long userId;
    private String mobile;
    private String nickname;
}
