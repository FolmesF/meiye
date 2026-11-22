package com.zhangmei.booking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppointmentView {

    private Long id;
    private String status;
    private LocalDateTime appointTime;
    private LocalDateTime appointEnd;
    private BigDecimal amount;
    private Long serviceItemId;
    private String serviceName;
    private Long beauticianId;
    private String beauticianName;
    private String beauticianAvatar;
    private boolean reviewed;
}
