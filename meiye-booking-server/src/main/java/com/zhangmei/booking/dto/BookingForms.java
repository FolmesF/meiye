package com.zhangmei.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class BookingForms {

    private BookingForms() {
    }

    @Data
    public static class SendCode {
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$")
        private String mobile;
    }

    @Data
    public static class Login {
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$")
        private String mobile;
        @NotBlank
        @Pattern(regexp = "^\\d{6}$")
        private String code;
    }

    @Data
    public static class ProfileUpdate {
        @NotBlank
        @Size(max = 64)
        private String nickname;
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$")
        private String mobile;
        private String gender;
        private LocalDate birthday;
        @Size(max = 255)
        private String address;
        @Size(max = 8)
        private List<@NotBlank @Size(max = 16) String> tags;
    }

    @Data
    public static class CreateAppointment {
        @NotNull
        private Long serviceItemId;
        @NotNull
        private Long beauticianId;
        @NotNull
        private LocalDateTime appointTime;
    }

    @Data
    public static class Reschedule {
        @NotNull
        private LocalDateTime appointTime;
    }

    @Data
    public static class Cancel {
        @Size(max = 255)
        private String reason;
    }

    @Data
    public static class ReviewCreate {
        @NotNull
        private Long appointmentId;
        @NotNull
        @Min(1)
        @Max(5)
        private Integer beauticianScore;
        @NotNull
        @Min(1)
        @Max(5)
        private Integer storeScore;
        @NotNull
        @Min(1)
        @Max(5)
        private Integer serviceScore;
        @Size(max = 500)
        private String content;
    }

    @Data
    public static class StoreSync {
        @NotNull
        private Long id;
        @NotBlank
        private String name;
        private String phone;
        private String address;
        @NotBlank
        private String status;
    }

    @Data
    public static class ServiceSync {
        @NotNull
        private Long id;
        @NotNull
        private Long storeId;
        @NotBlank
        private String name;
        @NotNull
        private BigDecimal price;
        @NotNull
        @Min(1)
        private Integer durationMin;
        @NotBlank
        private String status;
    }

    @Data
    public static class BeauticianSync {
        @NotNull
        private Long id;
        @NotNull
        private Long storeId;
        @NotBlank
        private String name;
        private String level;
        private String avatar;
        private LocalDate entryDate;
        @NotBlank
        private String status;
    }

    @Data
    public static class ServiceStaffSync {
        @NotNull
        @Valid
        private ServiceSync service;
        @NotNull
        @Valid
        private List<BeauticianSync> beauticians;
        @NotNull
        private List<Long> beauticianIds;
    }

    @Data
    public static class BindSync {
        @NotNull
        private Long beauticianId;
        @NotNull
        private List<Long> serviceItemIds;
    }

    @Data
    public static class MemberLevelSync {
        @NotBlank
        @Pattern(regexp = "^1\\d{10}$")
        private String mobile;
        @NotBlank
        @Size(max = 32)
        private String level;
    }

    @Data
    public static class CampaignNotify {
        @NotNull
        private Long campaignId;
        @NotBlank
        @Size(max = 64)
        private String name;
        @NotBlank
        @Size(max = 32)
        private String campaignType;
        @Size(max = 255)
        private String ruleText;
        @NotNull
        private LocalDateTime startTime;
        @NotNull
        private LocalDateTime endTime;
    }

    @Data
    public static class ScheduleSync {
        @NotNull
        private Long id;
        @NotNull
        private Long storeId;
        @NotNull
        private Long beauticianId;
        @NotNull
        private LocalDate workDate;
        @NotBlank
        private String shiftStatus;
        private LocalTime shiftStart;
        private LocalTime shiftEnd;
    }
}
