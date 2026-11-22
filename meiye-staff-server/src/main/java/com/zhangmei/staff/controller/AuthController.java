package com.zhangmei.staff.controller;

import com.zhangmei.staff.common.R;
import com.zhangmei.staff.dto.LoginResult;
import com.zhangmei.staff.dto.StaffForms;
import com.zhangmei.staff.service.AuthService;
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

    @PostMapping("/login")
    public R<LoginResult> login(@Valid @RequestBody StaffForms.Login request) {
        return R.ok(authService.login(request.getMobile()));
    }
}
