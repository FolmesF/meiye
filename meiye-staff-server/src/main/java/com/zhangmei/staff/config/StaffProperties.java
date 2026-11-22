package com.zhangmei.staff.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "meiye.staff")
public class StaffProperties {

    private int tokenTtlDays = 7;
    private int clockRadiusMeters = 500;
}
