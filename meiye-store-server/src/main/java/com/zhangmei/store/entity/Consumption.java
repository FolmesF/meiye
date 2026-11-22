package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_consumption")
public class Consumption extends PersistEntity {

    private Long customerId;
    private Long beauticianId;
    private Long serviceItemId;
    private Long appointmentId;
    private BigDecimal amount;
    private LocalDateTime consumedAt;
    private String remark;
}
