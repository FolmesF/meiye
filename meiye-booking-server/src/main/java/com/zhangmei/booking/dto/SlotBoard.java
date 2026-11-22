package com.zhangmei.booking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SlotBoard {

    private String beauticianName;
    private String serviceName;
    private BigDecimal price;
    private Integer durationMin;
    private boolean onDuty;
    private List<SlotCell> slots = new ArrayList<>();
}
