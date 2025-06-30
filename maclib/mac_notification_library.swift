import Foundation
import UserNotifications

// MARK: - MacNotification class to manage notifications
class MacNotification {
    var title: String
    var body: String
    var iconPath: String?
    var imagePath: String?
    var soundFilePath: String?
    var identifier: String
    var buttons: [(id: String, label: String, callback: ButtonClickedCallback?, userData: UnsafeMutableRawPointer?)] = []
    var textInputActions: [(id: String, label: String, placeholder: String, callback: TextInputSubmittedCallback?, userData: UnsafeMutableRawPointer?)] = []
    var clickedCallback: NotificationClickedCallback?
    var clickedUserData: UnsafeMutableRawPointer?
    var closedCallback: NotificationClosedCallback?
    var closedUserData: UnsafeMutableRawPointer?

    init(title: String, body: String, iconPath: String?) {
        self.title = title
        self.body = body
        self.iconPath = iconPath
        self.identifier = UUID().uuidString
    }

    func addButton(id: String, label: String, callback: ButtonClickedCallback?, userData: UnsafeMutableRawPointer?) {
        buttons.append((id: id, label: label, callback: callback, userData: userData))
    }

    func addTextInput(id: String, label: String, placeholder: String, callback: TextInputSubmittedCallback?, userData: UnsafeMutableRawPointer?) {
        textInputActions.append((id: id, label: label, placeholder: placeholder, callback: callback, userData: userData))
    }

    func send() -> Int {
        // Request permission if needed
        requestNotificationPermission { granted in
            if granted {
                self.deliverNotification()
            }
        }
        return 0 // Success
    }

    private func deliverNotification() {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body

        var attachments: [UNNotificationAttachment] = []

        // Add image attachment if available
        if let imagePath = imagePath {
            let imageURL = URL(string: imagePath) ?? URL(fileURLWithPath: imagePath)
            do {
                let attachment = try UNNotificationAttachment(identifier: "image",
                                                             url: imageURL,
                                                             options: nil)
                attachments.append(attachment)
            } catch {
                print("Error attaching image: \(error)")
            }
        }

        // Add sound if available
        if let soundPath = soundFilePath {
            let soundURL = URL(string: soundPath) ?? URL(fileURLWithPath: soundPath)

            do {
                // Create a temporary directory for sound files if it doesn't exist
                let tempSoundDir = FileManager.default.temporaryDirectory.appendingPathComponent("NotificationSounds", isDirectory: true)
                try FileManager.default.createDirectory(at: tempSoundDir, withIntermediateDirectories: true, attributes: nil)

                // Get the sound file name and create a destination URL
                let soundFileName = soundURL.lastPathComponent
                let tempSoundURL = tempSoundDir.appendingPathComponent(soundFileName)

                // Copy the sound file to the temporary directory if it doesn't exist
                if !FileManager.default.fileExists(atPath: tempSoundURL.path) {
                    try FileManager.default.copyItem(at: soundURL, to: tempSoundURL)
                }

                print("Setting notification sound: \(soundFileName) from path: \(tempSoundURL.path)")

                // Create a sound notification using the file name
                let soundNameWithoutExtension = soundFileName.components(separatedBy: ".").dropLast().joined(separator: ".")
                content.sound = UNNotificationSound(named: UNNotificationSoundName(soundNameWithoutExtension))

                // Attach the sound file as an attachment to make it work
                let options: [String: Any] = [UNNotificationAttachmentOptionsTypeHintKey: "public.audio"]
                let attachment = try UNNotificationAttachment(identifier: "sound",
                                                            url: tempSoundURL,
                                                            options: options)
                attachments.append(attachment)

                print("Sound attachment created successfully")
            } catch {
                print("Error setting up sound: \(error)")
                // Fallback to default sound
                content.sound = UNNotificationSound.default
            }
        }

        // Set attachments if any
        if !attachments.isEmpty {
            content.attachments = attachments
        }

        // Add actions for buttons and text inputs
        if !buttons.isEmpty || !textInputActions.isEmpty {
            var actions: [UNNotificationAction] = []

            // Add regular button actions
            for button in buttons {
                let action = UNNotificationAction(
                    identifier: button.id,
                    title: button.label,
                    options: .foreground
                )
                actions.append(action)
            }

            // Add text input actions
            for textInput in textInputActions {
                let action = UNTextInputNotificationAction(
                    identifier: textInput.id,
                    title: textInput.label,
                    options: .foreground,
                    textInputButtonTitle: "Submit",
                    textInputPlaceholder: textInput.placeholder
                )
                actions.append(action)
            }

            // Create a category with the actions
            let category = UNNotificationCategory(
                identifier: "NOTIFICATION_CATEGORY",
                actions: actions,
                intentIdentifiers: [],
                options: []
            )

            // Register the category
            UNUserNotificationCenter.current().setNotificationCategories([category])

            // Set the category identifier on the notification content
            content.categoryIdentifier = "NOTIFICATION_CATEGORY"
        }

        // Create a trigger (immediate delivery)
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 0.1, repeats: false)

        // Create the request
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)

        // Add the request to the notification center
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("Error sending notification: \(error)")
            }
        }

        // Set up a delegate to handle notification responses
        NotificationDelegate.shared.registerNotification(self)
    }

    func hide() {
        UNUserNotificationCenter.current().removeDeliveredNotifications(withIdentifiers: [identifier])
    }

    private func requestNotificationPermission(completion: @escaping (Bool) -> Void) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            DispatchQueue.main.async {
                completion(granted)
            }
        }
    }
}

// MARK: - Notification Delegate to handle responses
class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegate()
    private var notifications: [String: MacNotification] = [:]

    private override init() {
        super.init()
        UNUserNotificationCenter.current().delegate = self
    }

    func registerNotification(_ notification: MacNotification) {
        notifications[notification.identifier] = notification
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, 
                               didReceive response: UNNotificationResponse, 
                               withCompletionHandler completionHandler: @escaping () -> Void) {
        let identifier = response.notification.request.identifier

        if let notification = notifications[identifier] {
            if response.actionIdentifier == UNNotificationDefaultActionIdentifier {
                // Default action (notification was tapped)
                if let callback = notification.clickedCallback {
                    callback?(Unmanaged.passUnretained(notification).toOpaque(), notification.clickedUserData)
                }
            } else {
                // Check if it's a text input response
                if let textInputResponse = response as? UNTextInputNotificationResponse {
                    let userText = textInputResponse.userText
                    let actionId = response.actionIdentifier

                    // Find the matching text input action and call its callback
                    for textInput in notification.textInputActions where textInput.id == actionId {
                        if let callback = textInput.callback {
                            callback(Unmanaged.passUnretained(notification).toOpaque(), textInput.id, userText, textInput.userData)
                        }
                        break
                    }
                } else {
                    // Regular button action
                    for button in notification.buttons where button.id == response.actionIdentifier {
                        if let callback = button.callback {
                            callback(Unmanaged.passUnretained(notification).toOpaque(), button.id, button.userData)
                        }
                        break
                    }
                }
            }
        }

        completionHandler()
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, 
                               willPresent notification: UNNotification, 
                               withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        // Allow the notification to be shown even when the app is in the foreground
        if #available(macOS 11.0, *) {
            completionHandler([.banner, .sound])
        } else {
            completionHandler([.alert, .sound])
        }
    }
}

// MARK: - C Interface Functions
@_cdecl("create_notification")
public func create_notification(title: UnsafePointer<CChar>?, body: UnsafePointer<CChar>?, iconPath: UnsafePointer<CChar>?) -> UnsafeMutableRawPointer? {
    let titleString = title != nil ? String(cString: title!) : ""
    let bodyString = body != nil ? String(cString: body!) : ""
    let iconPathString = iconPath != nil ? String(cString: iconPath!) : nil

    let notification = MacNotification(title: titleString, body: bodyString, iconPath: iconPathString)
    return Unmanaged.passRetained(notification).toOpaque()
}

@_cdecl("add_button_to_notification")
public func add_button_to_notification(notification: UnsafeMutableRawPointer?, 
                                     buttonId: UnsafePointer<CChar>?, 
                                     buttonLabel: UnsafePointer<CChar>?, 
                                     callback: @convention(c) (UnsafeMutableRawPointer?, UnsafePointer<CChar>?, UnsafeMutableRawPointer?) -> Void,
                                     userData: UnsafeMutableRawPointer?) {
    guard let notification = notification else { return }
    guard let buttonId = buttonId, let buttonLabel = buttonLabel else { return }

    let buttonIdString = String(cString: buttonId)
    let buttonLabelString = String(cString: buttonLabel)

    let notificationObject = Unmanaged<MacNotification>.fromOpaque(notification).takeUnretainedValue()
    notificationObject.addButton(id: buttonIdString, label: buttonLabelString, callback: callback, userData: userData)
}

@_cdecl("add_text_input_to_notification")
public func add_text_input_to_notification(notification: UnsafeMutableRawPointer?, 
                                         actionId: UnsafePointer<CChar>?, 
                                         actionLabel: UnsafePointer<CChar>?, 
                                         placeholder: UnsafePointer<CChar>?,
                                         callback: @convention(c) (UnsafeMutableRawPointer?, UnsafePointer<CChar>?, UnsafePointer<CChar>?, UnsafeMutableRawPointer?) -> Void,
                                         userData: UnsafeMutableRawPointer?) {
    guard let notification = notification else { return }
    guard let actionId = actionId, let actionLabel = actionLabel, let placeholder = placeholder else { return }

    let actionIdString = String(cString: actionId)
    let actionLabelString = String(cString: actionLabel)
    let placeholderString = String(cString: placeholder)

    let notificationObject = Unmanaged<MacNotification>.fromOpaque(notification).takeUnretainedValue()
    notificationObject.addTextInput(id: actionIdString, label: actionLabelString, placeholder: placeholderString, callback: callback, userData: userData)
}

@_cdecl("set_notification_clicked_callback")
public func set_notification_clicked_callback(notification: UnsafeMutableRawPointer?, 
                                            callback: (@convention(c) (UnsafeMutableRawPointer?, UnsafeMutableRawPointer?) -> Void)?,
                                            userData: UnsafeMutableRawPointer?) {
    guard let notification = notification else { return }

    let notificationObject = Unmanaged<MacNotification>.fromOpaque(notification).takeUnretainedValue()
    notificationObject.clickedCallback = callback
    notificationObject.clickedUserData = userData
}

@_cdecl("set_notification_closed_callback")
public func set_notification_closed_callback(notification: UnsafeMutableRawPointer?, 
                                           callback: (@convention(c) (UnsafeMutableRawPointer?, UnsafeMutableRawPointer?) -> Void)?,
                                           userData: UnsafeMutableRawPointer?) {
    guard let notification = notification else { return }

    let notificationObject = Unmanaged<MacNotification>.fromOpaque(notification).takeUnretainedValue()
    notificationObject.closedCallback = callback
    notificationObject.closedUserData = userData
}

@_cdecl("set_notification_image")
public func set_notification_image(notification: UnsafeMutableRawPointer?, imagePath: UnsafePointer<CChar>?) {
    guard let notification = notification, let imagePath = imagePath else { return }

    let imagePathString = String(cString: imagePath)
    let notificationObject = Unmanaged<MacNotification>.fromOpaque(notification).takeUnretainedValue()
    notificationObject.imagePath = imagePathString
}

@_cdecl("set_notification_sound")
public func set_notification_sound(notification: UnsafeMutableRawPointer?, soundPath: UnsafePointer<CChar>?) {
    guard let notification = notification, let soundPath = soundPath else { return }

    let soundPathString = String(cString: soundPath)
    let notificationObject = Unmanaged<MacNotification>.fromOpaque(notification).takeUnretainedValue()
    notificationObject.soundFilePath = soundPathString
}

@_cdecl("send_notification")
public func send_notification(notification: UnsafeMutableRawPointer?) -> Int32 {
    guard let notification = notification else { return -1 }

    let notificationObject = Unmanaged<MacNotification>.fromOpaque(notification).takeUnretainedValue()
    return Int32(notificationObject.send())
}

@_cdecl("hide_notification")
public func hide_notification(notification: UnsafeMutableRawPointer?) {
    guard let notification = notification else { return }

    let notificationObject = Unmanaged<MacNotification>.fromOpaque(notification).takeUnretainedValue()
    notificationObject.hide()
}

@_cdecl("cleanup_notification")
public func cleanup_notification(notification: UnsafeMutableRawPointer?) {
    guard let notification = notification else { return }

    Unmanaged<MacNotification>.fromOpaque(notification).release()
}

// MARK: - Callback Types
// Define callback types using function pointer syntax
public typealias NotificationClickedCallback = (@convention(c) (UnsafeMutableRawPointer?, UnsafeMutableRawPointer?) -> Void)?
public typealias NotificationClosedCallback = (@convention(c) (UnsafeMutableRawPointer?, UnsafeMutableRawPointer?) -> Void)?
public typealias ButtonClickedCallback = @convention(c) (UnsafeMutableRawPointer?, UnsafePointer<CChar>?, UnsafeMutableRawPointer?) -> Void
public typealias TextInputSubmittedCallback = @convention(c) (UnsafeMutableRawPointer?, UnsafePointer<CChar>?, UnsafePointer<CChar>?, UnsafeMutableRawPointer?) -> Void
