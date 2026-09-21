package com.murphy.scarlet.inspector.internal

import android.app.Service
import android.content.Intent
import android.os.IBinder

class InspectorService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                val notification = InspectorNotificationManager.buildNotification(this)
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_DISMISS -> {
                stopForeground(true)
            }
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    companion object {
        const val ACTION_SHOW = "com.tinder.scarlet.inspector.action.SHOW_NOTIFICATION"
        const val ACTION_DISMISS = "com.tinder.scarlet.inspector.action.DISMISS_NOTIFICATION"
        private const val NOTIFICATION_ID = 1111
    }
}
