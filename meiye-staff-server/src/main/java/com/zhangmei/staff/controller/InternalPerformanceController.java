package com.zhangmei.staff.controller;

import com.zhangmei.staff.common.R;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.service.PerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/performances")
@RequiredArgsConstructor
public class InternalPerformanceController {

    private final PerformanceService performanceService;

    @PostMapping
    public R<Long> record(@Valid @RequestBody StaffForms.PerformanceRecord request) {
        return R.ok(performanceService.record(request));
    }
}
