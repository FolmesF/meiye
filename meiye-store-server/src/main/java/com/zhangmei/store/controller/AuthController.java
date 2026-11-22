package com.zhangmei.store.controller;

import com.zhangmei.store.auth.AuthService;
import com.zhangmei.store.common.R;
import com.zhangmei.store.dto.LoginResult;
import com.zhangmei.store.dto.MenuItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request.getMobile(), request.getPassword()));
    }

    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        authService.logout(bearer(request));
        return R.ok(null);
    }

    @GetMapping("/menus")
    public R<List<MenuItem>> menus(HttpServletRequest request) {
        return R.ok(authService.menus(bearer(request)));
    }

    private String bearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return "";
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String mobile;
        @NotBlank
        private String password;
    }
}
