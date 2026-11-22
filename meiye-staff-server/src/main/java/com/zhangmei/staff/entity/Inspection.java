package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hr_inspection")
public class Inspection {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long storeId;
    private Long employeeId;
    private String content;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalDateTime visitedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
