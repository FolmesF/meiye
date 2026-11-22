package com.zhangmei.booking.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceBrief {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer durationMin;
}
