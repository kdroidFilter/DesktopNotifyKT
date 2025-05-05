package io.github.kdroidfilter.knotify.builder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import io.github.kdroidfilter.knotify.NotificationInitializer
import io.github.kdroidfilter.knotify.NotificationManager

actual fun getNotificationProvider(): NotificationProvider {
    val androidContext = NotificationInitializer.appContext
    val activity = NotificationInitializer.activityReference?.get()
    return AndroidNotificationProvider(androidContext as Context, activity as Activity)
}

class AndroidNotificationProvider(private val context: Context, private val activity: Activity) : NotificationProvider {

    private val _hasPermissionState = mutableStateOf(hasPermission())
    override val hasPermissionState: State<Boolean> get() = _hasPermissionState

    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            _hasPermissionState.value = hasPermission()
            if (isWaitingForPermissionResult) {
                isWaitingForPermissionResult = false
                if (_hasPermissionState.value) {
                    onPermissionGranted?.invoke()
                } else {
                    onPermissionDenied?.invoke()
                }
                onPermissionGranted = null
                onPermissionDenied = null
            }
        }
    }

    private var isWaitingForPermissionResult = false
    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    override fun sendNotification(builder: NotificationBuilder) {
        NotificationManager(context).sendNotification(
            title = builder.title,
            message = builder.message,
            largeImage = builder.largeImagePath,
            buttons = builder.buttons,
            onActivated = builder.onActivated,
            onDismissed = builder.onDismissed,
            onFailed = builder.onFailed,
            notificationId = builder.id
        )
    }

    override fun hideNotification(builder: NotificationBuilder) {
        // Use NotificationManagerCompat to cancel the notification with the same ID used in sendNotification
        NotificationManagerCompat.from(context).cancel(builder.id)
    }

    override fun hasPermission(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) and higher: explicit permission required for notifications
            val permission = Manifest.permission.POST_NOTIFICATIONS

            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                onGranted()
            } else {
                onPermissionGranted = onGranted
                onPermissionDenied = onDenied
                isWaitingForPermissionResult = true
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permission),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            onGranted()
        }
    }

    // Method to update the permission state
    override fun updatePermissionState(isGranted: Boolean) {
        _hasPermissionState.value = isGranted
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}
