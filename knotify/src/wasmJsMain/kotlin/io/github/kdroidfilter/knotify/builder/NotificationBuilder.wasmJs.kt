package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.kdroidfilter.knotify.builder.NotificationBuilder
import io.github.kdroidfilter.knotify.builder.NotificationProvider
import io.github.kdroidfilter.knotify.model.DismissalReason
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.WorkerNavigator
import org.w3c.notifications.*
import org.w3c.notifications.Notification
import org.w3c.workers.ExtendableEvent
import org.w3c.workers.ServiceWorkerGlobalScope
import org.w3c.workers.ServiceWorkerRegistration
import kotlin.js.Promise
import kotlin.js.JsArray
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


/** Service Worker Kotlin/Js implémentant NotificationProvider */

external val self: ServiceWorkerGlobalScope

object NotificationServiceWorker : NotificationProvider {
    private val _hasPermissionState = mutableStateOf(
        NotificationPermission.GRANTED == Notification.permission
    )
    override val hasPermissionState: State<Boolean>
        get() = _hasPermissionState


    override fun hasPermission(): Boolean =
        NotificationPermission.GRANTED == Notification.permission

    init {
        window.addEventListener("notificationclick", { event ->

        })
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun sendNotification(builder: NotificationBuilder) {
        window.addEventListener("notificationclick", { event ->

        })
        val reg: ServiceWorkerRegistration =
            window.navigator.serviceWorker.ready.unsafeCast<Promise<ServiceWorkerRegistration>>().await()
        reg.showNotification(
            builder.title,
            NotificationOptions(
                body = builder.message,
                icon = builder.largeImagePath,
                tag = "demo-actions",
                actions = JsArray<NotificationAction>().apply {
                    this[0] = NotificationAction(
                        action = builder.buttons[0].onClick.toString(),
                        title = builder.buttons[0].label
                    )
                    this[1] = NotificationAction(
                        action = builder.buttons[1].onClick.toString(),
                        title = builder.buttons[1].label
                    )
                }

            )
        )

    }

    override suspend fun hideNotification(builder: NotificationBuilder) {
        val reg: ServiceWorkerRegistration =
            window.navigator.serviceWorker.ready.unsafeCast<Promise<ServiceWorkerRegistration>>().await()
        val notifications = reg.getNotifications(filter = GetNotificationOptions(tag = "demo-actions")).await<Array<Notification>>()
        if (notifications.isNotEmpty()) {
            notifications.forEach { notification ->
                notification.close()
            }
        }
    }
}


actual fun getNotificationProvider(): NotificationProvider = NotificationServiceWorker
