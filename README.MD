# KNotify

KNotify is a Kotlin library for sending native notifications across different operating systems. It provides a simple, unified interface for sending notifications on Linux, Windows, and macOS.

## Features

- Cross-platform support for Linux, Windows, and macOS
- Simple, unified API for sending notifications
- Customizable notification title, message, and application icon
- Automatic selection of the appropriate notifier based on the operating system

## Installation

To use KNotify in your project, add the following dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.kdroid:knotify:0.1.0")
}
```

## Usage

Here's a simple example of how to use KNotify:

```kotlin
import com.kdroid.knotify.NotifierFactory

fun main() {
    val notifier = NotifierFactory.getNotifier()
    
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

## API Reference

### Notifier Interface

```kotlin
interface Notifier {
    fun notify(title: String, message: String, appIcon: String): Boolean
}
```

- `title`: The title of the notification
- `message`: The message content of the notification
- `appIcon`: The path to the application icon to display with the notification (Note: not supported on macOS)
- Returns: `true` if the notification was successfully sent, `false` otherwise

### NotifierFactory

```kotlin
object NotifierFactory {
    fun getNotifier(): Notifier
}
```

- `getNotifier()`: Returns a platform-specific implementation of the `Notifier` interface based on the current operating system

## Platform Support

- Linux: Uses `LinuxNotifier`
- Windows: Uses `WindowsNotifier`
- macOS: Uses `MacNotifier`

## Note

The `appIcon` parameter is not yet supported on macOS.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the [MIT License](LICENSE).