package com.zhangmei.store.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public final class ServiceItemForms {

    private ServiceItemForms() {
    }

    @Data
    public static class Beauticians {
        @NotNull
        private List<Long> beauticianIds;
    }

    @Data
    public static class Save {
        @NotNull
        private Long storeId;
        @NotBlank
        @Size(max = 64)
        private String name;
        @Size(max = 64)
        private String category;
        @NotNull
        @DecimalMin("0.00")
        private BigDecimal price;
        @NotNull
        @Min(1)
        private Integer durationMin;
        @Size(max = 255)
        private String remark;
    }

    @Data
    public static class Update {
        @NotBlank
        @Size(max = 64)
        private String name;
        @Size(max = 64)
        private String category;
        @NotNull
        @DecimalMin("0.00")
        private BigDecimal price;
        @NotNull
        @Min(1)
        private Integer durationMin;
        @NotBlank
        private String status;
        @Size(max = 255)
        private String remark;
    }

    @Data
    public static class Materials {
        @NotNull
        @Valid
        private List<Line> materials;
    }

    @Data
    public static class Line {
        @NotNull
        private Long productId;
        @NotNull
        @DecimalMin("0.01")
        private BigDecimal quantity;
    }
}
