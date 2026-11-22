package com.zhangmei.booking.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BeauticianScore {

    private Long beauticianId;
    private BigDecimal score;
}
