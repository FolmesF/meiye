package com.zhangmei.staff.controller;

import com.zhangmei.staff.common.R;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.entity.Inspection;
import com.zhangmei.staff.entity.Store;
import com.zhangmei.staff.security.Permissions;
import com.zhangmei.staff.security.RequirePermission;
import com.zhangmei.staff.service.StoreWorkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreWorkService storeWorkService;

    @PostMapping
    @RequirePermission(Permissions.STORE_MANAGE)
    public R<Long> create(@Valid @RequestBody StaffForms.StoreSave request) {
        return R.ok(storeWorkService.createStore(request));
    }

    @PutMapping("/{id}")
    @RequirePermission(Permissions.STORE_MANAGE)
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody StaffForms.StoreSave request) {
        storeWorkService.updateStore(id, request);
        return R.ok(null);
    }

    @GetMapping
    @RequirePermission(Permissions.STORE_MANAGE)
    public R<List<Store>> mine() {
        return R.ok(storeWorkService.myStores());
    }

    @PostMapping("/inspections")
    @RequirePermission(Permissions.INSPECTION_REPORT)
    public R<Long> inspect(@Valid @RequestBody StaffForms.InspectionCreate request) {
        return R.ok(storeWorkService.inspect(request));
    }

    @GetMapping("/{id}/inspections")
    @RequirePermission(Permissions.INSPECTION_REPORT)
    public R<List<Inspection>> inspections(@PathVariable Long id) {
        return R.ok(storeWorkService.inspections(id));
    }
}
