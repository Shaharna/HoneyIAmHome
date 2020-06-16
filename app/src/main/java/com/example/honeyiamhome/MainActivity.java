package com.example.honeyiamhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private boolean wasTracking;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 1546;
    LocationTracker _locationTracker;
    private BroadcastReceiver startReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (intent.getAction() != "started") {
                return;
            }
            ShowLocationOnScreen();
        }
    };

    private BroadcastReceiver changeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            Button buttonSetHome = findViewById(R.id.main_set_homeLocation);
            if (intent.getAction() != "new_location") {
                return;
            }
            ShowLocationOnScreen();
            if (_locationTracker.getLocationInfo().getAccuracy() < 50) {
                buttonSetHome.setVisibility(View.VISIBLE);
            }
        }
    };
    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (intent.getAction() != "stopped") {
                return;
            }
        }
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("was_tracking",wasTracking);
    }

    private static  String PHONE = "+972545425015";
    private static  String CONTENT ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button buttonStartTrack = findViewById(R.id.main_start_tracking);
        final Button buttonStopTrack = findViewById(R.id.main_stop_tracking);
        final Button buttonSetHome = findViewById(R.id.main_set_homeLocation);
        final Button buttonClearHome = findViewById(R.id.main_clear_homeLocation);
        final Button buttonSetSMSNumber = findViewById(R.id.main_set_phone_number);
        final TextView yourLocationHeader = findViewById(R.id.mainActivity_currentLocationHeader);
        final TextView latitudeHeader = findViewById(R.id.mainActivity_latitudeHeader);
        final TextView latitudeField = findViewById(R.id.mainActivity_latitudeField);
        final TextView longitudeHeader = findViewById(R.id.mainActivity_longitudeHeader);
        final TextView longitudeField = findViewById(R.id.mainActivity_longitudeField);
        final TextView accuracyHeader = findViewById(R.id.mainActivity_accuracyHeader);
        final TextView accuracyField = findViewById(R.id.mainActivity_accuracyField);
        final TextView homeHeader = findViewById(R.id.main_homeHeader);
        final TextView homeField = findViewById(R.id.main_homeField);

        final MainActivity activity = this;
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        _locationTracker = LocationTracker.getInstance(this, manager);

        registerReceiver(startReceiver, new IntentFilter("started"));
        registerReceiver(stopReceiver, new IntentFilter("stopped"));
        registerReceiver(changeReceiver, new IntentFilter("new_location"));

        restoreHomeInfo();

        if(savedInstanceState == null) {
            wasTracking = false;
        }
        else{
            wasTracking  = savedInstanceState.getBoolean("was_tracking");
        }

        if (wasTracking)
        {
            buttonStartTrack.setVisibility(View.INVISIBLE);
            buttonStopTrack.setVisibility(View.VISIBLE);
            LocationInfoVisibility(yourLocationHeader, latitudeHeader, latitudeField,
                    longitudeHeader, longitudeField, accuracyHeader, accuracyField,
                    View.VISIBLE);
            startTrack();
        }

        buttonSetSMSNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasSMSPermission =
                        ActivityCompat.checkSelfPermission(activity,
                                Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED;
                if (hasSMSPermission) {

                    View activity_main = findViewById(R.id.main_activity);
                    LayoutInflater layoutInflater = (LayoutInflater)
                            MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View customView = layoutInflater.inflate(R.layout.popup_window, null);

                    Button cancelBtn = customView.findViewById(R.id.popup_cancel_btn);
                    Button setBtn = customView.findViewById(R.id.popup_set_btn);
                    final EditText editText = customView.findViewById(R.id.popup_edit_field);

                    //instantiate popup window
                    final PopupWindow popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    //display the popup window
                    popupWindow.showAtLocation(activity_main, Gravity.CENTER, 0, 0);

                    //close the popup window on button click
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });
                    setBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            PHONE = editText.getText().toString();
                            SharedPreferences sp =
                                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("phone_num", PHONE);
                            editor.apply();

                            popupWindow.dismiss();

                            Context context = getApplicationContext();
                            CharSequence text = "Phone number set";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    });
                } else {
                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_PERMISSION_LOCATION);


                }
            }
        });

        buttonStartTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasLocationPermission =
                        ActivityCompat.checkSelfPermission(activity,
                                Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED;
                if (hasLocationPermission) {
                        buttonStartTrack.setVisibility(View.INVISIBLE);
                        buttonStopTrack.setVisibility(View.VISIBLE);
                        LocationInfoVisibility(yourLocationHeader, latitudeHeader, latitudeField,
                                longitudeHeader, longitudeField, accuracyHeader, accuracyField,
                                View.VISIBLE);
                        startTrack();
                    }
                else {
                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_PERMISSION_LOCATION);
                }
            }
        });

        buttonStopTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonStartTrack.setVisibility(View.VISIBLE);
                buttonStopTrack.setVisibility(View.INVISIBLE);
                LocationInfoVisibility(yourLocationHeader, latitudeHeader, latitudeField,
                        longitudeHeader, longitudeField, accuracyHeader, accuracyField,
                        View.INVISIBLE);
                restoreHomeInfo();
                buttonSetHome.setVisibility(View.INVISIBLE);
                stopTrack();
                wasTracking = false;
            }
        });

        buttonSetHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = sp.edit();
                double homeLatitude = _locationTracker.getLocationInfo().getLatitude();
                double homeLongitude = _locationTracker.getLocationInfo().getLongitude();

                editor.putFloat("home_latitude", (float) homeLatitude);
                editor.putFloat("home_longitude", (float) homeLongitude);
                editor.apply();

                homeField.setText("< " + homeLatitude + ", " + homeLongitude + " >");
                HomeInfoVisibility(homeHeader, homeField, buttonClearHome, View.VISIBLE);
            }
        });

        buttonClearHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.apply();

                homeField.setText("");
                HomeInfoVisibility(homeHeader, homeField, buttonClearHome, View.INVISIBLE);
                buttonSetHome.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void HomeInfoVisibility(TextView homeHeader, TextView homeField, Button buttonClearHome,
                                    int visibility)
    {
        homeHeader.setVisibility(visibility);
        homeField.setVisibility(visibility);
        buttonClearHome.setVisibility(visibility);
    }

    private void LocationInfoVisibility(TextView yourLocationHeader, TextView latitudeHeader,
                                        TextView latitudeField, TextView longitudeHeader,
                                        TextView longitudeField, TextView accuracyHeader,
                                        TextView accuracyField, int visibility) {

        yourLocationHeader.setVisibility(visibility);
        latitudeHeader.setVisibility(visibility);
        latitudeField.setVisibility(visibility);
        longitudeHeader.setVisibility(visibility);
        longitudeField.setVisibility(visibility);
        accuracyHeader.setVisibility(visibility);
        accuracyField.setVisibility(visibility);
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        // we know we asked for only 1 permission, so we will surely get exactly 1 result
        // (grantResults.size == 1)
        // depending on your use case, if you get only SOME of your permissions
        // (but not all of them), you can act accordingly

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTrack(); // cool
        } else {
            // the user has denied our request! =-O

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // reached here? means we asked the user for this permission more than once,
                // and they still refuse. This would be a good time to open up a dialog
                // explaining why we need this permission
                // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title);

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                });
                // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    private void startTrack() {
        wasTracking = true;
        _locationTracker.startTracking(MainActivity.this);
    }

    private void stopTrack() {
        _locationTracker.stopTracking();
    }

    private void ShowLocationOnScreen() {

        double curLatitude = _locationTracker.getLocationInfo().getLatitude();
        double curLongitude = _locationTracker.getLocationInfo().getLongitude();
        float curAccuracy = _locationTracker.getLocationInfo().getAccuracy();
        TextView latitude = findViewById(R.id.mainActivity_latitudeField);
        TextView longitude = findViewById(R.id.mainActivity_longitudeField);
        TextView accuracy = findViewById(R.id.mainActivity_accuracyField);

        latitude.setText(String.format("%f", curLatitude));
        longitude.setText(String.format("%f", curLongitude));
        accuracy.setText(String.format("%f", curAccuracy));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        unregisterReceiver(startReceiver);
        unregisterReceiver(stopReceiver);
        unregisterReceiver(changeReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wasTracking)
        {
            stopTrack();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreHomeInfo();
        if (wasTracking)
        {
            startTrack();
        }
    }

    private void restoreHomeInfo() {

        final Button buttonClearHome = findViewById(R.id.main_clear_homeLocation);
        final TextView homeHeader = findViewById(R.id.main_homeHeader);
        final TextView homeField = findViewById(R.id.main_homeField);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final double homeLatitude = sp.getFloat("home_latitude", 0);
        final double homeLongitude = sp.getFloat("home_longitude", 0);
        if (homeLatitude != 0 && homeLongitude != 0) {
            // the sp is not empty and we are tracking
            homeField.setText("< " + homeLatitude + ", " + homeLongitude + " >");
            HomeInfoVisibility(homeHeader, homeField, buttonClearHome, View.VISIBLE);
        }
    }
}
