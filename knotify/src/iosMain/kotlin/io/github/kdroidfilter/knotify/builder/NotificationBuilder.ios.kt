package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.mutableStateOf
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d
import com.kdroid.kmplog.e
import io.github.kdroidfilter.knotify.model.DismissalReason
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UserNotifications.*
import platform.darwin.NSObject

actual fun getNotificationProvider(): NotificationProvider = IosNotificationProvider()

/**
 * A manager for notification delegates that can handle multiple
 * notifications.
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
            UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        )
    }
}

class IosNotificationProvider() : NotificationProvider {
    override val hasPermissionState = mutableStateOf(true)
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private val notificationDelegateManager = NotificationDelegateManager()

    init {
        checkPermissionStatus()
        // Set the delegate for UNUserNotificationCenter
        notificationCenter.setDelegate(notificationDelegateManager)
        Log.d("IosNotificationProvider", "Initializing with delegate set")
    }

    private fun checkPermissionStatus() {
        println("[DEBUG_LOG] Checking notification permission status")
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val authStatus = settings?.authorizationStatus?.toInt()
            val isAuthorized = authStatus == 2 // UNAuthorizationStatusAuthorized = 2
            Log.d("IosNotificationProvider", "Notification permission status: $authStatus (isAuthorized: $isAuthorized)")
            updatePermissionState(isAuthorized)
        }
    }

    override fun updatePermissionState(isGranted: Boolean) {
        hasPermissionState.value = isGranted
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun sendNotification(builder: NotificationBuilder) {
        Log.d("IosNotificationProvider", "Sending notification with title: ${builder.title}")

        // Check permission status synchronously before sending
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val authStatus = settings?.authorizationStatus?.toInt()
            val isAuthorized = authStatus == 2 // UNAuthorizationStatusAuthorized = 2
            Log.d("IosNotificationProvider", "Notification permission status: $authStatus (isAuthorized: $isAuthorized)")

            updatePermissionState(isAuthorized)

            if (!isAuthorized) {
                Log.d("IosNotificationProvider", "Notification permission not granted, cannot send notification")
                builder.onFailed?.invoke()
                return@getNotificationSettingsWithCompletionHandler
            }

            sendNotificationWithPermission(builder)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun sendNotificationWithPermission(builder: NotificationBuilder) {
        Log.d("IosNotificationProvider", "Sending notification with title: ${builder.title}")
        val content = UNMutableNotificationContent().apply {
            setTitle(builder.title)
            setBody(builder.message)
            setSound(UNNotificationSound.defaultSound)


//             Add attachment for large image if provided
            builder.largeImagePath?.let { imagePath ->
                try {
                    val attachment = UNNotificationAttachment.attachmentWithIdentifier(
                        identifier = "image", URL = NSURL.URLWithString(imagePath)!!, options = null, error = null
                    )
                    if (attachment != null) {
                        setAttachments(listOf(attachment))
                    }
                } catch (e: Exception) {
                    Log.e("Error", "Failed to create attachment: ${e.message}")
                }
            }

            // Add category identifier if there are buttons
            if (builder.buttons.isNotEmpty()) {
                val categoryId = "category_${builder.id}"
                setCategoryIdentifier(categoryId)

                // Create actions for buttons
                val actions = builder.buttons.mapIndexed { index, button ->
                    UNNotificationAction.actionWithIdentifier(
                        "button_${index}", button.label, UNNotificationActionOptionForeground
                    )
                }

                // Register category with actions
                val category = UNNotificationCategory.categoryWithIdentifier(
                    categoryId, actions, emptyList<UNNotificationCategory>(), UNNotificationCategoryOptionNone
                )

                notificationCenter.setNotificationCategories(setOf(category))
            }
        }

        // Create a trigger (immediate in this case)
        Log.d("IosNotificationProvider", "Creating notification trigger")
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(0.1, false)

        // Create the request
        Log.d("IosNotificationProvider", "Creating notification request with id: ${builder.id}")
        val request = UNNotificationRequest.requestWithIdentifier(
            "notification_${builder.id}", content, trigger
        )

        // Register the notification with the delegate manager
        Log.d("IosNotificationProvider", "Registering notification with delegate manager")
        notificationDelegateManager.registerNotification(builder)

        // Add the request to the notification center
        Log.d("IosNotificationProvider", "Adding notification request to notification center")
        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                Log.e("IosNotificationProvider", "Failed to add notification request: $error")
                builder.onFailed?.invoke()
            } else {
                Log.d("IosNotificationProvider", "Successfully added notification request")
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
        Log.d("IosNotificationProvider", "Requesting notification permission")
        // Check current permission status first
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val currentStatus = settings?.authorizationStatus?.toInt()
            Log.d("IosNotificationProvider", "Current notification permission status: $currentStatus")
        }

        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound

        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            if (granted) {
                Log.d("IosNotificationProvider", "Notification permission granted")
                updatePermissionState(true)
                onGranted()
            } else {
                Log.e("IosNotificationProvider", "Notification permission denied: $error")
                updatePermissionState(false)
                onDenied()
            }
        }
    }
}
