package com.zhangmei.store.controller;

import com.zhangmei.store.common.R;
import com.zhangmei.store.dto.ManualFeeForms;
import com.zhangmei.store.entity.ManualFeeRecord;
import com.zhangmei.store.entity.ManualFeeRule;
import com.zhangmei.store.service.ManualFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/manual-fees")
@RequiredArgsConstructor
public class ManualFeeController {

    private final ManualFeeService manualFeeService;

    @PostMapping("/rules")
    public R<Long> saveRule(@Valid @RequestBody ManualFeeForms.RuleSave request) {
        return R.ok(manualFeeService.saveRule(request));
    }

    @GetMapping("/rules")
    public R<List<ManualFeeRule>> rules(@RequestParam Long storeId,
                                         @RequestParam(required = false) Long serviceItemId) {
        return R.ok(manualFeeService.listRules(storeId, serviceItemId));
    }

    @PostMapping("/preview")
    public R<BigDecimal> preview(@Valid @RequestBody ManualFeeForms.Preview request) {
        return R.ok(manualFeeService.preview(request));
    }

    @PostMapping("/appointments/{appointmentId}/settle")
    public R<ManualFeeRecord> settle(@PathVariable Long appointmentId) {
        return R.ok(manualFeeService.settle(appointmentId));
    }

    @GetMapping("/records")
    public R<List<ManualFeeRecord>> records(@RequestParam Long storeId,
                                             @RequestParam(required = false) Long beauticianId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return R.ok(manualFeeService.listRecords(storeId, beauticianId, startDate, endDate));
    }
}
