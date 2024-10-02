internal object NotifierFactory {
    fun getNotifier(): Notifier {
        val os = OsUtils.getOsName()
        return when {
            os.contains("linux") -> LinuxNotifier()
            os.contains("windows") -> WindowsNotifier()
            os.contains("mac") -> MacNotifier()
            else -> throw UnsupportedOperationException("Système d'exploitation non supporté : $os")
        }
    }
}
