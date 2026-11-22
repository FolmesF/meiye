package com.zhangmei.store.controller;

import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.common.R;
import com.zhangmei.store.dto.CustomerDetail;
import com.zhangmei.store.dto.CustomerForms;
import com.zhangmei.store.dto.CustomerQuery;
import com.zhangmei.store.dto.CustomerRow;
import com.zhangmei.store.entity.Consumption;
import com.zhangmei.store.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register-sync")
    public R<Void> registerSync(@Valid @RequestBody CustomerForms.RegisterSync request) {
        customerService.ensureRegistered(request);
        return R.ok(null);
    }

    @PostMapping("/profile-sync")
    public R<Void> syncProfile(@Valid @RequestBody CustomerForms.ProfileSync request) {
        customerService.syncProfile(request);
        return R.ok(null);
    }

    @PostMapping
    public R<Long> create(@Valid @RequestBody CustomerForms.Save request) {
        return R.ok(customerService.create(request));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CustomerForms.Update request) {
        customerService.update(id, request);
        return R.ok(null);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return R.ok(null);
    }

    @PostMapping("/batch-delete")
    public R<Void> deleteBatch(@Valid @RequestBody CustomerForms.BatchDelete request) {
        customerService.deleteBatch(request.getStoreId(), request.getIds());
        return R.ok(null);
    }

    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody CustomerForms.StatusChange request) {
        customerService.changeStatus(id, request.getStatus());
        return R.ok(null);
    }

    @GetMapping("/{id}")
    public R<CustomerDetail> detail(@PathVariable Long id) {
        return R.ok(customerService.detail(id));
    }

    @GetMapping
    public R<PageResult<CustomerRow>> page(@Valid CustomerQuery query) {
        return R.ok(customerService.page(query));
    }

    @PostMapping("/{id}/tags")
    public R<Void> addTag(@PathVariable Long id, @Valid @RequestBody CustomerForms.Tag request) {
        customerService.addTag(id, request.getTagName());
        return R.ok(null);
    }

    @DeleteMapping("/{id}/tags")
    public R<Void> removeTag(@PathVariable Long id, @RequestParam String tagName) {
        customerService.removeTag(id, tagName);
        return R.ok(null);
    }

    @GetMapping("/{id}/tags")
    public R<List<String>> tags(@PathVariable Long id) {
        return R.ok(customerService.listTags(id));
    }

    @PostMapping("/{id}/consumptions")
    public R<Long> addConsumption(@PathVariable Long id, @Valid @RequestBody CustomerForms.ConsumptionCreate request) {
        return R.ok(customerService.addConsumption(id, request));
    }

    @GetMapping("/{id}/consumptions")
    public R<List<Consumption>> consumptions(@PathVariable Long id) {
        return R.ok(customerService.listConsumptions(id));
    }
}
