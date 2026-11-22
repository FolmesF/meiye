package com.zhangmei.store.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppointmentStatResponse {

    private String period;
    private long appointmentCount;
    private long arrivedCount;
    private long cancelledCount;
    /** 到店数 / 预约数，保留 4 位小数。 */
    private BigDecimal arrivalRate;
    /** 取消数 / 预约数，保留 4 位小数。 */
    private BigDecimal cancelRate;
}
