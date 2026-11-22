package com.zhangmei.booking.controller;

import com.zhangmei.booking.common.R;
import com.zhangmei.booking.dto.BookingForms;
import com.zhangmei.booking.entity.BookingAppointment;
import com.zhangmei.booking.service.AppointmentCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentCommandService appointmentCommandService;

    @PostMapping
    public R<BookingAppointment> create(@Valid @RequestBody BookingForms.CreateAppointment request) {
        return R.ok(appointmentCommandService.create(request));
    }

    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id, @Valid @RequestBody(required = false) BookingForms.Cancel request) {
        String reason = request == null ? null : request.getReason();
        appointmentCommandService.cancel(id, reason);
        return R.ok(null);
    }

    @PostMapping("/{id}/reschedule")
    public R<Void> reschedule(@PathVariable Long id, @Valid @RequestBody BookingForms.Reschedule request) {
        appointmentCommandService.reschedule(id, request.getAppointTime());
        return R.ok(null);
    }
}
