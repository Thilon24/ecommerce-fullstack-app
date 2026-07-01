package com.example.ecommerceapp.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class ChargerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String message = "";


        if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
            message = "Connected to Charger! ⚡";
            Toast.makeText(context, "Charging Connected", Toast.LENGTH_SHORT).show();
        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
            message = "Remove Charger! 🔌";
            Toast.makeText(context, "Charging Disconnected", Toast.LENGTH_SHORT).show();
        }

        if (!message.isEmpty()) {
            showNotification(context, message);
        }
    }

    private void showNotification(Context context, String message) {
        String channelId = "charger_status_channel";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Charger Status Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Shows battery charging and discharging status");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)

                .setSmallIcon(android.R.drawable.stat_sys_phone_call)
                .setContentTitle("Battery Status Update")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_STATUS);

        if (manager != null) {

            manager.notify(101, builder.build());
        }
    }
}