package com.zhangmei.staff.controller;

import com.zhangmei.staff.common.PageResult;
import com.zhangmei.staff.common.R;
import com.zhangmei.staff.dto.ProspectCard;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.entity.Followup;
import com.zhangmei.staff.entity.VisitTemplate;
import com.zhangmei.staff.security.Permissions;
import com.zhangmei.staff.security.RequirePermission;
import com.zhangmei.staff.service.SalesService;
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
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @PostMapping("/api/visit-templates")
    @RequirePermission(Permissions.VISIT_TEMPLATE)
    public R<Long> createTemplate(@Valid @RequestBody StaffForms.TemplateSave request) {
        return R.ok(salesService.saveTemplate(null, request));
    }

    @PutMapping("/api/visit-templates/{id}")
    @RequirePermission(Permissions.VISIT_TEMPLATE)
    public R<Long> updateTemplate(@PathVariable Long id, @Valid @RequestBody StaffForms.TemplateSave request) {
        return R.ok(salesService.saveTemplate(id, request));
    }

    @GetMapping("/api/visit-templates")
    @RequirePermission(Permissions.VISIT_TEMPLATE)
    public R<List<VisitTemplate>> templates() {
        return R.ok(salesService.templates());
    }

    @DeleteMapping("/api/visit-templates/{id}")
    @RequirePermission(Permissions.VISIT_TEMPLATE)
    public R<Void> deleteTemplate(@PathVariable Long id) {
        salesService.deleteTemplate(id);
        return R.ok(null);
    }

    @PostMapping("/api/prospects")
    @RequirePermission(Permissions.PROSPECT_WRITE)
    public R<Long> createProspect(@Valid @RequestBody StaffForms.ProspectSave request) {
        return R.ok(salesService.saveProspect(null, request));
    }

    @PutMapping("/api/prospects/{id}")
    @RequirePermission(Permissions.PROSPECT_WRITE)
    public R<Long> updateProspect(@PathVariable Long id, @Valid @RequestBody StaffForms.ProspectSave request) {
        return R.ok(salesService.saveProspect(id, request));
    }

    @GetMapping("/api/prospects")
    @RequirePermission(Permissions.PROSPECT_WRITE)
    public R<PageResult<ProspectCard>> prospects(@RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "20") long size) {
        return R.ok(salesService.prospects(status, page, size));
    }

    @PutMapping("/api/prospects/{id}/status")
    @RequirePermission(Permissions.PROSPECT_WRITE)
    public R<Void> prospectStatus(@PathVariable Long id, @Valid @RequestBody StaffForms.StatusChange request) {
        salesService.changeProspectStatus(id, request.getStatus());
        return R.ok(null);
    }

    @PostMapping("/api/followups")
    @RequirePermission(Permissions.FOLLOWUP_WRITE)
    public R<Long> follow(@Valid @RequestBody StaffForms.FollowupCreate request) {
        return R.ok(salesService.follow(request));
    }

    @GetMapping("/api/prospects/{id}/followups")
    @RequirePermission(Permissions.FOLLOWUP_WRITE)
    public R<List<Followup>> followups(@PathVariable Long id) {
        return R.ok(salesService.followups(id));
    }
}
