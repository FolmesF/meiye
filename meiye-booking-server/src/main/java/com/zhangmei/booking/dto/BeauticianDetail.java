package com.zhangmei.booking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class BeauticianDetail {

    private Long id;
    private Long storeId;
    private String name;
    private String avatar;
    private String level;
    private Integer years;
    private BigDecimal score;
    private int reviewCount;
    private List<ServiceBrief> services = new ArrayList<>();
}
