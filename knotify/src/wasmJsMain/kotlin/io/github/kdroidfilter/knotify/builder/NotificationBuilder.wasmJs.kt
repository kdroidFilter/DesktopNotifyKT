@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
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

    val scope = CoroutineScope(Dispatchers.Default)


    override fun hasPermission(): Boolean =
        NotificationPermission.GRANTED == Notification.permission

    init {
        scope.launch {
            navigator.serviceWorker.register("sw.js")

            navigator.serviceWorker.addEventListener("message") { event ->
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
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun sendNotification(builder: NotificationBuilder) {
        scope.launch {
            currentNotificationBuilder = builder
            val actions = mutableListOf<NotificationAction>()
            builder.buttons.forEach { button ->
                actions.add(NotificationAction(action = button.onClick.toString(), button.label))
            }
            val reg: ServiceWorkerRegistration = navigator.serviceWorker.ready.await()
            reg.showNotification(
                builder.title,
                NotificationOptions(
                    body = builder.message,
                    icon = builder.largeImagePath,
                    tag = "demo",
                    actions = actions.toJsArray()
                )
            )
        }

    }

    override fun hideNotification(builder: NotificationBuilder) {
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
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (!hasPermission()) {
            Notification.requestPermission()
        }
    }
}

actual fun getNotificationProvider(): NotificationProvider = NotificationServiceWorker
