package com.zhangmei.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

public final class CampaignForms {

    private CampaignForms() {
    }

    @Data
    public static class Save {
        @NotNull
        private Long storeId;
        @NotBlank
        @Size(max = 64)
        private String name;
        @NotBlank
        @Size(max = 32)
        private String campaignType;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
        @Size(max = 255)
        private String ruleText;
    }
}
