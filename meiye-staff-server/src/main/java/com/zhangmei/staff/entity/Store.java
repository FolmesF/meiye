package com.zhangmei.staff.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_store")
public class Store extends BaseEntity {

    private String name;
    private String address;
    private String phone;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String status;

    @TableLogic
    private Integer deleted;
}
