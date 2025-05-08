@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d
import com.kdroid.kmplog.e
import io.github.kdroidfilter.knotify.ServiceWorkerConfig
import io.github.kdroidfilter.knotify.model.DismissalReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.w3c.dom.AddEventListenerOptions
import org.w3c.dom.MessageEvent
import org.w3c.dom.WorkerNavigator
import org.w3c.notifications.*
import org.w3c.notifications.Notification
import org.w3c.workers.ServiceWorkerRegistration
import kotlin.time.ExperimentalTime

external val navigator: WorkerNavigator

external interface NotificationMessageData {
    val action: String?
}

object NotificationServiceWorker : NotificationProvider {

    var currentNotificationBuilder: NotificationBuilder? = null
    private val _hasPermissionState = mutableStateOf(
        NotificationPermission.GRANTED == Notification.permission
    )
    override val hasPermissionState: State<Boolean>
        get() = _hasPermissionState

    val scope = CoroutineScope(Dispatchers.Main)

    override fun hasPermission(): Boolean = NotificationPermission.GRANTED == Notification.permission

    init {
        if (ServiceWorkerConfig.useServiceWorker) {
            scope.launch {
                if (ServiceWorkerConfig.initializeServiceWorker) {
                    navigator.serviceWorker.register(ServiceWorkerConfig.serviceWorkerName)
                }

                navigator.serviceWorker.addEventListener(
                    "message", options = AddEventListenerOptions(passive = true), callback = { event ->
                        val messageEvent = event as MessageEvent
                        val data = (messageEvent.data as NotificationMessageData)

                        val actions = mutableMapOf<String, (() -> Unit)?>()
                        currentNotificationBuilder?.buttons?.forEach { action ->
                            actions.put(action.onClick.toString()) { action.onClick() }
                        }

                        if (data.action == "default") {
                            currentNotificationBuilder?.onActivated?.let { it() }
                        } else {
                            actions[data.action]?.let { it() }
                        }
                    })

                navigator.serviceWorker.addEventListener("notificationclose") { event ->
                   currentNotificationBuilder.let { builder ->
                       builder?.onDismissed?.invoke(DismissalReason.UserCanceled)
                   }
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun sendNotification(builder: NotificationBuilder) {
        if (Notification.permission == NotificationPermission.GRANTED) {
            currentNotificationBuilder = builder

            if (ServiceWorkerConfig.useServiceWorker) {
                // Use service worker for notifications
                scope.launch {
                    val actions = mutableListOf<NotificationAction>()
                    builder.buttons.forEach { button ->
                        actions.add(NotificationAction(action = button.onClick.toString(), button.label))
                    }
                    val reg: ServiceWorkerRegistration = navigator.serviceWorker.ready.await()
                    reg.showNotification(
                        builder.title, NotificationOptions(
                            body = builder.message,
                            icon = builder.largeImagePath,
                            actions = actions.toJsArray()
                        )
                    )
                }
            } else {
                // Send notification directly without service worker

                val options = NotificationOptions(
                    body = builder.message, icon = builder.largeImagePath,
                )

                val notification = Notification(builder.title, options)

                // Handle notification click event
                notification.onclick = {
                    builder.onActivated?.invoke()
                    // Close the notification
                    notification.close()
                }

                // Handle notification error event
                notification.onerror = {
                    builder.onFailed?.invoke()
                }
            }
        } else {
            // Permission not granted
            builder.onFailed?.invoke()
        }
    }

    override fun hideNotification(builder: NotificationBuilder) {
        if (ServiceWorkerConfig.useServiceWorker) {
            // Use service worker to hide notifications
            scope.launch {
                val reg: ServiceWorkerRegistration = navigator.serviceWorker.ready.await()
                val notifications =
                    reg.getNotifications(filter = GetNotificationOptions(tag = "demo")).await<JsArray<Notification>>()
                if (notifications.length > 0) {
                    notifications.toArray().forEach { notification ->
                        notification.close()
                    }
                }
            }
        } else {
            Log.e("NotificationServiceWorker", "Notification hiding is not supported without Service Worker.")
        }
        // When useServiceWorker is false, we can't access the notification object
        // since we don't store a reference to it. In a real implementation, you might
        // want to store references to created notifications to be able to close them later.
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (!hasPermission()) {
            Notification.requestPermission()
        }
    }
}

actual fun getNotificationProvider(): NotificationProvider = NotificationServiceWorker
