package com.zhangmei.booking.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bk_consumption")
public class BookingConsumption extends BaseEntity {

    private Long userId;
    private Long storeId;
    private Long appointmentId;
    private Long beauticianId;
    private Long serviceItemId;
    private BigDecimal amount;
    private LocalDateTime consumedAt;
}
