package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_manual_fee_record")
public class ManualFeeRecord extends PersistEntity {

    private Long beauticianId;
    private Long appointmentId;
    private Long serviceItemId;
    private Long ruleId;
    private String ruleType;
    private BigDecimal fixedAmount;
    private BigDecimal rate;
    private BigDecimal baseAmount;
    private BigDecimal feeAmount;
}
