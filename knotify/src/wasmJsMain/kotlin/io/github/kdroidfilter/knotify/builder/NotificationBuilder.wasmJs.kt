package io.github.kdroidfilter.knotify.builder

import io.github.kdroidfilter.knotify.NotificationServiceWorker


actual fun getNotificationProvider(): NotificationProvider = NotificationServiceWorker
