package com.kdroid.composenotification

import com.kdroid.composenotification.builder.Notification
import com.kdroid.composenotification.models.DismissalReason
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

// Main.kt
fun main() {

    createSwingApp()
}

fun createSwingApp() {
    // Créer une nouvelle fenêtre
    val frame = JFrame("Démo Swing Kotlin")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.setSize(400, 300)

    // Créer un panel pour ajouter des composants
    val panel = JPanel()

    // Créer un bouton
    val button = JButton("Cliquez-moi!")

    // Ajouter une action au bouton
    button.addActionListener {
        val iconPath = "C:\\Users\\Elyahou Gambache\\IdeaProjects\\KNotify\\knotify\\src\\jvmTest\\resources\\icon.ico"

        Notification(
            appName = "My Custom App",
            appIconPath = iconPath,
            title = "Hello World!",
            message = "This is a test notification with custom app name and icon.",
            largeImagePath = "C:\\Users\\Elyahou Gambache\\IdeaProjects\\KNotify\\knotify\\src\\jvmTest\\resources\\icon.png" // Set to null if no image
        ) {
            Button("Button 1") {
                // Callback for Button 1
                println("Button 1 clicked.")
            }
            Button("Button 2") {
                // Callback for Button 2
                println("Button 2 clicked.")
            }
            onActivated {
                println("Notification activated.")
            }
            onDismissed { reason ->
                when (reason) {
                    DismissalReason.UserCanceled -> println("User dismissed the notification.")
                    DismissalReason.ApplicationHidden -> println("Notification hidden by application.")
                    DismissalReason.TimedOut -> println("Notification timed out.")
                    else -> println("Notification dismissed for unknown reason.")
                }
            }
            onFailed {
                println("Notification failed to display.")
            }
        }
    }

    // Ajouter le bouton au panel
    panel.add(button)

    // Ajouter le panel à la fenêtre
    frame.contentPane = panel

    // Rendre la fenêtre visible
    frame.isVisible = true
}
