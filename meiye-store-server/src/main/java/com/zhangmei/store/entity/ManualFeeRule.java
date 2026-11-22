package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_manual_fee_rule")
public class ManualFeeRule extends LogicEntity {

    private Long serviceItemId;
    private String ruleType;
    private BigDecimal fixedAmount;
    private BigDecimal rate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer enabled;
}
