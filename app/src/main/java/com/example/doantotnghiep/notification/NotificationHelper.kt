package com.example.doantotnghiep.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.doantotnghiep.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import android.content.Intent
import android.app.PendingIntent
import com.example.doantotnghiep.ui.MainActivity

@Suppress("DEPRECATION")
class NotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {
    private val channelId = "flood_alerts"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, context.getString(R.string.alert_warning_flood), NotificationManager.IMPORTANCE_HIGH)
                .apply { description = context.getString(R.string.alert_warning_flood_2)}
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendAlert(title: String, message: String, status: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_dialog", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup("FLOOD_ALERTS")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if(status == R.string.status_danger) {
            builder.setSubText(context.getString(R.string.alert_danger))
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(3000)
            }
        }
        else if(status == R.string.status_warning) {
            builder.setSubText(context.getString(R.string.alert_warning))
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}