package com.skillsync.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skillsync.app.R
import com.skillsync.app.ui.main.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Message"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "You received a new message."
        val chatId = remoteMessage.data["chatId"]
        
        sendNotification(title, body, chatId)
    }

    override fun onNewToken(token: String) {
        val uid = com.skillsync.app.util.FirebaseUtil.currentUid
        if (uid.isNotEmpty()) {
            com.skillsync.app.util.FirebaseUtil.firestore.collection(com.skillsync.app.util.Constants.COLL_USERS)
                .document(uid).update("fcmToken", token)
        }
    }

    private fun sendNotification(title: String, messageBody: String, chatId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "chat_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (chatId != null) {
                putExtra("chatId", chatId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(chatId?.hashCode() ?: System.currentTimeMillis().toInt(), notification)
    }
}
