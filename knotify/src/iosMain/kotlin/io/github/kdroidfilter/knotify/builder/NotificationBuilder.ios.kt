package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.mutableStateOf
import io.github.kdroidfilter.knotify.model.DismissalReason
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionForeground
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionNone
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import platform.UserNotifications.UNNotificationDismissActionIdentifier
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

actual fun getNotificationProvider(): NotificationProvider = IosNotificationProvider()

/**
 * A manager for notification delegates that can handle multiple notifications.
 */
private class NotificationDelegateManager : NSObject(), UNUserNotificationCenterDelegateProtocol {
    // Map of notification IDs to their builders
    private val notificationBuilders = mutableMapOf<String, NotificationBuilder>()

    // Register a notification builder
    fun registerNotification(builder: NotificationBuilder) {
        val notificationId = "notification_${builder.id}"
        notificationBuilders[notificationId] = builder
    }

    // Unregister a notification builder
    fun unregisterNotification(builder: NotificationBuilder) {
        val notificationId = "notification_${builder.id}"
        notificationBuilders.remove(notificationId)
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        val actionIdentifier = didReceiveNotificationResponse.actionIdentifier
        val notificationId = didReceiveNotificationResponse.notification.request.identifier

        // Find the builder for this notification
        val builder = notificationBuilders[notificationId]

        if (builder != null) {
            when {
                // Handle button clicks
                actionIdentifier.startsWith("button_") -> {
                    val buttonIndex = actionIdentifier.removePrefix("button_").toIntOrNull() ?: -1
                    if (buttonIndex >= 0 && buttonIndex < builder.buttons.size) {
                        builder.buttons[buttonIndex].onClick()
                    }
                }
                // Handle notification tap
                actionIdentifier == UNNotificationDefaultActionIdentifier -> {
                    builder.onActivated?.invoke()
                }
                // Handle notification dismissal
                actionIdentifier == UNNotificationDismissActionIdentifier -> {
                    builder.onDismissed?.invoke(DismissalReason.UserCanceled)
                }
            }
        }

        withCompletionHandler()
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (ULong) -> Unit
    ) {
        // Allow showing the notification even when the app is in foreground
        withCompletionHandler(
            UNAuthorizationOptionAlert.toULong() or 
            UNAuthorizationOptionBadge.toULong() or 
            UNAuthorizationOptionSound.toULong()
        )
    }
}

class IosNotificationProvider() : NotificationProvider {
    override val hasPermissionState = mutableStateOf(true)
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private val notificationDelegateManager = NotificationDelegateManager()

    init {
        checkPermissionStatus()
        // Note: We don't set the delegate here as it's already set in the Swift app delegate
        println("[DEBUG_LOG] IosNotificationProvider initialized")
    }

    private fun checkPermissionStatus() {
        println("[DEBUG_LOG] Checking notification permission status")
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val authStatus = settings?.authorizationStatus?.toInt()
            val isAuthorized = authStatus == 2 // UNAuthorizationStatusAuthorized = 2
            println("[DEBUG_LOG] Notification authorization status: $authStatus (isAuthorized: $isAuthorized)")
            updatePermissionState(isAuthorized)
        }
    }

    override fun updatePermissionState(isGranted: Boolean) {
        hasPermissionState.value = isGranted
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun sendNotification(builder: NotificationBuilder) {
        println("[DEBUG_LOG] Sending notification with title: ${builder.title}")

        // Check permission status synchronously before sending
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val authStatus = settings?.authorizationStatus?.toInt()
            val isAuthorized = authStatus == 2 // UNAuthorizationStatusAuthorized = 2
            println("[DEBUG_LOG] Checking permission before sending: status=$authStatus, isAuthorized=$isAuthorized")

            updatePermissionState(isAuthorized)

            if (!isAuthorized) {
                println("[DEBUG_LOG] No permission to send notifications")
                builder.onFailed?.invoke()
                return@getNotificationSettingsWithCompletionHandler
            }

            sendNotificationWithPermission(builder)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun sendNotificationWithPermission(builder: NotificationBuilder) {
        println("[DEBUG_LOG] Has permission, creating notification content")
        val content = UNMutableNotificationContent().apply {
            setTitle(builder.title)
            setBody(builder.message)
            setSound(UNNotificationSound.defaultSound)

            // Add attachment for large image if provided
            builder.largeImagePath?.let { imagePath ->
                try {
                    val attachment = platform.UserNotifications.UNNotificationAttachment.attachmentWithIdentifier(
                        identifier = "image",
                        URL = NSURL.URLWithString(imagePath)!!,
                        options = null,
                        error = null
                    )
                    if (attachment != null) {
                        setAttachments(listOf(attachment))
                    }
                } catch (e: Exception) {
                    // Handle attachment error
                }
            }

            // Add category identifier if there are buttons
            if (builder.buttons.isNotEmpty()) {
                val categoryId = "category_${builder.id}"
                setCategoryIdentifier(categoryId)

                // Create actions for buttons
                val actions = builder.buttons.mapIndexed { index, button ->
                    UNNotificationAction.actionWithIdentifier(
                        "button_${index}",
                        button.label,
                        UNNotificationActionOptionForeground
                    )
                }

                // Register category with actions
                val category = UNNotificationCategory.categoryWithIdentifier(
                    categoryId,
                    actions,
                    emptyList<UNNotificationCategory>(),
                    UNNotificationCategoryOptionNone
                )

                notificationCenter.setNotificationCategories(setOf(category))
            }
        }

        // Create a trigger (immediate in this case)
        println("[DEBUG_LOG] Creating notification trigger")
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(0.1, false)

        // Create the request
        println("[DEBUG_LOG] Creating notification request with ID: notification_${builder.id}")
        val request = UNNotificationRequest.requestWithIdentifier(
            "notification_${builder.id}",
            content,
            trigger
        )

        // Register the notification with the delegate manager
        println("[DEBUG_LOG] Registering notification with delegate manager")
        notificationDelegateManager.registerNotification(builder)

        // Add the request to the notification center
        println("[DEBUG_LOG] Adding notification request to notification center")
        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                println("[DEBUG_LOG] Failed to add notification request: ${error}")
                builder.onFailed?.invoke()
            } else {
                println("[DEBUG_LOG] Successfully added notification request")
            }
        }
    }

    override fun hideNotification(builder: NotificationBuilder) {
        // Unregister the notification from the delegate manager
        notificationDelegateManager.unregisterNotification(builder)

        notificationCenter.removePendingNotificationRequestsWithIdentifiers(
            listOf("notification_${builder.id}")
        )
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(
            listOf("notification_${builder.id}")
        )
    }

    override fun hasPermission(): Boolean {
        return hasPermissionState.value
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        println("[DEBUG_LOG] Requesting notification permission")
        // Check current permission status first
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val currentStatus = settings?.authorizationStatus?.toInt()
            println("[DEBUG_LOG] Current permission status before request: $currentStatus")
        }

        val options = UNAuthorizationOptionAlert or 
                      UNAuthorizationOptionBadge or 
                      UNAuthorizationOptionSound

        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            if (granted) {
                println("[DEBUG_LOG] Notification permission granted")
                updatePermissionState(true)
                onGranted()
            } else {
                println("[DEBUG_LOG] Notification permission denied: ${error}")
                updatePermissionState(false)
                onDenied()
            }
        }
    }
}
