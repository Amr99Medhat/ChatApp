package com.example.chatapp.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.chatapp.R
import com.example.chatapp.activities.ChatActivity
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val user = User()
        user.id = remoteMessage.data.get(Constants.KEY_USER_ID)
        user.name = remoteMessage.data.get(Constants.KEY_NAME)
        user.token = remoteMessage.data.get(Constants.KEY_FCM_TOKEN)

        val notificationId: Int = java.util.Random().nextInt()
        val channelId = "chat_message"

        val intent = Intent(this, ChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(Constants.KEY_USER, user)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setContentTitle(user.name)
        builder.setContentText(remoteMessage.data[Constants.KEY_MESSAGE])
        builder.setStyle(
            NotificationCompat.BigTextStyle().bigText(
                remoteMessage.data[Constants.KEY_MESSAGE]
            )
        )

        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName: CharSequence = "Chat Message"
            val channelDescription =
                "This notification channel is used for chat message notification"
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)

            channel.description = channelDescription
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationManagerCompat: NotificationManagerCompat =
            NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId, builder.build())
    }
}
