package com.zhangmei.store.auth;

import com.zhangmei.store.common.BizException;
import com.zhangmei.store.common.StoreNames;
import com.zhangmei.store.dto.LoginResult;
import com.zhangmei.store.dto.MenuItem;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final List<Account> ACCOUNTS = List.of(
            new Account("13800000000", "123456", "店长", 1L, StoreMenus.ALL),
            new Account("13900000000", "123456", "前台", 1L, StoreMenus.byCodes(
                    "customer", "beautician", "service", "appointment-stat"))
    );

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public LoginResult login(String mobile, String password) {
        Account account = ACCOUNTS.stream()
                .filter(item -> item.mobile().equals(mobile) && item.password().equals(password))
                .findFirst()
                .orElseThrow(() -> new BizException(401, "手机号或密码错误"));
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new Session(account, Instant.now().plus(7, ChronoUnit.DAYS)));
        return toResult(token, account);
    }

    public List<MenuItem> menus(String token) {
        return require(token).menus();
    }

    public Account require(String token) {
        if (token == null || token.isBlank()) {
            throw new BizException(401, "未登录");
        }
        Session session = sessions.get(token);
        if (session == null || session.expireAt().isBefore(Instant.now())) {
            sessions.remove(token);
            throw new BizException(401, "登录已失效");
        }
        return session.account();
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private LoginResult toResult(String token, Account account) {
        LoginResult result = new LoginResult();
        result.setToken(token);
        result.setName(account.name());
        result.setMobile(account.mobile());
        result.setStoreId(account.storeId());
        result.setStoreName(StoreNames.of(account.storeId()));
        result.setMenus(account.menus());
        return result;
    }

    public record Account(String mobile, String password, String name, Long storeId, List<MenuItem> menus) {
    }

    private record Session(Account account, Instant expireAt) {
    }
}
