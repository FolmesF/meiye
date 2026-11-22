package com.zhangmei.store.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ManualFeeForms {

    private ManualFeeForms() {
    }

    @Data
    public static class RuleSave {
        @NotNull
        private Long storeId;
        @NotNull
        private Long serviceItemId;
        @NotBlank
        private String ruleType;
        @DecimalMin("0.00")
        private BigDecimal fixedAmount;
        private BigDecimal rate;
        @NotNull
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
    }

    @Data
    public static class Preview {
        @NotNull
        private Long storeId;
        @NotNull
        private Long serviceItemId;
        @DecimalMin("0.00")
        private BigDecimal performanceAmount;
    }
}
