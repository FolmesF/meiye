package com.zhangmei.booking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "meiye.booking")
public class BookingProperties {

    private int slotStepMinutes = 30;
    private boolean exposeLoginCode = true;
    private boolean smsEnabled = false;
    private boolean pushEnabled = false;
    private int loginCodeTtlSeconds = 300;
    private int todaySlotTtlSeconds = 60;
    private int futureSlotTtlSeconds = 300;
    private int slotVersionTtlHours = 48;
    private int beauticianLockSeconds = 10;
    private int tokenTtlDays = 7;
    private int arrivalRemindHours = 2;
}
