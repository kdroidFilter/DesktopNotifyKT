package com.kdroid.composenotification

import com.kdroid.composenotification.builder.NotificationBuilder

internal interface NotificationProvider {
    fun sendNotification(builder: NotificationBuilder)
}
