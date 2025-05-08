import UIKit
import ComposeApp
import UserNotifications

@main
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Set the delegate for UNUserNotificationCenter
        let center = UNUserNotificationCenter.current()
        center.delegate = self

        // Request authorization for notifications
        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            print("Notification permission granted: \(granted)")
            if let error = error {
                print("Notification permission error: \(error)")
            }

            if granted {
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            }
        }

        window = UIWindow(frame: UIScreen.main.bounds)
        if let window = window {
            window.rootViewController = MainKt.MainViewController()
            window.makeKeyAndVisible()
        }
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        // Handle successful registration
        print("Successfully registered for notifications with token: \(deviceToken)")
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        // Handle registration error
        print("Failed to register for notifications: \(error)")
    }

    // MARK: - UNUserNotificationCenterDelegate

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Allow showing the notification even when the app is in foreground
        print("Will present notification: \(notification.request.identifier)")

        // Extract notification ID from the identifier
        let identifier = notification.request.identifier
        if identifier.starts(with: "notification_") {
            let notificationId = String(identifier.dropFirst("notification_".count))
            print("Showing notification with ID: \(notificationId) in foreground")
        }

        // Use .list and .banner for iOS 14+
        if #available(iOS 14.0, *) {
            completionHandler([.list, .banner, .sound])
        } else {
            completionHandler([.alert, .badge, .sound])
        }
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        // Handle notification response
        print("Did receive notification response: \(response.notification.request.identifier), action: \(response.actionIdentifier)")

        // Extract notification ID from the identifier
        let identifier = response.notification.request.identifier
        if identifier.starts(with: "notification_") {
            let notificationId = String(identifier.dropFirst("notification_".count))
            print("Extracted notification ID: \(notificationId)")

            // Forward to Kotlin implementation if needed
            // This is handled automatically by the NotificationDelegateManager in Kotlin
        }

        completionHandler()
    }
}
