package com.example.doantotnghiep.notification

import com.example.doantotnghiep.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: message.notification?.title ?: ""
        val body = message.data["body"] ?: message.notification?.body ?: ""
        val statusStr = message.data["status"]

        val statusInt = when (statusStr) {
            "DANGER" -> R.string.status_danger
            "WARNING" -> R.string.status_warning
            else -> R.string.status_safe
        }

        notificationHelper.sendAlert(title, body, statusInt)
    }
}