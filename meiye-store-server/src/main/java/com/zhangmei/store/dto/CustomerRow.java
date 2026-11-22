package com.zhangmei.store.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerRow {

    private Long id;
    private Long storeId;
    private String name;
    private String mobile;
    private String accountNo;
    private String gender;
    private LocalDate birthday;
    private String level;
    private String source;
    private String address;
    private String remark;
    private String status;
    private BigDecimal totalAmount;
    private Integer visitCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastVisitAt;
}
