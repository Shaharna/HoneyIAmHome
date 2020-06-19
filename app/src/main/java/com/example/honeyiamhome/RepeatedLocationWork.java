package com.example.honeyiamhome;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.ActivityCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.common.util.concurrent.ListenableFuture;

public class RepeatedLocationWork extends ListenableWorker {

    private static final String LAST_LOCATION_LATITUDE = "last_location_latitude";
    private static final String LAST_LOCATION_LONGITUDE = "last_location_longitude";
    private static final String HONEY_I_M_HOME = "Honey I'm Home!";
    private static String ACTION_SEND_SMS = "send sms";
    private static String PHONE = "phone";
    private static  String CONTENT ="content";

    private CallbackToFutureAdapter.Completer<Result> _callback;
    private BroadcastReceiver _receiver;
    private Context _appContext;
    private LocationTracker _locationTracker;
    private LocationManager _manager;
    private static final String HOME_LATITUDE = "home_latitude";
    private static final String HOME_LONGITUDE = "home_longitude";


    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public RepeatedLocationWork(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        _appContext = appContext;
        _manager = (LocationManager) _appContext.getSystemService(Context.LOCATION_SERVICE);

    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        _locationTracker = LocationTracker.getInstance(_appContext.getApplicationContext(), _manager);
        // 1. here we create the future and store the callback for later use
        ListenableFuture<Result> future = CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Result>() {
            @Nullable
            @Override
            public Object attachCompleter(@NonNull CallbackToFutureAdapter.Completer<Result> callback) throws Exception {
                _callback = callback;
                return null;
            }
        });

        // we place the broadcast receiver and immediately return the "future" object
        placeReceiver();
        return future;
    }

    private void placeReceiver() {
        boolean hasSMSPermission =
                ActivityCompat.checkSelfPermission(_appContext.getApplicationContext(),
                        Manifest.permission.SEND_SMS) ==
                        PackageManager.PERMISSION_GRANTED;

        boolean hasLocationPermission =
                ActivityCompat.checkSelfPermission(_appContext.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;

        if (hasLocationPermission && hasSMSPermission) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_appContext.getApplicationContext());
            double homeLatitude = sp.getFloat(HOME_LATITUDE, 0);
            double homeLongitude = sp.getFloat(HOME_LONGITUDE, 0);
            String phone = sp.getString(PHONE, "");
            if (homeLatitude != 0 && homeLongitude != 0 && !phone.equals("")) {
                _locationTracker.startTracking();
            }
        }
        _callback.set(Result.success());

        _receiver = new BroadcastReceiver() {
            // notice that the fun onReceive() will get called in the future, not now
            @Override
            public void onReceive(Context context, Intent intent) {
                // got broadcast!
                if (intent == null) {
                    return;
                }
                if (intent.getAction() != "new_location") {
                    return;
                }
                if (_locationTracker.getLocationInfo().getAccuracy() < 50) {
                    _locationTracker.stopTracking();
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_appContext.getApplicationContext());
                    double previousLatitude = sp.getFloat(LAST_LOCATION_LATITUDE, 0);
                    double previousLongitude = sp.getFloat(LAST_LOCATION_LONGITUDE, 0);
                    double currentLatitude = _locationTracker.getLocationInfo().getLatitude();
                    double currentLongitude = _locationTracker.getLocationInfo().getLongitude();
                    float [] res = new float[1];
                    Location.distanceBetween(previousLatitude, previousLongitude, currentLatitude, currentLongitude,res);
                        if (res[0] > 50) {
                            double homeLatitude = sp.getFloat(HOME_LATITUDE, 0);
                            double homeLongitude = sp.getFloat(HOME_LONGITUDE, 0);
                            if (homeLatitude != 0 && homeLongitude != 0) {
                                Location.distanceBetween(homeLatitude, homeLongitude, currentLatitude, currentLongitude,res);
                                if (res[0] < 50)
                                {
                                    String phone = sp.getString(PHONE, "");
                                    Intent smsIntent = new Intent();
                                    smsIntent.setAction(ACTION_SEND_SMS);
                                    smsIntent.putExtra(PHONE, phone);
                                    smsIntent.putExtra(CONTENT, HONEY_I_M_HOME);
                                    _appContext.sendBroadcast(smsIntent);
                                }
                            }
                        }
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putFloat(LAST_LOCATION_LATITUDE, (float) currentLatitude);
                    editor.putFloat(LAST_LOCATION_LONGITUDE, (float) currentLongitude);
                    editor.apply();
                }
                _callback.set(Result.success());
            }
        };
        _appContext.registerReceiver(_receiver, new IntentFilter("new_location"));
    }

}