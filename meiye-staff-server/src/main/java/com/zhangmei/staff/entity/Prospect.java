package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_prospect")
public class Prospect extends BaseEntity {

    private Long employeeId;
    private Long templateId;
    private String name;
    private String mobile;
    private String company;
    private String address;
    private String content;
    private String photos;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String status;

    @TableLogic
    private Integer deleted;
}
