package com.zhangmei.store.controller;

import com.zhangmei.store.booking.BookingClient;
import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.common.R;
import com.zhangmei.store.dto.CampaignForms;
import com.zhangmei.store.entity.Campaign;
import com.zhangmei.store.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final BookingClient bookingClient;

    @PostMapping
    public R<Long> create(@Valid @RequestBody CampaignForms.Save request) {
        return R.ok(campaignService.create(request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return R.ok(null);
    }

    @GetMapping
    public R<PageResult<Campaign>> page(@RequestParam Long storeId,
                                         @RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "20") long size) {
        return R.ok(campaignService.page(storeId, page, size));
    }

    @PostMapping("/{id}/publish")
    public R<Integer> publish(@PathVariable Long id) {
        return R.ok(bookingClient.pushCampaign(campaignService.mustGet(id)));
    }
}
