# MacNotification Library

This library provides native macOS notifications for the Compose Native Notification project using Swift and JNA.

## Requirements

- macOS 10.14 or later
- Xcode 11 or later with Swift 5.0

## Building the Library

1. Navigate to the `src` directory:
   ```
   cd src
   ```

2. Make the build script executable:
   ```
   chmod +x build.sh
   ```

3. Run the build script:
   ```
   ./build.sh
   ```

4. The library will be built in the `lib` directory as `libMacNotification.dylib`.

## Integration with JNA

The library is designed to be used with JNA (Java Native Access). The JNA interface is defined in the `MacNativeNotificationIntegration.kt` file.

To use the library:

1. Make sure the `libMacNotification.dylib` file is in a directory that is in the Java library path.

2. You can set the library path when running your application:
   ```
   java -Djna.library.path=/path/to/lib -jar your-application.jar
   ```

3. Alternatively, you can copy the library to a standard library location like `/usr/local/lib`.

## API Reference

The library provides the following functions:

- `create_notification(title, body, iconPath)`: Creates a notification with the specified title, body, and icon.
- `add_button_to_notification(notification, buttonId, buttonLabel, callback, userData)`: Adds a button to the notification.
- `set_notification_clicked_callback(notification, callback, userData)`: Sets a callback for when the notification is clicked.
- `set_notification_closed_callback(notification, callback, userData)`: Sets a callback for when the notification is closed.
- `set_notification_image(notification, imagePath)`: Sets an image for the notification.
- `send_notification(notification)`: Sends the notification.
- `hide_notification(notification)`: Hides/removes the notification.
- `cleanup_notification(notification)`: Cleans up resources associated with the notification.

## Troubleshooting

If you encounter issues with the library:

1. Make sure you have the required macOS version and Xcode installed.
2. Check that the library is in the correct location and accessible to your application.
3. Look for error messages in the application logs.
4. If the library fails to load, the application will fall back to a dummy implementation that logs errors but doesn't crash.

## License

This library is part of the Compose Native Notification project and is licensed under the MIT License.
