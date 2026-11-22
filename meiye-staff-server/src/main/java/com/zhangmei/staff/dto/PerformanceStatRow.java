package com.zhangmei.staff.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PerformanceStatRow {

    private String period;
    private Long employeeId;
    private Long storeId;
    private BigDecimal amount;
    private Long bizCount;
}
