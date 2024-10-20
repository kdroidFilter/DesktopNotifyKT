package com.kdroid.composenotification.builder

import com.kdroid.composenotification.NotificationProvider
import com.kdroid.composenotification.models.ButtonModel
import com.kdroid.composenotification.models.DismissalReason
import com.kdroid.composenotification.platform.linux.LinuxNotificationProvider
import com.kdroid.composenotification.platform.windows.provider.WindowsNotificationProvider
import com.kdroid.composenotification.utils.OsUtils
import java.util.concurrent.CountDownLatch

/**
 * Affiche une notification avec les paramètres spécifiés.
 *
 * @param appName Le nom de l'application envoyant la notification. Par défaut "NotificationExample".
 * @param appIconPath Le chemin du fichier de l'icône de l'application. Peut être null si aucune icône n'est fournie.
 * @param title Le titre de la notification.
 * @param message Le corps du message de la notification.
 * @param largeImagePath Le chemin du fichier d'une grande image à afficher dans la notification. Peut être null si aucune image n'est fournie.
 * @param builderAction Une lambda avec récepteur pour configurer des propriétés supplémentaires de la notification, telles que les boutons et les callbacks.
 */
fun Notification(
    appName: String = "NotificationExample",
    appIconPath: String? = null,
    title: String = "",
    message: String = "",
    largeImagePath: String? = null,
    builderAction: NotificationBuilder.() -> Unit = {}
) {
    val builder = NotificationBuilder(appName, appIconPath, title, message, largeImagePath)
    builder.builderAction()
    builder.send()

    // Attendre que la notification soit traitée
    builder.waitForCompletion()
}

class NotificationBuilder(
    var appName: String,
    var appIconPath: String?,
    var title: String = "",
    var message: String = "",
    var largeImagePath: String?
) {
    internal val buttons = mutableListOf<ButtonModel>()

    internal var onActivated: (() -> Unit)? = null
    internal var onDismissed: ((DismissalReason) -> Unit)? = null
    internal var onFailed: (() -> Unit)? = null

    // Latch pour synchroniser l'attente de la notification
    private val latch = CountDownLatch(1)

    fun Button(label: String, onClick: () -> Unit) {
        buttons.add(ButtonModel(label, onClick))
    }

    fun onActivated(callback: () -> Unit) {
        this.onActivated = callback
    }

    fun onDismissed(callback: (DismissalReason) -> Unit) {
        this.onDismissed = callback
    }

    fun onFailed(callback: () -> Unit) {
        this.onFailed = callback
    }

    fun send() {
        val notificationProvider: NotificationProvider = when {
            OsUtils.isLinux() -> LinuxNotificationProvider()
            OsUtils.isWindows() -> WindowsNotificationProvider()
            else -> throw UnsupportedOperationException("Unsupported OS")
        }

        // Passer le latch au fournisseur de notifications
        if (notificationProvider is WindowsNotificationProvider) {
            notificationProvider.setCompletionLatch(latch)
        }

        notificationProvider.sendNotification(this)

        // Pour les autres OS, vous pouvez décompter le latch immédiatement
        if (notificationProvider !is WindowsNotificationProvider) {
            latch.countDown()
        }
    }

    fun waitForCompletion() {
        latch.await()
    }
}
