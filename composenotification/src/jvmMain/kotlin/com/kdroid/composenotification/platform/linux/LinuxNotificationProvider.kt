package com.kdroid.composenotification.platform.linux

import com.kdroid.composenotification.builder.NotificationBuilder
import com.kdroid.composenotification.builder.NotificationInitializer
import com.kdroid.composenotification.builder.NotificationProvider
import com.kdroid.composenotification.model.DismissalReason
import com.kdroid.composenotification.utils.extractToTempIfDifferent
import com.kdroid.kmplog.*
import com.sun.jna.Pointer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LinuxNotificationProvider : NotificationProvider {

    private val lib = LinuxNativeNotificationIntegration.INSTANCE
    private var isMainLoopRunning = false
    private var coroutineScope: CoroutineScope? = null
    private val appConfig = NotificationInitializer.getAppConfig()

    override fun sendNotification(builder: NotificationBuilder) {
        coroutineScope = CoroutineScope(Dispatchers.IO).also { scope ->
            scope.launch {
                val appIconPath = appConfig.iconPngPath
                Log.d("sendNotification", appConfig.appName)
                val notification = lib.create_notification(
                    app_name = appConfig.appName,
                    summary = builder.title,
                    body = builder.message,
                    icon_path = appIconPath ?: ""
                )

                if (notification == null) {
                    Log.e("LinuxNotificationProvider", "Failed to create notification.")
                    builder.onFailed?.invoke()
                    return@launch
                }

                builder.onActivated?.let {
                    lib.add_button_to_notification(notification, "default", "Open", { _, action, _ ->
                        action.let {
                            it()
                        }
                    }, Pointer.NULL)
                }


                builder.onDismissed?.let {
                    val closedCallback = NotifyClosedCallback { notification, userData ->
                        it(DismissalReason.UserCanceled)
                    }
                    lib.set_notification_closed_callback(notification, closedCallback , Pointer.NULL)
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
                    lib.add_button_to_notification(notification, button.label, button.label, { _, action, _ ->
                        if (action == button.label) {
                            button.onClick.invoke()
                        }
                        stopMainLoop() // Arrêter la boucle après le callback
                    }, Pointer.NULL)
                }

                val result = lib.send_notification(notification)
                if (result == 0) {
                    Log.i("LinuxNotificationProvider", "Notification sent successfully.")
                    builder.onActivated?.invoke()
                } else {
                    Log.e("LinuxNotificationProvider", "Failed to send notification.")
                    builder.onFailed?.invoke()
                }

                startMainLoop()
                lib.cleanup_notification()
            }
        }
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
