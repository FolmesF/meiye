package com.zhangmei.staff.controller;

import com.zhangmei.staff.common.R;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.dto.TeamStatusRow;
import com.zhangmei.staff.entity.WorkNote;
import com.zhangmei.staff.security.Permissions;
import com.zhangmei.staff.security.RequirePermission;
import com.zhangmei.staff.service.AuthService;
import com.zhangmei.staff.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final AuthService authService;
    private final TeamService teamService;

    @PostMapping("/employees")
    @RequirePermission(Permissions.EMPLOYEE_MANAGE)
    public R<Long> createEmployee(@Valid @RequestBody StaffForms.EmployeeCreate request) {
        return R.ok(authService.createEmployee(request.getName(), request.getMobile(), request.getRoleCode(),
                request.getBaseLongitude(), request.getBaseLatitude()));
    }

    @GetMapping("/status")
    @RequirePermission(Permissions.EMPLOYEE_MONITOR)
    public R<List<TeamStatusRow>> status(@RequestParam(required = false) String status) {
        return R.ok(teamService.status(status));
    }

    @PutMapping("/employees/{id}/status")
    @RequirePermission(Permissions.EMPLOYEE_MONITOR)
    public R<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StaffForms.StatusChange request) {
        teamService.changeStatus(id, request.getStatus());
        return R.ok(null);
    }

    @PostMapping("/notes")
    @RequirePermission(Permissions.WORK_ANNOTATE)
    public R<Long> annotate(@Valid @RequestBody StaffForms.WorkNoteCreate request) {
        return R.ok(teamService.annotate(request));
    }

    @GetMapping("/employees/{id}/notes")
    @RequirePermission(Permissions.WORK_ANNOTATE)
    public R<List<WorkNote>> notes(@PathVariable Long id) {
        return R.ok(teamService.notes(id));
    }
}
