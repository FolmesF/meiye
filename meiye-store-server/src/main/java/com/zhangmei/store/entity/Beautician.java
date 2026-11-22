package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_beautician")
public class Beautician extends LogicEntity {

    private String name;
    private String mobile;
    private String gender;
    private String level;
    private java.math.BigDecimal sessionFee;
    private String status;
    private LocalDate entryDate;
    private String remark;
}
