package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hr_work_note")
public class WorkNote {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long targetEmployeeId;
    private Long authorEmployeeId;
    private String bizType;
    private Long bizId;
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
