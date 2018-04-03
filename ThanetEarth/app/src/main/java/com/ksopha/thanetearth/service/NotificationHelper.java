package com.ksopha.thanetearth.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.ksopha.thanetearth.R;
import com.ksopha.thanetearth.activity.Main;

/**
 * Created by Kelvin Sopha on 31/03/18.
 */

public class NotificationHelper extends ContextWrapper{

    private Context base;
    private String id = "com.ksopha.thanetearth";
    private String name = "Thanet earth channel";

    public NotificationHelper(Context base){
        super(base);

        createChannel();
    }


    private void createChannel() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channel =  new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);

            channel.enableLights(true);
            channel.enableVibration(true);


            // Register the channel with the system
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(channel);
        }
    }



    public void sendNotification(){

        // intent to open activity if notification clicked
        Intent intent = new Intent(getApplicationContext(), Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Bundle bd = new Bundle();
        bd.putInt("alerts", 1);
        intent.putExtras(bd);

        PendingIntent action = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Thanet Earth - Alert")
                .setContentText("New alerts available")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(action)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // show notification
        notificationManager.notify(5, mBuilder.build());

    }


}
