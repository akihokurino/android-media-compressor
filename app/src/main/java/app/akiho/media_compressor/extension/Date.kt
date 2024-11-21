package app.akiho.media_compressor.extension

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.displayDate(): String {
  val format = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
  return format.format(this)
}

fun Date.displayDateTime(): String {
  val format = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
  return format.format(this)
}

fun Date.displayTime(): String {
  val format = SimpleDateFormat("a hh:mm", Locale.getDefault())
  return format.format(this)
}

fun Date.formatDate(): String {
  val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  return format.format(this)
}

fun Date.hour(): Int {
  val calendar = Calendar.getInstance()
  calendar.time = this
  return calendar.get(Calendar.HOUR_OF_DAY)
}

fun Date.minute(): Int {
  val calendar = Calendar.getInstance()
  calendar.time = this
  return calendar.get(Calendar.MINUTE)
}
