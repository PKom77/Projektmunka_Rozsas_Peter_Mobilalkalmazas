package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHandler {

    private Context cont;

    private int NOTIFICATION_ID = 0;
    private NotificationManager manager;

    private static final String CHANNEL_ID ="utazom_notification_channel";


    public NotificationHandler(Context context) {
        this.cont = context;
        this.manager = (NotificationManager) cont.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();
    }

    private void createChannel(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return;
        }

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Utazom Notification", NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setDescription("Buszjeggyel kapcsolatos informacio");
        this.manager.createNotificationChannel(channel);
    }

    public void send(String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(cont, CHANNEL_ID)
                .setContentTitle("Holnap utazom")
                .setContentText(message)
                .setSmallIcon(R.drawable.icon);

        this.manager.notify(NOTIFICATION_ID, builder.build());
    }
}
