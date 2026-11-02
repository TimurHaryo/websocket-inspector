package com.murphy.scarlet.inspector.internal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.murphy.scarlet.inspector.InspectorLauncher

/**
 * Manages the persistent notification for launching the Inspector.
 * Designed to be efficient and memory-leak free by using Application Context.
 */
object InspectorNotificationManager {

    private const val CHANNEL_ID = "websocket_inspector_channel"
    private const val CHANNEL_NAME = "WebSocket Inspector"

    fun showNotification(context: Context) {
        val intent = Intent(context, InspectorService::class.java).apply {
            action = InspectorService.ACTION_SHOW
        }
        // For O+, we might need startForegroundService if app is in background, 
        // but since we call this from Activity (foreground), startService is fine usually.
        // But to be safe for "startForeground" contract:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun dismissNotification(context: Context) {
        val intent = Intent(context, InspectorService::class.java).apply {
            action = InspectorService.ACTION_DISMISS
        }
        context.startService(intent)
    }

    // Moved logic here to be reused by Service
    fun buildNotification(context: Context): Notification {
        val appContext = context.applicationContext
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tap to open WebSocket Inspector"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = InspectorLauncher.getLaunchIntent(appContext)
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setContentTitle("WebSocket Inspector Active")
            .setContentText("Tap to view sessions")
            .setSmallIcon(android.R.drawable.ic_menu_info_details) 
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
