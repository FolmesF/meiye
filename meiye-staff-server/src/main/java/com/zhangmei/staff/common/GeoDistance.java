package com.zhangmei.staff.common;

import com.zhangmei.staff.common.BizException;

public final class GeoDistance {

    private static final double EARTH_METERS = 6_371_000D;

    private GeoDistance() {
    }

    public static void checkCoordinate(double longitude, double latitude) {
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            throw new BizException("经纬度超出有效范围");
        }
    }

    public static double meters(double longitude1, double latitude1, double longitude2, double latitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double dLat = lat2 - lat1;
        double dLng = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
