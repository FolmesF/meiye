package com.zhangmei.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CampaignExpiry {

    private final CampaignService campaignService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Shanghai")
    public void deleteExpired() {
        campaignService.purgeExpired();
    }
}
