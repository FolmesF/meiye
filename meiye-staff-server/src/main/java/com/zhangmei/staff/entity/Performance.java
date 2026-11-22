package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("hr_performance")
public class Performance {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long employeeId;
    private Long storeId;
    private LocalDate bizDate;
    private BigDecimal amount;
    private String sourceBizId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
