package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_employee")
public class Employee extends BaseEntity {

    private String name;
    private String mobile;
    private String jobType;
    private String status;
    private BigDecimal baseLongitude;
    private BigDecimal baseLatitude;

    @TableLogic
    private Integer deleted;
}
