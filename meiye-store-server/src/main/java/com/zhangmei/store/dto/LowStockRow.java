package com.zhangmei.store.dto;

import lombok.Data;

@Data
public class LowStockRow {

    private Long productId;
    private Long storeId;
    private String productName;
    private String skuCode;
    private String unit;
    private Integer warnQuantity;
    private Integer quantity;
}
