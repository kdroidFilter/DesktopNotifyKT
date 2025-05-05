package io.github.kdroidfilter.knotify.platform.mac

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.kdroid.kmplog.*
import com.sun.jna.Pointer
import io.github.kdroidfilter.knotify.builder.NotificationBuilder
import io.github.kdroidfilter.knotify.builder.NotificationInitializer
import io.github.kdroidfilter.knotify.builder.NotificationProvider
import io.github.kdroidfilter.knotify.model.DismissalReason
import io.github.kdroidfilter.knotify.utils.extractToTempIfDifferent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class MacNotificationProvider() : NotificationProvider {

    private val _hasPermissionState: MutableState<Boolean> = mutableStateOf(true)
    override val hasPermissionState: State<Boolean> get() = _hasPermissionState

    private val lib = MacNativeNotificationIntegration.INSTANCE
    private var coroutineScope: CoroutineScope? = null
    private val appConfig = NotificationInitializer.getAppConfig()

    // Map to store notifications by their ID
    private val activeNotifications = mutableMapOf<Int, Pointer?>()

    override fun updatePermissionState(isGranted: Boolean) {
        _hasPermissionState.value = isGranted
    }

    override fun hasPermission(): Boolean {
        return true
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (hasPermission()) {
            onGranted()
        } else {
            onDenied()
        }
    }

    override fun sendNotification(builder: NotificationBuilder) {
        coroutineScope = CoroutineScope(Dispatchers.IO).also { scope ->
            scope.launch {
                try {
                    val appIconPath = appConfig.smallIcon
                    Log.d("sendNotification", "Sending notification with title: ${builder.title}")

                    // Try to create the notification, but handle any exceptions
                    val notification = try {
                        lib.create_notification(
                            title = builder.title,
                            body = builder.message,
                            iconPath = appIconPath
                        )
                    } catch (e: Exception) {
                        Log.e("MacNotificationProvider", "Exception creating notification: ${e.message}")
                        null
                    }

                    if (notification == null) {
                        Log.e("MacNotificationProvider", "Failed to create notification.")
                        builder.onFailed?.invoke()
                        return@launch
                    }

                    // Store the notification pointer
                    activeNotifications[builder.id] = notification

                    try {
                        // Set up clicked callback
                        builder.onActivated?.let { onActivated ->
                            val clickedCallback = object : NotificationClickedCallback {
                                override fun invoke(notification: Pointer?, userData: Pointer?) {
                                    onActivated()
                                }
                            }
                            lib.set_notification_clicked_callback(notification, clickedCallback, Pointer.NULL)
                        }

                        // Set up closed callback
                        builder.onDismissed?.let { onDismissed ->
                            val closedCallback = object : NotificationClosedCallback {
                                override fun invoke(notification: Pointer?, userData: Pointer?) {
                                    onDismissed(DismissalReason.UserCanceled)
                                }
                            }
                            lib.set_notification_closed_callback(notification, closedCallback, Pointer.NULL)
                        }

                        // Add large image if available
                        val largeImagePath = builder.largeImagePath as String?
                        largeImagePath?.let { path ->
                            val largeImageAbsolutePath = extractToTempIfDifferent(path)?.absolutePath
                            largeImageAbsolutePath?.let {
                                lib.set_notification_image(notification, it)
                            }
                        }

                        // Add buttons
                        builder.buttons.forEach { button ->
                            val buttonCallback = object : ButtonClickedCallback {
                                override fun invoke(notification: Pointer?, buttonId: String?, userData: Pointer?) {
                                    button.onClick.invoke()
                                }
                            }
                            lib.add_button_to_notification(
                                notification = notification,
                                buttonId = button.label,
                                buttonLabel = button.label,
                                callback = buttonCallback,
                                userData = Pointer.NULL
                            )
                        }

                        // Send the notification, but catch any exceptions
                        val result = try {
                            lib.send_notification(notification)
                        } catch (e: Exception) {
                            Log.e("MacNotificationProvider", "Exception sending notification: ${e.message}")
                            // If we get an exception, consider it a failure
                            -1
                        }

                        if (result == 0) {
                            Log.i("MacNotificationProvider", "Notification sent successfully.")
                        } else {
                            Log.e("MacNotificationProvider", "Failed to send notification.")
                            builder.onFailed?.invoke()
                            activeNotifications.remove(builder.id)
                            try {
                                lib.cleanup_notification(notification)
                            } catch (e: Exception) {
                                Log.e("MacNotificationProvider", "Exception cleaning up notification: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MacNotificationProvider", "Unexpected exception: ${e.message}")
                        builder.onFailed?.invoke()
                        activeNotifications.remove(builder.id)
                        try {
                            lib.cleanup_notification(notification)
                        } catch (e: Exception) {
                            Log.e("MacNotificationProvider", "Exception cleaning up notification: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MacNotificationProvider", "Critical exception in sendNotification: ${e.message}")
                    builder.onFailed?.invoke()
                }
            }
        }
    }

    override fun hideNotification(builder: NotificationBuilder) {
        try {
            val notification = activeNotifications[builder.id]
            if (notification != null) {
                try {
                    lib.hide_notification(notification)
                } catch (e: Exception) {
                    Log.e("MacNotificationProvider", "Exception hiding notification: ${e.message}")
                }

                try {
                    lib.cleanup_notification(notification)
                } catch (e: Exception) {
                    Log.e("MacNotificationProvider", "Exception cleaning up notification: ${e.message}")
                }

                activeNotifications.remove(builder.id)
                Log.d("MacNotificationProvider", "Notification hidden: ${builder.id}")
            } else {
                Log.w("MacNotificationProvider", "No active notification found with ID: ${builder.id}")
            }
        } catch (e: Exception) {
            Log.e("MacNotificationProvider", "Critical exception in hideNotification: ${e.message}")
        }
    }
}
