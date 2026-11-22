package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hr_followup")
public class Followup {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long prospectId;
    private Long employeeId;
    private String content;
    private LocalDateTime nextFollowAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
