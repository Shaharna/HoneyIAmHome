package com.example.honeyiamhome;

public class LocationInfo {

    private double _latitude;
    private double _longitude;
    private float _accuracy;

    public LocationInfo(){

    }

    public double getLongitude() {
        return _longitude;
    }

    public double getLatitude() {
        return _latitude;
    }

    public float getAccuracy() {
        return _accuracy;
    }

    public void setAccuracy(float accuracy) {
        this._accuracy = accuracy;
    }

    public void setLongitude(double longitude) {
        this._longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this._latitude = latitude;
    }
}
