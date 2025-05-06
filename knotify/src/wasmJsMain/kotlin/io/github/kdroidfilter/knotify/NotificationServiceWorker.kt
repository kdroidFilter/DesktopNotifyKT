@file:Suppress("unused", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package io.github.kdroidfilter.knotify

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.kdroidfilter.knotify.builder.NotificationBuilder
import io.github.kdroidfilter.knotify.builder.NotificationProvider
import io.github.kdroidfilter.knotify.model.DismissalReason
import org.w3c.notifications.*
import org.w3c.workers.ExtendableEvent
import org.w3c.workers.ServiceWorkerGlobalScope
import kotlin.js.Promise
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// JSON.parse externe
external object JSON {
    fun <T : JsAny> parse(json: String): T
}

// Accès au scope global du Service Worker (self)
external val self: ServiceWorkerGlobalScope
private val sw: ServiceWorkerGlobalScope = self

/**
 * Service Worker Kotlin/Js implémentant NotificationProvider
 */
object NotificationServiceWorker : NotificationProvider {
    private val _hasPermissionState = mutableStateOf(
        NotificationPermission.GRANTED == Notification.permission
    )
    override val hasPermissionState: State<Boolean>
        get() = _hasPermissionState

    // Stocke les builders en attente par tag
    private val pendingBuilders = mutableMapOf<String, NotificationBuilder>()

    init {
        sw.addEventListener("install") { evt -> onInstall(evt.unsafeCast<ExtendableEvent>()) }
        sw.addEventListener("activate") { evt -> onActivate(evt.unsafeCast<ExtendableEvent>()) }
        sw.addEventListener("notificationclick") { evt -> onNotificationClick(evt.unsafeCast<NotificationEvent>()) }
        sw.addEventListener("notificationclose") { evt -> onNotificationClose(evt.unsafeCast<NotificationEvent>()) }
    }

    private fun onInstall(evt: ExtendableEvent) {
        println("SW → install")
        evt.waitUntil(sw.skipWaiting())
    }

    private fun onActivate(evt: ExtendableEvent) {
        println("SW → activate")
        evt.waitUntil(sw.clients.claim())
    }

    private fun onNotificationClick(e: NotificationEvent) {
        val tag = e.notification.tag
        e.notification.close()
        val builder = pendingBuilders[tag]
        val action = e.action.ifBlank { "_default_" }
        when {
            action == "_default_" -> builder?.onActivated?.invoke()
            else -> builder?.buttons?.firstOrNull { it.label == action }?.onClick?.invoke()
        }
        pendingBuilders.remove(tag)
        e.waitUntil(Promise.resolve(null))
    }

    private fun onNotificationClose(e: NotificationEvent) {
        val tag = e.notification.tag
        e.notification.close()
        val builder = pendingBuilders.remove(tag)
        builder?.onDismissed?.invoke(DismissalReason.UserCanceled)
    }

    override fun hasPermission(): Boolean =
        NotificationPermission.GRANTED == Notification.permission

    @OptIn(ExperimentalTime::class)
    override fun sendNotification(builder: NotificationBuilder) {
        if (!hasPermission()) {
            builder.onFailed?.invoke()
            return
        }
        // Tag unique basé sur l'id du builder
        val tag = "notif-${builder.id}-${Clock.System.now().toEpochMilliseconds()}"
        pendingBuilders[tag] = builder

        // Prépare les actions pour JS
        val actions: Array<NotificationAction> = builder.buttons.map { btn ->
            NotificationAction(
                action = btn.label,
                title = btn.label
            )
        }.toTypedArray()

        // Options incluant actions, body et tag
        val opts = NotificationOptions(
            body = builder.message,
            tag = tag,
            actions = actions.toJsArray(),
        )

        // Affiche via le Service Worker
        sw.registration.showNotification(builder.title, opts)
            .catch<JsAny?> { error -> 
                builder.onFailed?.invoke()
                null 
            }
    }

    override fun hideNotification(builder: NotificationBuilder) {
        println("not supported on JS")
    }
}
