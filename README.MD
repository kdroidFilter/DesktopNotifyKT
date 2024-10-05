# KNotify

KNotify is a Kotlin library for sending native notifications across different operating systems. It provides a simple, unified interface for sending notifications on Linux, Windows, and macOS.

## Features

- Cross-platform support for Linux, Windows, and macOS
- Simple, unified API for sending notifications
- Customizable notification title, message, and application icon
- Automatic selection of the appropriate notifier based on the operating system
- Application name customization for better identification

## Installation

To use KNotify in your project, add the JitPack repository and the dependency to your `build.gradle.kts` file:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.kdroidFilter:knotify:Version")
}
```

Replace `Version` with the latest version of the library or a specific commit. You can find available versions on the JitPack page of the project.

## Usage

Here's a simple example of how to use KNotify:

```kotlin
import com.kdroid.knotify.NotifierFactory

fun main() {
    val appName = "MyApp"
    val notifier = NotifierFactory.getNotifier(appName)

    val title = "Hello from KNotify"
    val message = "This is a test notification"
    val appIcon = "/path/to/your/app/icon.png"

    val success = notifier.notify(title, message, appIcon)

    if (success) {
        println("Notification sent successfully")
    } else {
        println("Failed to send notification")
    }
}
```

### Important Note for Windows Users

On Windows, the `appName` parameter must match the name of your application as it appears in the Start menu. To ensure you're using the correct name, you can use the following PowerShell command:

```powershell
Get-StartApps | Where-Object {$_.Name -like '*YourAppName*'}
```

Replace `YourAppName` with a part of your application's name. Use the exact `Name` value returned by this command as the `appName` parameter when creating the notifier.

## API Reference

### Notifier Interface

```kotlin
interface Notifier {
    fun notify(title: String, message: String, appIcon: String?): Boolean
}
```

- `title`: The title of the notification
- `message`: The message content of the notification
- `appIcon`: The path to the application icon to display with the notification (can be null)
- Returns: `true` if the notification was successfully sent, `false` otherwise

### NotifierFactory

```kotlin
object NotifierFactory {
    fun getNotifier(appName: String): Notifier
}
```

- `appName`: The name of the application sending the notification
- Returns: A platform-specific implementation of the `Notifier` interface based on the current operating system

## Platform Support

- Linux: Uses `LinuxNotifier`
- Windows: Uses `WindowsNotifier`
- macOS: Uses `MacNotifier`

Each platform-specific notifier is initialized with the provided `appName`.

## Notes

- The `appIcon` parameter is optional (can be null) and is not yet supported on macOS.
- An `UnsupportedOperationException` is thrown if the current operating system is not supported.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the [MIT License](LICENSE).