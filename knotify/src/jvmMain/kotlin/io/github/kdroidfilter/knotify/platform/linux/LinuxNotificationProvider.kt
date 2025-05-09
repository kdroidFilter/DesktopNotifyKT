package io.github.kdroidfilter.knotify.platform.linux

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.kdroidfilter.knotify.builder.NotificationBuilder
import io.github.kdroidfilter.knotify.builder.NotificationProvider
import io.github.kdroidfilter.knotify.model.DismissalReason
import com.kdroid.kmplog.*
import com.sun.jna.Pointer
import io.github.kdroidfilter.knotify.builder.NotificationInitializer
import io.github.kdroidfilter.knotify.utils.extractToTempIfDifferent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LinuxNotificationProvider : NotificationProvider {
    private val _hasPermissionState = mutableStateOf(hasPermission())
    override val hasPermissionState: State<Boolean> get() = _hasPermissionState

    private val lib = LinuxNativeNotificationIntegration.INSTANCE
    private var isMainLoopRunning = false
    private var coroutineScope: CoroutineScope? = null
    private val appConfig = NotificationInitializer.getAppConfig()

    override fun sendNotification(builder: NotificationBuilder) {
        coroutineScope = CoroutineScope(Dispatchers.IO).also { scope ->
            scope.launch {
                val appIconPath = appConfig.smallIcon
                Log.d("sendNotification", appConfig.appName)
                // Initialize notify with app name
                if (!lib.my_notify_init(appConfig.appName)) {
                    Log.e("LinuxNotificationProvider", "Failed to initialize notifications.")
                    builder.onFailed?.invoke()
                    return@launch
                }

                val notification = lib.create_notification(
                    summary = builder.title,
                    body = builder.message,
                    icon_path = appIconPath ?: ""
                )

                if (notification == null) {
                    Log.e("LinuxNotificationProvider", "Failed to create notification.")
                    builder.onFailed?.invoke()
                    return@launch
                }

                builder.onActivated?.let { onActivated ->
                    val actionCallback = NotifyActionCallback { _, _, _ ->
                        // When the notification is clicked, call the onActivated callback
                        onActivated()
                        stopMainLoop() // Stop the main loop after the callback
                    }
                    lib.set_notification_clicked_callback(notification, actionCallback, Pointer.NULL)
                }


                builder.onDismissed?.let { onDismissed ->
                    val closedCallback = NotifyClosedCallback { _, _ ->
                        onDismissed(DismissalReason.UserCanceled)
                        stopMainLoop() // Stop the main loop after the callback
                    }
                    lib.set_notification_closed_callback(notification, closedCallback, Pointer.NULL)
                }

                val largeImagePath = builder.largeImagePath as String?
                val largeImageAbsolutePath = largeImagePath?.let { extractToTempIfDifferent(it) }?.absolutePath
                largeImageAbsolutePath?.let {
                    val pixbufPointer = lib.load_pixbuf_from_file(it)
                    if (pixbufPointer != Pointer.NULL) {
                        lib.set_image_from_pixbuf(notification, pixbufPointer)
                    } else {
                        Log.w("LinuxNotificationProvider", "Unable to load image: $it")
                    }
                }

                builder.buttons.forEach { button ->
                    val buttonCallback = NotifyActionCallback { _, action, _ ->
                        if (action == button.label) {
                            button.onClick.invoke()
                        }
                        stopMainLoop() // Stop the main loop after the callback
                    }
                    lib.add_button_to_notification(notification, button.label, button.label, buttonCallback, Pointer.NULL)
                }

                val result = lib.send_notification(notification)
                if (result == 0) {
                    Log.i("LinuxNotificationProvider", "Notification sent successfully.")
                    // Don't call onActivated here, it will be called by the callback
                } else {
                    Log.e("LinuxNotificationProvider", "Failed to send notification.")
                    builder.onFailed?.invoke()
                    return@launch // Don't start the main loop if notification failed
                }

                startMainLoop()
                lib.cleanup_notification()
            }
        }
    }

    override fun hideNotification(builder: NotificationBuilder) {
        TODO("Not yet implemented")
    }

    override fun hasPermission(): Boolean {
        return true
    }

    private fun startMainLoop() {
        if (!isMainLoopRunning) {
            Log.d("LinuxNotificationProvider", "Starting main loop...")
            isMainLoopRunning = true
            lib.run_main_loop()
        }
    }

    private fun stopMainLoop() {
        if (isMainLoopRunning) {
            Log.d("LinuxNotificationProvider", "Stopping main loop...")
            lib.quit_main_loop()
            isMainLoopRunning = false
            coroutineScope?.cancel()
        }
    }
}
