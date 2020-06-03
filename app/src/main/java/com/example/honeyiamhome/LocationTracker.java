package com.example.honeyiamhome;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;


public class LocationTracker {

    private LocationManager locationManager;
    private Context _context;
    private LocationInfo _locationInfo;
    private static LocationTracker single_instance = null;

    // static method to create instance of Singleton class
    static LocationTracker getInstance(Context context, LocationManager manager)
    {
        if (single_instance == null)
            single_instance = new LocationTracker(context, manager);

        return single_instance;
    }

    private LocationTracker(Context context, LocationManager manager)
    {
        _context = context;
        _locationInfo = new LocationInfo();
        locationManager= manager;

    }

    LocationListener locationListenerGPS=new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            float accuracy = location.getAccuracy();
            _locationInfo.setLatitude(latitude);
            _locationInfo.setLongitude(longitude);
            _locationInfo.setAccuracy(accuracy);

            Intent intent = new Intent();
            intent.setAction("new_location");
            _context.sendBroadcast(intent);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            boolean hasTrackingPermission = ActivityCompat.checkSelfPermission(_context,
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED;
            if (hasTrackingPermission) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    void startTracking(Activity activity){
        //a basic check that assert you have the runtime location permission.
        boolean hasLocationPermission =
                ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
        if (hasLocationPermission) {
            Intent intent = new Intent();
            intent.setAction("started");
            _context.sendBroadcast(intent);
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGPS);
        }
    }

    void stopTracking(){
        locationManager.removeUpdates(locationListenerGPS);
        Intent intent = new Intent();
        intent.setAction("stopped");
        _context.sendBroadcast(intent);
    }

    LocationInfo getLocationInfo(){
        return _locationInfo;
    }

}

