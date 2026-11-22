package com.zhangmei.staff.controller;

import com.zhangmei.staff.common.R;
import com.zhangmei.staff.dto.PerformanceStatRow;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.security.Permissions;
import com.zhangmei.staff.security.RequirePermission;
import com.zhangmei.staff.service.PerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/self")
    @RequirePermission(Permissions.PERFORMANCE_SELF)
    public R<List<PerformanceStatRow>> self(@Valid StaffForms.PerformanceQuery query) {
        return R.ok(performanceService.self(query));
    }

    @GetMapping("/store")
    @RequirePermission(Permissions.PERFORMANCE_STORE)
    public R<List<PerformanceStatRow>> store(@Valid StaffForms.PerformanceQuery query) {
        return R.ok(performanceService.store(query));
    }

    @GetMapping("/team")
    @RequirePermission(Permissions.PERFORMANCE_TEAM)
    public R<List<PerformanceStatRow>> team(@Valid StaffForms.PerformanceQuery query) {
        return R.ok(performanceService.team(query));
    }

    @PostMapping
    @RequirePermission(Permissions.PERFORMANCE_TEAM)
    public R<Long> record(@Valid @RequestBody StaffForms.PerformanceRecord request) {
        return R.ok(performanceService.record(request));
    }
}
