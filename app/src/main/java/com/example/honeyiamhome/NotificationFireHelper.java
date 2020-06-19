package com.example.honeyiamhome;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationFireHelper {

    private String channelId = "CHANNEL_ID_FOR_NONIMPORTANT_NOTIFICATIONS";
    private Context _context;
    NotificationFireHelper(Context context, String phoneNum, String smsContent)
    {
        _context = context;
        createChannelIfNotExist();
        actualFire(phoneNum, smsContent);
    }

    private void createChannelIfNotExist()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationManager notificationManager =
                    (NotificationManager)_context.getSystemService(Context.NOTIFICATION_SERVICE);
            for (NotificationChannel channel:notificationManager.getNotificationChannels())
            {
                if (channel.getId().equals(channelId))
                {
                    return;
                }
            }
            String name = "Sms sender";
            String descriptionText = "Channel for Sms sender";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(descriptionText);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void  actualFire(String phoneNum, String smsContent)
    {
        String content = String.format("sending sms to <%s>: <%s>", phoneNum, smsContent);

        NotificationCompat.Builder builder  = new NotificationCompat.Builder(_context, channelId)
                .setSmallIcon(R.drawable.icon)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager =
                (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(123, builder.build());
    }
}
