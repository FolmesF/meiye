package com.zhangmei.staff.controller;

import com.zhangmei.staff.common.R;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.entity.Attendance;
import com.zhangmei.staff.entity.RouteReport;
import com.zhangmei.staff.security.Permissions;
import com.zhangmei.staff.security.RequirePermission;
import com.zhangmei.staff.service.FieldWorkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FieldWorkController {

    private final FieldWorkService fieldWorkService;

    @PostMapping("/api/attendance/clock")
    @RequirePermission(Permissions.ATTENDANCE_CLOCK)
    public R<Attendance> clock(@Valid @RequestBody StaffForms.Clock request) {
        return R.ok(fieldWorkService.clock(request));
    }

    @GetMapping("/api/attendance")
    @RequirePermission(Permissions.ATTENDANCE_CLOCK)
    public R<List<Attendance>> attendance(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return R.ok(fieldWorkService.myAttendance(startDate, endDate));
    }

    @PostMapping("/api/route-reports")
    @RequirePermission(Permissions.ROUTE_REPORT)
    public R<Long> report(@Valid @RequestBody StaffForms.RouteReportCreate request) {
        return R.ok(fieldWorkService.reportRoute(request));
    }

    @GetMapping("/api/route-reports")
    @RequirePermission(Permissions.ROUTE_REPORT)
    public R<List<RouteReport>> routes(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return R.ok(fieldWorkService.myRoutes(startDate, endDate));
    }
}
