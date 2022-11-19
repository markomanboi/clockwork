package com.example.clockwork_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String getName=intent.getExtras().getString("Name");
        String getClassName=intent.getExtras().getString("className");
        String getChannelID=intent.getExtras().getString("channelID");
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context, "notifyAppointment")
                .setSmallIcon(R.drawable.clockwork_logo)
                .setContentTitle("Clockwork - Appointment in 5 Minutes!")
                .setContentText("Appointment with "+getName+" from "+getClassName+" class")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager=NotificationManagerCompat.from(context);
        notificationManager.notify(Integer.parseInt(getChannelID), builder.build());
    }

}
