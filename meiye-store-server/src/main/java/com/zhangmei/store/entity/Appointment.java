package com.zhangmei.store.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_appointment")
public class Appointment extends LogicEntity {

    private Long customerId;
    private Long beauticianId;
    private Long serviceItemId;
    private LocalDateTime appointTime;
    private LocalDateTime appointEnd;
    private Long bookingId;
    private String customerName;
    private String customerMobile;
    private String serviceName;
    private String beauticianName;
    private String status;
    private BigDecimal amount;
    private String cancelReason;
}
