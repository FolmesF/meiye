package com.zhangmei.store.dto;

import lombok.Data;

@Data
public class InventorySummaryRow {

    private Long productId;
    private String name;
    private String skuCode;
    private Integer maxQuantity;
    private Integer warnQuantity;
    private Integer quantity;
    private Integer todayConsumed;
    private Integer totalConsumed;
}
