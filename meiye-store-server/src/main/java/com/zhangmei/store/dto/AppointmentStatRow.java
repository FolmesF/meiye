package com.zhangmei.store.dto;

import lombok.Data;

@Data
public class AppointmentStatRow {

    private String period;
    private Long appointmentCount;
    private Long arrivedCount;
    private Long cancelledCount;
}
