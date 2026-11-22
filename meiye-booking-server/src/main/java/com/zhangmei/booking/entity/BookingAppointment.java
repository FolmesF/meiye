package com.zhangmei.booking.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bk_appointment")
public class BookingAppointment extends BaseEntity {

    private Long userId;
    private Long storeId;
    private Long beauticianId;
    private Long serviceItemId;
    private LocalDateTime appointTime;
    private LocalDateTime appointEnd;
    private String status;
    private BigDecimal amount;
    private String slotLock;
    private String cancelReason;
    private Integer reminded;

    @TableLogic
    private Integer deleted;
}
