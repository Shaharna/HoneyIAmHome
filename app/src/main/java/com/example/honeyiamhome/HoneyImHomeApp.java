package com.example.honeyiamhome;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationManager;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class HoneyImHomeApp extends Application {

    private static String ACTION_SEND_SMS = "send sms";
    LocalSendSmsBroadcastReceiver _smsBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        _smsBroadcastReceiver = new LocalSendSmsBroadcastReceiver();
        this.registerReceiver(_smsBroadcastReceiver, new IntentFilter(ACTION_SEND_SMS));


        PeriodicWorkRequest locationWork =
                new PeriodicWorkRequest.Builder(RepeatedLocationWork.class, 15 , TimeUnit.MINUTES)
                .setConstraints(Constraints.NONE)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("locationWork",
                ExistingPeriodicWorkPolicy.REPLACE, locationWork);

    }
}
