package com.zhangmei.store.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardOverview {

    private String storeName;
    private long appointmentCount;
    private BigDecimal revenue = BigDecimal.ZERO.setScale(2);
    private long arrivedCount;
    private List<LowStockRow> alerts = new ArrayList<>();
}
