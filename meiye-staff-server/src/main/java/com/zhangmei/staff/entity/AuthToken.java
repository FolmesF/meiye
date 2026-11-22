package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hr_auth_token")
public class AuthToken {

    @TableId(type = IdType.INPUT)
    private String token;
    private Long employeeId;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
}
