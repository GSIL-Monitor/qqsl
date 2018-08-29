package com.hysw.qqsl.cloud.core.entity.builds;

/**
 * 地点坐标类
 */
public class GeoCoordinate {
    private double latitude;
    private double longitude;

    private GeoCoordinate() {
    }

    public GeoCoordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
