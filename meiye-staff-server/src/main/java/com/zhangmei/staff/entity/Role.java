package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_role")
public class Role extends BaseEntity {

    private String code;
    private String name;
    private String remark;
}
