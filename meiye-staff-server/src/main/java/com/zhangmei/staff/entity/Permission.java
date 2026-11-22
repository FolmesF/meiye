package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_permission")
public class Permission extends BaseEntity {

    private String code;
    private String name;
    private String module;
}
