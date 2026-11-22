package com.zhangmei.store.controller;

import com.zhangmei.store.common.R;
import com.zhangmei.store.dto.DashboardOverview;
import com.zhangmei.store.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public R<DashboardOverview> overview(@RequestParam(required = false) Long storeId) {
        return R.ok(dashboardService.overview(storeId));
    }
}
