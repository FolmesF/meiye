package com.zhangmei.staff.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoDistanceTest {

    @Test
    void samePointIsZero() {
        assertEquals(0D, GeoDistance.meters(116.397128, 39.916527, 116.397128, 39.916527), 0.001);
    }

    @Test
    void oneLatitudeStepIsAbout111Kilometers() {
        double meters = GeoDistance.meters(116.0, 39.0, 116.0, 40.0);
        assertTrue(meters > 110_000 && meters < 112_000);
    }

    @Test
    void rejectsOutOfRangeCoordinate() {
        assertThrows(BizException.class, () -> GeoDistance.checkCoordinate(181, 0));
    }
}
