package com.zhangmei.store.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

public final class InventoryForms {

    private InventoryForms() {
    }

    @Data
    public static class ProductSave {
        @NotNull
        private Long storeId;
        @NotBlank
        @Size(max = 64)
        private String name;
        @NotBlank
        @Size(max = 64)
        private String skuCode;
        @NotBlank
        @Size(max = 16)
        private String unit;
        @NotNull
        @Min(0)
        private Integer warnQuantity;
        @Size(max = 255)
        private String remark;
    }

    @Data
    public static class ProductUpdate {
        @NotBlank
        @Size(max = 64)
        private String name;
        @NotBlank
        @Size(max = 64)
        private String skuCode;
        @NotBlank
        @Size(max = 16)
        private String unit;
        @NotNull
        @Min(0)
        private Integer warnQuantity;
        @NotBlank
        private String status;
        @Size(max = 255)
        private String remark;
    }

    @Data
    public static class QuantityUpdate {
        @NotNull
        private Long storeId;
        @NotNull
        @Min(0)
        private Integer quantity;
    }

    @Data
    public static class MaxUpdate {
        @NotNull
        @Min(0)
        private Integer maxQuantity;
    }

    @Data
    public static class StockChange {
        @NotNull
        private Long storeId;
        @NotNull
        private Long productId;
        @NotNull
        @Min(1)
        private Integer quantity;
        @Size(max = 255)
        private String remark;
    }

    @Data
    public static class StocktakeCreate {
        @NotNull
        private Long storeId;
        @NotNull
        private Long productId;
        @NotNull
        @Min(0)
        private Integer actualQuantity;
        @Size(max = 255)
        private String remark;
    }
}
