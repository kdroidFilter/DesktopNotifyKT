package io.github.kdroidfilter.knotify.platform.linux

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.kdroidfilter.knotify.builder.NotificationBuilder
import io.github.kdroidfilter.knotify.builder.NotificationProvider
import io.github.kdroidfilter.knotify.model.DismissalReason
import com.sun.jna.Pointer
import io.github.kdroidfilter.knotify.builder.NotificationInitializer
import io.github.kdroidfilter.knotify.utils.extractToTempIfDifferent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger

/**
 * Linux implementation of the NotificationProvider interface.
 * Uses the native Linux notification library via JNA.
 */
class LinuxNotificationProvider(
) : NotificationProvider {
    private val debugMode: Boolean = false
    private val _hasPermissionState = mutableStateOf(hasPermission())
    override val hasPermissionState: State<Boolean> get() = _hasPermissionState

    private val lib = LinuxNativeNotificationIntegration.INSTANCE
    private var isMainLoopRunning = false
    private var coroutineScope: CoroutineScope? = null
    private val appConfig = NotificationInitializer.getAppConfig()
    private var activeNotifications = mutableMapOf<NotificationBuilder, Pointer?>()

    // Initialize Kermit logger
    private val logger = Logger.withTag("LinuxNotificationProvider")

    // Store callbacks to prevent garbage collection
    private val notificationCallbacks = mutableMapOf<Pointer, NotifyActionCallback>()
    private val closedCallbacks = mutableMapOf<Pointer, NotifyClosedCallback>()
    private val buttonCallbacks = mutableMapOf<Pointer, MutableMap<String, NotifyActionCallback>>()

    init {
        // Set debug mode in the native library
        lib.set_debug_mode(if (debugMode) 1 else 0)
        if (debugMode) logger.d { "Debug mode enabled" }
    }

    override fun sendNotification(builder: NotificationBuilder) {
        coroutineScope = CoroutineScope(Dispatchers.IO).also { scope ->
            scope.launch {
                val appIconPath = appConfig.smallIcon
                if (debugMode) logger.d { "Sending notification with app name: ${appConfig.appName}" }


                // Initialize notify with app name
                if (lib.my_notify_init(appConfig.appName) == 0) {
                    logger.e { "Failed to initialize notifications." }
                    builder.onFailed?.invoke()
                    return@launch
                }

                val notification = lib.create_notification(
                    summary = builder.title,
                    body = builder.message,
                    icon_path = appIconPath ?: ""
                )

                if (notification == null) {
                    logger.e { "Failed to create notification." }
                    builder.onFailed?.invoke()
                    return@launch
                }

                // Store callbacks as instance variables to prevent garbage collection
                val actionCallback = builder.onActivated?.let { onActivated ->
                    NotifyActionCallback { notif, _, _ ->
                        // When the notification is clicked, call the onActivated callback
                        if (debugMode) logger.d { "Notification clicked, invoking onActivated" }
                        onActivated()
                        // Remove the notification from the active notifications map
                        activeNotifications.entries.removeIf { it.value == notif }
                        // Clean up callbacks
                        notificationCallbacks.remove(notif)
                        closedCallbacks.remove(notif)
                        buttonCallbacks.remove(notif)
                        stopMainLoop() // Stop the main loop after the callback
                    }
                }

                if (actionCallback != null) {
                    // Store the callback in the map
                    notificationCallbacks[notification] = actionCallback
                    lib.set_notification_clicked_callback(notification, actionCallback, Pointer.NULL)
                }

                val closedCallback = builder.onDismissed?.let { onDismissed ->
                    NotifyClosedCallback { notif, _ ->
                        if (debugMode) logger.d { "Notification dismissed, invoking onDismissed" }
                        onDismissed(DismissalReason.UserCanceled)
                        // Remove the notification from the active notifications map
                        activeNotifications.entries.removeIf { it.value == notif }
                        // Clean up callbacks
                        notificationCallbacks.remove(notif)
                        closedCallbacks.remove(notif)
                        buttonCallbacks.remove(notif)
                        stopMainLoop() // Stop the main loop after the callback
                    }
                }

                if (closedCallback != null) {
                    // Store the callback in the map
                    closedCallbacks[notification] = closedCallback
                    lib.set_notification_closed_callback(notification, closedCallback, Pointer.NULL)
                }

                val largeImagePath = builder.largeImagePath
                val largeImageAbsolutePath = largeImagePath?.let { extractToTempIfDifferent(it) }?.absolutePath
                largeImageAbsolutePath?.let {
                    logger.d { "Loading image from: $it" }

                    val pixbufPointer = lib.load_pixbuf_from_file(it)
                    if (pixbufPointer != Pointer.NULL) {
                        lib.set_image_from_pixbuf(notification, pixbufPointer)
                    } else {
                        logger.w { "Unable to load image: $it" }
                    }
                }

                // Initialize button callbacks map for this notification
                buttonCallbacks[notification] = mutableMapOf()

                builder.buttons.forEach { button ->
                    logger.d { "Adding button: ${button.label}" }
                    val buttonCallback = NotifyActionCallback { notif, action, _ ->
                        if (action == button.label) {
                            logger.d { "Button clicked: $action" }
                            button.onClick.invoke()
                        }
                        // Remove the notification from the active notifications map
                        activeNotifications.entries.removeIf { it.value == notif }
                        // Clean up callbacks
                        notificationCallbacks.remove(notif)
                        closedCallbacks.remove(notif)
                        buttonCallbacks.remove(notif)
                        stopMainLoop() // Stop the main loop after the callback
                    }
                    // Store the callback in the class-level map
                    buttonCallbacks[notification]?.put(button.label, buttonCallback)
                    lib.add_button_to_notification(notification, button.label, button.label, buttonCallback, Pointer.NULL)
                }

                val result = lib.send_notification(notification)
                if (result == 0) {
                    logger.i { "Notification sent successfully." }
                    // Store the notification pointer in the map
                    activeNotifications[builder] = notification
                    // Don't call onActivated here, it will be called by the callback
                } else {
                    logger.e { "Failed to send notification." }
                    // Clean up callbacks since notification failed
                    notificationCallbacks.remove(notification)
                    closedCallbacks.remove(notification)
                    buttonCallbacks.remove(notification)
                    builder.onFailed?.invoke()
                    return@launch // Don't start the main loop if notification failed
                }

                startMainLoop()
            }
        }
    }

    override fun hideNotification(builder: NotificationBuilder) {
        val notification = activeNotifications[builder]
        if (notification != null) {
            if (debugMode) logger.d { "Hiding notification" }

            val result = lib.close_notification(notification)
            if (result == 0) {
                logger.i { "Notification hidden successfully." }

                // Clean up callbacks
                notificationCallbacks.remove(notification)
                closedCallbacks.remove(notification)
                buttonCallbacks.remove(notification)

                activeNotifications.remove(builder)

                // If this was the last notification and main loop is running, stop it
                if (activeNotifications.isEmpty() && isMainLoopRunning) {
                    stopMainLoop()
                }
            } else {
                logger.e { "Failed to hide notification." }
            }
        } else {
            logger.w { "No active notification found to hide." }
        }
    }

    override fun hasPermission(): Boolean {
        return true
    }

    private fun startMainLoop() {
        if (!isMainLoopRunning) {
            logger.d { "Starting main loop..." }
            isMainLoopRunning = true
            // Start the main loop in a separate thread to avoid blocking the UI thread
            Thread {
                lib.run_main_loop()
            }.apply {
                isDaemon = true
                start()
            }
        }
    }

    private fun stopMainLoop() {
        if (isMainLoopRunning) {
            logger.d { "Stopping main loop..." }
            lib.quit_main_loop()
            isMainLoopRunning = false
            coroutineScope?.cancel()
            // Clean up notification resources after stopping the main loop
            lib.cleanup_notification()
        }
    }
}
