import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DateUtil {
    fun getCurrentDateFormatted(format: String = "yyyy-MM-dd"): String {
        return try {
            val dateFormatter = DateTimeFormatter.ofPattern(format, Locale.US)
            LocalDate.now().format(dateFormatter)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            "Invalid Format"
        }
    }
    fun getCurrentTimeFormatted(format: String = "HH:mm:ss"): String {
        return try {
            val dateFormatter = DateTimeFormatter.ofPattern(format, Locale.US)
            LocalDateTime.now().format(dateFormatter)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            "Invalid Format"
        }
    }
}