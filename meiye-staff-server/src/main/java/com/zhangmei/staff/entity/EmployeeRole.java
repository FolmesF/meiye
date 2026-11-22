package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hr_employee_role")
public class EmployeeRole {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long employeeId;
    private Long roleId;
    private LocalDateTime createdAt;
}
