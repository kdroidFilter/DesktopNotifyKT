package com.kdroid.composenotification.builder

interface NotificationProvider {
    fun sendNotification(builder: NotificationBuilder)
}
