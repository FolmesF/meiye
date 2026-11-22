package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_visit_template")
public class VisitTemplate extends BaseEntity {

    private Long employeeId;
    private String title;
    private String content;
    /** 逗号分隔：mobile,company,address,content,photo,location */
    private String requiredFields;

    @TableLogic
    private Integer deleted;
}
