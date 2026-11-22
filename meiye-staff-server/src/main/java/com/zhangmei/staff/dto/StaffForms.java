package com.zhangmei.staff.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class StaffForms {

    private StaffForms() {
    }

    @Data
    public static class Login {
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$")
        private String mobile;
    }

    @Data
    public static class EmployeeCreate {
        @NotBlank
        @Size(max = 64)
        private String name;
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$")
        private String mobile;
        @NotBlank
        private String roleCode;
        private BigDecimal baseLongitude;
        private BigDecimal baseLatitude;
    }

    @Data
    public static class StatusChange {
        @NotBlank
        private String status;
    }

    @Data
    public static class StoreSave {
        @NotBlank
        @Size(max = 64)
        private String name;
        @Size(max = 255)
        private String address;
        @Size(max = 20)
        private String phone;
        private BigDecimal longitude;
        private BigDecimal latitude;
    }

    @Data
    public static class InspectionCreate {
        @NotNull
        private Long storeId;
        @NotBlank
        @Size(max = 500)
        private String content;
        @NotNull
        private BigDecimal longitude;
        @NotNull
        private BigDecimal latitude;
    }

    @Data
    public static class TemplateSave {
        @NotBlank
        @Size(max = 64)
        private String title;
        @NotBlank
        @Size(max = 1000)
        private String content;
        @Size(max = 255)
        private String requiredFields;
    }

    @Data
    public static class ProspectSave {
        private Long templateId;
        @NotBlank
        @Size(max = 64)
        private String name;
        @Size(max = 20)
        private String mobile;
        @Size(max = 128)
        private String company;
        @Size(max = 255)
        private String address;
        @Size(max = 1000)
        private String content;
        @Size(max = 1500000)
        private String photos;
        private BigDecimal longitude;
        private BigDecimal latitude;
    }

    @Data
    public static class FollowupCreate {
        @NotNull
        private Long prospectId;
        @NotBlank
        @Size(max = 500)
        private String content;
        private LocalDateTime nextFollowAt;
    }

    @Data
    public static class Clock {
        @NotNull
        @DecimalMin("-180")
        @DecimalMax("180")
        private BigDecimal longitude;
        @NotNull
        @DecimalMin("-90")
        @DecimalMax("90")
        private BigDecimal latitude;
    }

    @Data
    public static class RouteReportCreate {
        @NotNull
        private BigDecimal longitude;
        @NotNull
        private BigDecimal latitude;
        @Size(max = 255)
        private String address;
        @NotBlank
        @Size(max = 500)
        private String content;
    }

    @Data
    public static class PerformanceQuery {
        private Long employeeId;
        private Long storeId;
        @NotBlank
        private String dimension;
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate endDate;
        private boolean groupByPerson = true;
        private boolean groupByStore = true;
    }

    @Data
    public static class PerformanceRecord {
        @NotNull
        private Long employeeId;
        @NotNull
        private Long storeId;
        @NotNull
        private LocalDate bizDate;
        @NotNull
        private BigDecimal amount;
        @Size(max = 64)
        private String sourceBizId;
    }

    @Data
    public static class WorkNoteCreate {
        @NotNull
        private Long targetEmployeeId;
        private String bizType;
        private Long bizId;
        @NotBlank
        @Size(max = 500)
        private String content;
    }
}
