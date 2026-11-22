package com.zhangmei.store.auth;

import com.zhangmei.store.common.BizException;
import com.zhangmei.store.dto.LoginResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @Test
    void loginReturnsMenusOfTheAccount() {
        LoginResult manager = authService.login("13800000000", "123456");
        assertEquals(8, manager.getMenus().size());
        assertEquals("顾客管理", manager.getMenus().get(0).getTitle());

        LoginResult front = authService.login("13900000000", "123456");
        assertEquals(4, front.getMenus().size());
        assertEquals(front.getMenus(), authService.menus(front.getToken()));
    }

    @Test
    void wrongPasswordRejected() {
        BizException error = assertThrows(BizException.class, () -> authService.login("13800000000", "000000"));
        assertEquals(401, error.getCode());
    }
}
