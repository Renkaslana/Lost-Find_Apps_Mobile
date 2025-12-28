package com.campus.lostfound

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {
    private val CHANNEL_ID = "campus_lostfound_notifications"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        createChannelIfNeeded()

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Campus Lost & Found"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Anda memiliki notifikasi baru"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
        }
    }

    override fun onNewToken(token: String) {
        // Optional: send token to server if you later implement targeted messaging
        super.onNewToken(token)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notifikasi Campus Lost & Found"
            val descriptionText = "Channel untuk notifikasi aplikasi"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
