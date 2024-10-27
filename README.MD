# Compose Native Notification

## Overview

⚠️ **Warning**: This library is highly experimental, and bugs may occur. The API is subject to change.

The Compose Native Notification library is a Kotlin Multiplatform library designed to work with Compose Multiplatform that enables developers to add notifications to their applications in a unified way across different platforms, including Windows, Linux, Android, and macOS. The main goal of this library is to provide a declarative way to send notifications on Android while removing all boilerplate code, and to enable sending native notifications on other platforms. The library provides seamless integration with Jetpack Compose, allowing notifications to be handled consistently within Compose-based applications.

- **Android**: Full notification support, except for `onDismissed` callback which is currently not functional.
- **Windows & Linux**: Full notification support, including all callbacks (`onActivated`, `onDismissed`, `onFailed`).
- **macOS**: Supports only sending notifications without any callbacks.

This guide explains how to use the library in your KMP project.

## Installation

To use this library, add the following dependency to your KMP project:

```kotlin
implementation("com.kdroid:compose-native-notification:latest-version")
```

Make sure to replace `latest-version` with the version you want to use.

## Setup

The setup is platform-specific and involves initializing the notification settings for each platform where your app will run.

### Android Setup

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

### JVM Setup (Linux, Windows)

For JVM-based environments like Linux and Windows, the notification initialization can be done in the `main` function:

```kotlin
NotificationInitializer.configure(
    AppConfig(
        appName = "My awesome app",
        smallIcon = Res.getUri("drawable/kdroid.png"),
    )
)
```

## Usage Example

The library uses a Kotlin DSL to define notifications in a declarative way, without the usual boilerplate code. Here is an example of how you can create and send a notification:

```kotlin
Notification(
    title = "Notification from Screen 1",
    message = "This is a test notification from Screen 1",
    largeImage = Res.getUri("drawable/kdroid.png"),
    onActivated = { Log.d("NotificationLog", "Notification 1 activated") },
    onDismissed = { reason -> Log.d("NotificationLog", "Notification 1 dismissed: $reason") },
    onFailed = { Log.d("NotificationLog", "Notification 1 failed") }
) {
    Button("Show Message from Button 1") {
        Log.d("NotificationLog", "Button 1 from Screen 1 clicked")
        onShowMessage("Button 1 clicked from Screen 1's notification")
    }
    Button("Hide Message from Button 2") {
        Log.d("NotificationLog", "Button 2 from Screen 1 clicked")
        onShowMessage(null)
    }
}
```

With this DSL approach, you can create rich and interactive notifications while keeping the code clean and readable. The library handles all the necessary details, allowing you to focus on your app's core logic.

### Permission Handling Example

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

## Platform-Specific Considerations

- **Android**: Notifications require proper permission setup. You need to request permissions at runtime, as shown in the example.
- **Linux & Windows**: Full support, including callbacks such as `onActivated`, `onDismissed`, and `onFailed`.
- **macOS**: Notifications can be sent, but callbacks (`onDismissed`, `onActivated`) are not supported yet.

## Features

- **Rich Interactive Notifications**: Use a Kotlin DSL to create rich notifications with multiple actions, images, and buttons.
- **Callbacks for User Interaction**: Handle user actions such as `onActivated`, `onDismissed`, and `onFailed` to add interactivity to your notifications.
- **Custom Actions**: Add buttons with custom actions to your notifications, such as showing or hiding messages.
- **Button Support**: Notifications can include interactive buttons, allowing users to take specific actions directly from the notification.
- **Permission Request Handling**: Manage notification permissions seamlessly using `hasPermissionState` and `requestPermission` to ensure smooth user interaction. Note: Permission handling is currently necessary only for Android.
- **Cross-Platform Compatibility**: Seamless integration across Android, Linux, Windows, and macOS with platform-specific considerations.

## Screenshots

### Android
![Android](/screenshots/Android.png)

### Windows
![Windows](/screenshots/Windows.png)

### Gnome (Linux)
![Gnome](/screenshots/Gnome.png)

### KDE (Linux)
![KDE](/screenshots/KDE.png)

## Additional Usage Example

Here's another example that shows how to handle permission requests and send a notification:

```kotlin
val notificationProvider = getNotificationProvider()
val hasPermission by notificationProvider.hasPermissionState

if (hasPermission) {
    Notification(
        title = "Permission Granted Notification",
        message = "You have successfully granted notification permissions!",
        onActivated = { Log.d("NotificationLog", "Permission Granted Notification activated") },
        onFailed = { Log.d("NotificationLog", "Permission Granted Notification failed") }
    ) {
        Button("Acknowledge") {
            Log.d("NotificationLog", "Acknowledge button clicked")
            onShowMessage("Thank you for acknowledging!")
        }
    }
} else {
    notificationProvider.requestPermission(
        onGranted = {
            notificationProvider.updatePermissionState(true)
            Log.d("NotificationLog", "Notification permission granted")
        },
        onDenied = {
            notificationProvider.updatePermissionState(false)
            Log.d("NotificationLog", "Notification permission denied")
        }
    )
}
```

This example illustrates how to first check for notification permission, and if granted, send a notification with a custom action button.

## Roadmap

- **Native iOS and macOS Support**: Integrate native support for iOS and macOS to extend full functionality across Apple platforms.

- **WASM and JavaScript Support**: Add support for WebAssembly (WASM) and JavaScript environments.

- **Persistent Notifications for Audio Players**: Integrate persistent notifications, specifically tailored for use cases such as audio players.

- **Progress Bar Support**: Add support for notifications with progress bars, useful for tasks such as file downloads or ongoing processes.

- **Input Support for Notifications**: Add support for notifications with input fields, allowing users to respond directly within the notification.

## License

The Compose Notification Library is distributed under the [MIT License](LICENSE).

## Contributing

If you would like to contribute, feel free to submit issues or pull requests on the [GitHub repository](https://github.com/kdroidFilter/SecondTempleTimerLibrary).

## Troubleshooting

- **Android `onDismissed` callback**: This callback is not functional on Android. You can handle dismissal through other application state monitoring if needed.
- **macOS Callbacks**: macOS does not support interactive callbacks for notifications. Only basic notifications are available.

## Under the Hood

- **Linux**: Uses `libnotify` for handling notifications.
- **Windows**: Utilizes [WinToast](https://github.com/mohabouje/WinToast) and [WinToastLibC](https://github.com/AlienCowEatCake/WinToastLibC) for managing notifications.

Feel free to reach out for support or additional questions regarding implementation.

