package com.zhangmei.store.controller;

import com.zhangmei.store.common.R;
import com.zhangmei.store.dto.AppointmentForms;
import com.zhangmei.store.dto.AppointmentRecord;
import com.zhangmei.store.dto.AppointmentStatQuery;
import com.zhangmei.store.dto.AppointmentStatResponse;
import com.zhangmei.store.entity.Appointment;
import com.zhangmei.store.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public R<Long> create(@Valid @RequestBody AppointmentForms.Save request) {
        return R.ok(appointmentService.create(request));
    }

    @GetMapping("/{id}")
    public R<Appointment> detail(@PathVariable Long id) {
        return R.ok(appointmentService.detail(id));
    }

    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody AppointmentForms.StatusChange request) {
        appointmentService.changeStatus(id, request);
        return R.ok(null);
    }

    @GetMapping("/records")
    public R<List<AppointmentRecord>> records(@RequestParam Long storeId,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                              @RequestParam(required = false) String serviceName,
                                              @RequestParam(required = false) String beauticianName,
                                              @RequestParam(required = false) String mobile,
                                              @RequestParam(required = false) String phase) {
        return R.ok(appointmentService.records(storeId, startDate, endDate, serviceName, beauticianName, mobile, phase));
    }

    @GetMapping("/workable")
    public R<List<AppointmentRecord>> workable(@RequestParam Long storeId) {
        return R.ok(appointmentService.workable(storeId));
    }

    @PostMapping("/{id}/start")
    public R<Void> start(@PathVariable Long id) {
        appointmentService.start(id);
        return R.ok(null);
    }

    @PostMapping("/{id}/finish")
    public R<Void> finish(@PathVariable Long id) {
        appointmentService.finish(id);
        return R.ok(null);
    }

    @PostMapping("/booking-sync")
    public R<Void> syncFromBooking(@Valid @RequestBody AppointmentForms.BookingSync request) {
        appointmentService.syncFromBooking(request);
        return R.ok(null);
    }

    @GetMapping("/stats")
    public R<List<AppointmentStatResponse>> stats(@Valid AppointmentStatQuery query) {
        return R.ok(appointmentService.stats(query));
    }
}
