package com.zhangmei.booking.notify;

import com.zhangmei.booking.config.BookingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotifyChannel implements NotifyChannel {

    private final BookingProperties properties;

    @Override
    public String channel() {
        return "SMS";
    }

    @Override
    public void send(NotifyCommand command) {
        if (!properties.isSmsEnabled()) {
            return;
        }
        log.info("sms reserved userId={} type={} bizId={}", command.getUserId(), command.getType(), command.getBizId());
    }
}
