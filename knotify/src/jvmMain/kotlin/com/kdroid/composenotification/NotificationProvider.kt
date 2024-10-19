package com.kdroid.composenotification

import com.kdroid.composenotification.models.NotificationBuilder

interface NotificationProvider {
    fun sendNotification(builder: NotificationBuilder)
}
