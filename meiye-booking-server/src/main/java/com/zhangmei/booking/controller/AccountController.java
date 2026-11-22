package com.zhangmei.booking.controller;

import com.zhangmei.booking.common.PageResult;
import com.zhangmei.booking.common.R;
import com.zhangmei.booking.dto.AppointmentView;
import com.zhangmei.booking.dto.BookingForms;
import com.zhangmei.booking.dto.ProfileView;
import com.zhangmei.booking.entity.BookingConsumption;
import com.zhangmei.booking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public R<ProfileView> profile() {
        return R.ok(accountService.profile());
    }

    @PutMapping
    public R<Void> update(@Valid @RequestBody BookingForms.ProfileUpdate request) {
        accountService.updateProfile(request);
        return R.ok(null);
    }

    @GetMapping("/appointments")
    public R<PageResult<AppointmentView>> appointments(@RequestParam(required = false) String group,
                                                         @RequestParam(required = false) String status,
                                                         @RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "20") long size) {
        return R.ok(accountService.appointments(group, status, page, size));
    }

    @GetMapping("/consumptions")
    public R<PageResult<BookingConsumption>> consumptions(@RequestParam(defaultValue = "1") long page,
                                                           @RequestParam(defaultValue = "20") long size) {
        return R.ok(accountService.consumptions(page, size));
    }
}
