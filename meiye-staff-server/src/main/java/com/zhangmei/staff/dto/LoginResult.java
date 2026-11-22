package com.zhangmei.staff.dto;

import lombok.Data;

import java.util.Set;

@Data
public class LoginResult {

    private String token;
    private Long employeeId;
    private String name;
    private String jobType;
    private Set<String> permissions;
}
