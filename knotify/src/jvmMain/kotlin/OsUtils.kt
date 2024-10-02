internal object OsUtils {
    fun getOsName(): String {
        return System.getProperty("os.name").lowercase()
    }
}
