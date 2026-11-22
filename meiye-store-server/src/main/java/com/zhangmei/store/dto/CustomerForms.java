package com.zhangmei.store.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class CustomerForms {

    private CustomerForms() {
    }

    @Data
    public static class ProfileSync {
        @NotBlank
        @Size(max = 32)
        private String accountNo;
        @NotBlank
        @Size(max = 64)
        private String name;
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$")
        private String mobile;
        private String gender;
        private LocalDate birthday;
        @Size(max = 255)
        private String address;
        @Size(max = 32)
        private String memberLevel;
        private java.util.List<@Size(max = 16) String> tags;
    }

    @Data
    public static class RegisterSync {
        @NotBlank
        @Size(max = 32)
        private String accountNo;
        @NotBlank
        @Size(max = 64)
        private String name;
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$")
        private String mobile;
        @Size(max = 32)
        private String memberLevel;
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
        private LocalDate birthday;
        @Size(max = 32)
        private String level;
        @Size(max = 32)
        private String source;
        @Size(max = 255)
        private String address;
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
        private LocalDate birthday;
        @Size(max = 32)
        private String level;
        @Size(max = 32)
        private String source;
        @Size(max = 255)
        private String address;
        @Size(max = 255)
        private String remark;
    }

    @Data
    public static class StatusChange {
        @NotBlank
        @Pattern(regexp = "ACTIVE|DISABLED")
        private String status;
    }

    @Data
    public static class BatchDelete {
        @NotNull
        private Long storeId;
        @NotNull
        @Size(min = 1, max = 100)
        private java.util.List<Long> ids;
    }

    @Data
    public static class Tag {
        @NotBlank
        @Size(max = 32)
        private String tagName;
    }

    @Data
    public static class ConsumptionCreate {
        private Long beauticianId;
        private Long serviceItemId;
        private Long appointmentId;
        @NotNull
        @DecimalMin("0.00")
        private BigDecimal amount;
        @NotNull
        private LocalDateTime consumedAt;
        @Size(max = 255)
        private String remark;
    }
}
