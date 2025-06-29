# KNotify Compose

This module extends the KNotify library to support Compose UI elements in notifications.

## Features

- Render Composable UI elements as notification images
- Use the same familiar API as the core KNotify library
- Seamlessly integrate your Compose UI with desktop notifications

## Usage

### Basic Usage

```kotlin
// Create a notification with a Composable as the large image
val notification = composeNotification(
    title = "Hello from Compose",
    message = "This notification has a Compose UI element as its image",
    largeImageComposable = {
        // Your Composable UI here
        // For example, a blue circle with text "KN" in the center
    }
)

// Send the notification
notification.send()

// Or create and send in one step
sendComposeNotification(
    title = "Hello from Compose",
    message = "This notification has a Compose UI element as its image",
    largeImageComposable = {
        // Your Composable UI here
    }
)
```

### Adding Buttons

```kotlin
composeNotification(
    title = "Notification with Buttons",
    message = "This notification has buttons and a Compose UI element",
    largeImageComposable = {
        // Your Composable UI here
    }
) {
    // Add buttons
    button("Action 1") {
        println("Action 1 clicked")
    }

    button("Action 2") {
        println("Action 2 clicked")
    }
}
```

## How It Works

The `knotify-compose` module renders your Composable UI to a temporary PNG file, which is then used as the large image for the notification. This happens automatically when you use the `composeNotification` or `sendComposeNotification` functions.

## Requirements

- JVM 17 or higher
- Compose for Desktop
