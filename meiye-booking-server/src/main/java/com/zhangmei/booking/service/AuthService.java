package com.zhangmei.booking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangmei.booking.common.AccountNo;
import com.zhangmei.booking.common.BizException;
import com.zhangmei.booking.config.BookingProperties;
import com.zhangmei.booking.dto.LoginResult;
import com.zhangmei.booking.dto.SendCodeResult;
import com.zhangmei.booking.entity.BookingUser;
import com.zhangmei.booking.mapper.BookingUserMapper;
import com.zhangmei.booking.redis.LoginCodeStore;
import com.zhangmei.booking.redis.LoginTokenStore;
import com.zhangmei.booking.store.StoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final BookingUserMapper userMapper;
    private final LoginCodeStore loginCodeStore;
    private final LoginTokenStore loginTokenStore;
    private final BookingProperties properties;
    private final StoreClient storeClient;

    public SendCodeResult sendCode(String mobile) {
        if (!loginCodeStore.tryAcquireSendPermit(mobile)) {
            throw new BizException("验证码发送过于频繁，请稍后再试");
        }
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        loginCodeStore.save(mobile, code);
        SendCodeResult result = new SendCodeResult();
        result.setExpiresIn(properties.getLoginCodeTtlSeconds());
        if (properties.isExposeLoginCode()) {
            result.setCode(code);
        }
        return result;
    }

    public LoginResult login(String mobile, String code) {
        if (!loginCodeStore.matchesAndConsume(mobile, code)) {
            throw new BizException("验证码错误或已过期");
        }
        BookingUser user = userMapper.selectOne(new LambdaQueryWrapper<BookingUser>().eq(BookingUser::getMobile, mobile));
        if (user == null) {
            user = new BookingUser();
            user.setMobile(mobile);
            user.setAccountNo(AccountNo.of(mobile));
            user.setNickname("用户" + mobile.substring(mobile.length() - 4));
            user.setMemberLevel("普通会员");
            user.setStatus("ACTIVE");
            userMapper.insert(user);
        } else if (user.getAccountNo() == null || user.getAccountNo().isBlank()) {
            user.setAccountNo(AccountNo.of(user.getMobile()));
            userMapper.updateById(user);
        }
        if (user != null && !"ACTIVE".equals(user.getStatus())) {
            throw new BizException(403, "账号已停用");
        }
        storeClient.ensureCustomer(user);
        LoginResult result = new LoginResult();
        result.setToken(loginTokenStore.issue(user.getId()));
        result.setUserId(user.getId());
        result.setMobile(user.getMobile());
        result.setNickname(user.getNickname());
        return result;
    }
}
