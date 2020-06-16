package com.example.honeyiamhome;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class LocalSendSmsBroadcastReceiver extends BroadcastReceiver {

    private static final int REQUEST_CODE_PERMISSION_SMS = 1545;
    public static final String LOG_TAG = "SEND_SMS_BROADCAST";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean hasSmsPermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
                        PackageManager.PERMISSION_GRANTED;


        if (hasSmsPermission) {
            sendSms(intent);
        } else {
            Log.e(LOG_TAG, "Permission denied");
            return;
        }
    }
//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode,
//            @NonNull String[] permissions,
//            @NonNull int[] grantResults
//    ) {
//        // we know we asked for only 1 permission, so we will surely get exactly 1 result
//        // (grantResults.size == 1)
//        // depending on your use case, if you get only SOME of your permissions
//        // (but not all of them), you can act accordingly
//
//        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            sendSms(); // cool
//        } else {
//            // the user has denied our request! =-O
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                    this,
//                    Manifest.permission.SEND_SMS)) {
//                // reached here? means we asked the user for this permission more than once,
//                // and they still refuse. This would be a good time to open up a dialog
//                // explaining why we need this permission
//            }
//        }
//    }

    private void sendSms(Intent intent) {
        String phoneNum = intent.getStringExtra("phone");
        String content = intent.getStringExtra("content");
        if ((phoneNum==null) || (phoneNum.equals("")) || (content==null) || (content.equals(""))) {
            Log.e(LOG_TAG, "Empty phone number or content");
            return;
        }
        SmsManager.getDefault().sendTextMessage(
                phoneNum,
                null,
                content,
                null,
                null);
    }

}



