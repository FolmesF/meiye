package com.zhangmei.store.controller;

import com.zhangmei.store.common.PageResult;
import com.zhangmei.store.common.R;
import com.zhangmei.store.dto.ServiceCard;
import com.zhangmei.store.dto.ServiceItemForms;
import com.zhangmei.store.entity.ServiceItem;
import com.zhangmei.store.entity.ServiceMaterial;
import com.zhangmei.store.service.ServiceItemService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/service-items")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    @PostMapping
    public R<Long> create(@Valid @RequestBody ServiceItemForms.Save request) {
        return R.ok(serviceItemService.create(request));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ServiceItemForms.Update request) {
        serviceItemService.update(id, request);
        return R.ok(null);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        serviceItemService.delete(id);
        return R.ok(null);
    }

    @GetMapping("/cards")
    public R<PageResult<ServiceCard>> cards(@RequestParam Long storeId,
                                             @RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "16") long size) {
        return R.ok(serviceItemService.cards(storeId, page, size));
    }

    @GetMapping("/{id}/card")
    public R<ServiceCard> card(@PathVariable Long id) {
        return R.ok(serviceItemService.card(id));
    }

    @GetMapping("/{id}")
    public R<ServiceItem> detail(@PathVariable Long id) {
        return R.ok(serviceItemService.detail(id));
    }

    @GetMapping
    public R<PageResult<ServiceItem>> page(@RequestParam Long storeId,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "20") long size) {
        return R.ok(serviceItemService.page(storeId, status, keyword, page, size));
    }

    @PutMapping("/{id}/beauticians")
    public R<Void> replaceBeauticians(@PathVariable Long id, @Valid @RequestBody ServiceItemForms.Beauticians request) {
        serviceItemService.replaceBeauticians(id, request.getBeauticianIds());
        return R.ok(null);
    }

    @PutMapping("/{id}/materials")
    public R<Void> replaceMaterials(@PathVariable Long id, @Valid @RequestBody ServiceItemForms.Materials request) {
        serviceItemService.replaceMaterials(id, request.getMaterials());
        return R.ok(null);
    }

    @GetMapping("/{id}/materials")
    public R<List<ServiceMaterial>> materials(@PathVariable Long id) {
        return R.ok(serviceItemService.listMaterials(id));
    }
}
