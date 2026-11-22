package com.zhangmei.store.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class BeauticianForms {

    private BeauticianForms() {
    }

    @Data
    public static class Save {
        @NotNull
        private Long storeId;
        @NotBlank
        @Size(max = 64)
        private String name;
        @NotBlank
        @Pattern(regexp = "^[0-9]{6,20}$")
        private String mobile;
        private String gender;
        @Size(max = 32)
        private String level;
        private LocalDate entryDate;
        @DecimalMin("0.00")
        private BigDecimal sessionFee;
        @Size(max = 255)
        private String remark;
    }

    @Data
    public static class Update {
        @NotBlank
        @Size(max = 64)
        private String name;
        @NotBlank
        @Pattern(regexp = "^[0-9]{6,20}$")
        private String mobile;
        private String gender;
        @Size(max = 32)
        private String level;
        @NotBlank
        private String status;
        private LocalDate entryDate;
        @Size(max = 255)
        private String remark;
    }

    @Data
    public static class Bind {
        @NotNull
        private List<Long> serviceItemIds;
    }

    @Data
    public static class Schedule {
        @NotNull
        private LocalDate workDate;
        @NotBlank
        private String shiftStatus;
        private LocalTime shiftStart;
        private LocalTime shiftEnd;
        @Size(max = 255)
        private String remark;
    }
}
