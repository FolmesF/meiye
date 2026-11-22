package com.zhangmei.staff.service;

import com.zhangmei.staff.common.BizException;
import com.zhangmei.staff.common.GeoDistance;
import com.zhangmei.staff.config.StaffProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class LocationGuard {

    private final StaffProperties properties;

    public void assertNear(BigDecimal baseLongitude, BigDecimal baseLatitude, BigDecimal longitude, BigDecimal latitude) {
        GeoDistance.checkCoordinate(longitude.doubleValue(), latitude.doubleValue());
        if (baseLongitude == null || baseLatitude == null) {
            return;
        }
        double meters = GeoDistance.meters(
                baseLongitude.doubleValue(), baseLatitude.doubleValue(),
                longitude.doubleValue(), latitude.doubleValue());
        if (meters > properties.getClockRadiusMeters()) {
            throw new BizException("当前位置距基准点约 " + Math.round(meters) + " 米，超出允许范围");
        }
    }
}
