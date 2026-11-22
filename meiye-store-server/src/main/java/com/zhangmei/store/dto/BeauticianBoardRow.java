package com.zhangmei.store.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BeauticianBoardRow {

    private Long id;
    private String name;
    private LocalDate entryDate;
    private String mobile;
    private String level;
    private BigDecimal score;
    private BigDecimal sessionFee;
}
