package com.example.smriticounter

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class TrafficControlReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "smriti_traffic_control"
        private const val REQ_CODE = 901

        fun schedule(context: Context, enabled: Boolean) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TrafficControlReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                REQ_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (enabled) {
                val interval = AlarmManager.INTERVAL_HOUR
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + interval,
                    interval,
                    pending
                )
            } else {
                alarmManager.cancel(pending)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Traffic Control Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }

        val openBindu = PendingIntent.getActivity(
            context,
            902,
            Intent(context, BinduActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Traffic Control Pause")
            .setContentText("Take 1 minute: I am a peaceful soul. Tap to open Bindu Focus.")
            .setContentIntent(openBindu)
            .setAutoCancel(true)
            .build()

        nm.notify(903, notif)
    }
}
