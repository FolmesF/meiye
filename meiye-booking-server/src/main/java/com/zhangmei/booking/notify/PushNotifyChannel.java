package com.zhangmei.booking.notify;

import com.zhangmei.booking.config.BookingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotifyChannel implements NotifyChannel {

    private final BookingProperties properties;

    @Override
    public String channel() {
        return "PUSH";
    }

    @Override
    public void send(NotifyCommand command) {
        if (!properties.isPushEnabled()) {
            return;
        }
        log.info("push reserved userId={} type={} bizId={}", command.getUserId(), command.getType(), command.getBizId());
    }
}
