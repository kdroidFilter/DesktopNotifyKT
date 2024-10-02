internal class MacNotifier : Notifier {
    override fun notify(title: String, message: String, appIcon: String): Boolean {
        //TODO Implémentez la logique pour macOS (par exemple via AppleScript)
        throw UnsupportedOperationException("Notifications macOS non encore implémentées.")
    }
}
