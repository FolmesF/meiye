package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hr_employee_store")
public class EmployeeStore {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long employeeId;
    private Long storeId;
    private LocalDateTime createdAt;
}
