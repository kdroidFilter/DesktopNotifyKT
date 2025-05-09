# Compose Native Notification

## 🚀 Overview

⚠️ **Warning**: This library is highly experimental, and bugs may occur. The API is subject to change.

This guide explains how to use the library in your KMP project.

## 📦 Installation

To use this library, add the following dependency to your KMP project:

```kotlin
implementation("io.github.kdroidfilter:knotify:0.2.0")
```

Make sure to replace `latest-version` with the version you want to use.

## ⚙️ Setup

The setup is platform-specific and involves initializing the notification settings for each platform where your app will run.

### 🤖 Android Setup

For Android, you need to initialize the notification channel in a context receiver:

```kotlin
notificationInitializer(
    defaultChannelConfig = AndroidChannelConfig(
        channelId = "Notification Example 1",
        channelName = "Notification Example 1",
        channelDescription = "Notification Example 1",
        channelImportance = NotificationManager.IMPORTANCE_DEFAULT,
        smallIcon = android.R.drawable.ic_notification_overlay
    )
)
```

### 🖥️ JVM Setup (Linux, Windows)

For JVM-based environments like Linux and Windows, the notification initialization can be done in the `main` function:

```kotlin
NotificationInitializer.configure(
    AppConfig(
        appName = "My awesome app",
        smallIcon = Res.getUri("drawable/kdroid.png"),
    )
)
```

## ✨ Usage Example

The library uses a Kotlin DSL to define notifications in a declarative way, without the usual boilerplate code. Here are examples of how you can create and manage notifications:

### Creating and Sending a Notification Later

```kotlin
// Create a notification
val myNotification = notification(
    title = "Notification from Screen 1",
    message = "This is a test notification from Screen 1",
    largeImage = Res.getUri("drawable/kdroid.png"),
    onActivated = { Log.d("NotificationLog", "Notification 1 activated") },
    onDismissed = { reason -> Log.d("NotificationLog", "Notification 1 dismissed: $reason") },
    onFailed = { Log.d("NotificationLog", "Notification 1 failed") }
) {
    button(title = "Show Message from Button 1") {
        Log.d("NotificationLog", "Button 1 from Screen 1 clicked")
        onShowMessage("Button 1 clicked from Screen 1's notification")
    }
    button(title = "Hide Message from Button 2") {
        Log.d("NotificationLog", "Button 2 from Screen 1 clicked")
        onShowMessage(null)
    }
}

// Send it when needed
myNotification.send()

// Hide it when needed
myNotification.hide()
```

### Sending a Notification Immediately

```kotlin
// Send a notification immediately
sendNotification(
    title = "Immediate Notification",
    message = "This notification is sent immediately",
    largeImage = Res.getUri("drawable/kdroid.png"),
    onActivated = { Log.d("NotificationLog", "Notification activated") }
) {
    button(title = "Click Me") {
        Log.d("NotificationLog", "Button clicked")
    }
}
```

### Creating and Sending a Notification with Reference

```kotlin
// Send a notification immediately but keep a reference to it
val myNotification = sendNotification(
    title = "Immediate Notification with Reference",
    message = "This notification is sent immediately but can be manipulated later",
    onActivated = { Log.d("NotificationLog", "Notification activated") }
) {
    button(title = "Click Me") {
        Log.d("NotificationLog", "Button clicked")
    }
}

// Hide it later when needed
myNotification.hide()
```

With this DSL approach, you can create rich and interactive notifications while keeping the code clean and readable. The library handles all the necessary details, allowing you to focus on your app's core logic.

### 🔑 Permission Handling Example

Here's an example of how to handle notification permissions within your application:

```kotlin
val notificationProvider = getNotificationProvider()
val hasPermission by notificationProvider.hasPermissionState

notificationProvider.requestPermission(
    onGranted = {
        notificationProvider.updatePermissionState(true)
    },
    onDenied = {
        notificationProvider.updatePermissionState(false)
        permissionDenied = true
    }
)
```

This code helps ensure that your app properly manages permissions, updating the notification state as permissions are granted or denied.

## 🌍 Platform-Specific Considerations

- **Android**: Notifications require proper permission setup. You need to request permissions at runtime, as shown in the example.

## ⭐ Features

- **Rich Interactive Notifications**: Use a Kotlin DSL to create rich notifications with multiple actions, images, and buttons.
- **Callbacks for User Interaction**: Handle user actions such as `onActivated`, `onDismissed`, and `onFailed` to add interactivity to your notifications.
- **Custom Actions**: Add buttons with custom actions to your notifications, such as showing or hiding messages.
- **Button Support**: Notifications can include interactive buttons, allowing users to take specific actions directly from the notification.
- **Permission Request Handling**: Manage notification permissions seamlessly using `hasPermissionState` and `requestPermission` to ensure smooth user interaction. Note: Permission handling is currently necessary only for Android.
- **Cross-Platform Compatibility**: Seamless integration across Android, Linux, Windows, and macOS with platform-specific considerations.

## 📸 Screenshots

### Android
![Android](/screenshots/Android.png)

### Windows
![Windows](/screenshots/Windows.png)

### Gnome (Linux)
![Gnome](/screenshots/Gnome.png)

### KDE (Linux)
![KDE](/screenshots/KDE.png)


## 🛤️ Roadmap

- **Persistent Notifications for Audio Players**: Integrate persistent notifications, specifically tailored for use cases such as audio players.

- **Progress Bar Support**: Add support for notifications with progress bars, useful for tasks such as file downloads or ongoing processes.

- **Input Support for Notifications**: Add support for notifications with input fields, allowing users to respond directly within the notification.

## 📜 License

The Compose Notification Library is distributed under the [MIT License](LICENSE).

## 🤝 Contributing

If you would like to contribute, feel free to submit issues or pull requests on the [GitHub repository](https://github.com/kdroidFilter/ComposeNativeNotification).

## 🛠️ Troubleshooting

- **Android `onDismissed` callback**: This callback is not functional on Android. You can handle dismissal through other application state monitoring if needed.

Feel free to reach out for support or additional questions regarding implementation.
