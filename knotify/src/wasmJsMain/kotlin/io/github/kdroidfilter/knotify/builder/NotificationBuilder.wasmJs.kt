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
import org.w3c.workers.ClientQueryOptions
import org.w3c.workers.ClientType
import org.w3c.workers.ServiceWorkerGlobalScope
import org.w3c.workers.ServiceWorkerRegistration
import org.w3c.workers.WINDOW
import kotlin.time.ExperimentalTime


external val navigator: WorkerNavigator
external val self: ServiceWorkerGlobalScope

external interface NotificationMessageData {
    val action: String?
}

fun createNotificationMessageData(action: String?): NotificationMessageData = js("{ action: 'action' }") // Create a dynamic object with the action property

object NotificationServiceWorker : NotificationProvider {

    var builder : NotificationBuilder? = null
    private val _hasPermissionState = mutableStateOf(
        NotificationPermission.GRANTED == Notification.permission
    )
    override val hasPermissionState: State<Boolean>
        get() = _hasPermissionState

    val scope = CoroutineScope(Dispatchers.Main)


    override fun hasPermission(): Boolean =
        NotificationPermission.GRANTED == Notification.permission

    init {
        scope.launch {
            navigator.serviceWorker.register("sw.js")

            navigator.serviceWorker.addEventListener("message") { event ->
                val messageEvent = event as MessageEvent
                val data = (messageEvent.data as NotificationMessageData)
                val action = data.action

                // Accéder à la propriété action
                println("Action reçue: $action")

                // Traiter l'action
                when (action) {
                    "default" -> {
                        builder?.onActivated?.let { it() }
                    }
                    builder?.buttons[0]?.onClick.toString() -> {
                        builder?.buttons[0]?.onClick()
                    }
                    builder?.buttons[1]?.onClick.toString() -> {
                        builder?.buttons[1]?.onClick()
                    }
                    else -> println("Notification cliquée (hors actions).")
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun sendNotification(builder: NotificationBuilder) {
        scope.launch {
            NotificationServiceWorker.builder = builder
            val reg: ServiceWorkerRegistration = navigator.serviceWorker.ready.await()
            reg.showNotification(
                builder.title,
                NotificationOptions(
                    body = builder.message,
                    icon = builder.largeImagePath,
                    actions = arrayOf(
                        NotificationAction(action = builder.buttons[0].onClick.toString(), title = builder.buttons[0].label),
                        NotificationAction(action = builder.buttons[1].onClick.toString(), title = builder.buttons[1].label),
                    ).toJsArray()
                )
            )
        }

    }

    override fun hideNotification(builder: NotificationBuilder) {
        scope.launch {
            val reg: ServiceWorkerRegistration = navigator.serviceWorker.ready.await()
            try {
                val notifications =
                    reg.getNotifications().await<Array<Notification>>()
                if (notifications.isNotEmpty()) {
                    notifications.forEach { notification ->
                        notification.close()
                    }
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (!hasPermission()) {
            Notification.requestPermission()
        }
    }
}

fun registerServiceWorker() {
    self.addEventListener("notificationclick") { event ->
        val notificationEvent = event as NotificationEvent
        notificationEvent.notification.close()

        notificationEvent.waitUntil(
            self.clients.matchAll(
                ClientQueryOptions(type = ClientType.WINDOW, includeUncontrolled = true)
            ).then { clients ->
                clients.toArray().forEach { client ->
                    // Send the dynamic action data to the client
                    val messageData = createNotificationMessageData(notificationEvent.action)
                    client.postMessage(messageData as JsAny?)
                }
                null
            }
        )
    }
}

actual fun getNotificationProvider(): NotificationProvider = NotificationServiceWorker
