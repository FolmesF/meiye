package com.zhangmei.store.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AppointmentForms {

    private AppointmentForms() {
    }

    @Data
    public static class Save {
        @NotNull
        private Long storeId;
        @NotNull
        private Long customerId;
        @NotNull
        private Long beauticianId;
        @NotNull
        private Long serviceItemId;
        @NotNull
        private LocalDateTime appointTime;
        @NotNull
        @DecimalMin("0.00")
        private BigDecimal amount;
    }

    @Data
    public static class BookingSync {
        @NotNull
        private Long bookingId;
        @NotNull
        private Long storeId;
        @NotBlank
        @Size(max = 64)
        private String customerName;
        @NotBlank
        @Size(max = 20)
        private String mobile;
        private String gender;
        private String memberLevel;
        @Size(max = 32)
        private String accountNo;
        @NotNull
        private Long beauticianId;
        @NotBlank
        @Size(max = 64)
        private String beauticianName;
        @NotNull
        private Long serviceItemId;
        @NotBlank
        @Size(max = 64)
        private String serviceName;
        @NotNull
        private LocalDateTime appointTime;
        @NotNull
        private LocalDateTime appointEnd;
        @NotNull
        private BigDecimal amount;
        @NotBlank
        private String status;
        @Size(max = 255)
        private String cancelReason;
    }

    @Data
    public static class StatusChange {
        @NotBlank
        private String status;
        @Size(max = 255)
        private String cancelReason;
    }
}
