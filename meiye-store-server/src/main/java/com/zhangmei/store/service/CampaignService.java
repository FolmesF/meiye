package com.zhangmei.store.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangmei.store.common.BizException;
import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.dto.CampaignForms;
import com.zhangmei.store.entity.Campaign;
import com.zhangmei.store.mapper.CampaignMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignMapper campaignMapper;

    public Long create(CampaignForms.Save request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BizException("活动结束日期不能早于开始日期");
        }
        LocalDateTime start = request.getStartDate().atStartOfDay();
        LocalDateTime end = request.getEndDate().atTime(LocalTime.of(23, 59, 59));
        if (!end.isAfter(LocalDateTime.now())) {
            throw new BizException("活动日期已过期");
        }
        Campaign campaign = new Campaign();
        campaign.setStoreId(request.getStoreId());
        campaign.setName(request.getName().trim());
        campaign.setCampaignType(request.getCampaignType().trim());
        campaign.setStartTime(start);
        campaign.setEndTime(end);
        campaign.setRuleText(request.getRuleText());
        campaign.setStatus(statusOf(start, end));
        campaignMapper.insert(campaign);
        return campaign.getId();
    }

    public void delete(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BizException(404, "活动不存在");
        }
        campaignMapper.deleteById(id);
    }

    public PageResult<Campaign> page(Long storeId, long page, long size) {
        if (storeId == null) {
            throw new BizException("门店不能为空");
        }
        purgeExpired();
        Page<Campaign> result = campaignMapper.selectPage(
                new Page<>(Checks.page(page), Checks.size(size)),
                new LambdaQueryWrapper<Campaign>()
                        .eq(Campaign::getStoreId, storeId)
                        .orderByDesc(Campaign::getStartTime)
                        .orderByDesc(Campaign::getId));
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public Campaign mustGet(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BizException(404, "活动不存在");
        }
        return campaign;
    }

    public void purgeExpired() {
        LocalDateTime now = LocalDateTime.now();
        campaignMapper.delete(new LambdaQueryWrapper<Campaign>().lt(Campaign::getEndTime, now));
        campaignMapper.update(null, new LambdaUpdateWrapper<Campaign>()
                .gt(Campaign::getStartTime, now)
                .set(Campaign::getStatus, "未开始"));
        campaignMapper.update(null, new LambdaUpdateWrapper<Campaign>()
                .le(Campaign::getStartTime, now)
                .ge(Campaign::getEndTime, now)
                .ne(Campaign::getStatus, "进行中")
                .set(Campaign::getStatus, "进行中"));
    }

    private String statusOf(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            return "未开始";
        }
        if (now.isAfter(end)) {
            return "已结束";
        }
        return "进行中";
    }
}
