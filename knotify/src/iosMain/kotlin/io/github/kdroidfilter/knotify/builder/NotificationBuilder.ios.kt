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

class IosNotificationProvider() : NotificationProvider {
    override val hasPermissionState = mutableStateOf(false)
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    init {
        checkPermissionStatus()
    }

    private fun checkPermissionStatus() {
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val isAuthorized = settings?.authorizationStatus?.toInt() == 2 // UNAuthorizationStatusAuthorized = 2
            updatePermissionState(isAuthorized)
        }
    }

    override fun updatePermissionState(isGranted: Boolean) {
        hasPermissionState.value = isGranted
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun sendNotification(builder: NotificationBuilder) {
        if (!hasPermission()) {
            builder.onFailed?.invoke()
            return
        }

        val content = UNMutableNotificationContent().apply {
            setTitle(builder.title)
            setBody(builder.message)
            setSound(UNNotificationSound.defaultSound)

            // Add attachment for large image if provided
            builder.largeImagePath?.let { imagePath ->
                try {
                    val imageUrl = NSURL.fileURLWithPath(imagePath)
                    val attachment = platform.UserNotifications.UNNotificationAttachment.attachmentWithIdentifier(
                        "image",
                        imageUrl,
                        null,
                        null
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
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(0.1, false)

        // Create the request
        val request = UNNotificationRequest.requestWithIdentifier(
            "notification_${builder.id}",
            content,
            trigger
        )

        // Add the request to the notification center
        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                builder.onFailed?.invoke()
            }
        }

        // Set up notification response handler if not already set
        setupNotificationResponseHandler(builder)
    }

    override fun hideNotification(builder: NotificationBuilder) {
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
        val options = UNAuthorizationOptionAlert or 
                      UNAuthorizationOptionBadge or 
                      UNAuthorizationOptionSound

        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            if (granted) {
                updatePermissionState(true)
                onGranted()
            } else {
                updatePermissionState(false)
                onDenied()
            }
        }
    }

    private fun setupNotificationResponseHandler(builder: NotificationBuilder) {
        val delegate = NotificationDelegate(builder)
        notificationCenter.setDelegate(delegate)
    }
}

private class NotificationDelegate(private val builder: NotificationBuilder) : NSObject(), UNUserNotificationCenterDelegateProtocol {

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        val actionIdentifier = didReceiveNotificationResponse.actionIdentifier
        val notificationId = didReceiveNotificationResponse.notification.request.identifier

        // Check if this is our notification
        if (notificationId == "notification_${builder.id}") {
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
