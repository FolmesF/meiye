package com.zhangmei.booking.controller;

import com.zhangmei.booking.common.R;
import com.zhangmei.booking.dto.BookingForms;
import com.zhangmei.booking.dto.LoginResult;
import com.zhangmei.booking.dto.SendCodeResult;
import com.zhangmei.booking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/code")
    public R<SendCodeResult> sendCode(@Valid @RequestBody BookingForms.SendCode request) {
        return R.ok(authService.sendCode(request.getMobile()));
    }

    @PostMapping("/login")
    public R<LoginResult> login(@Valid @RequestBody BookingForms.Login request) {
        return R.ok(authService.login(request.getMobile(), request.getCode()));
    }
}
