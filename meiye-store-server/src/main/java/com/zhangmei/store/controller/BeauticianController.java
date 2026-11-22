package com.zhangmei.store.controller;

import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.common.R;
import com.zhangmei.store.dto.BeauticianBoardRow;
import com.zhangmei.store.dto.BeauticianForms;
import com.zhangmei.store.entity.Beautician;
import com.zhangmei.store.entity.BeauticianSchedule;
import com.zhangmei.store.entity.ServiceItem;
import com.zhangmei.store.service.BeauticianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/beauticians")
@RequiredArgsConstructor
public class BeauticianController {

    private final BeauticianService beauticianService;

    @PostMapping
    public R<Long> create(@Valid @RequestBody BeauticianForms.Save request) {
        return R.ok(beauticianService.create(request));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody BeauticianForms.Update request) {
        beauticianService.update(id, request);
        return R.ok(null);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        beauticianService.delete(id);
        return R.ok(null);
    }

    @GetMapping("/{id}")
    public R<Beautician> detail(@PathVariable Long id) {
        return R.ok(beauticianService.detail(id));
    }

    @GetMapping("/board")
    public R<List<BeauticianBoardRow>> board(@RequestParam Long storeId) {
        return R.ok(beauticianService.board(storeId));
    }

    @GetMapping
    public R<PageResult<Beautician>> page(@RequestParam Long storeId,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "20") long size) {
        return R.ok(beauticianService.page(storeId, status, keyword, page, size));
    }

    @PutMapping("/{id}/services")
    public R<Void> bindServices(@PathVariable Long id, @Valid @RequestBody BeauticianForms.Bind request) {
        beauticianService.bindServices(id, request.getServiceItemIds());
        return R.ok(null);
    }

    @GetMapping("/{id}/services")
    public R<List<ServiceItem>> services(@PathVariable Long id) {
        return R.ok(beauticianService.listServices(id));
    }

    @PutMapping("/{id}/schedules")
    public R<BeauticianSchedule> saveSchedule(@PathVariable Long id, @Valid @RequestBody BeauticianForms.Schedule request) {
        return R.ok(beauticianService.saveSchedule(id, request));
    }

    @GetMapping("/{id}/schedules")
    public R<List<BeauticianSchedule>> schedules(@PathVariable Long id,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return R.ok(beauticianService.listSchedules(id, from, to));
    }
}
